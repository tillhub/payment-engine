package de.tillhub.paymentengine.testing

import de.tillhub.paymentengine.helper.InstantTaskExecutor
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
abstract class ViewModelFunSpecs(body: FunSpec.() -> Unit = {}) : FunSpec(body) {
    override fun listeners(): List<TestListener> {
        return listOf(ViewModelListener())
    }
}

@ExperimentalCoroutinesApi
class ViewModelListener(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    private val instantTaskExecutor: InstantTaskExecutor = InstantTaskExecutor()
) : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        Dispatchers.setMain(testDispatcher)
        instantTaskExecutor.setupLiveData()
    }

    override suspend fun afterSpec(spec: Spec) {
        Dispatchers.resetMain()
        instantTaskExecutor.resetLiveData()
    }
}