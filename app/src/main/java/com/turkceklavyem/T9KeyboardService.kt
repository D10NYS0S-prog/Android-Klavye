package com.turkceklavyem

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.LayoutInflater
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.HorizontalScrollView
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.media.AudioManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.graphics.Color

class T9KeyboardService : InputMethodService(), SharedPreferences.OnSharedPreferenceChangeListener {
    
    // Klavye modları
    enum class KeyboardMode {
        T9,        // T9 modu (sayısal tuşlar)
        T16,       // T16 modu (kompakt QWERTY, 2 harf/tuş)
        STANDARD,  // Standart QWERTY modu
        SYMBOLS    // Sembol modu (sayılar ve özel karakterler)
    }
    
    private var currentMode = KeyboardMode.T9
    private var isShiftActive = false  // Shift tuşu aktif mi
    private var currentInput = StringBuilder()
    private val wordDatabase = WordDatabase.getInstance()
    private var currentKeyboardView: View? = null
    private var currentSuggestions: List<String> = emptyList()
    private var currentSuggestionIndex = 0
    
    // T16 tuş dizisi takibi (auto-correction için)
    private val t16KeySequence = mutableListOf<String>()
    
    // Kelime önerileri için (T9 modunda)
    private var suggestionsContainer: LinearLayout? = null
    private var suggestionsScrollView: HorizontalScrollView? = null
    
    // T16 ve Standard modda öneri butonları
    private var suggestion1Button: Button? = null
    private var suggestion2Button: Button? = null
    private var suggestion3Button: Button? = null
    private var suggestion4Button: Button? = null
    private var settingsButton: Button? = null
    
    // Klavye içi ayarlar
    private var inKeyboardSettingsPanel: View? = null
    private var isSettingsPanelVisible = false
    
    // Uzun basış popup için
    private lateinit var longPressPopupManager: LongPressPopupManager
    private val longPressDelay = 500L // 500ms uzun basış
    
    // Otomatik öğrenme için
    private var lastCommittedWord = StringBuilder()
    private var isLearningEnabled = true
    
    // Vibration ve ses için
    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private lateinit var prefs: SharedPreferences
    private var vibrateOnKeypress = true
    private var soundOnKeypress = true
    
    // Görünüm ayarları
    private var keyboardHeight = 80 // Yüzde olarak
    private var keyboardTheme = "light"
    
    // Çoklu basış için
    private var lastPressedKey: String? = null
    private var lastPressTime: Long = 0
    private var currentPressCount = 0
    private val multiTapDelay = 800L  // 800ms içinde basılırsa aynı tuş sayılır
    
    private val handler = Handler(Looper.getMainLooper())
    private var commitRunnable: Runnable? = null
    
    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
        longPressPopupManager = LongPressPopupManager(this)
        loadPreferences()
        
