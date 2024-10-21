package de.tillhub.paymentengine.opi.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.lifecycle.lifecycleScope
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.helper.TimeoutManager
import de.tillhub.paymentengine.opi.OPIService
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal abstract class OPITerminalActivity : AppCompatActivity() {

    private val opiServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as OPIService.OPIServiceLocalBinder
            opiService = binder.service()

            collectState()
            opiService.setBringToFront(::moveAppToFront)
            opiService.init(config)
        }

        override fun onServiceDisconnected(className: ComponentName) = Unit
    }

    protected lateinit var opiService: OPIService

    private lateinit var timeoutManager: TimeoutManager

    private val activityManager: ActivityManager by lazy {
        applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    protected val config: Terminal.OPI by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, Terminal.OPI::class.java)
        } ?: throw IllegalArgumentException("OPITerminalActivity: Extras is null")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService()

        timeoutManager = TimeoutManager(this) {
            showCancel()
        }

        onBackPressedDispatcher.addCallback(
            owner = this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            }
        )
    }

    override fun onDestroy() {
        unbindService(opiServiceConnection)
        opiService.stopSelf()
        super.onDestroy()
    }

    private fun bindService() {
        val intent = Intent(this, OPIService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        bindService(intent, opiServiceConnection, 0)
    }

    private fun collectState() {
        lifecycleScope.launch {
            opiService.opiOperationState.collect { state ->
                when (state) {
                    OPIService.State.NotInitialized -> Unit
                    OPIService.State.Idle -> {
                        timeoutManager.processStarted()

                        showLoader()
                        opiService.loginToTerminal()
                    }
                    OPIService.State.Pending.NoMessage -> {
                        timeoutManager.processUpdated()
                        showInstructions()
                    }
                    is OPIService.State.Pending.WithMessage -> {
                        timeoutManager.processUpdated()
                        showIntermediateStatus(state.message)
                    }
                    OPIService.State.Pending.Login -> {
                        timeoutManager.processUpdated()
                    }
                    OPIService.State.LoggedIn -> {
                        timeoutManager.processUpdated()
                        startOperation()
                    }
                    is OPIService.State.OperationError -> {
                        timeoutManager.processFinishedWithError()
                        handleErrorState(state)
                    }
                    is OPIService.State.ResultError -> {
                        timeoutManager.processFinishedWithResult()
                        finishWithError(state)
                    }
                    is OPIService.State.ResultSuccess -> {
                        timeoutManager.processFinishedWithResult()
                        finishWithSuccess(state)
                    }
                }
            }
        }
    }

    private fun finishWithSuccess(state: OPIService.State.ResultSuccess) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, state.data) }
        )
        finish()
    }

    private fun finishWithError(state: OPIService.State.ResultError) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, state.data) }
        )
        finish()
    }

    private fun handleErrorState(state: OPIService.State.OperationError) {
        showInstructions()
        showOperationErrorStatus(state.message)
    }

    private fun moveAppToFront() {
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
    }

    abstract fun showLoader()
    abstract fun showInstructions()
    abstract fun showIntermediateStatus(status: String)
    abstract fun showOperationErrorStatus(status: String)
    abstract fun startOperation()
    abstract fun showCancel()
}