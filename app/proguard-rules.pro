# Musyfy Native Android App ProGuard & R8 Optimization Rules

# --- Hilt Dependency Injection ---
-keep class * extends ::javax.inject.Provider
-keep class * extends ::dagger.internal.Factory
-keep class * extends ::dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories
-keep class com.musyfy.nativeapp.MusyfyApplication { *; }

# --- Room Database & Entities ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# --- Domain & Data Models ---
-keep class com.musyfy.nativeapp.domain.model.** { *; }
-keep class com.musyfy.nativeapp.data.local.room.entity.** { *; }

# --- Media3 / ExoPlayer ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Retrofit, OkHttp & Gson ---
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# --- Firebase Analytics & Crashlytics ---
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable
-dontwarn com.google.firebase.**

# --- YoutubeDL Android Native Wrapper ---
-keep class com.yausername.youtubedl_android.** { *; }
-keepclassmembers class com.yausername.youtubedl_android.** { *; }
-dontwarn com.yausername.youtubedl_android.**

# --- Strip Debug & Verbose Logs in Release Builds ---
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
