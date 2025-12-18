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
    
    override fun onCreateInputView(): View {
        // Klavye görünümünü bağla
        val inflater = LayoutInflater.from(this)
        val keyboardView = inflater.inflate(R.layout.keyboard_layout, null)
        
        // Tuş dinleyicilerini ayarla
        setupKeyListeners(keyboardView)
        
        return keyboardView
    }
    
    private fun setupKeyListeners(view: View) {
        // Sayı tuşları (1-9)
        val keyIds = arrayOf(
            R.id.key_1, R.id.key_2, R.id.key_3,
            R.id.key_4, R.id.key_5, R.id.key_6,
            R.id.key_7, R.id.key_8, R.id.key_9
        )
        
        keyIds.forEachIndexed { index, keyId ->
            view.findViewById<Button>(keyId)?.setOnClickListener {
                onKeyPressed(index + 1)
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
    
    private fun onKeyPressed(key: Int) {
        if (isT9Mode) {
            handleT9Input(key)
        } else {
            handleT12Input(key)
        }
    }
    
    private fun handleT9Input(key: Int) {
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
    
    private fun handleT12Input(key: Int) {
        // T12 modunda doğrudan harf girişi
        val letters = T12_MAPPING[key] ?: ""
        if (letters.isNotEmpty()) {
            // İlk harfi ekle (çoklu basım için mantık eklenecek)
            val char = letters[0]
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
        // TODO: Mod değişikliğini kullanıcıya göster
    }
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentInput.clear()
    }
}