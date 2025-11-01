package com.example.touchpad

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.concurrent.Executor
import kotlin.concurrent.thread

sealed class ConnectionState {

    object NoHidService: ConnectionState()

    object HidServiceConnected: ConnectionState()
    data class Unregistered(val context:Context, val hidDevice: BluetoothHidDevice): ConnectionState() {
        fun register(
            sdpSettings: BluetoothHidDeviceAppSdpSettings,
            callbackHandler: BluetoothHidDevice.Callback,
        ): ConnectionState {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(
                    "BluetoothAdapter",
                    "Permission BLUETOOTH_CONNECT was revoked after first start."
                )
                throw Exception("Permission BLUETOOTH_CONNECT was revoked after first start.")
            }

            if (hidDevice.unregisterApp()) {
                Log.i("HidServiceHandler", "An old hid service was unregistered.")
            }

            val executor = Executor { runnable ->
                thread {
                    Looper.prepare() // Needed for Toasts on this thread
                    runnable.run()
                }
            }

            if (!hidDevice.registerApp(sdpSettings, null, null, executor, callbackHandler)) {
                Log.e(
                    "InitialState",
                    "Device: ${sdpSettings.name} could not register app."
                )
                throw HidProfileException("Device: ${sdpSettings.name} could not register.")
            }

            return RegistrationPending
        }
    }
    object RegistrationPending : ConnectionState()

    data class Registered(val context: Context, val hidDevice: BluetoothHidDevice):ConnectionState(){
        fun connect(hostDevice: BluetoothDevice) : ConnectionState{

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(
                    "BluetoothAdapter",
                    "Permission BLUETOOTH_CONNECT was revoked after first start."
                )
                throw Exception("Permission BLUETOOTH_CONNECT was revoked after first start.")
            }

            if (!hidDevice.connect(hostDevice)) {
                Log.e(
                    "BluetoothDevice",
                    "Device found but connection might have failed. \n Possible causes: invalid hid descriptor, app not registered, bound revoked"
                )
                throw BluetoothSenderException("Failed to call hidDevice.connect(hostDevice)")
            }

            return Connecting
        }
    }

    object Connecting:ConnectionState()


    data class Connected(val context: Context, val hostDevice: BluetoothDevice, val hidDevice: BluetoothHidDevice):ConnectionState(){
        fun sendReport(hidReport: ByteArray) : Connected {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("BluetoothAdapter", "No permission for bluetooth connect.")
                throw Exception("Permission BLUETOOTH_CONNECT was revoked after first start.")
            }

            if (!hidDevice.sendReport(hostDevice, Hid.ID_TOUCHSCREEN.toInt(), hidReport)) {
                Log.e("BluetoothHidDevice", "Error sending report.")
                throw BluetoothSenderException("Error sending hid report.")
            }

            Log.d("BluetoothHidDevice", "Touch event sent to ${hostDevice.name}")

            return this
        }
    }
}



