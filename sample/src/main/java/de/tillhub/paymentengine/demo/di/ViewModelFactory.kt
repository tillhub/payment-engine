package de.tillhub.paymentengine.demo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tillhub.paymentengine.demo.MainViewModel
import de.tillhub.paymentengine.demo.storage.DeviceRepository

class ViewModelFactory(
    private val deviceRepository: DeviceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(deviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
