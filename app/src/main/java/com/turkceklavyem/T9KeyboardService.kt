package com.turkceklavyem

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.LayoutInflater
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button

class T9KeyboardService : InputMethodService() {
    
    private var isT9Mode = true  // true = T9 modu, false = T12 modu
    private var currentInput = StringBuilder()
    private val wordDatabase = WordDatabase()
    private var currentKeyboardView: View? = null
    
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
            onSymbolPressed()
        }
        
        view.findViewById<Button>(R.id.key_hash)?.setOnClickListener {
            onHashPressed()
        }
    }
    
    private fun setupT12KeyListeners(view: View) {
        // T12 tuşları - her tuş iki harf içerir
        val keyMap = mapOf(
            R.id.key_qw to "qw",
            R.id.key_er to "er",
            R.id.key_ty to "ty",
            R.id.key_ui to "uı",
            R.id.key_op to "op",
            R.id.key_as to "as",
            R.id.key_df to "df",
            R.id.key_gh to "gğ",
            R.id.key_jk to "jk",
            R.id.key_lu to "lü",
            R.id.key_zx to "zx",
            R.id.key_cv to "cç",
            R.id.key_bn to "bn",
            R.id.key_mo to "mö"
        )
        
        keyMap.forEach { (keyId, chars) ->
            view.findViewById<Button>(keyId)?.setOnClickListener {
                onT12KeyPressed(chars)
            }
        }
        
        view.findViewById<Button>(R.id.key_space)?.setOnClickListener {
            onSpacePressed()
        }
        
        view.findViewById<Button>(R.id.key_dot)?.setOnClickListener {
            currentInputConnection?.commitText(".", 1)
        }
    }
    
    private fun onT9KeyPressed(key: Int) {
        // T9 modunda tuş basımını işle
        currentInput.append(key)
        
        // TODO: WordDatabase'den kelime önerileri al
        val suggestions = wordDatabase.getPossibleWords(currentInput.toString())
        
        // Şimdilik sadece ilk öneriyi veya tuş dizisini göster
        if (suggestions.isNotEmpty()) {
            // İlk öneriyi göster
        } else {
            // Tuş dizisini göster
        }
    }
    
    private fun onT12KeyPressed(chars: String) {
        // T12 modunda doğrudan harf girişi
        // Şimdilik ilk harfi gir, gelecekte çoklu basım desteği eklenecek
        if (chars.isNotEmpty()) {
            val char = chars[0]
            currentInputConnection?.commitText(char.toString(), 1)
        }
    }
    
    private fun onSpacePressed() {
        currentInputConnection?.commitText(" ", 1)
        currentInput.clear()
    }
    
    private fun onSymbolPressed() {
        // Sembol modu geçişi (gelecekte eklenecek)
    }
    
    private fun onHashPressed() {
        // Özel karakter modu (gelecekte eklenecek)
    }
    
    private fun onBackspacePressed() {
        currentInputConnection?.deleteSurroundingText(1, 0)
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
        }
    }
    
    private fun onEnterPressed() {
        currentInputConnection?.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        )
        currentInput.clear()
    }
    
    private fun toggleMode() {
        isT9Mode = !isT9Mode
        // Klavye görünümünü yeniden oluştur
        setInputView(onCreateInputView())
    }
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentInput.clear()
    }
}