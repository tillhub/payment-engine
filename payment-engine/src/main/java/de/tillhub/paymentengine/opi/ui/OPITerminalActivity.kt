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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.opi.OPIChannelControllerImpl

internal abstract class OPITerminalActivity : AppCompatActivity() {

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as OPIChannelControllerImpl.OPIChannelControllerLocalBinder
            val opiController = binder.service()

            opiController.setBringToFront(::moveAppToFront)
            viewModel.setController(opiController)
            viewModel.init(config)
        }

        override fun onServiceDisconnected(arg0: ComponentName) = Unit
    }

    internal val viewModel by viewModels<OPITerminalViewModel>()

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

        viewModel.opiOperationState.observe(this) { state ->
            when (state) {
                OPITerminalViewModel.State.Waiting -> Unit
                OPITerminalViewModel.State.Idle -> {
                    showLoader()
                    viewModel.loginToTerminal()
                }
                OPITerminalViewModel.State.Pending.NoMessage -> showInstructions()
                is OPITerminalViewModel.State.Pending.WithMessage -> {
                    showIntermediateStatus(state.message)
                }
                OPITerminalViewModel.State.Pending.Login -> Unit
                OPITerminalViewModel.State.LoggedIn -> startOperation()
                is OPITerminalViewModel.State.OperationError -> handleErrorState(state)
                is OPITerminalViewModel.State.ResultError -> finishWithError(state)
                is OPITerminalViewModel.State.ResultSuccess -> finishWithSuccess(state)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, OPIChannelControllerImpl::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        bindService(intent, connection, 0)
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }

    private fun finishWithSuccess(state: OPITerminalViewModel.State.ResultSuccess) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, state.data) }
        )
        finish()
    }

    private fun finishWithError(state: OPITerminalViewModel.State.ResultError) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, state.data) }
        )
        finish()
    }

    private fun handleErrorState(state: OPITerminalViewModel.State.OperationError) {
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
}