package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal

interface OPIChannelController {
    fun init()

    suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        terminal: Terminal
    ): Flow<TerminalOperationStatus>

    // TODO implement other methods
}

class OPIChannelControllerImpl(

) : OPIChannelController {

    override fun init() {
        TODO("Not yet implemented")
    }

    override suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        terminal: Terminal
    ): Flow<TerminalOperationStatus> = flow {
        TODO("Not yet implemented")
    }
}