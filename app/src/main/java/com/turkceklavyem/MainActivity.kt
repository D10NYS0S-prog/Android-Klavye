package com.turkceklavyem

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Klavye durumunu kontrol et ve göster
        updateKeyboardStatus()
        
        // Ayarlar butonuna tıklama dinleyicisi
        findViewById<Button>(R.id.btn_open_settings)?.setOnClickListener {
            openKeyboardSettings()
        }
        
        // Klavyeyi etkinleştir butonu
        findViewById<Button>(R.id.btn_enable_keyboard)?.setOnClickListener {
            openInputMethodSettings()
        }
        
        // Klavye ayarları butonu - yeni eklendi
        findViewById<Button>(R.id.btn_keyboard_settings)?.setOnClickListener {
            openKeyboardPreferences()
        }
    }
    
    private fun openKeyboardPreferences() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        updateKeyboardStatus()
    }
    
    private fun updateKeyboardStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        
        val isKeyboardEnabled = enabledInputMethods.any { 
            it.packageName == packageName 
        }
        
        val statusText = findViewById<TextView>(R.id.tv_keyboard_status)
        statusText?.text = if (isKeyboardEnabled) {
            "✅ Klavye Etkinleştirildi\n\nŞimdi herhangi bir metin alanına dokunun ve klavye seçicide 'Türkçe Klavyem'i seçin."
        } else {
            "❌ Klavye Henüz Etkinleştirilmedi\n\nLütfen aşağıdaki butona tıklayarak klavyeyi etkinleştirin."
        }
    }
    
    private fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    
    private fun openInputMethodSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        startActivity(intent)
    }
}
