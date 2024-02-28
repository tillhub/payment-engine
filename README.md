
[![](https://jitpack.io/v/tillhub/payment-engine.svg)](https://jitpack.io/#tillhub/payment-engine)
[![API](https://img.shields.io/badge/API-24%2B-green.svg?style=flat)](https://android-arsenal.com/api?level-11) 
# Payment Engine

Android library which combines different payments protocols into single interface solution. So far supported payments are:
* **ZVT** 
# How to setup

**Step 1.** Add the JitPack repository to your `settings.gradle` file:

```groovy
dependencyResolutionManagement {
    repositories {
        ...
        mavenCentral()
		maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency to your app `build.gradle`:
```groovy
dependencies {
    implementation 'com.github.tillhub:payment-engine:1.3.3'
}
```
# Usage

There are two ways you can use payment SDK: 
* First one would be manually registering activity result contract (callback) 
* Second one is via helper class `PaymentEngine.kt`

### 1. Register a callback for an activity result

In UI component like Activity or Fragment first register one of Payment contracts:
* `PaymentContract` - use for new card payment
* `PaymentRefundContract` - use for refunds
* `PaymentReversalContract` - use for payment reversal
* `TerminalReconciliationContract` - use for terminal reconciliation

```kotlin
val getPayment = registerForActivityResult(PaymentContract()) {
    // Handle the returned payment response
}

override fun onCreate(savedInstanceState: Bundle?) {
    // ...

    val paymentButton = findViewById<Button>(R.id.payment_button)

    paymentButton.setOnClickListener {
        // Pass in PaymentRequest as the input
        getPayment.launch(PaymentRequest(...))
    }
}
```

### 2. Usage of PaymentEngine

`PaymentEngine.kt` is per Activity singlton helper class which tries to simplify the process of implementation and it gives you access to all library available actions (managers).

* `PaymentManager` is used to start of a card payment transaction
* `RefundManager` is used to start of a partial card payment refund
* `ReversalManager` is used to start of a card payment reversal
* `ReconciliationManager` is used to start of a terminal reconciliation

It also gives you more flexibility setting up terminal configuration, observing result through SharedFlow and triggering actions with multiple methods. 

```kotlin
private val paymentEngine: PaymentEngine by lazy {
    PaymentEngine.getInstance(this)
}

override fun onCreate(savedInstanceState: Bundle?) {
    // ...

    val paymentManager = paymentEngine.newPaymentManager().build(lifecycle)

    ...

    paymentButton.setOnClickListener {
        // Pass in PaymentRequest as the input
        paymentManager.startPaymentTransaction(...)
    }

    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            paymentManager.observePaymentState()
                .collect {
                    // Handle payment response
                }
        }
    }
}
```

Here's example of `PaymentManager` interface and what provides:

```kotlin
interface PaymentManager {
    fun putTerminalConfig(config: Terminal)
    fun observePaymentState(): SharedFlow<TerminalOperationStatus>
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, configName: String)
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, config: Terminal)
}
```
In case you do not provide terminal configuration default one will be used (ZVT, ip:*127.0.0.1*, port:*40007*)
## Authors

- [Đorđe Hrnjez](https://github.com/djordjeh)
- [Martin Širok](https://github.com/SloInfinity)
- [Chandrashekar Allam](https://github.com/shekar-allam)

