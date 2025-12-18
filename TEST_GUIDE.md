# Türkçe Klavyem - Test Rehberi

Bu rehber, Türkçe Klavyem uygulamasını test etmek için adım adım talimatlar içermektedir.

## 📋 Ön Hazırlık

### Gerekli Araçlar
- **Android Studio** (önerilen sürüm: Hedgehog veya üzeri)
- **Android SDK 21+** yüklü
- **Android cihaz** (fiziksel cihaz veya emulator)
- **USB Debugging** etkin (fiziksel cihaz kullanıyorsanız)

### Projeyi Açma
1. Android Studio'yu başlatın
2. **File > Open** menüsünden projeyi seçin
3. Gradle senkronizasyonunun tamamlanmasını bekleyin (ilk açılışta birkaç dakika sürebilir)
4. Hataları kontrol edin (Build Messages penceresinde)

## 🔨 Derleme ve Kurulum

### Yöntem 1: Android Studio'dan Doğrudan Çalıştırma (Önerilen)

1. **Cihaz Hazırlığı**
   - Fiziksel cihaz: USB ile bağlayın, USB debugging'i etkinleştirin
   - Emulator: AVD Manager'dan bir emulator başlatın (örn: Pixel 5, API 30+)

2. **Çalıştırma**
   - Üst toolbar'da cihazınızı seçin
   - Yeşil **Run** butonuna tıklayın (veya `Shift + F10`)
   - İlk derleme 2-5 dakika sürebilir
   - Uygulama otomatik olarak cihaza yüklenecek ve başlayacaktır

### Yöntem 2: APK Derleme ve Manuel Kurulum

1. **APK Oluşturma**
   ```
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```
   - İşlem tamamlandığında "locate" linkine tıklayın
   - APK dosyası: `app/build/outputs/apk/debug/app-debug.apk`

2. **APK Yükleme**
   - **Via ADB**: `adb install app/build/outputs/apk/debug/app-debug.apk`
   - **Manuel**: APK'yı cihaza kopyalayın ve dosya yöneticisinden açın
   - "Bilinmeyen kaynaklardan yükleme" iznini verin (gerekirse)

## ⚙️ Klavyeyi Etkinleştirme

### Android 10+ için Adımlar

1. **Ayarlar uygulamasını açın**
2. **Sistem** bölümüne gidin
3. **Diller ve giriş**'i seçin
4. **Ekrandaki klavye**'yi seçin
5. **Klavyeleri yönet**'e tıklayın
6. **Türkçe Klavyem** yanındaki anahtarı açın (mavi yapın)
7. Uyarı mesajını okuyun ve **Tamam**'a basın

### Klavyeyi Varsayılan Yapma (Opsiyonel)

1. Ayarlar > Sistem > Diller ve giriş
2. **Varsayılan klavye**'yi seçin
3. **Türkçe Klavyem**'i seçin

## 🧪 Test Senaryoları

### Test 1: Temel Klavye Görünümü

**Amaç**: Klavyenin düzgün yüklendiğini doğrulama

1. Herhangi bir uygulamayı açın (örn: Mesajlar, Google Keep, Chrome)
2. Bir metin alanına dokunun
3. Klavye açılmazsa, klavye seçici butonuna basın ve "Türkçe Klavyem"i seçin

**Beklenen Sonuç**: 
- T9 klavyesi varsayılan olarak görünmeli
- 3x4 sayısal tuş düzeni görünmeli
- Üstte "T9/T12" ve "⌫" butonları olmalı

### Test 2: T9 Modu - Temel Karakter Girişi

**Test Adımları**:

| Tuş | Basış | Beklenen Çıktı |
|-----|-------|----------------|
| 2 | 1x | A |
| 2 | 2x | B |
| 2 | 3x | C |
| 2 | 4x | Ç |
| 4 | 1x | G |
| 4 | 2x | Ğ |
| 4 | 3x | H |
| 4 | 4x | I |
| 4 | 5x | İ |
| 6 | 4x | Ö |
| 8 | 4x | Ü |
| 7 | 5x | Ş |
| 0 | 1x | (Boşluk) |
| 1 | 1x | . |
| 1 | 2x | , |
| 1 | 3x | ? |
| 1 | 4x | ! |

**Test Cümlesi**: "Merhaba dünya!" yazmayı deneyin
- Tuş dizisi: 6-3-7-4-2-2-2-0-3-8-6-9-2-1(x4)

### Test 3: T9 Modu - Özel Tuşlar

1. **Backspace (⌫)**
   - Birkaç karakter yazın
   - Backspace'e basın
   - Son karakter silinmeli

2. **Enter**
   - Birkaç karakter yazın
   - Enter'a basın
   - Yeni satır eklenmeli

3. **Space (0)**
   - 0 tuşuna basın
   - Boşluk karakteri eklenmeli

### Test 4: Mod Değiştirme (T9 → T12)

1. T9 modundayken **T9/T12** butonuna basın
2. Klavye düzeni değişmeli
3. QWERTY-tarzı layout görünmeli

