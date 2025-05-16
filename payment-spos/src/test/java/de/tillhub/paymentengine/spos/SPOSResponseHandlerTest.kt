package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import de.tillhub.paymentengine.R
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.spos.data.ReceiptDto
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.StringToReceiptDtoConverter
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

class SPOSResponseHandlerTest : DescribeSpec({

    lateinit var intent: Intent
    lateinit var receiptDto: ReceiptDto
    lateinit var receiptConverter: StringToReceiptDtoConverter

    beforeTest {
        receiptDto = mockk {
            every { toReceiptString() } returns "RECEIPT"
        }

        intent = mockk {
            every { extras } returns null
        }

        receiptConverter = mockk {
            every { convert(any()) } returns receiptDto
        }
    }

    describe("handleTerminalConnectResponse") {
        it("success") {
            val result = SPOSResponseHandler.handleTerminalConnectResponse(
                Activity.RESULT_OK,
                intent
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Login.Connected>()
        }

        it("error") {
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.ERROR)
            } returns "CARD_PAYMENT_NOT_ONBOARDED"
            every {
                intent.extras?.keySet()
            } returns setOf(SPOSKey.ResultExtra.ERROR)

            val result = SPOSResponseHandler.handleTerminalConnectResponse(
                Activity.RESULT_CANCELED,
                intent
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Login.Error>()
            result.resultCode.shouldBeInstanceOf<TransactionResultCode.Known>()
            result.resultCode.errorMessage shouldBe R.string.spos_error_terminal_not_onboarded
        }

        it("canceled") {
            val result = SPOSResponseHandler.handleTerminalConnectResponse(
                Activity.RESULT_CANCELED,
                intent
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Login.Canceled>()
        }
    }

    describe("handleTerminalDisconnectResponse") {
        it("success") {
            val result = SPOSResponseHandler.handleTerminalDisconnectResponse(
                Activity.RESULT_OK
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Login.Disconnected>()
        }

        it("canceled") {
            val result = SPOSResponseHandler.handleTerminalDisconnectResponse(
                Activity.RESULT_CANCELED
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Login.Canceled>()
        }
    }

    describe("handleTransactionResponse") {
        it("canceled") {
            val result = SPOSResponseHandler.handleTransactionResult(
                Activity.RESULT_CANCELED,
                intent,
                TerminalOperationStatus.Payment::class
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Payment.Canceled>()
        }

        describe("error") {
            it("not onboarded") {
                every {
                    intent.extras?.getString(any())
                } returns null
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.ERROR)
                } returns "CARD_PAYMENT_NOT_ONBOARDED"

                every {
                    intent.extras?.keySet()
                } returns setOf(SPOSKey.ResultExtra.ERROR)

                val result = SPOSResponseHandler.handleTransactionResult(
                    Activity.RESULT_CANCELED,
                    intent,
                    TerminalOperationStatus.Payment::class
                )

                result.shouldBeInstanceOf<TerminalOperationStatus.Payment.Error>()
                result.response.resultCode.shouldBeInstanceOf<TransactionResultCode.Known>()
                result.response.resultCode.errorMessage shouldBe R.string.spos_error_terminal_not_onboarded
            }

            it("no extras") {
                every {
                    intent.extras?.keySet()
                } returns setOf()

                every { intent.extras?.getString(any()) } returns null

                val result = SPOSResponseHandler.handleTransactionResult(
                    Activity.RESULT_OK,
                    intent,
                    TerminalOperationStatus.Payment::class
                )

                result.shouldBeInstanceOf<TerminalOperationStatus.Payment.Error>()
                result.response.resultCode.shouldBeInstanceOf<TransactionResultCode.Unknown>()
                result.response.resultCode.errorMessage shouldBe R.string.zvt_error_code_unknown
            }

            it("tx error") {
                every {
                    intent.extras?.keySet()
                } returns setOf(
                    SPOSKey.ResultExtra.RECEIPT_MERCHANT,
                    SPOSKey.ResultExtra.RECEIPT_CUSTOMER,
                    SPOSKey.ResultExtra.RESULT_STATE,
                    SPOSKey.ResultExtra.TRANSACTION_RESULT,
                    SPOSKey.ResultExtra.TERMINAL_ID,
                    SPOSKey.ResultExtra.TRANSACTION_DATA,
                    SPOSKey.ResultExtra.CARD_CIRCUIT,
                    SPOSKey.ResultExtra.CARD_PAN,
                    SPOSKey.ResultExtra.MERCHANT
                )
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.RECEIPT_MERCHANT)
                } returns "merchant_receipt"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.RECEIPT_CUSTOMER)
                } returns "customer_receipt"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.RESULT_STATE)
                } returns "Failure"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_RESULT)
                } returns "FAILED"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.TERMINAL_ID)
                } returns "terminal_id"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_DATA)
                } returns "transaction_data"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.CARD_CIRCUIT)
                } returns "card_circuit"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.CARD_PAN)
                } returns "card_pan"
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.MERCHANT)
                } returns "merchant"

                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.ERROR)
                } returns null
                every {
                    intent.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)
                } returns "CARD_DETECTION_FAILED"

                val result = SPOSResponseHandler.handleTransactionResult(
                    Activity.RESULT_OK,
                    intent,
                    TerminalOperationStatus.Payment::class,
                    receiptConverter
                )

                result.shouldBeInstanceOf<TerminalOperationStatus.Payment.Error>()
                result.response.resultCode.shouldBeInstanceOf<TransactionResultCode.Known>()
                result.response.resultCode.errorMessage shouldBe R.string.spos_error_card_detection_failed
                result.response.customerReceipt shouldBe "RECEIPT"
                result.response.merchantReceipt shouldBe "RECEIPT"
                result.response.rawData shouldBe "Extras {\n" +
                        "${SPOSKey.ResultExtra.RECEIPT_MERCHANT} = merchant_receipt\n" +
                        "${SPOSKey.ResultExtra.RECEIPT_CUSTOMER} = customer_receipt\n" +
                        "${SPOSKey.ResultExtra.RESULT_STATE} = Failure\n" +
                        "${SPOSKey.ResultExtra.TRANSACTION_RESULT} = FAILED\n" +
                        "${SPOSKey.ResultExtra.TERMINAL_ID} = terminal_id\n" +
                        "${SPOSKey.ResultExtra.TRANSACTION_DATA} = transaction_data\n" +
                        "${SPOSKey.ResultExtra.CARD_CIRCUIT} = card_circuit\n" +
                        "${SPOSKey.ResultExtra.CARD_PAN} = card_pan\n" +
                        "${SPOSKey.ResultExtra.MERCHANT} = merchant\n" +
                        "}"
                result.response.data?.terminalId shouldBe "terminal_id"
                result.response.data?.transactionId shouldBe "transaction_data"
                result.response.data?.cardCircuit shouldBe "card_circuit"
                result.response.data?.cardPan shouldBe "card_pan"
                result.response.data?.paymentProvider shouldBe "merchant"
            }
        }

        it("success reprintRequired = false") {
            every {
                intent.extras?.keySet()
            } returns setOf(
                SPOSKey.ResultExtra.RECEIPT_MERCHANT,
                SPOSKey.ResultExtra.RECEIPT_CUSTOMER,
                SPOSKey.ResultExtra.RESULT_STATE,
                SPOSKey.ResultExtra.TRANSACTION_RESULT,
                SPOSKey.ResultExtra.TERMINAL_ID,
                SPOSKey.ResultExtra.TRANSACTION_DATA,
                SPOSKey.ResultExtra.CARD_CIRCUIT,
                SPOSKey.ResultExtra.CARD_PAN,
                SPOSKey.ResultExtra.MERCHANT
            )
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.RECEIPT_MERCHANT)
            } returns "merchant_receipt"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.RECEIPT_CUSTOMER)
            } returns "customer_receipt"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.RESULT_STATE)
            } returns "Success"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_RESULT)
            } returns "ACCEPTED"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.TERMINAL_ID)
            } returns "terminal_id"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_DATA)
            } returns "transaction_data"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.CARD_CIRCUIT)
            } returns "card_circuit"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.CARD_PAN)
            } returns "card_pan"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.MERCHANT)
            } returns "merchant"

            every {
                intent.extras?.getString(SPOSKey.ResultExtra.ERROR)
            } returns null
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)
            } returns null

            val result = SPOSResponseHandler.handleTransactionResult(
                Activity.RESULT_OK,
                intent,
                TerminalOperationStatus.Payment::class,
                receiptConverter
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Payment.Success>()
            result.response.customerReceipt shouldBe "RECEIPT"
            result.response.merchantReceipt shouldBe "RECEIPT"
            result.response.rawData shouldBe "Extras {\n" +
                    "${SPOSKey.ResultExtra.RECEIPT_MERCHANT} = merchant_receipt\n" +
                    "${SPOSKey.ResultExtra.RECEIPT_CUSTOMER} = customer_receipt\n" +
                    "${SPOSKey.ResultExtra.RESULT_STATE} = Success\n" +
                    "${SPOSKey.ResultExtra.TRANSACTION_RESULT} = ACCEPTED\n" +
                    "${SPOSKey.ResultExtra.TERMINAL_ID} = terminal_id\n" +
                    "${SPOSKey.ResultExtra.TRANSACTION_DATA} = transaction_data\n" +
                    "${SPOSKey.ResultExtra.CARD_CIRCUIT} = card_circuit\n" +
                    "${SPOSKey.ResultExtra.CARD_PAN} = card_pan\n" +
                    "${SPOSKey.ResultExtra.MERCHANT} = merchant\n" +
                    "}"
            result.response.data?.terminalId shouldBe "terminal_id"
            result.response.data?.transactionId shouldBe "transaction_data"
            result.response.data?.cardCircuit shouldBe "card_circuit"
            result.response.data?.cardPan shouldBe "card_pan"
            result.response.data?.paymentProvider shouldBe "merchant"
            result.response.reprintRequired.shouldBeFalse()
        }

        it("success reprintRequired = true") {
            every {
                intent.extras?.keySet()
            } returns setOf(
                SPOSKey.ResultExtra.RECEIPT_MERCHANT,
                SPOSKey.ResultExtra.RECEIPT_CUSTOMER,
                SPOSKey.ResultExtra.RESULT_STATE,
                SPOSKey.ResultExtra.TRANSACTION_RESULT,
                SPOSKey.ResultExtra.TERMINAL_ID,
                SPOSKey.ResultExtra.TRANSACTION_DATA,
                SPOSKey.ResultExtra.CARD_CIRCUIT,
                SPOSKey.ResultExtra.CARD_PAN,
                SPOSKey.ResultExtra.MERCHANT
            )
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.RECEIPT_MERCHANT)
            } returns null
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.RECEIPT_CUSTOMER)
            } returns null
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.RESULT_STATE)
            } returns "Success"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_RESULT)
            } returns "ACCEPTED"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.TERMINAL_ID)
            } returns "terminal_id"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_DATA)
            } returns "transaction_data"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.CARD_CIRCUIT)
            } returns "card_circuit"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.CARD_PAN)
            } returns "card_pan"
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.MERCHANT)
            } returns "merchant"

            every {
                intent.extras?.getString(SPOSKey.ResultExtra.ERROR)
            } returns null
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)
            } returns null

            val result = SPOSResponseHandler.handleTransactionResult(
                Activity.RESULT_OK,
                intent,
                TerminalOperationStatus.Payment::class,
                receiptConverter
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Payment.Success>()
            result.response.customerReceipt shouldBe ""
            result.response.merchantReceipt shouldBe ""
            result.response.rawData shouldBe "Extras {\n" +
                    "${SPOSKey.ResultExtra.RECEIPT_MERCHANT} = null\n" +
                    "${SPOSKey.ResultExtra.RECEIPT_CUSTOMER} = null\n" +
                    "${SPOSKey.ResultExtra.RESULT_STATE} = Success\n" +
                    "${SPOSKey.ResultExtra.TRANSACTION_RESULT} = ACCEPTED\n" +
                    "${SPOSKey.ResultExtra.TERMINAL_ID} = terminal_id\n" +
                    "${SPOSKey.ResultExtra.TRANSACTION_DATA} = transaction_data\n" +
                    "${SPOSKey.ResultExtra.CARD_CIRCUIT} = card_circuit\n" +
                    "${SPOSKey.ResultExtra.CARD_PAN} = card_pan\n" +
                    "${SPOSKey.ResultExtra.MERCHANT} = merchant\n" +
                    "}"
            result.response.data?.terminalId shouldBe "terminal_id"
            result.response.data?.transactionId shouldBe "transaction_data"
            result.response.data?.cardCircuit shouldBe "card_circuit"
            result.response.data?.cardPan shouldBe "card_pan"
            result.response.data?.paymentProvider shouldBe "merchant"
            result.response.reprintRequired.shouldBeTrue()
        }
    }
})
