package de.tillhub.paymentengine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lavego.sdk.Payment
import de.tillhub.paymentengine.TerminalManager
import de.tillhub.paymentengine.SetupProtocol
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentManager: TerminalManager
) : ViewModel() {

    private val _transactionState: MutableStateFlow<PaymentState> =
        MutableStateFlow(PaymentState.Idle)
    val transactionState: StateFlow<PaymentState> = _transactionState.stateIn(
        scope = viewModelScope,
        started = Eagerly,
        initialValue = PaymentState.Idle
    )

    fun init(cardPaymentConfig: CardPaymentConfig, cardSaleConfig: CardSaleConfig) {
        if (transactionState.value == PaymentState.Idle) {
            _transactionState.value = PaymentState.Setup(
                paymentManager.getSetupProtocol(cardPaymentConfig, cardSaleConfig)
            )
        }
    }

    fun doPayment(amountToPay: String) {
        _transactionState.value = PaymentState.DoPayment(
            Payment().apply {
                amount = BigDecimal(amountToPay)
            }
        )
    }

    fun transportConfiguration(cardPaymentConfig: CardPaymentConfig) =
        paymentManager.getTransportConfiguration(cardPaymentConfig)

    fun saleConfiguration(cardSaleConfig: CardSaleConfig) =
        paymentManager.getSaleConfiguration(cardSaleConfig)

    fun onStatus(status: String) {
        paymentManager.onStatus(status)
    }

    fun onCompletion(completion: String) {
        if (transactionState.value is PaymentState.Setup) {
            _transactionState.value = PaymentState.Ready
        } else {
            viewModelScope.launch {
                _transactionState.value = PaymentState.Outcome(
                    paymentManager.onCompletion(completion)
                )
            }
        }
    }

    fun onReceipt(receipt: String) {
        paymentManager.onReceipt(receipt)
    }

    fun onError(error: String) {
        viewModelScope.launch {
            _transactionState.value = PaymentState.Outcome(
                paymentManager.onError(error)
            )
        }
    }
}

sealed class PaymentState {
    object Idle : PaymentState()
    data class Setup(val protocol: SetupProtocol) : PaymentState()
    object Ready : PaymentState()
    data class DoPayment(val payment: Payment) : PaymentState()
    data class Outcome(val response: LavegoTerminalOperation) : PaymentState()
}