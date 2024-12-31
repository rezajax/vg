/*
package ir.rezajax.vg

import org.apache.sshd.common.forward.PortForwardingEventListener
import org.apache.sshd.common.forward.PortForwardingManager
import org.apache.sshd.common.forward.SocksProxy
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.nio.file.Paths

class Socks5ServerManager(
    private val bindAddress: String = "0.0.0.0",
    private val port: Int = 1080,
    private val username: String,
    private val password: String
) {
    private var sshServer: SshServer? = null

    fun start() {
        try {
            // Set up SSH server
            sshServer = SshServer.setUpDefaultServer()
            sshServer?.apply {
                host = bindAddress
                port = this@Socks5ServerManager.port
                keyPairProvider = SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser"))

                // Set authentication methods
                passwordAuthenticator = { inputUsername, inputPassword, _ ->
                    inputUsername == username && inputPassword == password
                }

                // Enable SOCKS proxy
                val socksProxy = SocksProxy()
                socksProxy.forwardingEventListener = PortForwardingEventListener.EMPTY

                tcpipForwarderFactory = PortForwardingManager.DEFAULT_FORWARDER_FACTORY
                sessionFactory = socksProxy.createSessionFactory()

                start()
                println("SOCKS5 server started at $bindAddress:$port")
            }
        } catch (e: Exception) {
            println("Failed to start SOCKS5 server: $e")
        }
    }

    fun stop() {
        try {
            sshServer?.stop(true)
            println("SOCKS5 server stopped.")
        } catch (e: Exception) {
            println("Error stopping SOCKS5 server: $e")
        }
    }
}
*/
