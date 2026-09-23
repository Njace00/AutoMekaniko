# Project specific ProGuard rules for AutoMekaniko

# SceneView & Filament 3D Engine Keep Rules
-keep class io.github.sceneview.** { *; }
-keep class com.google.android.filament.** { *; }
-dontwarn io.github.sceneview.**
-dontwarn com.google.android.filament.**

# Kotlin Coroutines Keep Rules
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepclassmembers class ** {
    @kotlinx.coroutines.InternalCoroutinesApi *;
}
