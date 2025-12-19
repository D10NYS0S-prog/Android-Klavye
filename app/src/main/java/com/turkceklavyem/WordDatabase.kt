package com.turkceklavyem

/**
 * WordDatabase - Kelime veritabanı yönetimi ve tahmin sistemi
 * 
 * Bu sınıf T9/T16 sisteminde kelime tahminleri için kullanılır.
 * Basit bir in-memory sözlük ile başlayıp, ileride SQLite/Room entegrasyonu yapılabilir.
 * Singleton pattern kullanılarak tüm uygulamada tek bir instance kullanılır.
 */
class WordDatabase private constructor() {
    
    // Temel Türkçe kelime sözlüğü (demo amaçlı)
    private val turkishWords = mutableMapOf<String, MutableList<String>>().apply {
        // T9 tuş dizileri için kelimeler - genişletilmiş
        // "6375222" -> "merhaba"
        put("6375222", mutableListOf("merhaba"))
        put("6372", mutableListOf("mera", "merk"))
        put("63726", mutableListOf("merak", "meran", "meram"))
        put("637746", mutableListOf("mersin"))
        
        put("72526", mutableListOf("salam", "salak"))
        put("6376", mutableListOf("merk", "nerk"))
        put("626", mutableListOf("mam", "nan"))
        put("4764", mutableListOf("işık"))
        put("5366", mutableListOf("küçük", "küçüm"))
        
        // Daha fazla yaygın Türkçe kelime
        put("9374", mutableListOf("yazı"))
        put("93746", mutableListOf("yazık", "yazım"))
        put("46374", mutableListOf("güzel"))
        put("26786", mutableListOf("çorum"))
        put("26746", mutableListOf("çoğun"))
        put("29", mutableListOf("ay"))
        put("292", mutableListOf("aya"))
        put("3838", mutableListOf("düdü", "fütü"))
        put("3936", mutableListOf("evim"))
        put("7374", mutableListOf("sesi"))
        put("837", mutableListOf("üçü", "tes"))
        put("427", mutableListOf("göç", "gör"))
        put("32", mutableListOf("da", "de", "fa"))
        put("49", mutableListOf("ıy"))
        put("46", mutableListOf("ğm", "ğn", "ğo", "gm", "gn", "go", "ho", "ım", "in", "io"))
        
        // Kısa kelimeler
        put("93", mutableListOf("ya"))
        put("83", mutableListOf("te", "ve"))
        put("36", mutableListOf("en", "em", "fn"))
        put("84", mutableListOf("tg", "tı", "ti", "uğ", "ui", "ug"))
        put("68", mutableListOf("mu", "nu", "ot"))
        
        // Yaygın kelimeler
        put("4674", mutableListOf("işık", "gösı"))
        put("6876", mutableListOf("okul", "olum"))
        put("9378", mutableListOf("yurt"))
        put("4968", mutableListOf("ilgi", "ilmu"))
        put("9286", mutableListOf("yağı", "yaum"))
        put("53384", mutableListOf("keşif", "leduf"))
        put("43746", mutableListOf("işlem", "gesin"))
        put("6677", mutableListOf("motor"))
    }
    
    // T16 için kelime eşlemeleri (tuş kombinasyonlarına göre)
    private val t16Words = mutableMapOf<String, MutableList<String>>().apply {
        // Örnek: "m-er-gh-as-bn-as" tuş dizisi için
        // Her tuşun ilk harfleri: m,e,g,a,b,a = "megaba" benzeri
        put("meghab", mutableListOf("merhaba"))
        put("merghaba", mutableListOf("merhaba"))
        // Daha fazla yaygın kelimeler
        put("gherluier", mutableListOf("gelir"))
        put("gherlui", mutableListOf("gelir"))
        put("ghel", mutableListOf("gel"))
        put("ghit", mutableListOf("git"))
        put("ghun", mutableListOf("gün"))
        put("ghuzel", mutableListOf("güzel"))
        put("gheldi", mutableListOf("geldi"))
        put("ghiit", mutableListOf("gitti"))
        put("ghore", mutableListOf("göre"))
        put("ghoster", mutableListOf("göster"))
        put("ghelecek", mutableListOf("gelecek"))
    }
    
    // Kullanıcı tarafından öğrenilen kelimeler
    private val learnedWords = mutableMapOf<String, MutableList<String>>()
    