**Beklenen Düzen**:
```
┌────┬────┬────┬────┬────┐
│ qw │ er │ ty │ uı │ op │
└────┴────┴────┴────┴────┘
┌────┬────┬────┬────┬────┐
│ as │ df │ gğ │ jk │ lü │
└────┴────┴────┴────┴────┘
┌────┬────┬────┬────┐
│ zx │ cç │ bn │ mö │
└────┴────┴────┴────┘
```

### Test 5: T12 Modu - Karakter Girişi

**Tek Basış Testleri**:

| Tuş | Beklenen | Not |
|-----|----------|-----|
| qw | q | İlk harf |
| er | e | İlk harf |
| gğ | g | İlk harf |
| cç | c | İlk harf |
| mö | m | İlk harf |
| lü | l | İlk harf |

**Çoklu Basış Testleri** (Gelecek sürümde):
- qw'ye 2x basınca "w" yazmalı
- gğ'ye 2x basınca "ğ" yazmalı

### Test 6: T12 Modu - Tam Kelime

T12 modunda "merğaba" yazmayı deneyin:
1. [mö] → m
2. [er] → e
3. [er] → r
4. [gğ] 2x → ğ (çoklu basış özelliği henüz aktif değilse "g" yazar)
5. [as] → a
6. [bn] → b
7. [as] → a

### Test 7: Mod Geçişi Sürekliliği

1. T9 modunda "ABC" yazın
2. T12 moduna geçin
3. "def" yazın
4. Tekrar T9'a dönün
5. "123" yazın

**Beklenen**: Her mod geçişinde önceki içerik kaybolmamalı

### Test 8: Farklı Uygulamalarda Test

Klavyeyi şu uygulamalarda test edin:

1. **WhatsApp / Telegram**: Mesaj yazma
2. **Google Chrome**: Arama çubuğu
3. **Google Keep**: Not oluşturma
4. **Gmail**: E-posta yazma
5. **Contacts**: İsim girişi

Her uygulamada klavye düzgün açılmalı ve çalışmalı.

## 🐛 Bilinen Sorunlar ve Çözümler

### Klavye Listede Görünmüyor
**Çözüm**:
1. Ayarlar > Uygulamalar > Türkçe Klavyem
2. "Depolama"ya git
3. "Önbelleği temizle" ve "Verileri sil"
4. Uygulamayı yeniden başlat

### Karakterler Görünmüyor
**Çözüm**:
- Cihazın sistem yazı tipinin Türkçe karakterleri desteklediğinden emin olun
- Başka bir klavye ile Türkçe karakter yazarak test edin

### Mod Değişikliği Yanıt Vermiyor
**Çözüm**:
1. Klavyeyi kapat (geri tuşu)
2. Tekrar aç
3. Hala çalışmıyorsa, uygulamayı zorla durdur ve yeniden başlat

### APK Yüklenmiyor
**Çözüm**:
- Ayarlar > Güvenlik > "Bilinmeyen kaynaklardan yükleme"yi etkinleştir
- Android 8+: Her uygulama için ayrı izin gerekir (örn: Chrome, Dosya Yöneticisi)

## 📊 Test Checklist

Testi tamamladığınızda aşağıdaki maddeleri işaretleyin:

- [ ] Uygulama başarıyla yüklendi
- [ ] Klavye ayarlarda etkinleştirilebildi
- [ ] T9 modu düzgün görüntüleniyor
- [ ] T9 modunda tüm sayısal tuşlar çalışıyor
- [ ] T9 modunda Türkçe karakterler (Ç,Ğ,İ,Ö,Ş,Ü) yazılabiliyor
- [ ] Backspace tuşu çalışıyor
- [ ] Enter tuşu çalışıyor
- [ ] Space tuşu çalışıyor
- [ ] T9/T12 mod değiştirme çalışıyor
- [ ] T12 modu düzgün görüntüleniyor
- [ ] T12 modunda tüm harfler yazılabiliyor
- [ ] T12 modunda Türkçe karakterler (ğ,ç,ö,ü,ı) yazılabiliyor
- [ ] Farklı uygulamalarda klavye çalışıyor
- [ ] Mod geçişleri sorunsuz yapılabiliyor

## 📝 Test Sonuçlarını Raporlama

Test sırasında sorun bulursanız, lütfen şu bilgileri kaydedin:

1. **Cihaz Bilgileri**: Model, Android sürümü
2. **Sorun Tanımı**: Ne oldu? Ne olması bekleniyordu?
3. **Tekrar Adımları**: Sorunu tekrar oluşturma adımları
4. **Ekran Görüntüleri**: Mümkünse ekran görüntüsü veya video
5. **Logcat Çıktısı**: Android Studio'dan hata logları

## 🎯 İleri Düzey Test

### Performans Testi
1. Hızlı ve sürekli tuşlara basın
2. Klavye takılma yapmamalı
3. Tüm tuşlar zamanında yanıt vermeli

### Bellek Testi
1. Klavyeyi açıp kapatın (10 kez)
2. Bellek sızıntısı olmamalı
3. Android Studio Profiler ile bellek kullanımını izleyin

### Çoklu Dil Testi
1. Sistem dilini değiştirin
2. Klavye hala çalışıyor olmalı
3. Türkçe karakterler düzgün görünmeli

---

**Test rehberini tamamladıktan sonra, bulgularınızı GitHub Issues'a rapor edebilirsiniz.**
