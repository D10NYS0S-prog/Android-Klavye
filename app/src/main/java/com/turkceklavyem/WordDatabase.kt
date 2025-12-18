package com.turkceklavyem

/**
 * WordDatabase - Kelime veritabanı yönetimi
 * 
 * Bu sınıf T9/T12 sisteminde kelime tahminleri için kullanılacak
 * veritabanını yönetir. İleride SQLite veya Room kullanarak
 * Türkçe kelime sözlüğü entegrasyonu yapılacak.
 */
class WordDatabase {
    
    /**
     * Verilen tuş dizisine göre olası kelimeleri döndürür
     * @param keySequence Basılan tuşların dizisi (örn: "2665" -> "BOOK" benzeri)
     * @return Olası kelime listesi
     */
    fun getPossibleWords(keySequence: String): List<String> {
        // TODO: Veritabanından kelime sorgusu yapılacak
        return emptyList()
    }
    
    /**
     * Kullanıcı tercihlerine göre kelime sıklığını günceller
     * @param word Seçilen kelime
     */
    fun updateWordFrequency(word: String) {
        // TODO: Kelime kullanım sıklığı güncellenecek
    }
    
    /**
     * Veritabanını başlatır ve varsayılan Türkçe sözlüğü yükler
     */
    fun initialize() {
        // TODO: Veritabanı başlatma ve sözlük yükleme
    }
}