    // Kelime kullanım sıklığı
    private val wordFrequency = mutableMapOf<String, Int>()
    
    init {
        initialize()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null
        
        fun getInstance(): WordDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WordDatabase().also { INSTANCE = it }
            }
        }
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
     * T16: Tuş kombinasyonlarından kelime tahmini
     * @param keyPattern Tuş dizisi pattern'i (örn: "meghaba")
     * @return Olası kelime listesi
     */
    fun predictT16Words(keyPattern: String): List<String> {
        if (keyPattern.isEmpty()) return emptyList()
        
        val words = mutableListOf<String>()
        
        // Önce tam eşleşme ara
        t16Words[keyPattern.lowercase()]?.let { words.addAll(it) }
        learnedWords[keyPattern.lowercase()]?.let { words.addAll(it) }
        
        // Kısmi eşleşmeleri de ara
        for ((pattern, wordList) in t16Words) {
            if (pattern.startsWith(keyPattern.lowercase()) || 
                keyPattern.lowercase().startsWith(pattern)) {
                words.addAll(wordList)
            }
        }
        
        return words.distinct().sortedByDescending { wordFrequency[it] ?: 0 }.take(5)
    }
    
    /**
     * Prefix ile başlayan kelimeleri döndürür (T16 modu için)
     * @param prefix Kelime ön eki
     * @return Ön ekle başlayan kelime listesi
     */
    fun getWordsByPrefix(prefix: String): List<String> {
        if (prefix.isEmpty() || prefix.length < 2) return emptyList()
        
        val words = mutableSetOf<String>()
        val lowerPrefix = prefix.lowercase()
        
        // Tüm kelimelerden prefix ile başlayanları bul
        turkishWords.values.flatten().forEach { word ->
            if (word.lowercase().startsWith(lowerPrefix)) {
                words.add(word)
            }
        }
        
        learnedWords.values.flatten().forEach { word ->
            if (word.lowercase().startsWith(lowerPrefix)) {
                words.add(word)
            }
        }
        
        // Sıklığa göre sırala
        return words.toList().sortedByDescending { wordFrequency[it] ?: 0 }
    }
    
    /**
     * T16 tuş dizisinden kelime önerileri üretir
     * @param keySequence T16 tuş isimleri dizisi (örn: ["m", "er", "er", "gh", "as", "bn", "as"])
     * @return Olası kelime listesi
     */
    fun getWordsFromT16KeySequence(keySequence: List<String>): List<String> {
        if (keySequence.isEmpty()) return emptyList()
        
        // T16 tuş eşlemeleri - Türkçe karakterler dahil
        val keyToChars = mapOf(
            "qw" to "qw",
            "er" to "er",
            "ty" to "ty",
            "ui" to "uıi",  // u, ı, i karakterleri
            "op" to "opö",   // o, p, ö karakterleri
            "as" to "as",
            "df" to "df",
            "gh" to "gğh",   // g, ğ, h karakterleri
            "jk" to "jk",
            "l" to "l",
            "zx" to "zx",
            "cv" to "cçv",   // c, ç, v karakterleri
            "bn" to "bn",
            "m" to "m"
        )
        
        // Her tuş için olası karakterleri al
        val possibleCharsPerKey = keySequence.map { key ->
            val normalizedKey = key.replace("key_", "").lowercase()
            keyToChars[normalizedKey]?.toList() ?: listOf()
        }
        
        // Olası kombinasyonları oluştur
        val combinations = generateCombinations(possibleCharsPerKey)
        
        // Sözlükte var olan kelimeleri bul
        val matchedWords = mutableSetOf<String>()
        val allWords = turkishWords.values.flatten() + learnedWords.values.flatten()
        
        for (combo in combinations) {
            val word = combo.joinToString("").lowercase()
            // Tam eşleşme kontrolü
            if (allWords.any { it.lowercase() == word }) {
                allWords.find { it.lowercase() == word }?.let { matchedWords.add(it) }
            }
        }
        
        // Prefix bazlı eşleşmeleri de ekle (kombinasyonların prefix'i ile başlayan kelimeler)
        if (matchedWords.size < 4 && combinations.isNotEmpty()) {
            val prefixes = combinations.map { it.joinToString("").lowercase() }.filter { it.length >= 2 }
            for (prefix in prefixes.take(3)) {
                allWords.filter { it.lowercase().startsWith(prefix) }
                    .forEach { matchedWords.add(it) }
            }
        }
        
        // Sıklığa göre sırala ve en fazla 4 sonuç döndür
        return matchedWords.toList()
            .sortedByDescending { wordFrequency[it] ?: 0 }
            .take(4)
    }
    
    /**
     * Karakter listelerinden tüm kombinasyonları üretir
     */
    private fun generateCombinations(charLists: List<List<Char>>): List<List<Char>> {
        if (charLists.isEmpty()) return listOf(emptyList())
        if (charLists.size == 1) return charLists[0].map { listOf(it) }
        
        val result = mutableListOf<List<Char>>()
        val firstChars = charLists[0]
        val remainingCombos = generateCombinations(charLists.drop(1))
        
        for (char in firstChars) {
            for (combo in remainingCombos) {
                result.add(listOf(char) + combo)
            }
        }
        
        return result
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
     * Kelimeyi doğrudan veritabanına ekler (dosya içe aktarma için)
     * @param word Eklenecek kelime
     */
    fun addWordToDatabase(word: String) {
        if (word.isEmpty() || word.length < 2) return
        
        val keySeq = wordToT9Sequence(word)
        if (keySeq.isNotEmpty()) {
            val words = turkishWords.getOrPut(keySeq) { mutableListOf() }
            if (!words.contains(word)) {
                words.add(word)
                // Yeni kelimeye başlangıç sıklığı ver
                wordFrequency[word] = 1
            }
        }
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
        wordFrequency["günaydın"] = 90
        wordFrequency["iyi"] = 95
        wordFrequency["teşekkür"] = 85
        wordFrequency["evet"] = 100
        wordFrequency["hayır"] = 90
        wordFrequency["tamam"] = 95
        
        // Yaygın kelimeleri otomatik yükle
        addCommonWords()
        
        // Gelecekte: SQLite/Room veritabanından yükleme
        // veya assets klasöründen Türkçe sözlük yükleme
    }
    
    /**
     * Sözlüğe toplu kelime ekler (örnek Türkçe kelimeler)
     */
    fun addCommonWords() {
        val commonWords = listOf(
            "merhaba", "günaydın", "iyi", "teşekkür", "ederim",
            "lütfen", "evet", "hayır", "tamam", "hoşça", "kal",
            "nasıl", "naber", "selam", "hoşgeldin", "görüşürüz",
            "yarın", "bugün", "dün", "akşam", "sabah", "öğle",
            "para", "araba", "ev", "iş", "okul", "öğrenci",
            "öğretmen", "anne", "baba", "kardeş", "arkadaş",
            "sevgili", "aşk", "mutlu", "üzgün", "kötü", "güzel",
            "büyük", "küçük", "yeni", "eski", "hızlı", "yavaş",
            "su", "yemek", "içmek", "gelmek", "gitmek", "almak",
            "vermek", "yapmak", "etmek", "olmak", "bilmek", "görmek",
            // Fiil çekimleri ve yaygın kelimeler
            "gelir", "gider", "geldi", "gitti", "gelecek", "gidecek",
            "gel", "git", "gün", "göre", "göster", "gör",
            "için", "ancak", "veya", "ile", "kadar", "gibi",
            "daha", "çok", "az", "bir", "iki", "üç",
            "var", "yok", "oldu", "olur", "olacak", "olan",
            "ben", "sen", "biz", "siz", "mı", "mi", "mu", "mü"
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
     * Assets klasöründen Türkçe kelime listesini yükler
     * @param context Android context
     */
    fun loadWordsFromAssets(context: android.content.Context) {
        try {
            val inputStream = context.assets.open("turkce_kelime_listesi.txt")
            val words = inputStream.bufferedReader().use { it.readLines() }
            
            var loadedCount = 0
            for (word in words) {
                val cleanWord = word.trim().lowercase()
                if (cleanWord.isNotEmpty() && cleanWord.length >= 2) {
                    // Sadece Türkçe karakterler içeren kelimeleri al
                    if (cleanWord.matches(Regex("[a-zçğıöşü\\s]+"))) {
                        addWordToDatabase(cleanWord)
                        loadedCount++
                    }
                }
            }
            
            android.util.Log.d("WordDatabase", "Loaded $loadedCount words from turkce_kelime_listesi.txt")
        } catch (e: Exception) {
            android.util.Log.e("WordDatabase", "Error loading word list from assets", e)
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
