# Sonora — Musiqa Pleer

Android uchun original, zamonaviy Music Player ilovasi. Kotlin + Jetpack
Compose + Media3 (ExoPlayer) bilan noldan yozilgan.

## Arxitektura

MVVM + Clean Architecture'ga yaqin qatlamlarga bo'lingan:

```
app/src/main/java/com/sonora/player/
├── di/              — Hilt modullari (Database, Repository)
├── data/
│   ├── database/    — Room entity'lari va DAO'lar (playlist, favorite, tarix)
│   └── repository/  — MediaScanner (MediaStore) + MusicRepositoryImpl
├── domain/
│   ├── model/       — Song, Playlist, PlaybackState (sof Kotlin data class'lar)
│   └── repository/  — MusicRepository interfeysi
├── player/          — PlaybackService (Media3 MediaSessionService) + PlayerConnection
├── ui/
│   ├── theme/       — Material You dinamik rang temasi
│   ├── navigation/  — NavHost, bottom navigation
│   ├── library/     — Kutubxona ekrani + ViewModel
│   ├── player/      — MiniPlayer, to'liq PlayerScreen + ViewModel
│   ├── playlist/    — Pleylist ro'yxati va tafsilot ekranlari
│   ├── favorites/   — Sevimlilar ekrani
│   └── settings/    — Sozlamalar (hozircha placeholder)
└── MainActivity.kt
```

### Nima uchun bunday bo'lingan

- **Musiqa fayllari Room'da saqlanmaydi** — ular har doim `MediaScanner` orqali
  to'g'ridan-to'g'ri `MediaStore`dan o'qiladi. Room faqat foydalanuvchi
  yaratgan ma'lumotlarni (pleylistlar, sevimlilar, tarix) saqlaydi va ularni
  qo'shiqning barqaror MediaStore ID'si orqali bog'laydi. Bu ikki manbani
  sinxronlashtirish muammosini butunlay yo'q qiladi.
- **PlaybackService — yagona haqiqat manbai.** UI hech qachon ExoPlayer bilan
  to'g'ridan-to'g'ri ishlamaydi; faqat `PlayerConnection` orqali
  `MediaController`ga buyruq beradi. Bu pleer holatini UI qayta
  yaratilganda ham (ekran aylantirilganda, ilova fonga o'tganda) saqlab
  qoladi — chunki pleerning o'zi alohida, uzoq umr ko'radigan xizmatda
  ishlaydi.
- **Media3 orqali** notification, lock screen boshqaruvi, Bluetooth/simli
  tinglagich tugmalari va audio focus (masalan qo'ng'iroq kelganda pauza)
  deyarli bepul olinadi — bularning barchasini qo'lda yozish shart emas.

## Texnologiya

- Kotlin, Gradle Kotlin DSL
- Jetpack Compose + Material 3 (Material You dinamik ranglar bilan)
- Hilt (Dependency Injection)
- Room (playlist/favorite/history saqlash)
- Media3 / ExoPlayer + MediaSession (pleyback dvigateli)
- Coroutines + StateFlow
- Coil (albom rasm yuklash)
- Navigation Compose
- Timber (logging)

## Qurish (Build)

### Variant A — Android Studio orqali

1. Loyihani Android Studio'da oching.
2. Birinchi sinxronlashda Gradle wrapper avtomatik yuklanadi (internet kerak).
3. `Build → Make Project`.
4. Ishga tushirish uchun ▶ tugmasini bosing.

### Variant B — faqat telefon orqali (GitHub Actions)

Loyihada tayyor `.github/workflows/build-apk.yml` bor:

1. GitHub'da repo yarating va loyiha fayllarini yuklang.
2. **Actions** bo'limiga o'ting — build avtomatik boshlanadi.
3. Tugagach, **Artifacts**'dan `SonoraPlayer-debug-apk`ni yuklab, telefoningizga o'rnating.

Terminal orqali:

```bash
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK (imzosiz)
./gradlew bundleRelease      # Play Store uchun AAB
```

## Release / Signing

`app/build.gradle.kts`dagi `release` build type'iga signing config qo'shing:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("your-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "sonora"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

Keystore faylini git'ga hech qachon qo'shmang.

## Testlash

```bash
./gradlew test              # Unit testlar
./gradlew connectedCheck    # Instrumentatsiya testlar (qurilma kerak)
```

## Hozirgi holat (v0.1.0 — vertikal kesim)

- ✅ MediaStore orqali to'liq kutubxona skanerlash
- ✅ Qidiruv, ruxsat so'rash oqimi (Android 13+ READ_MEDIA_AUDIO)
- ✅ To'liq pleer: Play/Pause/Next/Prev, Shuffle, Repeat (Off/One/All), Queue
- ✅ Playback Speed (0.5x–2.0x)
- ✅ Mini player + to'liq ekran pleer, seek bar
- ✅ Notification/Lock screen boshqaruvi, fon rejimida ijro, Bluetooth tugmalari
- ✅ Pleylist yaratish/o'chirish, qo'shiq qo'shish/olib tashlash
- ✅ Favorites, Recently Played, Most Played (repository darajasida tayyor)
- ✅ Material You dinamik ranglar

## Kelajakdagi kengaytirish (Future Expansion)

To'liq texnik topshiriqda so'ralgan quyidagi qismlar hali yo'q — bular
haqiqiy studiya loyihasida ham alohida sprint talab qiladigan ishlar:

1. **10-band Equalizer, Bass Boost, Virtualizer, Loudness Enhancer, Reverb** —
   `androidx.media3:media3-exoplayer` allaqachon loyihada bor;
   `android.media.audiofx.Equalizer` va boshqa `AudioEffect` klasslarini
   ExoPlayer'ning audio session ID'siga bog'lash kerak. Yangi
   `player/audioeffects/EqualizerController.kt` moduli sifatida qo'shilishi
   tavsiya etiladi.
2. **Sleep Timer** — `player/SleepTimerManager.kt`: WorkManager yoki oddiy
   coroutine `delay()` orqali belgilangan vaqtda `PlayerConnection`ga pauza
   buyrug'ini yuborish.
3. **Crossfade / Gapless Playback** — Media3 ExoPlayer'ning
   `setGaplessPlayback`/custom `AudioProcessor` orqali amalga oshiriladi.
4. **Lyrics (.lrc)** — `lyrics/LrcParser.kt` (fayl formatini o'qish) +
   `lyrics/LyricsRepository.kt` + pleer ekraniga sinxron aylanuvchi matn
   qatlami.
5. **Android Auto** — `PlaybackService`dagi mavjud `MediaSession` Android
   Auto uchun ham ishlatiladi; `res/xml/automotive_app_desc.xml` va tegishli
   manifest metadata qo'shish kerak.
6. **Recently Played / Most Played UI ekranlari** — repository darajasida
   tayyor (`observeRecentlyPlayed()`, `observeMostPlayed()`); faqat ularni
   ko'rsatadigan Compose ekran qo'shish qoladi (`LibraryScreen` naqshini
   takrorlash orqali).
7. **Keng testlar** — UI testlar (Compose test framework), integratsiya
   testlari (Room + repository), performance testlari (Macrobenchmark).

## Papka tuzilishi

Yuqoridagi "Arxitektura" bo'limiga qarang.
