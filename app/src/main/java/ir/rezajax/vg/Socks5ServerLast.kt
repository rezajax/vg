package ir.rezajax.vg

import org.bbottema.javasocksproxyserver.SocksServer

object Socks5ServerManager {
    private var server: SocksServer? = null
    var isRunning = false
        private set

    fun startServer(port: Int, onLogUpdate: (String) -> Unit) {
        if (isRunning) return onLogUpdate("Server is already running.")

        server = SocksServer(port).apply { start() }
        isRunning = true
        onLogUpdate("SOCKS5 server started on port $port")
    }

    fun stopServer(onLogUpdate: (String) -> Unit) {
        if (!isRunning) return onLogUpdate("Server is not running.")

        server?.stop()
        isRunning = false
        onLogUpdate("SOCKS5 server stopped")
    }
}