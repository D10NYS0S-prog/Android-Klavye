# Türkçe Klavyem - Turkish T9/T12 Keyboard

Android için Türkçe karakterleri destekleyen T9/T12 tuşlu telefon klavyesi uygulaması.

## 📱 Proje Hakkında

Bu proje, eski tuşlu telefonlarda kullanılan T9 ve T12 klavye sistemlerini Android cihazlar için hayata geçiren bir klavye uygulamasıdır. Türkçe özel karakterler (Ç, Ğ, İ, Ö, Ş, Ü) tam destekle birlikte gelir.

## 🏗️ Proje Yapısı

```
TurkceKlavyem/
├── app/
│   ├── src/main/
│   │   ├── java/com/turkceklavyem/
│   │   │   ├── T9KeyboardService.kt      # Ana klavye servisi
│   │   │   ├── KeyboardView.kt            # Tuş haritaları ve yardımcı fonksiyonlar
│   │   │   └── WordDatabase.kt            # Kelime veritabanı yönetimi (placeholder)
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── keyboard_layout.xml    # Klavye görünüm düzeni
│   │   │   ├── values/
│   │   │   │   └── strings.xml            # String kaynakları
│   │   │   └── xml/
│   │   │       └── method.xml             # InputMethod yapılandırması
│   │   └── AndroidManifest.xml            # Uygulama manifestosu
│   ├── build.gradle                        # App modülü Gradle yapılandırması
│   └── proguard-rules.pro                  # ProGuard kuralları
├── build.gradle                            # Proje Gradle yapılandırması
├── settings.gradle                         # Gradle ayarları
└── gradle.properties                       # Gradle özellikleri
```

## ✨ Özellikler

## 🎨 Tasarım ve Tema

### Modern Material Design
Uygulama, profesyonel bir görünüm için Material Design prensipleriyle tasarlanmıştır:

**Özellikler:**
- ✅ Light ve Dark tema desteği
- ✅ Rounded corners (8dp yuvarlat köşeler)
- ✅ Press feedback (basma efekti)
- ✅ Elevation (yükseklik) efektleri
- ✅ Özelleştirilebilir renkler ve boyutlar
- ✅ Professional button styling

