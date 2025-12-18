package com.turkceklavyem

/**
 * T9 Tuş Haritası - Her sayı tuşuna atanan harfler
 * Türkçe özel karakterler (Ç, Ğ, İ, Ö, Ş, Ü) dahil
 * 
 * Not: Türkçe'de iki farklı I harfi vardır:
 * - İ (noktalı büyük i) ve i (noktalı küçük i)
 * - I (noktasız büyük ı) ve ı (noktasız küçük ı)
 */
val T9_MAPPING: Map<Int, String> = mapOf(
    1 to ".,?!1",
    2 to "ABCÇ2",
    3 to "DEF3",
    4 to "GĞHIİ4",
    5 to "JKL5",
    6 to "MNOÖ6",
    7 to "PQRSŞ7",
    8 to "TUVÜ8",
    9 to "WXYZ9",
    0 to " 0"
)

/**
 * T12 Tuş Düzeni - QWERTY tarzı kompakt klavye düzeni
 * Her tuşta iki harf bulunur, çoklu basış ile karakterler arasında geçiş yapılır
 * 
 * Düzen:
 * Satır 1: [qw] [er] [ty] [uı] [op]
 * Satır 2: [as] [df] [gğ] [jk] [lü]
 * Satır 3: [zx] [cç] [bn] [mö]
 */
val T12_LAYOUT = mapOf(
    "qw" to "qw",
    "er" to "er",
    "ty" to "ty",
    "ui" to "uı",
    "op" to "op",
    "as" to "as",
    "df" to "df",
    "gh" to "gğ",
    "jk" to "jk",
    "lu" to "lü",
    "zx" to "zx",
    "cv" to "cç",
    "bn" to "bn",
    "mo" to "mö"
)

/**
 * Tuş basım sayısına göre karakter döndürür (T9 için)
 * @param key Basılan tuş (1-9)
 * @param pressCount Kaç kez basıldığı
 * @return Karşılık gelen karakter
 */
fun getCharacterForKeyPress(key: Int, pressCount: Int): Char? {
    val chars = T9_MAPPING[key] ?: return null
    
    if (chars.isEmpty()) return null
    
    val index = (pressCount - 1) % chars.length
    return chars[index]
}

/**
 * T12 tuşu için çoklu basış ile karakter seçimi
 * @param chars Tuştaki karakterler (örn: "qw")
 * @param pressCount Kaç kez basıldığı
 * @return Seçilen karakter
 */
fun getT12Character(chars: String, pressCount: Int): Char? {
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