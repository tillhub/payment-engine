package de.tillhub.paymentengine.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class TimeoutManagerTest : FunSpec({

    lateinit var owner: TestLifecycleOwner
    lateinit var target: TimeoutManager

    lateinit var callback: Callback

    lateinit var testScope: TestScope
    lateinit var dispatcher: TestDispatcher

    beforeTest {
        dispatcher = StandardTestDispatcher()
        testScope = TestScope(dispatcher)

        Dispatchers.setMain(dispatcher)

        callback = mockk {
            every { cancelEnabled() } just Runs
        }
        owner = spyk(
            TestLifecycleOwner(
                coroutineDispatcher = dispatcher,
                initialState = Lifecycle.State.RESUMED
            )
        )

        target = TimeoutManager(owner, callback::cancelEnabled)
    }

    test("init") {
        owner.observerCount shouldBe 1
    }

    test("processStarted") {
        testScope.runTest {
            target.processStarted()

            advanceTimeBy(15_100)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            advanceTimeBy(15_101)

            verify {
                callback.cancelEnabled()
            }
        }
    }

    test("processUpdated") {
        testScope.runTest {
            target.processStarted()

            advanceTimeBy(15_000)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            target.processUpdated()

            advanceTimeBy(15_001)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            advanceTimeBy(15_000)

            verify {
                callback.cancelEnabled()
            }
        }
    }

    test("processFinishedWithResult") {
        testScope.runTest {
            target.processStarted()

            advanceTimeBy(15_000)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            target.processFinishedWithResult()

            advanceTimeBy(15_001)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            advanceTimeBy(15_000)

            verify(inverse = true) {
                callback.cancelEnabled()
            }
        }
    }

    test("processFinishedWithError") {
        testScope.runTest {
            target.processStarted()

            advanceTimeBy(15_000)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            target.processFinishedWithError()

            advanceTimeBy(2_501)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            advanceTimeBy(2_500)

            verify {
                callback.cancelEnabled()
            }
        }
    }

    test("lifecycle resume + pause") {
        testScope.runTest {
            target.processStarted()

            advanceTimeBy(29_000)

            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)

            advanceTimeBy(1_001)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            advanceTimeBy(30_001)

            verify {
                callback.cancelEnabled()
            }
        }
    }

    test("lifecycle destroy") {
        testScope.runTest {
            target.processStarted()

            advanceTimeBy(29_000)

            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

            advanceTimeBy(1_001)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            owner.observerCount shouldBe 0
        }
    }
}) {
    interface Callback {
        fun cancelEnabled()
    }
}
