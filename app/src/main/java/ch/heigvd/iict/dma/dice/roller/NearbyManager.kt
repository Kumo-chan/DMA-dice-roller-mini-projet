package ch.heigvd.iict.dma.dice.roller

import android.app.Activity
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener


class NearbyManager(private val context: Activity) {

    // Add a listener interface to communicate with the UI
    interface ConnectionListener {
        fun onDeviceConnected(endpointId: String, deviceName: String)
        fun onConnectionFailed(endpointId: String, reason: String)
        fun onMessageReceived(endpointId: String, message: String)
        fun onDeviceDisconnected(endpointId: String)

    }

    private val STRATEGY: Strategy = Strategy.P2P_CLUSTER
    private val SERVICE_ID = "ch.heigvd.iict.dma.dice.roller"
    private val advertisingOptions : AdvertisingOptions =
        AdvertisingOptions.Builder().setStrategy(STRATEGY).build()

    // Add a connection listener field
    private var connectionListener: ConnectionListener? = null

    // Method to set the listener
    fun setConnectionListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes()
                receivedBytes?.let {
                    val message = String(it, Charsets.UTF_8)

                    // Check if it's an initial name payload or a regular message
                    if (message.startsWith("NAME:")) {
                        val deviceName = message.substring(5)
                        Log.d("NearbyManager", "Received device name: $deviceName from $endpointId")
                        connectionListener?.onDeviceConnected(endpointId, deviceName)
                    } else {
                        Log.d("NearbyManager", "Received message: $message from $endpointId")
                        connectionListener?.onMessageReceived(endpointId, message)
                    }
                }
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Not needed for simple text messages
        }
    }


    private val endpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // A nearby endpoint was found. You can initiate a connection here.
                Nearby.getConnectionsClient(context)
                    .requestConnection(getLocalUserName(), endpointId, connectionLifecycleCallback)
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint is no longer available.
            }
        }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Automatically accept the connection on both sides
            Nearby.getConnectionsClient(context)
                .acceptConnection(endpointId, payloadCallback)

            Log.d("NearbyManager", "Connection initiated with endpoint: $endpointId")
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // We're connected - send our device name
                    sendMessage(endpointId, "hello")
                    Log.d("NearbyManager", "Connected to endpoint: $endpointId")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("NearbyManager", "Connection rejected by endpoint: $endpointId")
                    connectionListener?.onConnectionFailed(endpointId, "Connection rejected")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d("NearbyManager", "Connection error with endpoint: $endpointId")
                    connectionListener?.onConnectionFailed(endpointId, "Connection error")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("NearbyManager", "Disconnected from endpoint: $endpointId")
            connectionListener?.onDeviceDisconnected(endpointId)
        }
    }




    fun startAdvertising() {
        Nearby.getConnectionsClient(context)
            .startAdvertising(
                getLocalUserName(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions
            )
            .addOnSuccessListener(
                { unused: Void? ->
                    Log.d("NearbyManager", "Advertising started successfully")
                })
            .addOnFailureListener(
                { e: Exception? ->
                    Log.e("NearbyManager", "Advertising failed to start", e)
                })
    }

    fun startDiscovery() {
        val discoveryOptions =
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(context)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener(
                OnSuccessListener { unused: Void? ->
                    Log.d("NearbyManager", "Discovery started successfully")
                })
            .addOnFailureListener(
                OnFailureListener { e: java.lang.Exception? ->
                    Log.e("NearbyManager", "Discovery failed to start", e)
                })
    }

    private fun getLocalUserName(): String {
        return "Device_" + android.os.Build.MODEL + "_" + android.os.Build.ID
    }


    fun sendMessage(endpointId: String, message: String) {

        val bytesPayload = Payload.fromBytes(message.toByteArray(Charsets.UTF_8))

        Nearby.getConnectionsClient(context)
            .sendPayload(endpointId, bytesPayload)
            .addOnSuccessListener {
                Log.d("NearbyManager", "Message sent successfully to $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e("NearbyManager", "Failed to send message to $endpointId", e)
            }
    }
}