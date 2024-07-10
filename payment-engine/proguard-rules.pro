# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-keep class org.simpleframework.** { *; }

-keeppackagenames de.tillhub.paymentengine.**

# Contracts
-keep class de.tillhub.paymentengine.contract.** { *; }

# Engine and managers
-keep class de.tillhub.paymentengine.PaymentEngine { *; }
-keep class de.tillhub.paymentengine.PaymentEngine$Companion { *; }
-keep class de.tillhub.paymentengine.CardManager { *; }
-keep class de.tillhub.paymentengine.CardManagerImpl { *; }
-keep class de.tillhub.paymentengine.PaymentManager { *; }
-keep class de.tillhub.paymentengine.ReconciliationManager { *; }
-keep class de.tillhub.paymentengine.RefundManager { *; }
-keep class de.tillhub.paymentengine.ReversalManager { *; }
-keep class de.tillhub.paymentengine.helper.SingletonHolder { *; }

# Data classes
-keep class de.tillhub.paymentengine.data.CardSaleConfig { *; }
-keep class de.tillhub.paymentengine.data.CardSaleConfig$* { *; }
-keep class de.tillhub.paymentengine.data.ISOAlphaCurrency { *; }
-keep class de.tillhub.paymentengine.data.Payment { *; }
-keep class de.tillhub.paymentengine.data.Terminal { *; }
-keep class de.tillhub.paymentengine.data.Terminal$* { *; }
-keep class de.tillhub.paymentengine.data.TerminalOperationStatus { *; }
-keep class de.tillhub.paymentengine.data.TerminalOperationStatus$* { *; }
-keep class de.tillhub.paymentengine.data.TransactionData { *; }
-keep class de.tillhub.paymentengine.data.TransactionResultCode { *; }
-keep class de.tillhub.paymentengine.data.TransactionResultCode$* { *; }

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Breaking changes with AGP 8.0
# R8 upgrade documentation
-dontwarn java.lang.invoke.StringConcatFactory