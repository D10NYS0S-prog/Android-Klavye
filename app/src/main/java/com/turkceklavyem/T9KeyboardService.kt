package com.turkceklavyem

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.LayoutInflater
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.media.AudioManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class T9KeyboardService : InputMethodService(), SharedPreferences.OnSharedPreferenceChangeListener {
    
    private var isT9Mode = true  // true = T9 modu, false = T12 modu
    private var isShiftActive = false  // Shift tuşu aktif mi
    private var currentInput = StringBuilder()
    private val wordDatabase = WordDatabase.getInstance()
    private var currentKeyboardView: View? = null
    private var currentSuggestions: List<String> = emptyList()
    private var currentSuggestionIndex = 0
    
    // Vibration ve ses için
    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private lateinit var prefs: SharedPreferences
    private var vibrateOnKeypress = true
    private var soundOnKeypress = true
    
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
        loadPreferences()
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
        }
    }
    
    private fun loadPreferences() {
        vibrateOnKeypress = prefs.getBoolean("vibrate_on", true)
        soundOnKeypress = prefs.getBoolean("sound_on", true)
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
    
    override fun onCreateInputView(): View {
        // Klavye görünümünü bağla - mod seçimine göre
        val inflater = LayoutInflater.from(this)
        val layoutId = if (isT9Mode) R.layout.keyboard_layout else R.layout.keyboard_layout_t12
        val keyboardView = inflater.inflate(layoutId, null)
        currentKeyboardView = keyboardView
        
        // Tuş dinleyicilerini ayarla
        setupKeyListeners(keyboardView)
        
        return keyboardView
    }
    
    private fun setupKeyListeners(view: View) {
        if (isT9Mode) {
            setupT9KeyListeners(view)
        } else {
            setupT12KeyListeners(view)
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
            view.findViewById<Button>(keyId)?.setOnClickListener {
                onT9KeyPressed(index + 1)
            }
        }
        
        // Özel tuşlar
        view.findViewById<Button>(R.id.key_0)?.setOnClickListener {
            onSpacePressed()
        }
        
        view.findViewById<Button>(R.id.key_star)?.setOnClickListener {
            cycleSuggestion()
        }
        
        view.findViewById<Button>(R.id.key_hash)?.setOnClickListener {
            acceptSuggestion()
        }
    }
    
    private fun setupT12KeyListeners(view: View) {
        // T12 tuşları - yeni düzen
        val keyMap = mapOf(
            R.id.key_qw to "qwQW",
            R.id.key_er to "erER",
            R.id.key_ty to "tyTY",
            R.id.key_ui to "uıUİ",
            R.id.key_op to "opöOPÖ",
            R.id.key_as to "asAS",
            R.id.key_df to "dfDF",
            R.id.key_gh to "gğhGĞH",
            R.id.key_jk to "jkJK",
            R.id.key_l to "l-L_",
            R.id.key_zx to "zxZX",
            R.id.key_cv to "cçvCÇV",
            R.id.key_bn to "bnBN",
            R.id.key_m to "m'öM'Ö"
        )
        
        keyMap.forEach { (keyId, chars) ->
            view.findViewById<Button>(keyId)?.setOnClickListener {
                onT12KeyPressed(keyId.toString(), chars)
            }
        }
        
        // Shift tuşu
        view.findViewById<Button>(R.id.key_shift)?.setOnClickListener {
            toggleShift()
        }
        
        // Sembol tuşu
        view.findViewById<Button>(R.id.key_symbols)?.setOnClickListener {
            // TODO: Sembol modu
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
        
        if (currentSuggestions.isNotEmpty()) {
            // İlk öneriyi göster (composing text olarak)
            val suggestion = currentSuggestions[0]
            currentInputConnection?.setComposingText(suggestion, 1)
        } else {
            // Tuş dizisini göster
            currentInputConnection?.setComposingText(currentInput.toString(), 1)
        }
    }
    
    private fun onT12KeyPressed(keyId: String, chars: String) {
        // Haptic ve ses feedback
        performHapticFeedback()
        playSoundEffect()
        
        val currentTime = System.currentTimeMillis()
        
        // Aynı tuşa çabuk basıldıysa çoklu basış
        if (keyId == lastPressedKey && (currentTime - lastPressTime) < multiTapDelay) {
            currentPressCount++
            
            // Önceki karakteri sil
            currentInputConnection?.deleteSurroundingText(1, 0)
            
            // Yeni karakteri ekle
            val char = getT12Character(chars, currentPressCount, isShiftActive)
            currentInputConnection?.commitText(char.toString(), 1)
            
            // Zamanlayıcıyı iptal et
            commitRunnable?.let { handler.removeCallbacks(it) }
        } else {
            // Yeni tuş basıldı
            currentPressCount = 1
            val char = getT12Character(chars, currentPressCount, isShiftActive)
            currentInputConnection?.commitText(char.toString(), 1)
        }
        
        lastPressedKey = keyId
        lastPressTime = currentTime
        
        // Shift'i tek kullanım için kapat
        if (isShiftActive) {
            isShiftActive = false
            updateShiftButton()
        }
        
        // Zamanlayıcı ayarla
        commitRunnable = Runnable {
            lastPressedKey = null
            currentPressCount = 0
        }
        handler.postDelayed(commitRunnable!!, multiTapDelay)
    }
    
    private fun cycleSuggestion() {
        // * tuşu ile öneriler arasında geçiş
        if (currentSuggestions.isEmpty()) return
        
        currentSuggestionIndex = (currentSuggestionIndex + 1) % currentSuggestions.size
        val suggestion = currentSuggestions[currentSuggestionIndex]
        currentInputConnection?.setComposingText(suggestion, 1)
    }
    
    private fun acceptSuggestion() {
        // # tuşu ile öneriyi kabul et
        if (currentSuggestions.isNotEmpty()) {
            val word = currentSuggestions[currentSuggestionIndex]
            currentInputConnection?.finishComposingText()
            wordDatabase.updateWordFrequency(word)
            currentInput.clear()
            currentSuggestions = emptyList()
        }
    }
    
    private fun toggleShift() {
        isShiftActive = !isShiftActive
        updateShiftButton()
    }
    
    private fun updateShiftButton() {
        currentKeyboardView?.findViewById<Button>(R.id.key_shift)?.apply {
            alpha = if (isShiftActive) 1.0f else 0.5f
        }
    }
    
    private fun onSpacePressed() {
        // Eğer composing text varsa önce onu kabul et
        currentInputConnection?.finishComposingText()
        currentInputConnection?.commitText(" ", 1)
        currentInput.clear()
        currentSuggestions = emptyList()
    }
    
    private fun onBackspacePressed() {
        if (currentInput.isNotEmpty()) {
            // Henüz commit edilmemiş metin varsa
            currentInput.deleteCharAt(currentInput.length - 1)
            
            if (currentInput.isNotEmpty() && isT9Mode) {
                // Yeni öneriler al
                currentSuggestions = wordDatabase.getPossibleWords(currentInput.toString())
                if (currentSuggestions.isNotEmpty()) {
                    currentInputConnection?.setComposingText(currentSuggestions[0], 1)
                } else {
                    currentInputConnection?.setComposingText(currentInput.toString(), 1)
                }
            } else {
                currentInputConnection?.setComposingText("", 1)
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
        isT9Mode = !isT9Mode
        isShiftActive = false
        currentInput.clear()
        currentSuggestions = emptyList()
        // Klavye görünümünü yeniden oluştur
        setInputView(onCreateInputView())
    }
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentInput.clear()
        currentSuggestions = emptyList()
        isShiftActive = false
    }
}