**Light Tema:**
- Temiz beyaz tuşlar (#FFFFFF)
- Açık gri keyboard arkaplanı (#ECEFF1)
- Functional tuşlar için mavi-gri vurgu (#CFD8DC)

**Dark Tema:**
- Koyu mavi-gri arkaplan (#263238)
- Medium gri tuşlar (#37474F)
- Yüksek kontrast metin

### Mevcut Özellikler
- ✅ Temel Android InputMethodService altyapısı
- ✅ T9 ve T12 tuş haritaları (Türkçe karakter destekli)
- ✅ 3x4 T9 tuş düzeni (0-9, *, #)
- ✅ 4-satır T12 QWERTY düzeni (shift, sembol tuşlarıyla)
- ✅ **Kelime Tahmini Sistemi** - T9 modunda otomatik kelime önerisi
- ✅ **Çoklu Basış Desteği** - Aynı tuşa hızlıca basarak farklı karakterler
- ✅ **Shift Tuşu** - Büyük/küçük harf geçişi
- ✅ **WordDatabase** - Kelime öğrenme ve tahmin sistemi
- ✅ Silme (Backspace) ve Enter tuşları
- ✅ T9/T12 mod değiştirme düğmesi
- ✅ Composing text desteği (gerçek zamanlı öneri)

### Gelecek Özellikler (TODO)
- 🔄 PDF okuma ve kelime öğrenme (altyapı hazır)
- 🔄 SQLite/Room veritabanı entegrasyonu
- 🔄 Daha geniş Türkçe sözlük
- 🔄 Uzun basış popup menüleri
- 🔄 Sembol klavyesi (12# tuşu)
- 🔄 Tema ve görünüm özelleştirmeleri

## 🔧 Teknik Detaylar

### T9 Modu - Kelime Tahmini

T9 modunda sayısal tuşlarla yazarken otomatik kelime tahmini yapılır:

**Tuş Haritası:**
```
1: . , ? ! 1
2: A B C Ç 2
3: D E F 3
4: G Ğ H I İ 4
5: J K L 5
6: M N O Ö 6
7: P Q R S Ş 7
8: T U V Ü 8
9: W X Y Z 9
0: Boşluk 0
```

**Kullanım Örneği:**
```
6-3-7-5-2-2-2 tuşlarına basın
→ Ekranda "merhaba" önerisi görünür
→ * tuşu ile diğer önerilere geçin
→ # tuşu ile öneriyi kabul edin
→ Boşluk tuşu ile de kabul edilir
```

### T12 Modu - Çoklu Basış

T12 modu QWERTY tarzı kompakt bir klavye düzenidir. Yeni düzen:

```
Satır 1: [qw] [er] [ty] [uı] [op]
Satır 2: [as] [df] [gğ] [jk] [l-]
Satır 3: [⇧] [zx] [cç] [bn] [m'] [⌫]
Satır 4: [12#][,] [boşluk] [.] [↵]
```

**Çoklu Basış Özelliği:**
- Aynı tuşa 800ms içinde tekrar basılırsa karakterler arasında geçiş yapar
- Örnek: **gh** tuşuna basınca → g, ğ, h, G, Ğ, H (sırayla döner)
- **⇧ (Shift)** tuşu aktifken büyük harf

**Kullanım Örnekleri:**
```
"merhaba" yazmak için:
m' → e-r → e-r → g-ğ → a-s → b-n → a-s
(her tuşa 1x basın, ilk harf otomatik gelir)

"Ğ" karakteri için:
1. ⇧ (shift) tuşuna bas
2. g-ğ tuşuna 2x hızlıca bas → Ğ

"ğ" karakteri için:
g-ğ tuşuna 2x hızlıca bas → ğ
```

## 🚀 Kurulum ve Kullanım

### Gereksinimler
- Android Studio Arctic Fox veya üzeri
- Android SDK 21 (Lollipop) veya üzeri
- Kotlin 1.9.20
- Android cihaz veya emulator

### Derleme Adımları
1. Projeyi Android Studio'da açın
2. Gradle senkronizasyonunu bekleyin
3. Build > Build Bundle(s) / APK(s) > Build APK(s)
4. APK dosyası `app/build/outputs/apk/debug/` klasöründe oluşacaktır

### Alternatif: Doğrudan Çalıştırma (Önerilen)
1. Android cihazınızı USB ile bağlayın veya emulator başlatın
2. Android Studio'da "Run" butonuna (yeşil üçgen) tıklayın veya `Shift + F10` tuşlarına basın
3. Hedef cihazınızı seçin
4. Uygulama otomatik olarak derlenip yüklenecektir
5. **Kurulum ekranı açılacaktır** - Bu ekran klavyeyi nasıl etkinleştireceğinizi gösterir

### Klavyeyi Etkinleştirme

Uygulama ilk açıldığında bir **kurulum rehberi** ekranı görünecektir. Bu ekran:
- Klavyenin etkinleştirilip etkinleştirilmediğini gösterir
- Klavye ayarlarına direkt yönlendirme sağlar
- Adım adım kurulum talimatları içerir

#### Adım 1: Klavyeyi Aktif Hale Getirin
1. Kurulum ekranındaki **"Klavyeyi Etkinleştir"** butonuna tıklayın
2. Açılan ayarlar ekranında **Türkçe Klavyem** yanındaki düğmeyi aktif edin
3. Uyarı mesajını okuyun ve **Tamam**'a basın
4. Geri tuşuna basarak uygulamaya dönün

#### Adım 2: Klavyeyi Seçin
1. Herhangi bir uygulamada (Mesajlar, Notlar, vb.) bir metin alanına dokunun
2. Klavye açıldığında, alt tarafta bulunan **klavye simgesine** dokunun
3. Açılır menüden **Türkçe Klavyem**'i seçin

#### Adım 3: Modlar Arasında Geçiş
- **T9 Modu**: Sayısal tuşlarla, çoklu basış ile harf girişi
- **T12 Modu**: QWERTY düzeninde, her tuşta iki harf
- **Mod Değiştirme**: Klavyenin üst kısmındaki **T9/T12** butonuna basın

### Test Senaryoları

#### T9 Modunu Test Etme
1. T9 modunda olduğunuzdan emin olun
2. Tuş **2**'ye bir kez basın → "A" yazmalı
3. Tuş **2**'ye iki kez basın → "B" yazmalı
4. Tuş **2**'ye üç kez basın → "C" yazmalı
5. Tuş **2**'ye dört kez basın → "Ç" yazmalı
6. **0** tuşuna basın → Boşluk eklemeli

#### T12 Modunu Test Etme
1. **T9/T12** butonuna basarak T12 moduna geçin
2. **[qw]** tuşuna bir kez basın → "q" yazmalı
3. **[qw]** tuşuna iki kez basın → "w" yazmalı
4. **[gğ]** tuşuna basarak Türkçe "ğ" karakterini test edin
5. **Boşluk** tuşuna basın → Boşluk eklemeli

#### Diğer Fonksiyonlar
- **⌫ (Backspace)**: Son karakteri silmeli
- **↵ (Enter)**: Yeni satıra geçmeli
- **. (Nokta)**: Nokta karakteri eklemeli (T12 modunda)

### Sorun Giderme

#### Android Studio'da "No Module" Hatası
Projeyi açarken "No module" hatası alıyorsanız, **TROUBLESHOOTING.md** dosyasına bakın. Kısa çözüm:
1. Android Studio'yu kapatın
2. `.gradle` ve `.idea` klasörlerini silin
3. Projeyi **File > Open** ile tekrar açın
4. Gradle senkronizasyonunu bekleyin

Detaylı çözümler için: **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)**

#### Klavye Görünmüyorsa
1. Ayarlar > Uygulamalar > Türkçe Klavyem > İzinler'i kontrol edin
2. Cihazı yeniden başlatın
3. Klavyeyi devre dışı bırakıp tekrar etkinleştirin

#### Karakterler Düzgün Gösterilmiyorsa
1. Cihazınızın Türkçe dil desteği olduğundan emin olun
2. Sistem yazı tipi ayarlarını kontrol edin

#### Mod Değişikliği Çalışmıyorsa
1. Uygulamayı tamamen kapatıp tekrar açın
2. APK'yı yeniden derleyip yükleyin

## 📝 Geliştirme Notları

### T9KeyboardService.kt
Ana klavye servisi. InputMethodService'i genişletir ve tuş basışlarını, mod değiştirmeyi ve metin girişini yönetir.

### KeyboardView.kt
T9 ve T12 tuş haritalarını içerir. Yardımcı fonksiyonlar:
- `getCharacterForKeyPress()`: Tuş basışına göre karakter döndürür
- `keysToSequence()`: Tuş dizisini string'e çevirir

### WordDatabase.kt
Kelime veritabanı yönetimi için placeholder. İleride:
- SQLite/Room entegrasyonu
- Türkçe kelime sözlüğü
- Kullanıcı kelime tercihleri

## 🤝 Katkıda Bulunma

Projeye katkıda bulunmak isterseniz:
1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some AmazingFeature'`)
4. Branch'e push yapın (`git push origin feature/AmazingFeature`)
5. Pull Request açın

## 📄 Lisans

Bu proje açık kaynak olarak geliştirilmektedir.

## 📧 İletişim

Proje Sahibi: D10NYS0S-prog

---

**Not:** Bu proje aktif geliştirme aşamasındadır. Temel altyapı tamamlanmış olup, kelime tahmini ve veritabanı özellikleri gelecek sürümlerde eklenecektir.
