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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.util.Timer
import java.util.TimerTask

@ExperimentalCoroutinesApi
class TimeoutManagerTest : FunSpec({

    lateinit var owner: TestLifecycleOwner
    lateinit var timerFactory: TimerFactory
    lateinit var target: TimeoutManager

    lateinit var callback: Callback
    lateinit var timer: Timer

    lateinit var testScope: TestScope
    lateinit var dispatcher: TestDispatcher

    var currentTask: TimerTask?
    var currentTaskJob: Job? = null

    beforeTest {
        dispatcher = StandardTestDispatcher()
        testScope = TestScope(dispatcher)

        Dispatchers.setMain(dispatcher)

        callback = mockk {
            every { cancelEnabled() } just Runs
        }
        timer = mockk {
            every { schedule(any<TimerTask>(), any<Long>()) } answers {
                currentTask = firstArg<TimerTask>()
                val taskDelay = secondArg<Long>()

                currentTaskJob = testScope.launch {
                    delay(taskDelay)
                    currentTask?.run()
                }
            }

            every { cancel() } answers {
                currentTaskJob?.cancel()
            }
        }

        owner = spyk(
            TestLifecycleOwner(
                coroutineDispatcher = dispatcher,
                initialState = Lifecycle.State.RESUMED
            )
        )
        timerFactory = mockk {
            every { createTimer() } returns timer
        }

        target = TimeoutManager(owner, timerFactory, callback::cancelEnabled)
    }

    afterTest {
        currentTask = null
        currentTaskJob = null
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
            verify {
                timerFactory.createTimer()
                timer.schedule(any(), 30_000)
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

            verify {
                timer.cancel()
                timerFactory.createTimer()
                timer.schedule(any(), 30_000)
            }

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

            verify {
                timer.cancel()
            }

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

            verify {
                timer.cancel()
                timerFactory.createTimer()
                timer.schedule(any(), 5_000)
            }

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

            owner.setCurrentState(Lifecycle.State.STARTED)

            verify {
                timer.cancel()
            }

            advanceTimeBy(1_001)

            verify(inverse = true) {
                callback.cancelEnabled()
            }

            owner.setCurrentState(Lifecycle.State.RESUMED)

            verify {
                timer.cancel()
                timerFactory.createTimer()
                timer.schedule(any(), 30_000)
            }

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

            owner.setCurrentState(Lifecycle.State.DESTROYED)

            verify {
                timer.cancel()
            }

            advanceTimeBy(1_001)

            verify(inverse = true) {
                callback.cancelEnabled()
            }
        }
    }
}) {
    interface Callback {
        fun cancelEnabled()
    }
}
