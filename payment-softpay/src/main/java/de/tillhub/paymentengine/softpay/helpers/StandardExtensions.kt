package de.tillhub.paymentengine.softpay.helpers

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.softpay.sdk.flow.Flow
import io.softpay.sdk.flow.FlowAction
import io.softpay.sdk.flow.FlowFactory
import io.softpay.sdk.flow.FlowModel
import io.softpay.sdk.flow.FlowOptions

internal inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
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