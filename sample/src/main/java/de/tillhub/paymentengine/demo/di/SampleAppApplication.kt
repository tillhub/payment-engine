package de.tillhub.paymentengine.demo.di

import android.app.Application
import de.tillhub.paymentengine.demo.storage.DeviceRepository
import de.tillhub.paymentengine.demo.storage.DeviceRepositoryImpl
import kotlinx.coroutines.MainScope

class SampleAppApplication : Application() {

    private lateinit var factory: Factory

    val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(
            storage = factory.createDeviceStorage(),
            scope = MainScope()
        )
    }

    override fun onCreate() {
        super.onCreate()
        factory = Factory(this)
        val viewModelFactory = ViewModelFactory(deviceRepository)


    }
}
