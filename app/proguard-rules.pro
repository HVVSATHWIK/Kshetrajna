# Project-specific ProGuard/R8 Rules for Kshetrajna

# Keep line numbers and file attributes for stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Application Data Models and Enums
-keep class com.kshetrajna.app.data.** { *; }
-keep class com.kshetrajna.app.util.CapturedLocation { *; }
-keep class com.kshetrajna.app.util.ParsedExifData { *; }

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
