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
 * Yeni Düzen:
 * Satır 1: [qw] [er] [ty] [uı] [op]
 * Satır 2: [as] [df] [gğ] [jk] [l-]
 * Satır 3: [⇧][zx] [cç] [bn] [m'][⌫]
 * Satır 4: [12#][,] [boşluk][.] [↵]
 */
val T12_LAYOUT = mapOf(
    "qw" to "qwQW",
    "er" to "erER",
    "ty" to "tyTY",
    "ui" to "uıUİ",
    "op" to "opöOPÖ",
    "as" to "asAS",
    "df" to "dfDF",
    "gh" to "gğhGĞH",  // gh tuşuna basılı tutunca: g, ğ, G, Ğ, h, H
    "jk" to "jkJK",
    "l" to "l-L_",
    "zx" to "zxZX",
    "cv" to "cçvCÇV",
    "bn" to "bnBN",
    "m" to "m'öM'Ö"
)

/**
 * T12 tuş ID'lerini karakter eşlemelerine bağlar
 */
val T12_KEY_MAP = mapOf(
    "key_qw" to "qwQW",
    "key_er" to "erER",
    "key_ty" to "tyTY",
    "key_ui" to "uıiUİI",  // u, ı, i karakterleri
    "key_op" to "opöOPÖ",
    "key_as" to "asAS",
    "key_df" to "dfDF",
    "key_gh" to "gğhGĞH",  // g, ğ, h karakterleri
    "key_jk" to "jkJK",
    "key_l" to "l-L_",
    "key_zx" to "zxZX",
    "key_cv" to "cçvCÇV",  // c, ç, v karakterleri
    "key_bn" to "bnBN",
    "key_m" to "m'öM'Ö"
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
 * @param chars Tuştaki karakterler (örn: "qwQW")
 * @param pressCount Kaç kez basıldığı
 * @param isShiftActive Shift tuşu aktif mi
 * @return Seçilen karakter
 */
fun getT12Character(chars: String, pressCount: Int, isShiftActive: Boolean = false): Char? {
    if (chars.isEmpty()) return null
    
    // Shift aktifse, büyük harf karakterlerini kullan
    val availableChars = if (isShiftActive) {
        // Sadece büyük harfleri ve büyük harf versiyonu olan karakterleri al
        chars.filter { it.isUpperCase() || (!it.isLetter() && it.uppercaseChar() in chars) }
    } else {
        // Sadece küçük harfleri ve küçük harf versiyonu olan karakterleri al
        chars.filter { it.isLowerCase() || (!it.isLetter()) }
    }
    
    if (availableChars.isEmpty()) return null
    
    val index = (pressCount - 1) % availableChars.length
    return availableChars[index]
}

/**
 * Tuş dizisinden kelime tahmini için kod dizisi oluşturur
 * T9 kelime tahmini için kullanılır
 * @param keys Basılan tuşların listesi
 * @return Sayısal dizi string
 */
fun keysToSequence(keys: List<Int>): String {
    return keys.joinToString("")
}

/**
 * T12 tuş dizisinden kelime tahmini için karakter dizisi oluşturur
 * @param keySequence Basılan T12 tuşlarının listesi (örn: ["m", "er", "gh", "as", "bn", "as"])
 * @return Olası kelime kombinasyonları
 */
fun t12KeysToPattern(keySequence: List<String>): List<String> {
    if (keySequence.isEmpty()) return listOf("")
    
    val firstKey = keySequence.first()
    val chars = T12_LAYOUT[firstKey] ?: return listOf("")
    
    if (keySequence.size == 1) {
        return chars.map { it.toString() }
    }
    
    val restPatterns = t12KeysToPattern(keySequence.drop(1))
    val result = mutableListOf<String>()
    
    for (char in chars) {
        for (pattern in restPatterns) {
            result.add(char + pattern)
        }
    }
    
    return result
}