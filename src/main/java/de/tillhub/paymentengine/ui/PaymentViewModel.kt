package de.tillhub.paymentengine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.tillhub.paymentengine.PaymentManager
import de.tillhub.paymentengine.SetupProtocol
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentManager: PaymentManager
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

    fun transportConfiguration(cardPaymentConfig: CardPaymentConfig) =
        paymentManager.getTransportConfiguration(cardPaymentConfig)

    fun saleConfiguration(cardSaleConfig: CardSaleConfig) =
        paymentManager.getSaleConfiguration(cardSaleConfig)

    fun onStatus(status: String) {
        TODO("Not yet implemented")
    }

    fun onCompletion(completion: String) {
        TODO("Not yet implemented")
    }

    fun onReceipt(receipt: String) {
        TODO("Not yet implemented")
    }

    fun onError(error: String) {
        TODO("Not yet implemented")
    }
}

sealed class PaymentState {
    object Idle : PaymentState()
    data class Setup(val protocol: SetupProtocol) : PaymentState()
    object Ready : PaymentState()
    data class Success(val response: LavegoTerminalOperation) : PaymentState()
}