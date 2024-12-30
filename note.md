package ir.rezajax.vg

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.rezajax.vg.ui.theme.VgTheme
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VgTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var output by remember { mutableStateOf("") }

                    Column (modifier= Modifier.padding(innerPadding)) {
                        Button(onClick = {
                            runXray(this@MainActivity) { result ->
                                output = result
                            }
                        }) {
                            Text("اجرای Xray")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (output.isEmpty()) "خروجی نمایش داده می‌شود." else output,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}



/*private fun runXray(context: Context, onResult: (String) -> Unit) {
try {
val assetManager = context.assets
val inputStream = assetManager.open("xray")
val tempFile = File(context.cacheDir, "xray")
tempFile.outputStream().use { outputStream ->
inputStream.copyTo(outputStream)
}


        tempFile.setExecutable(true)

        val process = ProcessBuilder(tempFile.absolutePath, "--help")
            .directory(context.cacheDir)
            .start()


        val inputStreamReader = process.inputStream.bufferedReader()
        val output = inputStreamReader.readText()


        onResult(output)

    } catch (e: IOException) {
        onResult("خطا در اجرای فایل xray: ${e.message}")
    }
}*/



private fun runXray(context: Context, onResult: (String) -> Unit) {
try {
val assetManager = context.assets
val inputStream = assetManager.open("xray")
val tempFile = File(context.cacheDir, "xray")
tempFile.outputStream().use { outputStream ->
inputStream.copyTo(outputStream)
}

        tempFile.setExecutable(true)

        // Use ls instead of running the file
        val process = ProcessBuilder("./xray", "--version")
            .directory(context.cacheDir)
            .start()

        val inputStreamReader = process.inputStream.bufferedReader()
        val output = inputStreamReader.readText()

        onResult(output)
    } catch (e: IOException) {
        onResult("خطا در اجرای ls: ${e.message}")
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

