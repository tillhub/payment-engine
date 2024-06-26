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

-keep class de.lavego.** {
    *;
}

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

# Communication classes
-keep class de.tillhub.paymentengine.opi.data.CardServiceRequest { *; }
-keep class de.tillhub.paymentengine.opi.data.PosData { *; }
-keep class de.tillhub.paymentengine.opi.data.TotalAmount { *; }
-keep class de.tillhub.paymentengine.opi.data.OriginalTransaction { *; }
-keep class de.tillhub.paymentengine.opi.data.ServiceRequestType { *; }
-keep class de.tillhub.paymentengine.opi.data.CardServiceResponse { *; }
-keep class de.tillhub.paymentengine.opi.data.Terminal { *; }
-keep class de.tillhub.paymentengine.opi.data.Tender { *; }
-keep class de.tillhub.paymentengine.opi.data.CardDetails { *; }
-keep class de.tillhub.paymentengine.opi.data.CardValue { *; }
-keep class de.tillhub.paymentengine.opi.data.PrivateData { *; }
-keep class de.tillhub.paymentengine.opi.data.Authorisation { *; }
-keep class de.tillhub.paymentengine.opi.data.ValueElement { *; }
-keep class de.tillhub.paymentengine.opi.data.OverallResult { *; }
-keep class de.tillhub.paymentengine.opi.data.DeviceRequest { *; }
-keep class de.tillhub.paymentengine.opi.data.Output { *; }
-keep class de.tillhub.paymentengine.opi.data.TextLine { *; }
-keep class de.tillhub.paymentengine.opi.data.DeviceType { *; }
-keep class de.tillhub.paymentengine.opi.data.DeviceRequestType { *; }
-keep class de.tillhub.paymentengine.opi.data.DeviceResponse { *; }
-keep class de.tillhub.paymentengine.opi.data.ServiceRequest { *; }
-keep class de.tillhub.paymentengine.opi.data.ServiceResponse { *; }
-keep class de.tillhub.paymentengine.opi.data.Reconciliation { *; }

-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }
-keepattributes *Annotation*
-keepattributes Signature

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Breaking changes with AGP 8.0
# R8 upgrade documentation
-dontwarn java.lang.invoke.StringConcatFactory