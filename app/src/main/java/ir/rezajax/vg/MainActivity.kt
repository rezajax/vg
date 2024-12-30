package ir.rezajax.vg

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import ir.rezajax.vg.ui.theme.VgTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.Properties
import javax.crypto.KeyGenerator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var log by remember { mutableStateOf("Initializing SSH Tunnel...") }
            var username by remember { mutableStateOf("root") }
            var host by remember { mutableStateOf("107.175.73.102") }
            var password by remember { mutableStateOf("") }

            /*// Start SSH connection in lifecycleScope
            LaunchedEffect(Unit) {
                val sshManager = SshTunnelManager(
                    username = username,
                    host = host,
                    port = 22,
                    password = password,
                    onLogUpdate = { log = it } // Callback to update the log
                )

                sshManager.connect(
                    localPort = 1080,
                    remoteHost = "localhost",
                    remotePort = 1080
                )
            }*/

            var sshManager: SshTunnelManager? by remember { mutableStateOf(null) }

            VgTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // User inputs for SSH connection details
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                        TextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Host") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            visualTransformation = PasswordVisualTransformation()
                        )

                        // Display log output
                        Text(
                            text = log,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        // Button to trigger SSH connection
                        Button(
                            onClick = {
                                // Start SSH connection after user input
                                lifecycleScope.launch {
                                    sshManager = SshTunnelManager(
                                        username = username,
                                        host = host,
                                        port = 22,
                                        password = password,
                                        onLogUpdate = { log = it }
                                    )
                                    sshManager?.connect(localPort = 1080, remoteHost = "localhost", remotePort = 1080)
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Connect")
                        }

                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    sshManager?.disconnect()
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Disconnect")
                        }

                        Button(
                            onClick = {
                                println("start badvpn")
                            }
                        ) {
                            Text ("Start Badvpn")
                        }
                    }
                }
            }
        }
    }
}



class SshTunnelManager(
    private val username: String,
    private val host: String,
    private val port: Int,
    private val password: String,
    private val onLogUpdate: (String) -> Unit // Callback function for updating logs
) {
    private var session: Session? = null

    suspend fun connect(localPort: Int, remoteHost: String, remotePort: Int) {
        withContext(Dispatchers.IO) {
            try {
                onLogUpdate("Connecting to SSH...") // Log the connection attempt

                val jsch = JSch()
                session = jsch.getSession(username, host, port)
                session?.setPassword(password)

                // Ignore host key checking (for simplicity)
                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                session?.setConfig(config)

                // Connect the session
                session?.connect()
                onLogUpdate("Connected to SSH.")

                // Setup remote forwarding (equivalent to -R)
                session?.setPortForwardingR(remotePort, remoteHost, remotePort)

                onLogUpdate("SSH Tunnel established successfully.")
            } catch (e: Exception) {
                onLogUpdate("Error: $e") // Log any errors
            }
        }
    }

    fun disconnect() {

        session?.disconnect()
        onLogUpdate("SSH Tunnel closed.")
    }


    suspend fun startUdpForwarding() {
        withContext(Dispatchers.IO) {
            try {
                onLogUpdate("Starting UDP forwarding with socat...")

                val command = listOf("socat", "UDP4-RECVFROM:12345,fork", "TCP:127.0.0.1:1080")
                val process = ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()

                onLogUpdate("UDP forwarding started.")
            } catch (e: Exception) {
                onLogUpdate("Error starting UDP forwarding: $e")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VgTheme {
        Greeting("Android")
    }
}
