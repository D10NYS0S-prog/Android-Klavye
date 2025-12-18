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

### Mevcut Özellikler
- ✅ Temel Android InputMethodService altyapısı
- ✅ T9 ve T12 tuş haritaları (Türkçe karakter destekli)
- ✅ 3x4 tuş düzeni (0-9, *, #)
- ✅ Silme (Backspace) ve Enter tuşları
- ✅ T9/T12 mod değiştirme düğmesi
- ✅ Temel tuş dinleyicileri ve giriş yönetimi

### Gelecek Özellikler (TODO)
- 🔄 Kelime tahmini sistemi
- 🔄 Türkçe kelime veritabanı entegrasyonu
- 🔄 T12 modunda çoklu tuş basışı desteği
- 🔄 Sembol ve özel karakter modu
- 🔄 Kullanıcı kelime öğrenme sistemi
- 🔄 Tema ve görünüm özelleştirmeleri

## 🔧 Teknik Detaylar

### T9 Tuş Haritası
```
1: . , ? ! 1
2: A B C Ç 2
3: D E F 3
4: G Ğ H İ 4
5: J K L 5
6: M N O Ö 6
7: P Q R S Ş 7
8: T U V Ü 8
9: W X Y Z 9
0: Boşluk 0
```

### T12 Tuş Haritası
T9'a ek olarak hem büyük hem küçük harfleri ve daha fazla sembolü içerir.

## 🚀 Kurulum ve Kullanım

### Gereksinimler
- Android Studio Arctic Fox veya üzeri
- Android SDK 21 (Lollipop) veya üzeri
- Kotlin 1.9.20

### Derleme Adımları
1. Projeyi Android Studio'da açın
2. Gradle senkronizasyonunu bekleyin
3. Build > Build Bundle(s) / APK(s) > Build APK(s)

### Klavyeyi Etkinleştirme
1. APK'yı cihazınıza yükleyin
2. Ayarlar > Sistem > Diller ve giriş > Ekrandaki klavye
3. "Türkçe Klavyem"i etkinleştirin
4. Herhangi bir metin alanına tıklayın ve klavye seçiciyi açın
5. "Türkçe Klavyem"i seçin

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
