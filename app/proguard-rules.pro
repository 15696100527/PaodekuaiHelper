# proguard-rules.pro - 简化版，不混淆
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 不混淆 Kotlin
-dontwarn kotlin.**
-keep class kotlin.** { *; }

# 不混淆自定义类
-keep class com.paodekuai.helper.** { *; }

# ViewBinding
-keepclassmembers class * {
    public *;
}
