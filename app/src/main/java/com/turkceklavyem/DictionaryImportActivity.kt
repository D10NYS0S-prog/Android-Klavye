package com.turkceklavyem

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var btnSelectFile: Button
    private lateinit var btnImportTxt: Button
    private lateinit var btnImportPdf: Button
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val REQUEST_CODE_TXT = 1001
        private const val REQUEST_CODE_PDF = 1002
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
            openFilePicker("text/plain", REQUEST_CODE_TXT)
        }
        
        btnImportPdf.setOnClickListener {
            openFilePicker("application/pdf", REQUEST_CODE_PDF)
        }
        
        updateStatus("TXT veya PDF dosyası seçin", false)
    }
    
    private fun openFilePicker(mimeType: String, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }
        startActivityForResult(intent, requestCode)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                when (requestCode) {
                    REQUEST_CODE_TXT -> importFromTxt(uri)
                    REQUEST_CODE_PDF -> importFromPdf(uri)
                }
            }
        }
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
        updateStatus("PDF dosyası okunuyor...", true)
        
        scope.launch(Dispatchers.IO) {
            try {
                // PDF okuma için basit metin çıkarma
                // Not: Tam PDF desteği için Apache PDFBox veya iText gerekir
                // Şimdilik basit metin çıkarma deniyoruz
                val text = readTextFromUri(uri)
                val wordCount = processAndImportText(text)
                
                withContext(Dispatchers.Main) {
                    if (wordCount > 0) {
                        updateStatus("✅ Başarılı! $wordCount kelime eklendi.", false)
                        Toast.makeText(this@DictionaryImportActivity, 
                            "$wordCount kelime sözlüğe eklendi", 
                            Toast.LENGTH_LONG).show()
                    } else {
                        updateStatus("⚠️ PDF'den kelime çıkarılamadı. TXT dosyası kullanın.", false)
                        Toast.makeText(this@DictionaryImportActivity, 
                            "PDF okuma sınırlı. Lütfen TXT dosyası kullanın.", 
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateStatus("❌ Hata: ${e.message}", false)
                    Toast.makeText(this@DictionaryImportActivity, 
                        "PDF okunamadı. TXT dosyası deneyin.", 
                        Toast.LENGTH_LONG).show()
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
