package ir.rezajax.vg
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import android.net.TrafficStats
import android.os.Process


private const val TAG = "Socks5Proxy"
private const val SOCKS_VERSION: Byte = 0x05
private const val NO_AUTH: Byte = 0x00
private const val CMD_CONNECT: Byte = 0x01
private const val ATYP_IPV4: Byte = 0x01
private const val ATYP_DOMAIN: Byte = 0x03
private const val ATYP_IPV6: Byte = 0x04


private const val TRAFFIC_STATS_TAG = 0xF00D // Use your app's tag value

private fun tagSocket(socket: Socket) {
    try {
        TrafficStats.tagSocket(socket)
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG)
    } catch (e: Exception) {
        Log.w(TAG, "Socket tagging failed: ${e.message}")
    }
}


private fun untagSocket(socket: Socket) {
    try {
        TrafficStats.untagSocket(socket)
    } catch (e: Exception) {
        Log.w(TAG, "Socket untagging failed: ${e.message}")
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
suspend fun newStartSocks5Proxy2(localPort: Int, onLogUpdate: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        val serverSocket = ServerSocket(localPort)
        onLogUpdate("SOCKS5 server started on port $localPort")

        try {
            while (true) {
                val clientSocket = serverSocket.accept()
                onLogUpdate("New connection from ${clientSocket.inetAddress.hostAddress}")

                launch {
                    handleClientConnection(clientSocket, onLogUpdate)
                }
            }
        } catch (e: Exception) {
            onLogUpdate("Server error: ${e.message}")
        } finally {
            serverSocket.close()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun handleClientConnection(clientSocket: Socket, onLogUpdate: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {

            // Tag sockets before use
            tagSocket(clientSocket)

            // SOCKS5 handshake
            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()

            // Read client greeting
            val version = input.read().toByte()
            val numAuthMethods = input.read()
            val authMethods = input.readNBytes(numAuthMethods)

            // Send server greeting (no auth)
            output.write(byteArrayOf(SOCKS_VERSION, NO_AUTH))
            output.flush()

            // Read client request
            val requestVersion = input.read().toByte()
            val command = input.read().toByte()
            val reserved = input.read()
            val addressType = input.read().toByte()

            val targetAddress = when (addressType) {
                ATYP_IPV4 -> {
                    val ip = input.readNBytes(4)
                    Inet4Address.getByAddress(ip).hostAddress
                }
                ATYP_IPV6 -> {
                    val ip = input.readNBytes(16)
                    Inet6Address.getByAddress(ip).hostAddress
                }
                ATYP_DOMAIN -> {
                    val domainLength = input.read()
                    String(input.readNBytes(domainLength))
                }
                else -> throw Exception("Unsupported address type")
            }

            val targetPort = (input.read() shl 8) or input.read()

            // Connect to target
            val targetSocket = Socket().apply {
                tagSocket(this)
            }

            try {
                targetSocket.connect(InetSocketAddress(targetAddress, targetPort))
                onLogUpdate("Connected to $targetAddress:$targetPort")

                // Send success response
                val response = byteArrayOf(
                    SOCKS_VERSION, 0x00, 0x00,
                    ATYP_IPV4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
                )
                output.write(response)
                output.flush()

                // Start data transfer
                val clientToTarget = Channel<Byte>(Channel.UNLIMITED)
                val targetToClient = Channel<Byte>(Channel.UNLIMITED)

                launch {
                    transferData(input, targetSocket.getOutputStream(), clientToTarget, onLogUpdate)
                }

                launch {
                    transferData(targetSocket.getInputStream(), output, targetToClient, onLogUpdate)
                }

            } catch (e: Exception) {
                onLogUpdate("Connection failed: ${e.message}")
                val response = byteArrayOf(SOCKS_VERSION, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
                output.write(response)
                output.flush()
            }
        } catch (e: Exception) {
            onLogUpdate("Connection error: ${e.message}")
        } finally {
            untagSocket(clientSocket)
            clientSocket.close()
        }
    }

}



private suspend fun transferData(
    input: InputStream,
    output: OutputStream,
    channel: Channel<Byte>,
    onLogUpdate: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val buffer = ByteArray(8192)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead == -1) break
                output.write(buffer, 0, bytesRead)
                output.flush()
            }
        } catch (e: Exception) {
            onLogUpdate("Data transfer error: ${e.message}")
        }
    }
}