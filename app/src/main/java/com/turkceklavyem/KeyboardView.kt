package com.turkceklavyem

/**
 * T9 Tuş Haritası - Her sayı tuşuna atanan harfler
 * Türkçe özel karakterler (Ç, Ğ, İ, Ö, Ş, Ü) dahil
 */
val T9_MAPPING: Map<Int, String> = mapOf(
    1 to ".,?!1",
    2 to "ABCÇ2",
    3 to "DEF3",
    4 to "GĞHİ4",
    5 to "JKL5",
    6 to "MNOÖ6",
    7 to "PQRSŞ7",
    8 to "TUVÜ8",
    9 to "WXYZ9",
    0 to " 0"
)

/**
 * T12 Tuş Haritası - Genişletilmiş klavye düzeni
 * T9'a ek olarak daha fazla harf ve sembol içerir
 */
val T12_MAPPING: Map<Int, String> = mapOf(
    1 to ".,?!;:1",
    2 to "ABCÇabc2",
    3 to "DEFdef3",
    4 to "GĞHİgğhı4",
    5 to "JKLjkl5",
    6 to "MNOÖmnoö6",
    7 to "PQRSŞpqrsş7",
    8 to "TUVÜtuvü8",
    9 to "WXYZwxyz9",
    0 to " _-0"
)

/**
 * Tuş basım sayısına göre karakter döndürür
 * @param key Basılan tuş (1-9)
 * @param pressCount Kaç kez basıldığı
 * @param isT9Mode T9 modu mu T12 modu mu
 * @return Karşılık gelen karakter
 */
fun getCharacterForKeyPress(key: Int, pressCount: Int, isT9Mode: Boolean): Char? {
    val mapping = if (isT9Mode) T9_MAPPING else T12_MAPPING
    val chars = mapping[key] ?: return null
    
    if (chars.isEmpty()) return null
    
    val index = (pressCount - 1) % chars.length
    return chars[index]
}

/**
 * Verilen tuş dizisini sayısal stringe çevirir
 * T9 kelime tahmini için kullanılır
 * @param keys Basılan tuşların listesi
 * @return Sayısal dizi string
 */
fun keysToSequence(keys: List<Int>): String {
    return keys.joinToString("")
}