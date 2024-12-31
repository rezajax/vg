//
//package ir.rezajax.vg
//import java.io.IOException
//import java.net.InetSocketAddress
//import java.nio.channels.DatagramChannel
//import java.nio.channels.ServerSocketChannel
//import java.nio.channels.SocketChannel
//import java.nio.ByteBuffer
//
//class Socks5Server {
//    private val tcpPort = 1080
//    private val udpPort = 1081
//
//    fun start() {
//        // راه‌اندازی سرور TCP
//        Thread {
//            try {
//                startTcpServer()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }.start()
//
//        // راه‌اندازی سرور UDP
//        Thread {
//            try {
//                startUdpServer()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }.start()
//    }
//
//    private fun startTcpServer() {
//        val serverSocketChannel = ServerSocketChannel.open()
//        serverSocketChannel.bind(InetSocketAddress("0.0.0.0", tcpPort))
//        println("Socks5 TCP server started on port $tcpPort")
//        while (true) {
//            val socketChannel: SocketChannel = serverSocketChannel.accept()
//            println("TCP connection established: ${socketChannel.remoteAddress}")
//            val buffer = ByteBuffer.allocate(1024)
//            try {
//                socketChannel.read(buffer)
//                buffer.flip()
//                val receivedString = String(buffer.array(), 0, buffer.position())
//                println("Received data: $receivedString")
//                val response = "Socks5 TCP Response"
//                buffer.clear()
//                buffer.put(response.toByteArray())
//                buffer.flip()
//                socketChannel.write(buffer)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } finally {
//                socketChannel.close()
//            }
//        }
//    }
//
//    private fun startUdpServer() {
//        val datagramChannel = DatagramChannel.open()
//        datagramChannel.bind(InetSocketAddress("0.0.0.0", udpPort))
//        println("Socks5 UDP server started on port $udpPort")
//        val buffer = ByteBuffer.allocate(1024)
//        while (true) {
//            val senderAddress = datagramChannel.receive(buffer)
//            buffer.flip()
//            val receivedString = String(buffer.array(), 0, buffer.position())
//            println("Received UDP data from $senderAddress: $receivedString")
//            val response = "Socks5 UDP Response"
//            buffer.clear()
//            buffer.put(response.toByteArray())
//            buffer.flip()
//            datagramChannel.send(buffer, senderAddress)
//            buffer.clear()
//        }
//    }
//
//
//
//    * private fun startTcpServer() {
//        // تغییر آدرس به 0.0.0.0 برای گوش دادن به تمام آدرس‌ها
//        val serverSocketChannel = ServerSocketChannel.open()
//        serverSocketChannel.bind(InetSocketAddress("0.0.0.0", tcpPort)) // 0.0.0.0 به تمام دستگاه‌های LAN اجازه دسترسی می‌دهد
//        println("Socks5 TCP server started on port $tcpPort")
//
//        while (true) {
//            val socketChannel: SocketChannel = serverSocketChannel.accept()
//            println("TCP connection established: ${socketChannel.remoteAddress}")
//
//            // مدیریت درخواست‌ها (در اینجا فقط یک نمونه ساده است)
//            val buffer = ByteBuffer.allocate(1024)
//            socketChannel.read(buffer)
//            buffer.flip()
//            println("Received data: ${String(buffer.array())}")
//
//            // ارسال پاسخ به کلاینت
//            val response = "Socks5 TCP Response"
//            buffer.clear()
//            buffer.put(response.toByteArray())
//            buffer.flip()
//            socketChannel.write(buffer)
//            socketChannel.close()
//        }
//    }
//
//    private fun startUdpServer() {
//        // تغییر آدرس به 0.0.0.0 برای گوش دادن به تمام آدرس‌ها
//        val datagramChannel = DatagramChannel.open()
//        datagramChannel.bind(InetSocketAddress("0.0.0.0", udpPort)) // 0.0.0.0 به تمام دستگاه‌های LAN اجازه دسترسی می‌دهد
//        println("Socks5 UDP server started on port $udpPort")
//
//        val buffer = ByteBuffer.allocate(1024)
//
//        while (true) {
//            // دریافت داده‌ها از یک کلاینت
//            val senderAddress = datagramChannel.receive(buffer)
//            buffer.flip()
//            println("Received UDP data from $senderAddress: ${String(buffer.array())}")
//
//            // ارسال پاسخ به کلاینت
//            val response = "Socks5 UDP Response"
//            buffer.clear()
//            buffer.put(response.toByteArray())
//            buffer.flip()
//            datagramChannel.send(buffer, senderAddress)
//            buffer.clear()
//        }
//    }
//
//}
//
//fun main() {
//    val socks5Server = Socks5Server()
//    socks5Server.start()
//}
