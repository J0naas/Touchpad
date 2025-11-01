package com.example.touchpad

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.touchpad.R

class DevicesAdapter(private val context: Context, private val devices: List<BluetoothDevice>) :
    ArrayAdapter<BluetoothDevice>(context, android.R.layout.simple_list_item_1, devices) {

    private var selectedPosition = -1

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }

    @SuppressLint("MissingPermission")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView?: LayoutInflater.from(context).inflate(R.layout.device_item, parent, false)
        val deviceNameTextView = view.findViewById<TextView>(R.id.device_name)
        val deviceAddressTextView = view.findViewById<TextView>(R.id.device_address)
        val selectedView = view.findViewById<View>(R.id.selected_view)

        val device = getItem(position)
        deviceNameTextView.text = device?.name ?: "null"
        deviceAddressTextView.text = device?.address ?: "null"

        if (position == selectedPosition) {
            selectedView.visibility = View.VISIBLE
        } else {
            selectedView.visibility = View.GONE
        }

        return view
    }
}

@SuppressLint("MissingPermission")
class BluetoothDeviceSelector @JvmOverloads constructor(
    context: Context,
    private val onSelect: (BluetoothDevice) -> Unit,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val listView: ListView
    private val btAdapter: BluetoothAdapter
    private val selectDeviceButton: Button
    private val noDevicesTextView: TextView

    private var devices: List<BluetoothDevice>
    private val devicesAdapter: DevicesAdapter
    private var selectedDevice: BluetoothDevice? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.bluetooth_device_selector, this, true)

        listView = findViewById(R.id.devicesList)
        btAdapter = BluetoothAdapter.getDefaultAdapter()

        devices = btAdapter.bondedDevices.toList()
        devicesAdapter = DevicesAdapter(context, devices)
        listView.adapter = devicesAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            devicesAdapter.setSelectedPosition(position)
            devicesAdapter.notifyDataSetChanged()
            selectedDevice = devices[position]
        }

        selectDeviceButton = findViewById(R.id.select_device_button)
        selectDeviceButton.setOnClickListener {
            selectedDevice?.let { device ->
                onDeviceSelected(device)
            }
        }
        noDevicesTextView = findViewById<TextView>(R.id.no_devices_text)
        noDevicesTextView.text = "No devices found"

        listPairedDevices()

    }

    @SuppressLint("MissingPermission")
    private fun listPairedDevices() {
        devicesAdapter.clear()
        devices = btAdapter.bondedDevices.toList()
        if (btAdapter.isEnabled) {
            for (device in devices) {
                devicesAdapter.add(device)
            }
            noDevicesTextView.visibility = View.GONE
        } else {
            noDevicesTextView.visibility = View.VISIBLE
            Toast.makeText(context, "Please Switch On The Bluetooth First", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun onDeviceSelected(device: BluetoothDevice) {
        onSelect(device)
        Toast.makeText(context, "Device selected: ${device.name} (${device.address})", Toast.LENGTH_SHORT).show()
    }
}