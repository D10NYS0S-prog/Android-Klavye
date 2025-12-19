package com.turkceklavyem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

/**
 * Klavye ayarları ekranı
 * Kullanıcıların klavye tercihlerini özelleştirmesine olanak tanır
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Ayarlar fragmentini yükle
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
        
        // Action bar başlığı
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Klavye Ayarları"
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    /**
     * Ayarlar fragmenti - klavye tercihlerini gösterir
     */
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.keyboard_preferences, rootKey)
            
            // Öğrenilen kelimeleri sil butonu
            findPreference<androidx.preference.Preference>("clear_learned_words")?.setOnPreferenceClickListener {
                clearLearnedWords()
                true
            }
        }
        
        private fun clearLearnedWords() {
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Öğrenilen Kelimeleri Sil")
            builder.setMessage("Klavyenin öğrendiği tüm kelimeler silinecek. Devam etmek istiyor musunuz?")
            builder.setPositiveButton("Evet") { _, _ ->
                // Kelime veritabanını sıfırla - singleton kullan
                WordDatabase.getInstance().clearLearnedWords()
                
                android.widget.Toast.makeText(
                    requireContext(),
                    "Öğrenilen kelimeler silindi",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            builder.setNegativeButton("İptal", null)
            builder.show()
        }
    }
}
