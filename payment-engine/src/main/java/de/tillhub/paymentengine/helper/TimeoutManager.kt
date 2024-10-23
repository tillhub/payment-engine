package de.tillhub.paymentengine.helper

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

internal class TimeoutManager(
    private val owner: LifecycleOwner,
    private val cancelEnabled: () -> Unit
) : DefaultLifecycleObserver {

    private var timerStarted = false
    private var timerJob: Job? = null

    init {
        owner.lifecycle.addObserver(this)
    }

    fun processStarted() {
        timerStarted = true
        timerJob = createTimerJob(TIMEOUT_DURATION)
    }

    fun processUpdated() {
        if (timerStarted) {
            timerJob?.cancel()
            timerJob = createTimerJob(TIMEOUT_DURATION)
        }
    }

    fun processFinishedWithResult() {
        timerJob?.cancel()
        timerStarted = false
    }

    fun processFinishedWithError() {
        if (timerStarted) {
            timerJob?.cancel()
            timerJob = createTimerJob(ERROR_DURATION)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        timerJob?.cancel()
        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (timerStarted) {
            timerJob?.cancel()
            timerJob = createTimerJob(TIMEOUT_DURATION)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        timerJob?.cancel()

        timerStarted = false
        owner.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    private fun createTimerJob(timerDelay: Long) = owner.lifecycleScope.launch {
        delay(timerDelay)

        if (isActive) {
            cancelEnabled()
            timerJob = null
            timerStarted = false
        }
    }

    companion object {
        private val TIMEOUT_DURATION = TimeUnit.SECONDS.toMillis(30)
        private val ERROR_DURATION = TimeUnit.SECONDS.toMillis(5)
    }
}