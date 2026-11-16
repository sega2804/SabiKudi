# SQLCipher Keep Rules
-keep class net.zetetic.database.sqlcipher.** { *; }
-keep class net.zetetic.database.** { *; }

# Room Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt & Dagger
-keep class * extends java.lang.Enum { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Kotlin Serialization
-keepattributes *Annotation*, ElementValueAttribute
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    @kotlinx.serialization.Serializable *;
}

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# CameraX
-keep class androidx.camera.** { *; }

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Datastore
-keep class androidx.datastore.** { *; }