        // Türkçe kelime listesini assets'ten yükle
        wordDatabase.loadWordsFromAssets(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        commitRunnable?.let { handler.removeCallbacks(it) }
    }
    
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Ayarlar değiştiğinde tercihleri yeniden yükle
        when (key) {
            "vibrate_on", "sound_on" -> loadPreferences()
            "keyboard_height", "keyboard_theme" -> {
                loadPreferences()
                // Klavyeyi yeniden oluştur
                setInputView(onCreateInputView())
            }
        }
    }
    
    private fun loadPreferences() {
        vibrateOnKeypress = prefs.getBoolean("vibrate_on", true)
        soundOnKeypress = prefs.getBoolean("sound_on", true)
        keyboardHeight = prefs.getInt("keyboard_height", 80)
        keyboardTheme = prefs.getString("keyboard_theme", "light") ?: "light"
        isLearningEnabled = prefs.getBoolean("auto_learn", true)
    }
    
    private fun performHapticFeedback() {
        if (vibrateOnKeypress && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ için yeni API
                val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                // Eski cihazlar için deprecated method
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }
    
    private fun playSoundEffect() {
        if (soundOnKeypress) {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f)
        }
    }
    
    private fun updateSuggestions(suggestions: List<String>) {
        suggestionsContainer?.removeAllViews()
        
        if (suggestions.isEmpty()) {
            suggestionsScrollView?.visibility = View.GONE
            return
        }
        
        suggestionsScrollView?.visibility = View.VISIBLE
        
        suggestions.take(5).forEachIndexed { index, word ->
            val button = Button(this).apply {
                text = word
                textSize = 16f
                setPadding(24, 8, 24, 8)
                isAllCaps = false
                
                // İlk öneriye vurgu yap - tema uyumlu
                if (index == 0) {
                    setBackgroundColor(resources.getColor(R.color.suggestion_primary, null))
                    setTextColor(Color.WHITE)
                } else {
                    setBackgroundColor(resources.getColor(R.color.suggestion_secondary, null))
                    setTextColor(Color.BLACK)
                }
                
                setOnClickListener {
                    onSuggestionClicked(word)
                }
            }
            
            suggestionsContainer?.addView(button)
            
            // Butonlar arasına boşluk ekle
            if (index < suggestions.size - 1) {
                val spacer = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(16, 1)
                }
                suggestionsContainer?.addView(spacer)
            }
        }
    }
    
    private fun setupT16SuggestionButtons() {
        // Öneri butonlarını ayarla
        listOf(suggestion1Button, suggestion2Button, suggestion3Button, suggestion4Button).forEach { button ->
            button?.setOnClickListener {
                val word = it.tag as? String
                if (word != null) {
                    onT16SuggestionClicked(word)
                }
            }
        }
        
        // Ayarlar butonunu ayarla
        settingsButton?.setOnClickListener {
            toggleSettingsPanel()
        }
    }
    
    private fun updateT16Suggestions(suggestions: List<String>) {
        val buttons = listOf(suggestion1Button, suggestion2Button, suggestion3Button, suggestion4Button)
        
        // Ensure at least 3 suggestions are visible - add placeholders if needed
        val minSuggestions = 3
        val suggestionsToShow = if (suggestions.size >= minSuggestions) {
            suggestions.take(4)
        } else {
            // Add empty placeholders to fill up to minimum
            suggestions + List(minSuggestions - suggestions.size) { "" }
        }
        
        suggestionsToShow.take(4).forEachIndexed { index, word ->
            buttons[index]?.apply {
                if (word.isNotEmpty()) {
                    text = word
                    tag = word
                    visibility = View.VISIBLE
                    isEnabled = true
                } else {
                    text = ""
                    tag = null
                    visibility = View.VISIBLE
                    isEnabled = false
                }
            }
        }
    }
    
    private fun onT16SuggestionClicked(word: String) {
        performHapticFeedback()
        playSoundEffect()
        
        // Yazılan metni temizle ve öneriyi ekle
        val deletedChars = currentInput.length
        if (deletedChars > 0) {
            currentInputConnection?.deleteSurroundingText(deletedChars, 0)
        }
        
        currentInputConnection?.commitText(word + " ", 1)
        wordDatabase.updateWordFrequency(word)
        currentInput.clear()
        currentSuggestions = emptyList()
        updateT16Suggestions(emptyList())
        
        // Otomatik öğrenme
        if (isLearningEnabled) {
            lastCommittedWord.clear()
            lastCommittedWord.append(word)
        }
    }
    
    private fun toggleSettingsPanel() {
        performHapticFeedback()
        
        if (isSettingsPanelVisible) {
            hideSettingsPanel()
        } else {
            showSettingsPanel()
        }
    }
    
    private fun showSettingsPanel() {
        if (inKeyboardSettingsPanel == null) {
            val inflater = LayoutInflater.from(this)
            inKeyboardSettingsPanel = inflater.inflate(R.layout.in_keyboard_settings_panel, null)
            setupSettingsPanelButtons()
        }
        
        // Mevcut klavye görünümünün yerine ayarlar panelini göster
        setInputView(inKeyboardSettingsPanel)
        isSettingsPanelVisible = true
    }
    
    private fun hideSettingsPanel() {
        setInputView(onCreateInputView())
        isSettingsPanelVisible = false
    }
    
    private fun setupSettingsPanelButtons() {
        inKeyboardSettingsPanel?.apply {
            findViewById<Button>(R.id.btn_mode_switch)?.setOnClickListener {
                toggleMode()
                hideSettingsPanel()
            }
            
            findViewById<Button>(R.id.btn_height_adjust)?.setOnClickListener {
                // Klavye boyutunu değiştir (cycle through: 60%, 80%, 100%)
                keyboardHeight = when (keyboardHeight) {
                    60 -> 80
                    80 -> 100
                    else -> 60
                }
                prefs.edit().putInt("keyboard_height", keyboardHeight).apply()
                hideSettingsPanel()
            }
            
            findViewById<Button>(R.id.btn_feedback_settings)?.setOnClickListener {
                // Titreşim ve ses ayarlarını değiştir
                vibrateOnKeypress = !vibrateOnKeypress
                prefs.edit().putBoolean("vibrate_on", vibrateOnKeypress).apply()
            }
            
            findViewById<Button>(R.id.btn_theme_settings)?.setOnClickListener {
                // Temayı değiştir (cycle through: light, dark, blue, green)
                keyboardTheme = when (keyboardTheme) {
                    "light" -> "dark"
                    "dark" -> "blue"
                    "blue" -> "green"
                    else -> "light"
                }
                prefs.edit().putString("keyboard_theme", keyboardTheme).apply()
                hideSettingsPanel()
            }
            
            findViewById<Button>(R.id.btn_close_settings)?.setOnClickListener {
                hideSettingsPanel()
            }
        }
    }
    
    private fun onSuggestionClicked(word: String) {
        performHapticFeedback()
        playSoundEffect()
        
        currentInputConnection?.finishComposingText()
        currentInputConnection?.commitText(word, 1)
        wordDatabase.updateWordFrequency(word)
        currentInput.clear()
        currentSuggestions = emptyList()
        updateSuggestions(emptyList())
        
        // Otomatik öğrenme
        if (isLearningEnabled) {
            lastCommittedWord.clear()
            lastCommittedWord.append(word)
        }
    }
    
    override fun onCreateInputView(): View {
        // Klavye görünümünü bağla - mod seçimine göre
        val inflater = LayoutInflater.from(this)
        val layoutId = when (currentMode) {
            KeyboardMode.T9 -> R.layout.keyboard_layout
            KeyboardMode.T16 -> R.layout.keyboard_layout_t16
            KeyboardMode.STANDARD -> R.layout.keyboard_layout_standard
            KeyboardMode.SYMBOLS -> R.layout.keyboard_layout_symbols
        }
        val keyboardView = inflater.inflate(layoutId, null)
        currentKeyboardView = keyboardView
        
        // Klavye yüksekliğini ayarla
        applyKeyboardHeight(keyboardView)
        
        // Temayı uygula
        applyKeyboardTheme(keyboardView)
        
        // T9 modunda kelime önerileri container'ı bul
        if (currentMode == KeyboardMode.T9) {
            suggestionsScrollView = keyboardView.findViewById(R.id.suggestions_scroll)
            suggestionsContainer = keyboardView.findViewById(R.id.suggestions_container)
        } else if (currentMode == KeyboardMode.SYMBOLS) {
            // Sembol modunda da öneri butonlarını kullan
            suggestion1Button = keyboardView.findViewById(R.id.suggestion_1)
            suggestion2Button = keyboardView.findViewById(R.id.suggestion_2)
            suggestion3Button = keyboardView.findViewById(R.id.suggestion_3)
            suggestion4Button = keyboardView.findViewById(R.id.suggestion_4)
            settingsButton = keyboardView.findViewById(R.id.settings_button)
            
            setupT16SuggestionButtons()
        } else {
            // T16 ve Standard modunda öneri butonlarını ve ayarlar butonunu bul
            suggestion1Button = keyboardView.findViewById(R.id.suggestion_1)
            suggestion2Button = keyboardView.findViewById(R.id.suggestion_2)
            suggestion3Button = keyboardView.findViewById(R.id.suggestion_3)
            suggestion4Button = keyboardView.findViewById(R.id.suggestion_4)
            settingsButton = keyboardView.findViewById(R.id.settings_button)
            
            setupT16SuggestionButtons()
        }
        
        // Tuş dinleyicilerini ayarla
        setupKeyListeners(keyboardView)
        
        return keyboardView
    }
    
    private fun applyKeyboardHeight(view: View) {
        // Klavye yüksekliğini yüzde olarak uygula (maksimum 400dp)
        val params = view.layoutParams
        if (params != null) {
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            // Maksimum yükseklik: 400dp
            val maxHeight = (400 * displayMetrics.density).toInt()
            val calculatedHeight = (screenHeight * keyboardHeight / 100).toInt()
            val newHeight = minOf(calculatedHeight, maxHeight)
            params.height = newHeight
            view.layoutParams = params
        }
    }
    
    private fun applyKeyboardTheme(view: View) {
        // Temayı uygula
        when (keyboardTheme) {
            "dark" -> {
                view.setBackgroundColor(Color.parseColor("#263238"))
                applyDarkThemeToButtons(view)
            }
            "blue" -> {
                view.setBackgroundColor(Color.parseColor("#1565C0"))
                applyBlueThemeToButtons(view)
            }
            "green" -> {
                view.setBackgroundColor(Color.parseColor("#2E7D32"))
                applyGreenThemeToButtons(view)
            }
            else -> {
                // Light tema (varsayılan)
                view.setBackgroundColor(Color.parseColor("#ECEFF1"))
            }
        }
    }
    
    private fun applyDarkThemeToButtons(view: View) {
        // Koyu temayı tuşlara uygula
        val buttons = getAllButtons(view)
        buttons.forEach { button ->
            button.setBackgroundColor(Color.parseColor("#37474F"))
            button.setTextColor(Color.WHITE)
        }
    }
    
    private fun applyBlueThemeToButtons(view: View) {
        // Mavi temayı tuşlara uygula
        val buttons = getAllButtons(view)
        buttons.forEach { button ->
            button.setBackgroundColor(Color.parseColor("#1976D2"))
            button.setTextColor(Color.WHITE)
        }
    }
    
    private fun applyGreenThemeToButtons(view: View) {
        // Yeşil temayı tuşlara uygula
        val buttons = getAllButtons(view)
        buttons.forEach { button ->
            button.setBackgroundColor(Color.parseColor("#388E3C"))
            button.setTextColor(Color.WHITE)
        }
    }
    
    private fun getAllButtons(view: View): List<Button> {
        val buttons = mutableListOf<Button>()
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is Button) {
                    buttons.add(child)
                } else if (child is android.view.ViewGroup) {
                    buttons.addAll(getAllButtons(child))
                }
            }
        }
        return buttons
    }
    
    private fun setupKeyListeners(view: View) {
        when (currentMode) {
            KeyboardMode.T9 -> setupT9KeyListeners(view)
            KeyboardMode.T16 -> setupT16KeyListeners(view)
            KeyboardMode.STANDARD -> setupStandardKeyListeners(view)
            KeyboardMode.SYMBOLS -> setupSymbolsKeyListeners(view)
        }
        
        // Ortak tuşlar
        view.findViewById<Button>(R.id.key_backspace)?.setOnClickListener {
            onBackspacePressed()
        }
        
        view.findViewById<Button>(R.id.key_enter)?.setOnClickListener {
            onEnterPressed()
        }
        
        view.findViewById<Button>(R.id.mode_switch)?.setOnClickListener {
            toggleMode()
        }
    }
    
    private fun setupT9KeyListeners(view: View) {
        // Sayı tuşları (1-9)
        val keyIds = arrayOf(
            R.id.key_1, R.id.key_2, R.id.key_3,
            R.id.key_4, R.id.key_5, R.id.key_6,
            R.id.key_7, R.id.key_8, R.id.key_9
        )
        
        keyIds.forEachIndexed { index, keyId ->
            val button = view.findViewById<Button>(keyId)
            button?.setOnClickListener {
                if (!longPressPopupManager.isShowing()) {
                    onT9KeyPressed(index + 1)
                }
            }
            
            // Uzun basış desteği
            button?.setOnLongClickListener {
                val keyIdStr = resources.getResourceEntryName(keyId)
                longPressPopupManager.showPopup(button, keyIdStr) { char ->
                    performHapticFeedback()
                    playSoundEffect()
                    currentInputConnection?.commitText(char, 1)
                }
                true
            }
        }
        
        // Özel tuşlar
        val key0 = view.findViewById<Button>(R.id.key_0)
        key0?.setOnClickListener {
            if (!longPressPopupManager.isShowing()) {
                onSpacePressed()
            }
        }
        key0?.setOnLongClickListener {
            longPressPopupManager.showPopup(key0, "key_0") { char ->
                performHapticFeedback()
                playSoundEffect()
                currentInputConnection?.commitText(char, 1)
            }
            true
        }
        
        view.findViewById<Button>(R.id.key_star)?.setOnClickListener {
            cycleSuggestion()
        }
        
        view.findViewById<Button>(R.id.key_hash)?.setOnClickListener {
            acceptSuggestion()
        }
    }
    
    private fun setupT16KeyListeners(view: View) {
        // T12 tuşları - yeni düzen
        val keyMap = mapOf(
            R.id.key_qw to "qwQW",
            R.id.key_er to "erER",
            R.id.key_ty to "tyTY",
            R.id.key_ui to "uıUİ",
            R.id.key_op to "opOP",
            R.id.key_as to "asAS",
            R.id.key_df to "dfDF",
            R.id.key_gh to "ghGH",
            R.id.key_jk to "jkJK",
            R.id.key_l to "l-L_",
            R.id.key_zx to "zxZX",
            R.id.key_cv to "cvCV",
            R.id.key_bn to "bnBN",
            R.id.key_m to "m'M'"
        )
        
        keyMap.forEach { (keyId, chars) ->
            val button = view.findViewById<Button>(keyId)
            button?.setOnClickListener {
                if (!longPressPopupManager.isShowing()) {
                    onT16KeyPressed(keyId.toString(), chars)
                }
            }
            
            // Uzun basış desteği ekle
            button?.setOnLongClickListener {
                val keyIdStr = resources.getResourceEntryName(keyId)
                longPressPopupManager.showPopup(button, keyIdStr) { char ->
                    performHapticFeedback()
                    playSoundEffect()
                    currentInputConnection?.commitText(char, 1)
                }
                true
            }
        }
        
        // Shift tuşu
        view.findViewById<Button>(R.id.key_shift)?.setOnClickListener {
            toggleShift()
        }
        
        // Sembol tuşu
        view.findViewById<Button>(R.id.key_symbols)?.setOnClickListener {
            switchToSymbolsMode()
        }
        
        // Noktalama tuşları
        view.findViewById<Button>(R.id.key_space)?.setOnClickListener {
            onSpacePressed()
        }
        
        val dotButton = view.findViewById<Button>(R.id.key_dot)
        dotButton?.setOnClickListener {
            if (!longPressPopupManager.isShowing()) {
                currentInputConnection?.commitText(".", 1)
            }
        }
        dotButton?.setOnLongClickListener {
            longPressPopupManager.showPopup(dotButton, "key_dot") { char ->
                performHapticFeedback()
                playSoundEffect()
                currentInputConnection?.commitText(char, 1)
            }
            true
        }
        
        val commaButton = view.findViewById<Button>(R.id.key_comma)
        commaButton?.setOnClickListener {
            if (!longPressPopupManager.isShowing()) {
                currentInputConnection?.commitText(",", 1)
            }
        }
        commaButton?.setOnLongClickListener {
            longPressPopupManager.showPopup(commaButton, "key_comma") { char ->
                performHapticFeedback()
                playSoundEffect()
                currentInputConnection?.commitText(char, 1)
            }
            true
        }
    }
    
    private fun setupStandardKeyListeners(view: View) {
        // Standart QWERTY klavye tuşları
        val letterKeys = listOf(
            R.id.key_q to "q", R.id.key_w to "w", R.id.key_e to "e", R.id.key_r to "r", 
            R.id.key_t to "t", R.id.key_y to "y", R.id.key_u to "u", R.id.key_ı to "ı",
            R.id.key_i to "i", R.id.key_o to "o", R.id.key_p to "p",
            R.id.key_a to "a", R.id.key_s to "s", R.id.key_d to "d", R.id.key_f to "f",
            R.id.key_g to "g", R.id.key_h to "h", R.id.key_j to "j", R.id.key_k to "k",
            R.id.key_l to "l",
            R.id.key_z to "z", R.id.key_x to "x", R.id.key_c to "c", R.id.key_v to "v",
            R.id.key_b to "b", R.id.key_n to "n", R.id.key_m to "m"
        )
        
        letterKeys.forEach { (keyId, char) ->
            val button = view.findViewById<Button>(keyId)
            button?.setOnClickListener {
                onStandardKeyPressed(char)
            }
        }
        
        // Shift tuşu
        view.findViewById<Button>(R.id.key_shift)?.setOnClickListener {
            toggleShift()
        }
        
        // Sembol tuşu
        view.findViewById<Button>(R.id.key_symbols)?.setOnClickListener {
            switchToSymbolsMode()
        }
        
        // Noktalama tuşları
        view.findViewById<Button>(R.id.key_space)?.setOnClickListener {
            onSpacePressed()
        }
        
        view.findViewById<Button>(R.id.key_dot)?.setOnClickListener {
            currentInputConnection?.commitText(".", 1)
        }
        
        view.findViewById<Button>(R.id.key_comma)?.setOnClickListener {
            currentInputConnection?.commitText(",", 1)
        }
    }
    
    private fun onT9KeyPressed(key: Int) {
        // Haptic ve ses feedback
        performHapticFeedback()
        playSoundEffect()
        
        // T9 modunda tuş basımını işle ve kelime tahmini yap
        currentInput.append(key)
        
        // WordDatabase'den kelime önerileri al
        currentSuggestions = wordDatabase.getPossibleWords(currentInput.toString())
        currentSuggestionIndex = 0
        
        // Önerileri güncelle
        updateSuggestions(currentSuggestions)
        
        if (currentSuggestions.isNotEmpty()) {
            // İlk öneriyi göster (composing text olarak)
            val suggestion = currentSuggestions[0]
            currentInputConnection?.setComposingText(suggestion, 1)
        } else {
            // Tuş dizisini göster
            currentInputConnection?.setComposingText(currentInput.toString(), 1)
        }
    }
    
    private fun onT16KeyPressed(keyId: String, chars: String) {
        // Haptic ve ses feedback
        performHapticFeedback()
        playSoundEffect()
        
        val currentTime = System.currentTimeMillis()
        
        // Tuş adını normalize et (key_qw -> qw)
        val normalizedKeyId = keyId.replace("key_", "").lowercase()
        
        // Aynı tuşa çabuk basıldıysa çoklu basış
        if (keyId == lastPressedKey && (currentTime - lastPressTime) < multiTapDelay) {
            currentPressCount++
            
            // Önceki karakteri sil
            currentInputConnection?.deleteSurroundingText(1, 0)
            
            // currentInput'tan son karakteri sil
            if (currentInput.isNotEmpty()) {
                currentInput.deleteCharAt(currentInput.length - 1)
            }
            
            // Yeni karakteri ekle
            val char = getT16Character(chars, currentPressCount, isShiftActive)
            currentInputConnection?.commitText(char.toString(), 1)
            currentInput.append(char)
            
            // Otomatik öğrenme için karakter ekle
            if (isLearningEnabled && lastCommittedWord.isNotEmpty()) {
                lastCommittedWord.deleteCharAt(lastCommittedWord.length - 1)
                lastCommittedWord.append(char)
            }
            
            // Zamanlayıcıyı iptal et
            commitRunnable?.let { handler.removeCallbacks(it) }
        } else {
            // Yeni tuş basıldı - tuş dizisine ekle
            if (lastPressedKey != null) {
                // Önceki tuştan sonra yeni tuş, diziye ekle
                t16KeySequence.add(normalizedKeyId)
            } else {
                // İlk tuş
                t16KeySequence.clear()
                t16KeySequence.add(normalizedKeyId)
            }
            
            currentPressCount = 1
            val char = getT16Character(chars, currentPressCount, isShiftActive)
            currentInputConnection?.commitText(char.toString(), 1)
            currentInput.append(char)
            
            // Otomatik öğrenme için karakter ekle
            if (isLearningEnabled) {
                lastCommittedWord.append(char)
            }
        }
        
        lastPressedKey = keyId
        lastPressTime = currentTime
        
        // T16 modunda otomatik düzeltme: tuş dizisinden kelime tahminleri al
        if (t16KeySequence.size >= 2) {
            // Tuş dizisinden olası kelimeleri bul
            val keySequenceWords = wordDatabase.getWordsFromT16KeySequence(t16KeySequence)
            
            // Prefix tabanlı önerileri de al
            val inputText = currentInput.toString().lowercase()
            val prefixWords = wordDatabase.getWordsByPrefix(inputText).take(4)
            
            // Her iki yöntemi birleştir, tuş dizisi eşleşmeleri öncelikli
            val combinedSuggestions = (keySequenceWords + prefixWords).distinct().take(4)
            currentSuggestions = combinedSuggestions
            updateT16Suggestions(currentSuggestions)
            
            // Eğer tuş dizisinden tam eşleşme varsa, ilk öneriyi vurgula
            if (keySequenceWords.isNotEmpty()) {
                // İlk öneri en iyi eşleşme - kullanıcı isterse bunu seçebilir
                // veya boşluk tuşuna basınca otomatik olarak uygulanır
            }
        } else {
            currentSuggestions = emptyList()
            updateT16Suggestions(emptyList())
        }
        
        // Shift'i tek kullanım için kapat
        if (isShiftActive) {
            isShiftActive = false
            updateShiftButton()
            updateKeyLabels()
        }
        
        // Zamanlayıcı ayarla
        commitRunnable = Runnable {
            lastPressedKey = null
            currentPressCount = 0
        }
        handler.postDelayed(commitRunnable!!, multiTapDelay)
    }
    
    private fun onStandardKeyPressed(char: String) {
        // Haptic ve ses feedback
        performHapticFeedback()
        playSoundEffect()
        
        // Shift durumuna göre karakteri belirle
        val outputChar = if (isShiftActive) char.uppercase() else char
        currentInputConnection?.commitText(outputChar, 1)
        currentInput.append(outputChar)
        
        // Kelime önerileri al
        if (currentInput.length >= 2) {
            val inputText = currentInput.toString().lowercase()
            currentSuggestions = wordDatabase.getWordsByPrefix(inputText).take(4)
            updateT16Suggestions(currentSuggestions)
        } else {
            currentSuggestions = emptyList()
            updateT16Suggestions(emptyList())
        }
        
        // Shift'i tek kullanım için kapat
        if (isShiftActive) {
            isShiftActive = false
            updateShiftButton()
            updateKeyLabels()
        }
        
        // Otomatik öğrenme için karakter ekle
        if (isLearningEnabled) {
            lastCommittedWord.append(outputChar)
        }
    }
    
    private fun cycleSuggestion() {
        // * tuşu ile öneriler arasında geçiş
        if (currentSuggestions.isEmpty()) return
        
        currentSuggestionIndex = (currentSuggestionIndex + 1) % currentSuggestions.size
        val suggestion = currentSuggestions[currentSuggestionIndex]
        currentInputConnection?.setComposingText(suggestion, 1)
        
        // Önerileri güncelle (seçili öneriyi vurgula)
        updateSuggestions(currentSuggestions)
    }
    
    private fun acceptSuggestion() {
        // # tuşu ile öneriyi kabul et
        if (currentSuggestions.isNotEmpty()) {
            val word = currentSuggestions[currentSuggestionIndex]
            currentInputConnection?.finishComposingText()
            wordDatabase.updateWordFrequency(word)
            currentInput.clear()
            currentSuggestions = emptyList()
            updateSuggestions(emptyList())
        }
    }
    
    private fun toggleShift() {
        isShiftActive = !isShiftActive
        updateShiftButton()
        updateKeyLabels()
    }
    
    private fun updateShiftButton() {
        currentKeyboardView?.findViewById<Button>(R.id.key_shift)?.apply {
            alpha = if (isShiftActive) 1.0f else 0.5f
        }
    }
    
    private fun updateKeyLabels() {
        // Update key labels based on shift state
        when (currentMode) {
            KeyboardMode.T16 -> updateT16KeyLabels()
            KeyboardMode.STANDARD -> updateStandardKeyLabels()
            KeyboardMode.T9 -> {} // T9 doesn't need label updates
        }
    }
    
    private fun updateT16KeyLabels() {
        currentKeyboardView?.apply {
            val keyMap = mapOf(
                R.id.key_qw to if (isShiftActive) "QW" else "qw",
                R.id.key_er to if (isShiftActive) "ER" else "er",
                R.id.key_ty to if (isShiftActive) "TY" else "ty",
                R.id.key_ui to if (isShiftActive) "UİI" else "uıi",
                R.id.key_op to if (isShiftActive) "OP" else "op",
                R.id.key_as to if (isShiftActive) "AS" else "as",
                R.id.key_df to if (isShiftActive) "DF" else "df",
                R.id.key_gh to if (isShiftActive) "GH" else "gh",
                R.id.key_jk to if (isShiftActive) "JK" else "jk",
                R.id.key_l to if (isShiftActive) "L-" else "l-",
                R.id.key_zx to if (isShiftActive) "ZX" else "zx",
                R.id.key_cv to if (isShiftActive) "CV" else "cv",
                R.id.key_bn to if (isShiftActive) "BN" else "bn",
                R.id.key_m to if (isShiftActive) "M'" else "m'"
            )
            
            keyMap.forEach { (keyId, label) ->
                findViewById<Button>(keyId)?.text = label
            }
        }
    }
    
    private fun updateStandardKeyLabels() {
        currentKeyboardView?.apply {
            val letterKeys = listOf(
                R.id.key_q to "q", R.id.key_w to "w", R.id.key_e to "e", R.id.key_r to "r",
                R.id.key_t to "t", R.id.key_y to "y", R.id.key_u to "u", R.id.key_ı to "ı",
                R.id.key_i to "i", R.id.key_o to "o", R.id.key_p to "p",
                R.id.key_a to "a", R.id.key_s to "s", R.id.key_d to "d", R.id.key_f to "f",
                R.id.key_g to "g", R.id.key_h to "h", R.id.key_j to "j", R.id.key_k to "k",
                R.id.key_l to "l",
                R.id.key_z to "z", R.id.key_x to "x", R.id.key_c to "c", R.id.key_v to "v",
                R.id.key_b to "b", R.id.key_n to "n", R.id.key_m to "m"
            )
            
            letterKeys.forEach { (keyId, char) ->
                findViewById<Button>(keyId)?.text = if (isShiftActive) char.uppercase() else char
            }
        }
    }
    
    private fun onSpacePressed() {
        // Eğer composing text varsa önce onu kabul et ve öğren
        val composingText = currentInputConnection?.getExtractedText(
            android.view.inputmethod.ExtractedTextRequest(), 0
        )?.text?.toString() ?: ""
        
        if (currentInput.isNotEmpty()) {
            if (currentMode == KeyboardMode.T9) {
                // T9 modunda önerilen kelimeyi kabul et ve öğren
                if (currentSuggestions.isNotEmpty()) {
                    val word = currentSuggestions[0]
                    currentInputConnection?.finishComposingText()
                    
                    // Otomatik öğrenme
                    if (isLearningEnabled && word.length >= 2) {
                        wordDatabase.addWordToDatabase(word)
                        lastCommittedWord.clear()
                        lastCommittedWord.append(word)
                    }
                } else {
                    currentInputConnection?.finishComposingText()
                }
            } else {
                // T16 ve Standard modunda yazılan kelimeyi öğren
                val word = currentInput.toString()
                if (isLearningEnabled && word.length >= 2) {
                    wordDatabase.addWordToDatabase(word)
                    lastCommittedWord.clear()
                    lastCommittedWord.append(word)
                }
            }
        }
        
        currentInputConnection?.commitText(" ", 1)
        currentInput.clear()
        currentSuggestions = emptyList()
        t16KeySequence.clear()  // T16 tuş dizisini temizle
        
        if (currentMode == KeyboardMode.T9) {
            updateSuggestions(emptyList())
        } else {
            updateT16Suggestions(emptyList())
        }
    }
    
    private fun onBackspacePressed() {
        if (currentInput.isNotEmpty()) {
            // Henüz commit edilmemiş metin varsa
            currentInput.deleteCharAt(currentInput.length - 1)
            
            // T16 tuş dizisinden de son tuşu sil
            if (t16KeySequence.isNotEmpty()) {
                t16KeySequence.removeAt(t16KeySequence.size - 1)
            }
            
            // T16 ve Standard modunda da silme işlemi yap
            currentInputConnection?.deleteSurroundingText(1, 0)
            
            if (currentInput.isNotEmpty()) {
                if (currentMode == KeyboardMode.T9) {
                    // T9 modunda yeni öneriler al
                    currentSuggestions = wordDatabase.getPossibleWords(currentInput.toString())
                    updateSuggestions(currentSuggestions)
                    if (currentSuggestions.isNotEmpty()) {
                        currentInputConnection?.setComposingText(currentSuggestions[0], 1)
                    } else {
                        currentInputConnection?.setComposingText(currentInput.toString(), 1)
                    }
                } else if (currentMode == KeyboardMode.T16) {
                    // T16 modunda tuş dizisine göre öneriler al
                    if (t16KeySequence.size >= 2) {
                        val keySequenceWords = wordDatabase.getWordsFromT16KeySequence(t16KeySequence)
                        val inputText = currentInput.toString().lowercase()
                        val prefixWords = wordDatabase.getWordsByPrefix(inputText).take(4)
                        val combinedSuggestions = (keySequenceWords + prefixWords).distinct().take(4)
                        currentSuggestions = combinedSuggestions
                        updateT16Suggestions(currentSuggestions)
                    } else {
                        currentSuggestions = emptyList()
                        updateT16Suggestions(emptyList())
                    }
                } else {
                    // Standard modunda prefix bazlı öneriler al
                    if (currentInput.length >= 2) {
                        val inputText = currentInput.toString().lowercase()
                        currentSuggestions = wordDatabase.getWordsByPrefix(inputText).take(4)
                        updateT16Suggestions(currentSuggestions)
                    } else {
                        currentSuggestions = emptyList()
                        updateT16Suggestions(emptyList())
                    }
                }
            } else {
                if (currentMode == KeyboardMode.T9) {
                    currentInputConnection?.setComposingText("", 1)
                    updateSuggestions(emptyList())
                } else {
                    updateT16Suggestions(emptyList())
                }
            }
        } else {
            // Normal backspace
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
    }
    
    private fun onEnterPressed() {
        currentInputConnection?.finishComposingText()
        currentInputConnection?.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        )
        currentInput.clear()
        currentSuggestions = emptyList()
    }
    
    private fun toggleMode() {
        // Cycle through modes: T9 -> T16 -> Standard -> T9 (excluding SYMBOLS)
        currentMode = when (currentMode) {
            KeyboardMode.T9 -> KeyboardMode.T16
            KeyboardMode.T16 -> KeyboardMode.STANDARD
            KeyboardMode.STANDARD -> KeyboardMode.T9
            KeyboardMode.SYMBOLS -> KeyboardMode.T16  // Return to T16 if somehow in symbols
        }
        isShiftActive = false
        currentInput.clear()
        currentSuggestions = emptyList()
        t16KeySequence.clear()  // T16 tuş dizisini temizle
        // Klavye görünümünü yeniden oluştur
        setInputView(onCreateInputView())
    }
    
    private fun switchToSymbolsMode() {
        performHapticFeedback()
        playSoundEffect()
        currentMode = KeyboardMode.SYMBOLS
        isShiftActive = false
        currentInput.clear()
        currentSuggestions = emptyList()
        t16KeySequence.clear()
        // Klavye görünümünü yeniden oluştur
        setInputView(onCreateInputView())
    }
    
    private fun switchToLettersMode() {
        performHapticFeedback()
        playSoundEffect()
        // Return to T16 mode (most versatile letter mode)
        currentMode = KeyboardMode.T16
        isShiftActive = false
        currentInput.clear()
        currentSuggestions = emptyList()
        t16KeySequence.clear()
        // Klavye görünümünü yeniden oluştur
        setInputView(onCreateInputView())
    }
    
    private fun setupSymbolsKeyListeners(view: View) {
        // Sayı tuşları (0-9)
        val numberKeys = mapOf(
            R.id.key_sym_1 to "1", R.id.key_sym_2 to "2", R.id.key_sym_3 to "3",
            R.id.key_sym_4 to "4", R.id.key_sym_5 to "5", R.id.key_sym_6 to "6",
            R.id.key_sym_7 to "7", R.id.key_sym_8 to "8", R.id.key_sym_9 to "9",
            R.id.key_sym_0 to "0"
        )
        
        numberKeys.forEach { (keyId, char) ->
            view.findViewById<Button>(keyId)?.setOnClickListener {
                performHapticFeedback()
                playSoundEffect()
                currentInputConnection?.commitText(char, 1)
            }
        }
        
        // Özel karakterler
        val symbolKeys = mapOf(
            R.id.key_sym_at to "@", R.id.key_sym_hash to "#", R.id.key_sym_dollar to "$",
            R.id.key_sym_percent to "%", R.id.key_sym_ampersand to "&", R.id.key_sym_asterisk to "*",
            R.id.key_sym_minus to "-", R.id.key_sym_plus to "+", R.id.key_sym_equals to "=",
            R.id.key_sym_lparen to "(", R.id.key_sym_rparen to ")", R.id.key_sym_lbrace to "{",
            R.id.key_sym_rbrace to "}", R.id.key_sym_lbracket to "[", R.id.key_sym_rbracket to "]",
            R.id.key_sym_less to "<", R.id.key_sym_greater to ">", R.id.key_sym_slash to "/",
            R.id.key_sym_backslash to "\\", R.id.key_sym_exclamation to "!", R.id.key_sym_question to "?",
            R.id.key_sym_colon to ":", R.id.key_sym_semicolon to ";", R.id.key_sym_quote to "\"",
            R.id.key_sym_apostrophe to "'", R.id.key_sym_backtick to "`", R.id.key_sym_tilde to "~"
        )
        
        symbolKeys.forEach { (keyId, char) ->
            view.findViewById<Button>(keyId)?.setOnClickListener {
                performHapticFeedback()
                playSoundEffect()
                currentInputConnection?.commitText(char, 1)
            }
        }
        
        // ABC tuşu - harflere dön
        view.findViewById<Button>(R.id.key_letters)?.setOnClickListener {
            switchToLettersMode()
        }
        
        // Shift tuşu - sembol modunda fonksiyonel değil, sadece görsel
        view.findViewById<Button>(R.id.key_sym_shift)?.setOnClickListener {
            performHapticFeedback()
            // Sembol modunda shift işlevi yok, ileride ek semboller için kullanılabilir
        }
        
        // Backspace
        view.findViewById<Button>(R.id.key_backspace)?.setOnClickListener {
            onBackspacePressed()
        }
        
        // Enter
        view.findViewById<Button>(R.id.key_enter)?.setOnClickListener {
            onEnterPressed()
        }
        
        // Space
        view.findViewById<Button>(R.id.key_space)?.setOnClickListener {
            performHapticFeedback()
            playSoundEffect()
            currentInputConnection?.commitText(" ", 1)
        }
        
        // Comma
        view.findViewById<Button>(R.id.key_comma)?.setOnClickListener {
            performHapticFeedback()
            playSoundEffect()
            currentInputConnection?.commitText(",", 1)
        }
        
        // Dot
        view.findViewById<Button>(R.id.key_dot)?.setOnClickListener {
            performHapticFeedback()
            playSoundEffect()
            currentInputConnection?.commitText(".", 1)
        }
    }
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentInput.clear()
        currentSuggestions = emptyList()
        t16KeySequence.clear()  // T16 tuş dizisini temizle
        updateSuggestions(emptyList())
        isShiftActive = false
    }
}