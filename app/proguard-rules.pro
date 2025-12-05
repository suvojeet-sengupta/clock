# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep Room entities and DAOs
-keep class com.suvojeet.clock.data.alarm.AlarmEntity { *; }
-keep class com.suvojeet.clock.data.alarm.AlarmDao { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose related classes
-dontwarn androidx.compose.**

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Amazon Login SDK classes
-keep class com.amazon.identity.auth.device.** { *; }

# Keep Retrofit and Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.squareup.retrofit2.** { *; }

# Gson TypeToken - Critical for R8
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep generic signature of TypeToken and its subclasses
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Prevent R8 from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @Provides methods)
-keep,allowobfuscation,allowshrinking interface com.google.gson.TypeAdapterFactory
-keep,allowobfuscation,allowshrinking interface com.google.gson.JsonSerializer
-keep,allowobfuscation,allowshrinking interface com.google.gson.JsonDeserializer

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep data classes for serialization
-keep class com.suvojeet.clock.data.settings.AppTheme { *; }
-keep class com.suvojeet.clock.data.settings.DismissMethod { *; }
-keep class com.suvojeet.clock.data.settings.MathDifficulty { *; }