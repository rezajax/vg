package ir.rezajax.vg

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bbottema.javasocksproxyserver.SocksServer
import java.net.ServerSocket

object Socks5ServerManager {
    private var server: SocksServer? = null
    private var serverSocket: ServerSocket? = null
    var isRunning = false
        private set

    suspend fun startServer(localPort: Int, onLogUpdate: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            if (isRunning) {
                onLogUpdate("Server is already running.")
                return@withContext
            }

            try {
                serverSocket = ServerSocket(localPort)
                server = SocksServer(localPort).apply { start() }
                isRunning = true
                onLogUpdate("SOCKS5 server started on port $localPort")

                serverSocket?.accept()  // Keep the server alive
            } catch (e: Exception) {
                onLogUpdate("Server error: ${e.message}")
            }
        }
    }

    fun stopServer(onLogUpdate: (String) -> Unit) {
        if (!isRunning) {
            onLogUpdate("Server is not running.")
            return
        }

        try {
            server?.stop()
            serverSocket?.close()
            isRunning = false
            onLogUpdate("SOCKS5 server stopped")
        } catch (e: Exception) {
            onLogUpdate("Error stopping server: ${e.message}")
        }
    }
}