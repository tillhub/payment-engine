package de.tillhub.paymentengine.opi.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.contract.ExtraKeys
import de.tillhub.paymentengine.data.Terminal

abstract class OPITerminalActivity : AppCompatActivity() {

    protected val viewModel by viewModels<OPITerminalViewModel>()

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
                OPITerminalViewModel.State.Idle -> {
                    showLoader()
                    startOperation()
                }
                OPITerminalViewModel.State.Pending.NoMessage -> showInstructions()
                is OPITerminalViewModel.State.Pending.WithMessage -> {
                    showIntermediateStatus(state.message)
                }
                is OPITerminalViewModel.State.OperationError -> showOperationErrorStatus(
                    state.message
                )
                is OPITerminalViewModel.State.ResultError -> finishWithError(state)
                is OPITerminalViewModel.State.ResultSuccess -> finishWithSuccess(state)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.init(config)
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }

    private fun finishWithSuccess(state: OPITerminalViewModel.State.ResultSuccess) {
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(ExtraKeys.EXTRAS_RESULT, state.data.toTerminalOperation())
        })
        finish()
    }

    private fun finishWithError(state: OPITerminalViewModel.State.ResultError) {
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(ExtraKeys.EXTRAS_RESULT, state.data.toTerminalOperation())
        })
        finish()
    }

    abstract fun showLoader()
    abstract fun showInstructions()
    abstract fun showIntermediateStatus(status: String)
    abstract fun showOperationErrorStatus(status: String)
    abstract fun startOperation()
}