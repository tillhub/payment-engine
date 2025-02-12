package de.tillhub.paymentengine.demo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private const val PREFERENCES_DATASTORE = "PREFERENCES_DATASTORE"

class PreferencesDataStore (context: Context) {
    private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(PREFERENCES_DATASTORE)
    private val preferences = context.dataStore

    companion object {
        var IP_ADDRESS = stringPreferencesKey("IP_ADDRESS")
        var PORT1 = stringPreferencesKey("PORT1")
        var PORT2 = stringPreferencesKey("PORT2")
    }

    suspend fun setIPAddress(ipAddress: String){
        preferences.edit {
            it[IP_ADDRESS] = ipAddress
        }
    }

    fun getIPAddress() = preferences.data.map {
            it[IP_ADDRESS]?:""
    }

    suspend fun setPort1(port1: String){
        preferences.edit {
            it[PORT1] = port1
        }
    }

    fun getPort1() = preferences.data.map {
        it[PORT1]?:""
    }

    suspend fun setPort2(port2: String){
        preferences.edit {
            it[PORT2] = port2
        }
    }

    fun getPort2() = preferences.data.map {
        it[PORT2]?:""
    }
}