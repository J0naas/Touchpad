package com.example.touchpad

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var hidServiceHandler: HidServiceHandler
    private lateinit var connectionHandler: ConnectionHandler
    private val recorder = Recorder()

    private lateinit var viewModel: ConnectionViewModel

    // Store the current file URI (if any)
    private var currentUri: Uri? = null
    private var currentScript: String? = null

    // Launcher for creating a new file.
    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        uri?.let {
            currentUri = it
            contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(recorder.getAsScriptString().toByteArray())
                recorder.reset()
            }
            Toast.makeText(this, "Script saved", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for opening an existing file.
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            currentUri = it

            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.openInputStream(it)?.use { inputStream ->
                currentScript = inputStream.bufferedReader().readText()
            }
            Toast.makeText(this, "Script loaded", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    private fun createFileDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter file name")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("Save") { _, _ ->
            val fileName = input.text.toString().ifBlank { "Untitled.txt" }
            // Launch the create document intent. The given file name is used as the suggested title.
            createFileLauncher.launch(fileName)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun edit(){
        currentUri?.let { uri ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No external app available to open text files", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_script -> {
                openFileLauncher.launch(arrayOf("text/plain"))
                true
            }
            R.id.edit_script -> {
                edit()
                true
            }
            R.id.execute_script -> {
                currentScript?.let {
                    parseAndExecuteScript(currentScript!!)
                } ?: run {
                    Toast.makeText(this, "No script loaded", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "No script loaded")
                }
                true
            }
            R.id.record_script ->{
                if(recorder.active){
                    // Stop recording and save script
                    recorder.active = false
                    item.title= "Record script"
                    createFileDialog()
                }
                else{
                    recorder.active = true
                    item.title = "Stop"
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }





    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Hide the title
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Check + request Bluetooth connect and scan permissions
        if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT ) == PackageManager.PERMISSION_DENIED ||
            this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED){
            this.requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 0)
            throw Exception("Permissions need to be granted on start for now.")
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // Check if Bluetooth is enabled
        if (!bluetoothManager.adapter.isEnabled) {
            throw Exception("Bluetooth needs to be enabled before starting the app for now")
        }

        viewModel = ViewModelProvider(this)[ConnectionViewModel::class.java]

        connectionHandler = ConnectionHandler(application, viewModel)
        hidServiceHandler = HidServiceHandler(application, viewModel, connectionHandler)

        // Check the hid service and get hid device in HidServiceHandler
        if (!(bluetoothManager.adapter.getProfileProxy(
                this,
                hidServiceHandler,
                BluetoothProfile.HID_DEVICE
            ))
        ) {
            throw ServiceException("Could not get HID service.")
        }


        val selector = BluetoothDeviceSelector(this, {device ->
            hidServiceHandler.onHostSelected(device, Hid.SDP_TOUCHPAD)
        })

        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                Log.i("MainActivity", "State: $state")
                when (state) {
                    is ConnectionState.Unregistered -> {
                        setContentView(selector)
                    }
                    is ConnectionState.Connected -> {
                        setTouchLayout(state.hostDevice)
                    }
                    else -> {
                        // Toast.makeText(this@MainActivity, "Loading", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun setTouchLayout(device: BluetoothDevice) {
        setContentView(R.layout.touch_layout)
        val touchView = TouchView(this, viewModel, recorder)
        findViewById<FrameLayout>(R.id.touch_view_container).addView(touchView)

        val toggleButton = findViewById<Button>(R.id.hover_toggle_button)
        toggleButton.setOnClickListener {
            touchView.hoverMode = !touchView.hoverMode
            toggleButton.text = if (touchView.hoverMode) {
                "Hover"
            } else {
                "Press"
            }
        }
    }
}
