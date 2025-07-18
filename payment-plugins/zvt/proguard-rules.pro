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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class de.lavego.** { *; }

-keeppackagenames de.tillhub.paymentengine.**
-keeppackagenames de.tillhub.paymentengine.zvt.**

-keep class de.tillhub.paymentengine.zvt.data.ZvtTerminal { *; }
-keep class de.tillhub.paymentengine.zvt.data.ZvtTerminal$* { *; }

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Breaking changes with AGP 8.0
# R8 upgrade documentation
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn de.tillhub.paymentengine.databinding.**