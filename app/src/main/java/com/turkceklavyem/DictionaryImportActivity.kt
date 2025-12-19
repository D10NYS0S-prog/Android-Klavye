package com.turkceklavyem

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.*

/**
 * Sözlük içe aktarma aktivitesi
 * Kullanıcıların TXT veya PDF dosyalarından kelime içe aktarmasına olanak tanır
 */
class DictionaryImportActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnImportTxt: Button
    private lateinit var btnImportPdf: Button
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    // Modern Activity Result API kullanımı
    private val txtFilePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { importFromTxt(it) }
    }
    
    private val pdfFilePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { importFromPdf(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary_import)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sözlük İçe Aktar"
        
        statusText = findViewById(R.id.tv_import_status)
        progressBar = findViewById(R.id.progress_import)
        btnImportTxt = findViewById(R.id.btn_import_txt)
        btnImportPdf = findViewById(R.id.btn_import_pdf)
        
        btnImportTxt.setOnClickListener {
            txtFilePicker.launch("text/plain")
        }
        
        btnImportPdf.setOnClickListener {
            pdfFilePicker.launch("application/pdf")
        }
        
        updateStatus("TXT veya PDF dosyası seçin", false)
    }
    
    private fun importFromTxt(uri: Uri) {
        updateStatus("TXT dosyası okunuyor...", true)
        
        scope.launch(Dispatchers.IO) {
            try {
                val text = readTextFromUri(uri)
                val wordCount = processAndImportText(text)
                
                withContext(Dispatchers.Main) {
                    updateStatus("✅ Başarılı! $wordCount kelime eklendi.", false)
                    Toast.makeText(this@DictionaryImportActivity, 
                        "$wordCount kelime sözlüğe eklendi", 
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateStatus("❌ Hata: ${e.message}", false)
                    Toast.makeText(this@DictionaryImportActivity, 
                        "Dosya okunamadı: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun importFromPdf(uri: Uri) {
        // PDF desteği sınırlıdır - kullanıcıya bilgi ver
        updateStatus("⚠️ PDF desteği sınırlı", false)
        
        scope.launch(Dispatchers.Main) {
            Toast.makeText(
                this@DictionaryImportActivity,
                "PDF okuma sınırlı desteklenir.\nEn iyi sonuç için TXT dosyası kullanın.\n\nPDF'den metin çıkarma deneniyor...",
                Toast.LENGTH_LONG
            ).show()
            
            // Basit PDF metin çıkarma denemesi
            delay(1000) // Toast'un görünmesi için
            
            withContext(Dispatchers.IO) {
                try {
                    val text = readTextFromUri(uri)
                    
                    // PDF binary içeriğini temizlemeye çalış
                    val cleanedText = text.replace(Regex("[^a-zA-ZçğıöşüÇĞİÖŞÜ\\s]"), " ")
                    
                    if (cleanedText.isBlank()) {
                        withContext(Dispatchers.Main) {
                            updateStatus("❌ PDF'den metin çıkarılamadı", false)
                            Toast.makeText(
                                this@DictionaryImportActivity,
                                "PDF dosyası okunamadı.\nLütfen TXT formatını kullanın.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@withContext
                    }
                    
                    val wordCount = processAndImportText(cleanedText)
                    
                    withContext(Dispatchers.Main) {
                        if (wordCount > 0) {
                            updateStatus("✅ PDF'den $wordCount kelime eklendi.", false)
                            Toast.makeText(
                                this@DictionaryImportActivity,
                                "$wordCount kelime eklendi (PDF desteği sınırlı)",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            updateStatus("❌ PDF'den kelime çıkarılamadı", false)
                            Toast.makeText(
                                this@DictionaryImportActivity,
                                "PDF okunamadı. TXT dosyası kullanın.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        updateStatus("❌ PDF okuma hatası", false)
                        Toast.makeText(
                            this@DictionaryImportActivity,
                            "PDF okunamadı. Lütfen TXT dosyası kullanın.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
    
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    stringBuilder.append('\n')
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
    
    private fun processAndImportText(text: String): Int {
        // WordDatabase'e kelimeleri ekle
        val wordDatabase = WordDatabase.getInstance()
        
        // Metni temizle ve kelimelere ayır
        val words = text.lowercase()
            .replace(Regex("[^a-zçğıöşü\\s]"), " ") // Sadece Türkçe harfler
            .split(Regex("\\s+"))
            .filter { it.length >= 2 } // En az 2 harfli kelimeler
            .distinct()
        
        // Her kelimeyi sözlüğe ekle
        words.forEach { word ->
            wordDatabase.addWordToDatabase(word)
        }
        
        return words.size
    }
    
    private fun updateStatus(message: String, showProgress: Boolean) {
        statusText.text = message
        progressBar.isVisible = showProgress
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
