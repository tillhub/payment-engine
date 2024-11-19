package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import de.tillhub.paymentengine.R
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.spos.data.ReceiptDto
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.StringToReceiptDtoConverter
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
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

            result.shouldBeInstanceOf<TerminalOperationStatus.Success.SPOS>()
        }

        it("error") {
            every {
                intent.extras?.getString(SPOSKey.ResultExtra.ERROR)
            } returns "CARD_PAYMENT_NOT_ONBOARDED"

            val result = SPOSResponseHandler.handleTerminalConnectResponse(
                Activity.RESULT_CANCELED,
                intent
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
            result.resultCode.shouldBeInstanceOf<TransactionResultCode.Known>()
            result.resultCode.errorMessage shouldBe R.string.spos_error_terminal_not_onboarded
        }

        it("canceled") {
            val result = SPOSResponseHandler.handleTerminalConnectResponse(
                Activity.RESULT_CANCELED,
                intent
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Canceled>()
        }
    }

    describe("handleTerminalDisconnectResponse") {
        it("success") {
            val result = SPOSResponseHandler.handleTerminalDisconnectResponse(
                Activity.RESULT_OK
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Success.SPOS>()
        }

        it("canceled") {
            val result = SPOSResponseHandler.handleTerminalDisconnectResponse(
                Activity.RESULT_CANCELED
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Canceled>()
        }
    }

    describe("handleTransactionResponse") {
        it("canceled") {
            val result = SPOSResponseHandler.handleTransactionResponse(
                Activity.RESULT_CANCELED,
                intent
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Canceled>()
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

                val result = SPOSResponseHandler.handleTransactionResponse(
                    Activity.RESULT_CANCELED,
                    intent
                )

                result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
                result.resultCode.shouldBeInstanceOf<TransactionResultCode.Known>()
                result.resultCode.errorMessage shouldBe R.string.spos_error_terminal_not_onboarded
            }

            it("no extras") {
                val result = SPOSResponseHandler.handleTransactionResponse(
                    Activity.RESULT_OK,
                    intent
                )

                result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
                result.resultCode.shouldBeInstanceOf<TransactionResultCode.Unknown>()
                result.resultCode.errorMessage shouldBe R.string.zvt_error_code_unknown
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

                val result = SPOSResponseHandler.handleTransactionResponse(
                    Activity.RESULT_OK,
                    intent,
                    receiptConverter
                )

                result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
                result.resultCode.shouldBeInstanceOf<TransactionResultCode.Known>()
                result.resultCode.errorMessage shouldBe R.string.spos_error_failure
                result.customerReceipt shouldBe "RECEIPT"
                result.merchantReceipt shouldBe "RECEIPT"
                result.rawData shouldBe "Extras {\n" +
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
                result.data?.terminalId shouldBe "terminal_id"
                result.data?.transactionId shouldBe "transaction_data"
                result.data?.cardCircuit shouldBe "card_circuit"
                result.data?.cardPan shouldBe "card_pan"
                result.data?.paymentProvider shouldBe "merchant"
            }
        }

        it("success") {
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

            val result = SPOSResponseHandler.handleTransactionResponse(
                Activity.RESULT_OK,
                intent,
                receiptConverter
            )

            result.shouldBeInstanceOf<TerminalOperationStatus.Success.SPOS>()
            result.customerReceipt shouldBe "RECEIPT"
            result.merchantReceipt shouldBe "RECEIPT"
            result.rawData shouldBe "Extras {\n" +
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
            result.data?.terminalId shouldBe "terminal_id"
            result.data?.transactionId shouldBe "transaction_data"
            result.data?.cardCircuit shouldBe "card_circuit"
            result.data?.cardPan shouldBe "card_pan"
            result.data?.paymentProvider shouldBe "merchant"
        }
    }
})
