# Android Studio Kurulum ve Sorun Giderme

Bu doküman, Android Klavye projesini Android Studio'da açarken karşılaşılan yaygın sorunları ve çözümlerini içerir.

## 🔴 "Module not specified" / "Error: Module not specified" Hatası

### Sorun
"Module not specified" veya "Error: Module not specified" hatası alıyorsanız, run configuration eksik veya hatalı demektir.

### Çözüm 1: Otomatik Run Configuration (Önerilen)

1. **File > Sync Project with Gradle Files** yapın
2. Android Studio otomatik olarak run configuration oluşturacak
3. Toolbar'da "app" seçili olmalı
4. Yeşil **Run** butonuna tıklayın

### Çözüm 2: Manuel Run Configuration Oluşturma

1. **Run > Edit Configurations** menüsüne gidin (veya toolbar'da dropdown > "Edit Configurations")

2. Sol üst köşede **"+"** butonuna tıklayın

3. **Android App** seçin

4. Ayarları yapın:
   - **Name**: app
   - **Module**: app (veya TurkceKlavyem.app)
   - **Launch**: Default Activity

5. **Apply** ve **OK** tıklayın

### Çözüm 3: Gradle Sync ve Rebuild

1. **File > Invalidate Caches and Restart**
2. Yeniden başladıktan sonra:
   - **File > Sync Project with Gradle Files**
   - **Build > Clean Project**
   - **Build > Rebuild Project**
3. Run configuration'ı kontrol edin

### Çözüm 4: .idea Klasörünü Yenileme

Eğer sorun devam ediyorsa:
1. Android Studio'yu kapatın
2. `.idea` klasörünü silin
3. Projeyi yeniden açın
4. Gradle sync'i bekleyin
5. Run configuration otomatik oluşacak

## 🔴 "No Module" Hatası

### Sorun
Android Studio'da projeyi açtığınızda "No module" veya benzer bir hata alıyorsanız.

### Çözüm 1: Projeyi Yeniden İçe Aktarma (Önerilen)

1. **Android Studio'yu kapatın** (tamamen çıkış yapın)

2. **Proje dizinindeki geçici dosyaları temizleyin**:
   ```bash
   # Windows (PowerShell)
   Remove-Item -Recurse -Force .gradle, .idea, build, app/build
   
   # Linux/Mac
   rm -rf .gradle .idea build app/build
   ```

3. **Android Studio'yu yeniden açın**

4. **File > Open** menüsünden projenin kök dizinini seçin
   - `build.gradle` ve `settings.gradle` dosyalarının bulunduğu klasörü seçin
   - ⚠️ `app` klasörünü değil, üst klasörü seçtiğinizden emin olun

5. **Gradle senkronizasyonunu bekleyin**
   - Sağ alt köşede "Gradle sync" mesajı görünecek
   - İlk seferde 2-5 dakika sürebilir
   - İnternet bağlantınızın aktif olduğundan emin olun

6. **"Sync Now" veya "Sync Project with Gradle Files" butonuna tıklayın**
   - Toolbar'da fil simgesi (Gradle sync)
   - Veya: **File > Sync Project with Gradle Files**

### Çözüm 2: Manuel Gradle Sync

1. Android Studio'da projeyi açın

2. **File > Invalidate Caches and Restart** seçin
   - "Invalidate and Restart" butonuna tıklayın
   - Android Studio yeniden başlayacak

3. Yeniden başladıktan sonra:
   - **File > Sync Project with Gradle Files**
   - Veya toolbar'daki Gradle sync simgesine tıklayın

### Çözüm 3: Android Studio Ayarlarını Kontrol

1. **File > Project Structure** (Ctrl+Alt+Shift+S)

2. **Project** sekmesinde:
   - **Android Gradle Plugin Version**: 8.2.0 veya üzeri
   - **Gradle Version**: 8.2 veya üzeri

3. **Modules** sekmesinde:
   - `app` modülü görünüyor olmalı
   - Görünmüyorsa: "+" butonuna tıklayın > "Import Gradle Project"

### Çözüm 4: Gradle Wrapper'ı Güncelleme

1. Proje kök dizininde terminal açın:
   ```bash
   # Windows
   gradlew.bat wrapper --gradle-version=8.2
   
   # Linux/Mac
   ./gradlew wrapper --gradle-version=8.2
   ```

2. Android Studio'da projeyi yeniden yükleyin

## 🔴 "SDK not found" Hatası

### Sorun
Android SDK bulunamadı hatası alıyorsanız.

### Çözüm

1. **File > Settings** (Windows/Linux) veya **Android Studio > Preferences** (Mac)

2. **Appearance & Behavior > System Settings > Android SDK**

3. **SDK Location** alanını kontrol edin:
   - Windows: `C:\Users\[KullanıcıAdı]\AppData\Local\Android\Sdk`
   - Mac: `~/Library/Android/sdk`
   - Linux: `~/Android/Sdk`

4. SDK yüklü değilse:
   - **SDK Platforms** sekmesinden **Android 13.0 (API 33)** veya **Android 14.0 (API 34)** seçin
   - **SDK Tools** sekmesinden gerekli araçları seçin:
     - Android SDK Build-Tools
     - Android SDK Platform-Tools
     - Android Emulator (emulator kullanacaksanız)
   - **Apply** butonuna tıklayın

## 🔴 "Gradle sync failed" Hatası

### Sorun
Gradle senkronizasyonu başarısız oluyor.

### Çözüm

1. **İnternet bağlantınızı kontrol edin**
   - Gradle, bağımlılıkları indirmek için internet gerektirir

2. **Gradle önbelleğini temizleyin**:
   ```bash
   # Windows
   gradlew.bat clean
   
   # Linux/Mac
   ./gradlew clean
   ```

3. **Bağımlılık repository'lerini kontrol edin**
   
   `settings.gradle` dosyasında şunlar olmalı:
   ```gradle
   pluginManagement {
       repositories {
           google()
           mavenCentral()
           gradlePluginPortal()
       }
   }
   ```

4. **Proxy ayarlarını kontrol edin** (kurumsal ağdaysanız)
   - **File > Settings > Appearance & Behavior > System Settings > HTTP Proxy**

## 🔴 "Build variant" veya "Configuration" Hatası

### Sorun
Build variant seçilemiyor veya yapılandırma hatası var.

### Çözüm

1. **Build Variants** panelini açın:
   - **View > Tool Windows > Build Variants**
   - Sol kenar çubuğunda "Build Variants" sekmesi

2. **app** modülü için **debug** variant'ı seçin

3. **Run Configuration** kontrol edin:
   - Toolbar'da "app" seçili olmalı
   - Yoksa: **Run > Edit Configurations**
   - "+" butonuna tıklayın > "Android App"
   - Module: app, Launch: Default Activity seçin

## 🔴 "Cannot resolve symbol" Hatası

### Sorun
Kotlin kodunda sınıflar veya fonksiyonlar tanınmıyor.

### Çözüm

1. **Gradle senkronizasyonu yapın**:
   - **File > Sync Project with Gradle Files**

2. **Cache'i temizleyin**:
   - **File > Invalidate Caches and Restart**

3. **Build > Clean Project** sonra **Build > Rebuild Project**

## 🔴 "Unresolved reference: R" Hatası

### Sorun
`R` sınıfı bulunamıyor, kaynaklar erişilemiyor.

### Çözüm

1. **XML dosyalarında syntax hatası kontrol edin**
   - `res/layout`, `res/values` dosyalarını kontrol edin
   - Eksik kapanış tag'i veya hatalı attribute var mı?

2. **Build > Clean Project**

3. **Gradle sync** yapın

4. `app/build/generated/` klasörünü silin ve rebuild edin

## 📋 Hızlı Kontrol Listesi

Projeyi açarken şu adımları izleyin:

- [ ] Android Studio güncel mi? (Hedgehog 2023.1.1 veya üzeri)
- [ ] JDK 17 yüklü mü?
- [ ] Android SDK 34 yüklü mü?
- [ ] İnternet bağlantısı aktif mi?
- [ ] Proje kök dizinini açtınız mı? (app klasörünü değil)
- [ ] Gradle sync tamamlandı mı?
- [ ] Build variant "debug" seçili mi?
- [ ] Run configuration "app" seçili mi?

## 🆘 Hala Çalışmıyor mu?

### Son Çare: Tamamen Temiz Başlangıç

1. **Projeyi kapatın ve Android Studio'dan çıkın**

2. **Tüm geçici dosyaları silin**:
   ```bash
   # Proje dizininde
   rm -rf .gradle .idea build app/build
   
   # Kullanıcı dizininde (dikkatli!)
   # Windows: %USERPROFILE%\.gradle\caches
   # Linux/Mac: ~/.gradle/caches
   rm -rf ~/.gradle/caches
   ```

3. **Android Studio'yu yeniden başlatın**

4. **File > Open** ile projeyi açın

5. **Trust project** diyen uyarıya "Trust" deyin

6. Gradle senkronizasyonunu bekleyin (5-10 dakika sürebilir)

## 📞 Destek

Sorun devam ederse:

1. **Hata logunu kaydedin**:
   - **Help > Show Log in Explorer/Finder**
   - `idea.log` dosyasını kontrol edin

2. **Build Output'u kontrol edin**:
   - **View > Tool Windows > Build**
   - Hata mesajlarını not edin

3. **GitHub Issues'a rapor edin**:
   - Hata mesajını
   - Android Studio versiyonunu
   - İşletim sistemini
   - Denediğiniz çözümleri ekleyin

---

## ✅ Başarılı Kurulum Göstergeleri

Proje düzgün yüklendiğinde:

- ✅ **Project** panelinde `app` modülü görünür
- ✅ **Build Variants** panelinde "debug" ve "release" seçenekleri var
- ✅ Toolbar'da "app" run configuration seçili
- ✅ Yeşil "Run" butonu aktif
- ✅ Kotlin dosyaları syntax highlighting ile gösteriliyor
- ✅ `R.` yazdığınızda otomatik tamamlama çalışıyor

Bu göstergeler varsa proje hazır! 🎉

**Run** butonuna tıklayabilir veya `Shift + F10` basabilirsiniz.
