package com.example.touchpad

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionViewModel(private val app: Application) : AndroidViewModel(app) {

    lateinit var hidDevice: BluetoothHidDevice
    lateinit var hostDevice: BluetoothDevice

    val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.NoHidService)

    fun sendReport(report: ByteArray){
        connectionState.value = when(connectionState.value){
            is ConnectionState.Connected -> (connectionState.value as ConnectionState.Connected).sendReport(report)
            else -> {
                Log.e("ConnectionViewModel", "Invalid state for sending report: $connectionState")
                connectionState.value
            }
        }
    }
}