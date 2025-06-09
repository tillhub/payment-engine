package de.tillhub.paymentengine.data

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest

interface Terminal : Parcelable {
    val id: String
    val saleConfig: CardSaleConfig
    val contract: TerminalContract
}

interface TerminalContract {
    fun connectIntent(context: Context, terminal: Terminal): Intent
    fun paymentIntent(context: Context, input: PaymentRequest): Intent
    fun refundIntent(context: Context, input: RefundRequest): Intent
    fun reversalIntent(context: Context, input: ReversalRequest): Intent
    fun reconciliationIntent(context: Context, terminal: Terminal): Intent
    fun ticketReprintIntent(context: Context, terminal: Terminal): Intent
    fun recoveryIntent(context: Context, terminal: Terminal): Intent
    fun disconnectIntent(context: Context, terminal: Terminal): Intent
}