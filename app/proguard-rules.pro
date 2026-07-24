# Sonora Player proguard rules
-keepattributes *Annotation*
-keep class com.sonora.player.domain.model.** { *; }
-keep class com.sonora.player.data.database.** { *; }
-dontwarn kotlinx.coroutines.**

# Media3
-keep class androidx.media3.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
