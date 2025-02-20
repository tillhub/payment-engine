package de.tillhub.paymentengine.demo.di

import android.app.Application
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import de.tillhub.paymentengine.demo.storage.DeviceStorage
import de.tillhub.paymentengine.demo.storage.DeviceStorageImpl
import de.tillhub.paymentengine.demo.storage.TerminalDataSerializer
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

class Factory (private val app: Application) {
    fun createDeviceStorage(): DeviceStorage = commonCreateDeviceStorage(
    terminalPath = { app.filesDir.resolve("terminal.json").absolutePath },
    )
}

// Local storage
internal fun <T>createDataStore(
    serializer: OkioSerializer<T>,
    produceFilePath: () -> String,
    migrations: List<DataMigration<T>> = listOf(),
): DataStore<T> =
    DataStoreFactory.create(
        migrations = migrations,
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = {
                produceFilePath().toPath()
            },
        ),
    )
internal fun commonCreateDeviceStorage(
    terminalPath: () -> String,
): DeviceStorage = DeviceStorageImpl(
        terminalDataDs = createDataStore(
            serializer = TerminalDataSerializer,
            produceFilePath = terminalPath,
    ),
)
val json = Json
