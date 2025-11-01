package com.example.touchpad

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.touchpad.ConnectionState.Connecting

private const val CALLBACK_TAG = "BluetoothHidDevice.Callback"

class ConnectionHandler(
    private val context: Context,
    private val viewModel: ConnectionViewModel
) : BluetoothHidDevice.Callback() {



    @SuppressLint("MissingPermission")
    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        super.onAppStatusChanged(pluggedDevice, registered)

        // set state as registered and try to connect to host
        if(registered && viewModel.connectionState.value is ConnectionState.RegistrationPending){
            val state = ConnectionState.Registered(context, viewModel.hidDevice)
            viewModel.connectionState.value = state.connect(viewModel.hostDevice)
        }
    }

    override fun onGetReport(
            pluggedDevice: BluetoothDevice?,
            type: Byte,
            id: Byte,
            bufferSize: Int
    ) {
        super.onGetReport(pluggedDevice, type, id, bufferSize)
        Log.i(CALLBACK_TAG, "Get report request: $type, $id, $bufferSize")
    }

    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
        super.onConnectionStateChanged(device, state)

        when (state) {
            BluetoothHidDevice.STATE_CONNECTED -> {
               viewModel.connectionState.value = ConnectionState.Connected(context, device, viewModel.hidDevice)
            }
            BluetoothHidDevice.STATE_CONNECTING -> {
                Log.i(CALLBACK_TAG, "Host confirmed connection process...")
            }
            BluetoothHidDevice.STATE_DISCONNECTED -> {
                viewModel.connectionState.value = ConnectionState.Registered(context, viewModel.hidDevice)
            }
            else -> {
                Log.e(CALLBACK_TAG, "Unknown state: $state")
            }
        }
    }

    override fun onSetReport(
            pluggedDevice: BluetoothDevice?,
            type: Byte,
            id: Byte,
            data: ByteArray?
    ) {
        Log.i(CALLBACK_TAG, "Set report request: $type, $id, $data")
        super.onSetReport(pluggedDevice, type, id, data)
    }

    override fun onSetProtocol(pluggedDevice: BluetoothDevice?, protocol: Byte) {
        Log.i(CALLBACK_TAG, "Set protocol request: $protocol")
        super.onSetProtocol(pluggedDevice, protocol)
    }

    override fun onInterruptData(
            pluggedDevice: BluetoothDevice?,
            reportId: Byte,
            data: ByteArray?
    ) {
        Log.i(CALLBACK_TAG, "Interrupt data: $reportId, $data")
        super.onInterruptData(pluggedDevice, reportId, data)
    }





}
