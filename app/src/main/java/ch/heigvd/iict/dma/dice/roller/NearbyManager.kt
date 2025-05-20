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

import RollsResult
import com.google.gson.Gson
import java.io.IOException

sealed class NearbyMessage {
    data class HelloMessage(val username: String) : NearbyMessage()
    data class RollResultMessage(val rollResult: RollsResult) : NearbyMessage()

    companion object {
        private val gson = Gson()

        fun serialize(message: NearbyMessage): String {
            val type = when(message) {
                is HelloMessage -> "HELLO"
                is RollResultMessage -> "ROLL"
            }
            return "$type:${gson.toJson(message)}"
        }

        fun deserialize(data: String): NearbyMessage? {
            val parts = data.split(":", limit = 2)
            if (parts.size != 2) return null

            return when(parts[0]) {
                "HELLO" -> gson.fromJson(parts[1], HelloMessage::class.java)
                "ROLL" -> gson.fromJson(parts[1], RollResultMessage::class.java)
                else -> null
            }
        }
    }
}

class NearbyManager(private val context: Activity) {

    // Add a listener interface to communicate with the UI
    interface ConnectionListener {
        fun onDeviceConnected(endpointId: String, deviceName: String)
        fun onConnectionFailed(endpointId: String, reason: String)
        fun onHelloMessageReceived(endpointId: String, username: String)
        fun onRollResultReceived(endpointId: String, rollResult: RollsResult)
        fun onDeviceDisconnected(endpointId: String)
        fun getUsername(): String
    }

    private val STRATEGY: Strategy = Strategy.P2P_CLUSTER
    private val SERVICE_ID = "ch.heigvd.iict.dma.dice.roller"
    private val advertisingOptions: AdvertisingOptions =
        AdvertisingOptions.Builder().setStrategy(STRATEGY).build()

    private val activeConnections = mutableSetOf<String>()
    fun isConnected(endpointId: String): Boolean {
        return endpointId in activeConnections
    }


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

                    try {
                        val nearbyMessage = NearbyMessage.deserialize(message)
                        when (nearbyMessage) {
                            is NearbyMessage.HelloMessage -> {
                                Log.d(
                                    "NearbyManager",
                                    "Received hello from: ${nearbyMessage.username}"
                                )
                                connectionListener?.onHelloMessageReceived(
                                    endpointId,
                                    nearbyMessage.username
                                )
                            }

                            is NearbyMessage.RollResultMessage -> {
                                Log.d("NearbyManager", "Received roll result from $endpointId")
                                connectionListener?.onRollResultReceived(
                                    endpointId,
                                    nearbyMessage.rollResult
                                )
                            }

                            null -> {
                                Log.e("NearbyManager", "Received invalid message format: $message")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NearbyManager", "Error processing message", e)
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
                    .requestConnection(
                        connectionListener?.getUsername().toString(),
                        endpointId,
                        connectionLifecycleCallback
                    )
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
                    // Add to active connections set
                    activeConnections.add(endpointId)
                    // We're connected - send our device name
                    sendHello(endpointId, connectionListener?.getUsername().toString())
                    Log.d("NearbyManager", "Connected to endpoint: $endpointId")
                    connectionListener?.onDeviceConnected(endpointId, endpointId) // Use actual name if available
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
            activeConnections.remove(endpointId)
            connectionListener?.onDeviceDisconnected(endpointId)
        }
    }


    fun startAdvertising() {
        Nearby.getConnectionsClient(context)
            .startAdvertising(
                connectionListener?.getUsername().toString(),
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
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

    fun stopAdvertising() {
        Nearby.getConnectionsClient(context)
            .stopAdvertising()
    }

    fun stopDiscovery() {
        Nearby.getConnectionsClient(context)
            .stopDiscovery()
    }

    fun disconnectFromAllEndpoints() {
        if (activeConnections.isNotEmpty()) {
            Nearby.getConnectionsClient(context).stopAllEndpoints()
            activeConnections.clear()
        }
    }

    fun sendHello(endpointId: String, username: String) {
        val message = NearbyMessage.HelloMessage(username)
        sendSerializedMessage(endpointId, NearbyMessage.serialize(message))
    }

    // Send a roll result
    fun sendRollResult(endpointId: String, rollResult: RollsResult) {
        val message = NearbyMessage.RollResultMessage(rollResult)
        sendSerializedMessage(endpointId, NearbyMessage.serialize(message))
    }

    // Helper method to send serialized messages
    private fun sendSerializedMessage(endpointId: String, serialized: String) {
        if (!isConnected(endpointId)) {
            Log.w(
                "NearbyManager",
                "Attempted to send message to disconnected endpoint: $endpointId"
            )
            return
        }

        val bytesPayload = Payload.fromBytes(serialized.toByteArray(Charsets.UTF_8))

        Nearby.getConnectionsClient(context)
            .sendPayload(endpointId, bytesPayload)
            .addOnSuccessListener {
                Log.d("NearbyManager", "Message sent successfully to $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e("NearbyManager", "Failed to send message to $endpointId", e)
                // Handle potential disconnection
                if (e is IOException && e.message?.contains("closed") == true) {
                    Log.w("NearbyManager", "Connection appears to be closed, removing endpoint")
                    activeConnections.remove(endpointId)
                    connectionListener?.onDeviceDisconnected(endpointId)
                }

            }
    }
}