/*
package ir.rezajax.vg

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler
import io.netty.handler.codec.socksx.v5.*
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.proxy.ProxyHandler
import java.net.InetSocketAddress

class Socks5Server(private val port: Int) {

    fun start() {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .handler(LoggingHandler(LogLevel.INFO))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        // Handle SOCKS5 protocol
                        pipeline.addLast(SocksPortUnificationServerHandler()) // Automatically detects SOCKS version
                        pipeline.addLast(Socks5CommandRequestDecoder())
                        pipeline.addLast(Socks5ServerHandler()) // Custom handler to process requests
                    }
                })

            val channelFuture = bootstrap.bind(port).sync()
            println("SOCKS5 Proxy Server started on port $port")

            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    private class Socks5ServerHandler : SimpleChannelInboundHandler<Socks5CommandRequest>() {
        override fun channelRead(ctx: ChannelHandlerContext, msg: Socks5CommandRequest) {
            if (msg.type() == Socks5CommandType.CONNECT) {
                val targetAddress = msg.dstAddr()
                val targetPort = msg.dstPort()

                println("SOCKS5 request to connect to $targetAddress:$targetPort")

                val promise = ctx.executor().newPromise<Channel>()
                val outboundHandler = object : ChannelInboundHandlerAdapter() {
                    override fun channelActive(ctx: ChannelHandlerContext) {
                        // Send success response
                        ctx.pipeline().remove(this)
                        ctx.writeAndFlush(DefaultSocks5CommandResponse(
                            Socks5CommandStatus.SUCCESS,
                            Socks5AddressType.IPv4
                        ))
                        promise.setSuccess(ctx.channel())
                    }

                    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                        println("Connection to $targetAddress:$targetPort failed: ${cause.message}")
                        promise.setFailure(cause)
                        ctx.close()
                    }
                }

                val outboundBootstrap = Bootstrap()
                outboundBootstrap.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().javaClass)
                    .handler(outboundHandler)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)

                // Connect to the target server
                outboundBootstrap.connect(targetAddress, targetPort).addListener {
                    if (!it.isSuccess) {
                        ctx.writeAndFlush(DefaultSocks5CommandResponse(
                            Socks5CommandStatus.FAILURE,
                            Socks5AddressType.IPv4
                        ))
                        ctx.close()
                    }
                }
            } else {
                ctx.writeAndFlush(DefaultSocks5CommandResponse(
                    Socks5CommandStatus.COMMAND_UNSUPPORTED,
                    Socks5AddressType.IPv4
                ))
                ctx.close()
            }
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            ctx.close()
        }

        override fun messageReceived(ctx: ChannelHandlerContext?, msg: Socks5CommandRequest?) {
            TODO("Not yet implemented")
        }
    }
}
*/
