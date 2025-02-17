package de.tillhub.paymentengine.spos.data

internal object SPOSKey {
    object Action {
        const val CONNECT_ACTION = "de.spayment.akzeptanz.S_SWITCH_CONNECT"
        const val DISCONNECT_ACTION = "de.spayment.akzeptanz.S_SWITCH_DISCONNECT"
        const val TRANSACTION_ACTION = "de.spayment.akzeptanz.TRANSACTION"
        const val RECONCILIATION_ACTION = "de.spayment.akzeptanz.RECONCILIATION"
        const val RECOVERY_ACTION = "de.spayment.akzeptanz.REPEAT_LAST_MESSAGE"
    }

    object Extra {
        const val APP_ID = "APP_ID"
        const val TRANSACTION_TYPE = "de.spayment.akzeptanz.TransactionType"
        const val CURRENCY_ISO = "de.spayment.akzeptanz.CurrencyISO"
        const val AMOUNT = "de.spayment.akzeptanz.Amount"
        const val TIP_AMOUNT = "de.spayment.akzeptanz.TipAmount"
        const val TAX_AMOUNT = "de.spayment.akzeptanz.TaxAmount"
        const val TRANSACTION_ID = "de.spayment.akzeptanz.TransactionId"
        const val TRANSACTION_DATA = "de.spayment.akzeptanz.TransactionData"
        const val LANGUAGE_CODE = "de.spayment.akzeptanz.LanguageCode"
    }

    object ResultExtra {
        const val ERROR = "ERROR"
        const val TRANSACTION_RESULT = "de.spayment.akzeptanz.TransactionResult"
        const val TRANSACTION_TYPE = "de.spayment.akzeptanz.TransactionType"
        const val RESULT_STATE = "de.spayment.akzeptanz.ResultState"
        const val AMOUNT = "de.spayment.akzeptanz.Amount"
        const val TIP_AMOUNT = "de.spayment.akzeptanz.TipAmount"
        const val TAX_AMOUNT = "de.spayment.akzeptanz.TaxAmount"
        const val TRANSACTION_DATA = "de.spayment.akzeptanz.TransactionData"
        const val CARD_CIRCUIT = "de.spayment.akzeptanz.CardCircuit"
        const val CARD_PAN = "de.spayment.akzeptanz.CardPAN"
        const val MERCHANT = "de.spayment.akzeptanz.Merchant"
        const val TERMINAL_ID = "de.spayment.akzeptanz.TerminalID"
        const val RECEIPT_MERCHANT = "de.spayment.akzeptanz.MerchantReceipt"
        const val RECEIPT_CUSTOMER = "de.spayment.akzeptanz.CustomerReceipt"
        const val ERROR_MESSAGE = "de.spayment.akzeptanz.ErrorMessage"
        const val RECONCILIATION_DATA = "de.spayment.akzeptanz.ReconciliationData"
    }
}