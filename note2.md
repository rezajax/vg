package ir.rezajax.vg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import ir.rezajax.vg.ui.theme.VgTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties

class MainActivity : ComponentActivity() {
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

        setContent {
            var log by remember { mutableStateOf("Initializing SSH Tunnel...") }

            // Start SSH connection in lifecycleScope
            LaunchedEffect(Unit) {
                val sshManager = SshTunnelManager(
                    username = "root",
                    host = "107.175.73.102",
                    port = 22,
                    password = "reza1122",
                    onLogUpdate = { log = it } // Callback to update the log
                )

                sshManager.connect(
                    localPort = 1080,
                    remoteHost = "localhost",
                    remotePort = 1080
                )
            }

            VgTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = log,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
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
