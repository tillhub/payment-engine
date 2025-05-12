package de.tillhub.paymentengine.softpay.helpers

import android.view.LayoutInflater
import androidx.core.app.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import io.softpay.sdk.flow.Flow
import io.softpay.sdk.flow.FlowAction
import io.softpay.sdk.flow.FlowFactory
import io.softpay.sdk.flow.FlowModel
import io.softpay.sdk.flow.FlowOptions
import kotlinx.coroutines.launch

internal inline fun <T : ViewBinding> ComponentActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
) = lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }

/**
 * Returns the first active flow from a [FlowFactory], if any.
 */
internal val <
        O : FlowOptions,
        M : FlowModel,
        A : FlowAction,
        F : Flow<M, A>
    > FlowFactory<O, M, A, F>.activeFlow: F?
    get() = filter(active = true) { true }.firstOrNull()

internal inline fun <T> kotlinx.coroutines.flow.Flow<T>.collectWithOwner(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            collect { action(it) }
        }
    }
}