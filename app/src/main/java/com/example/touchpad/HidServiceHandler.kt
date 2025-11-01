package com.example.touchpad

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.example.touchpad.ConnectionState.RegistrationPending
import java.util.concurrent.Executor
import kotlin.concurrent.thread


class HidServiceHandler(
    private val app: Application,
    private val viewModel: ConnectionViewModel,
    private val connectionHandler: ConnectionHandler
)
    : BluetoothProfile.ServiceListener{

    // HID device created by onServiceConnected(..).
    private var hidDevice:BluetoothHidDevice? = null


    // Called by android framework if service registered
    @SuppressLint("MissingPermission")
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        if (profile != BluetoothProfile.HID_DEVICE) {
            Log.wtf("BluetoothProfile", "Profile: $profile should not be here.")
            return
        }

        hidDevice = proxy as BluetoothHidDevice
        viewModel.connectionState.value = ConnectionState.Unregistered(app, hidDevice!!)

        Log.i("BluetoothProfile", "HID profile connected.")
    }

    /** Bluetooth connection **/
    fun onHostSelected(hostDevice: BluetoothDevice, sdpSettings:BluetoothHidDeviceAppSdpSettings){
        if (hidDevice != null){
            viewModel.hidDevice = hidDevice!!
            viewModel.hostDevice = hostDevice

            val state = viewModel.connectionState.value

            viewModel.connectionState.value = when(state){
                is ConnectionState.Unregistered -> {
                    Log.i("HidServiceHandler", "Registering...")
                    state.register(sdpSettings, connectionHandler)
                }
                else -> {
                    Log.e("HidServiceHandler", "Invalid state for registration: $state")
                    state
                }
            }
        }
        else{
            Log.e("HidServiceHandler", "No hid service connected.")
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            throw Exception("HID service disconnected.")
        } else {
            Log.wtf("BluetoothProfile", "Service: $profile disconnected (not HID service).")
        }
    }



}
