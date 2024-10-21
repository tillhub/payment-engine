package de.tillhub.paymentengine.helper

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

internal class TimeoutManager(
    private val owner: LifecycleOwner,
    private val timerFactory: TimerFactory = TimerFactory(),
    private val cancelEnabled: () -> Unit
) : DefaultLifecycleObserver {

    private var timerStarted = false
    private var timeoutTimer: Timer? = null
    private var errorTimer: Timer? = null
    private var currentTimerTask: TimerTask? = null

    init {
        owner.lifecycle.addObserver(this)
    }

    fun processStarted() {
        timerStarted = true
        timeoutTimer = timerFactory.createTimer()
        timeoutTimer?.schedule(createTimerTask(), TIMEOUT_DURATION)
    }

    fun processUpdated() {
        if (timerStarted) {
            timeoutTimer?.cancel()
            currentTimerTask?.cancel()

            timeoutTimer = timerFactory.createTimer()
            timeoutTimer?.schedule(createTimerTask(), TIMEOUT_DURATION)
        }
    }

    fun processFinishedWithResult() {
        timeoutTimer?.cancel()
        timerStarted = false
    }

    fun processFinishedWithError() {
        if (timerStarted) {
            timeoutTimer?.cancel()
            currentTimerTask?.cancel()

            errorTimer = timerFactory.createTimer()
            errorTimer?.schedule(createTimerTask(), ERROR_DURATION)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (timerStarted) {
            timeoutTimer?.cancel()
            currentTimerTask?.cancel()

            timeoutTimer = timerFactory.createTimer()
            timeoutTimer?.schedule(createTimerTask(), TIMEOUT_DURATION)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        timeoutTimer?.cancel()
        currentTimerTask?.cancel()
        super.onPause(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)

        timeoutTimer?.cancel()
        errorTimer?.cancel()
        currentTimerTask?.cancel()

        timeoutTimer = null
        errorTimer = null
        currentTimerTask = null

        timerStarted = false

        super.onDestroy(owner)
    }

    private fun createTimerTask(): TimerTask {
        val task = object : TimerTask() {
            override fun run() {
                timerStarted = false
                owner.lifecycleScope.launch {
                    cancelEnabled()
                }
            }
        }

        currentTimerTask = task
        return task
    }

    companion object {
        private val TIMEOUT_DURATION = TimeUnit.SECONDS.toMillis(30)
        private val ERROR_DURATION = TimeUnit.SECONDS.toMillis(5)
    }
}

internal class TimerFactory {
    fun createTimer(): Timer = Timer()
}