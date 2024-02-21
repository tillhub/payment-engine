package de.tillhub.paymentengine.helper

import androidx.lifecycle.Lifecycle

interface ManagerBuilder<T> {
    fun build(lifecycle: Lifecycle): T
}