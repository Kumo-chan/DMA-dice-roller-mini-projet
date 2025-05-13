package ch.heigvd.iict.dma.dice.roller

import HistoricViewModel
import Roll
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.content.ContextCompat
import ch.heigvd.iict.dma.dice.roller.roll.Roller
import ch.heigvd.iict.dma.dice.roller.ui.Layout


class MainActivity : ComponentActivity() {

    private val layout = Layout()

    private val roller = Roller()

    private val viewModel: HistoricViewModel by viewModels()

    // State for tracking connected devices and messages
    private val connectedDevices = mutableStateMapOf<String, String>()
    private val connectedEndpointIds = mutableSetOf<String>()
    //private val receivedMessages = mutableStateListOf<String>()

    private val nearbyManager = NearbyManager(this)
    // Create permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            Log.d("Permissions", "All required permissions granted")
        } else {
            Log.e("Permissions", "Some permissions were denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val history by viewModel.history.collectAsState()
            layout.MainLayout(rollsResults = history,
                onRollDice = { diceSize, diceCount ->
                viewModel.addRollsResult(roller.roll(Roll(diceSize, diceCount)))
            })
        }

        checkAndRequestPermissions()

        nearbyManager.setConnectionListener(object : NearbyManager.ConnectionListener {
            override fun onDeviceConnected(endpointId: String, deviceName: String) {
                // Update will now trigger recomposition
                connectedDevices[endpointId] = deviceName
                connectedEndpointIds.add(endpointId)

                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Connected to: $deviceName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onConnectionFailed(endpointId: String, reason: String) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Connection failed: $reason",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onMessageReceived(endpointId: String, message: String) {
                val senderName = connectedDevices[endpointId] ?: "Unknown"
                val formattedMessage = "$senderName: $message"


                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "New message: $formattedMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onDeviceDisconnected(endpointId: String) {
                // Remove from connected devices
                connectedDevices.remove(endpointId)
                connectedEndpointIds.remove(endpointId)

                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Device disconnected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        // Start discovering nearby devices
        nearbyManager.startDiscovery()

        // Start advertising this device
        nearbyManager.startAdvertising()
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf<String>()

        // Location permissions required for all Android versions
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // WiFi state permissions
        requiredPermissions.add(Manifest.permission.ACCESS_WIFI_STATE)
        requiredPermissions.add(Manifest.permission.CHANGE_WIFI_STATE)

        // Add Bluetooth permissions for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        // Add Nearby Wifi permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        // Check which permissions need to be requested
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            Log.d("Permissions", "Requesting: ${permissionsToRequest.joinToString()}")
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }

    override fun onPause() {
        super.onPause()
    }
    private val requestBlePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        val isBLEGranted =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false)
        else
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) &&
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH, false) &&
                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_ADMIN, false)

    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}