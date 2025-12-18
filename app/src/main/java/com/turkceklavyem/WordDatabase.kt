package com.turkceklavyem

/**
 * WordDatabase - Kelime veritabanı yönetimi ve tahmin sistemi
 * 
 * Bu sınıf T9/T12 sisteminde kelime tahminleri için kullanılır.
 * Basit bir in-memory sözlük ile başlayıp, ileride SQLite/Room entegrasyonu yapılabilir.
 */
class WordDatabase {
    
    // Temel Türkçe kelime sözlüğü (demo amaçlı)
    private val turkishWords = mutableMapOf<String, MutableList<String>>().apply {
        // T9 tuş dizileri için kelimeler
        // "6375222" -> "merhaba"
        put("6375222", mutableListOf("merhaba"))
        put("72526", mutableListOf("salam", "salak"))
        put("6376", mutableListOf("merk", "nerk"))
        put("626", mutableListOf("mam", "nan"))
        put("4764", mutableListOf("işık"))
        put("5366", mutableListOf("küçük", "küçüm"))
    }
    
    // T12 için kelime eşlemeleri (tuş kombinasyonlarına göre)
    private val t12Words = mutableMapOf<String, MutableList<String>>().apply {
        // Örnek: "m-er-gh-as-bn-as" tuş dizisi için
        // Her tuşun ilk harfleri: m,e,g,a,b,a = "megaba" benzeri
        put("meghab", mutableListOf("merhaba"))
        put("merghaba", mutableListOf("merhaba"))
        // Daha fazla kombinasyon eklenebilir
    }
    
    // Kullanıcı tarafından öğrenilen kelimeler
    private val learnedWords = mutableMapOf<String, MutableList<String>>()
    
    // Kelime kullanım sıklığı
    private val wordFrequency = mutableMapOf<String, Int>()
    
    init {
        initialize()
    }
    
    /**
     * T9: Verilen tuş dizisine göre olası kelimeleri döndürür
     * @param keySequence Basılan tuşların dizisi (örn: "2665" -> "BOOK" benzeri)
     * @return Olası kelime listesi, sıklığa göre sıralanmış
     */
    fun getPossibleWords(keySequence: String): List<String> {
        if (keySequence.isEmpty()) return emptyList()
        
        val words = mutableListOf<String>()
        
        // Önce kullanıcı kelimelerinden ara
        learnedWords[keySequence]?.let { words.addAll(it) }
        
        // Sonra sözlükten ara
        turkishWords[keySequence]?.let { words.addAll(it) }
        
        // Sıklığa göre sırala
        return words.distinct().sortedByDescending { wordFrequency[it] ?: 0 }
    }
    
    /**
     * T12: Tuş kombinasyonlarından kelime tahmini
     * @param keyPattern Tuş dizisi pattern'i (örn: "meghaba")
     * @return Olası kelime listesi
     */
    fun predictT12Words(keyPattern: String): List<String> {
        if (keyPattern.isEmpty()) return emptyList()
        
        val words = mutableListOf<String>()
        
        // Önce tam eşleşme ara
        t12Words[keyPattern.lowercase()]?.let { words.addAll(it) }
        learnedWords[keyPattern.lowercase()]?.let { words.addAll(it) }
        
        // Kısmi eşleşmeleri de ara
        for ((pattern, wordList) in t12Words) {
            if (pattern.startsWith(keyPattern.lowercase()) || 
                keyPattern.lowercase().startsWith(pattern)) {
                words.addAll(wordList)
            }
        }
        
        return words.distinct().sortedByDescending { wordFrequency[it] ?: 0 }.take(5)
    }
    
    /**
     * Kullanıcı tercihlerine göre kelime sıklığını günceller
     * @param word Seçilen kelime
     */
    fun updateWordFrequency(word: String) {
        wordFrequency[word] = (wordFrequency[word] ?: 0) + 1
    }
    
    /**
     * Yeni kelime öğrenir (kullanıcı tarafından yazılan kelimeler)
     * @param keySequence Tuş dizisi
     * @param word Kelime
     */
    fun learnWord(keySequence: String, word: String) {
        if (keySequence.isEmpty() || word.isEmpty()) return
        
        val words = learnedWords.getOrPut(keySequence) { mutableListOf() }
        if (!words.contains(word)) {
            words.add(word)
        }
        updateWordFrequency(word)
    }
    
    /**
     * PDF veya metin dosyasından kelimeleri öğrenir (gelecekte implement edilecek)
     * @param text Metin içeriği
     */
    fun learnFromText(text: String) {
        // Metni kelimelere ayır
        val words = text.lowercase()
            .replace(Regex("[^a-zçğıöşü\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
            .distinct()
        
        // Her kelime için T9 tuş dizisini hesapla ve kaydet
        for (word in words) {
            val keySeq = wordToT9Sequence(word)
            if (keySeq.isNotEmpty()) {
                learnWord(keySeq, word)
            }
        }
    }
    
    /**
     * Kelimeyi T9 tuş dizisine çevirir
     * @param word Kelime
     * @return T9 tuş dizisi
     */
    private fun wordToT9Sequence(word: String): String {
        val charToKey = mapOf(
            'a' to '2', 'b' to '2', 'c' to '2', 'ç' to '2',
            'd' to '3', 'e' to '3', 'f' to '3',
            'g' to '4', 'ğ' to '4', 'h' to '4', 'ı' to '4', 'i' to '4',
            'j' to '5', 'k' to '5', 'l' to '5',
            'm' to '6', 'n' to '6', 'o' to '6', 'ö' to '6',
            'p' to '7', 'q' to '7', 'r' to '7', 's' to '7', 'ş' to '7',
            't' to '8', 'u' to '8', 'v' to '8', 'ü' to '8',
            'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
        )
        
        return word.lowercase().mapNotNull { charToKey[it] }.joinToString("")
    }
    
    /**
     * Veritabanını başlatır ve varsayılan Türkçe sözlüğü yükler
     */
    fun initialize() {
        // Temel kelimeler için sıklık değerleri
        wordFrequency["merhaba"] = 100
        wordFrequency["salam"] = 50
        wordFrequency["salak"] = 20
        
        // Gelecekte: SQLite/Room veritabanından yükleme
        // veya assets klasöründen Türkçe sözlük yükleme
    }
    
    /**
     * Sözlüğe toplu kelime ekler (örnek Türkçe kelimeler)
     */
    fun addCommonWords() {
        val commonWords = listOf(
            "merhaba", "günaydın", "iyi", "teşekkür", "ederim",
            "lütfen", "evet", "hayır", "tamam", "hoşça", "kal"
        )
        
        for (word in commonWords) {
            val keySeq = wordToT9Sequence(word)
            val words = turkishWords.getOrPut(keySeq) { mutableListOf() }
            if (!words.contains(word)) {
                words.add(word)
            }
        }
    }
    
    /**
     * Öğrenilen kelimeleri temizler
     */
    fun clearLearnedWords() {
        learnedWords.clear()
        wordFrequency.clear()
        initialize()
    }
}
