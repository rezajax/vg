package ir.rezajax.vg

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.socksx.v5.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun newStartSocks5Proxy(localPort: Int, onLogUpdate: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(Socks5ServerEncoder.DEFAULT)
                        pipeline.addLast(Socks5InitialRequestDecoder())
                        pipeline.addLast(Socks5CommandRequestDecoder())
                        pipeline.addLast(Socks5ProxyHandler(onLogUpdate))
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            val channelFuture = bootstrap.bind(localPort).sync()
            onLogUpdate("SOCKS5 server started on port $localPort")
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}

class Socks5ProxyHandler(private val onLogUpdate: (String) -> Unit) : SimpleChannelInboundHandler<Socks5Message>() {

    fun channelRead0(ctx: ChannelHandlerContext, msg: Socks5Message) {
        when (msg) {
            is Socks5InitialRequest -> {
                onLogUpdate("Received Socks5InitialRequest")
                ctx.writeAndFlush(DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH))
            }

            is Socks5CommandRequest -> {
                onLogUpdate("Received Socks5CommandRequest: ${msg.type()} to ${msg.dstAddr()}:${msg.dstPort()}")
                if (msg.type() == Socks5CommandType.CONNECT) {
                    connectToTarget(ctx, msg, onLogUpdate)
                } else {
                    ctx.writeAndFlush(
                        DefaultSocks5CommandResponse(
                            Socks5CommandStatus.COMMAND_UNSUPPORTED,
                            msg.dstAddrType()
                        )
                    )
                    ctx.close()
                }
            }

            else -> {
                onLogUpdate("Unknown message type: $msg")
                ctx.close()
            }
        }
    }

    private fun connectToTarget(ctx: ChannelHandlerContext, msg: Socks5CommandRequest, onLogUpdate: (String) -> Unit) {
        val targetHost = msg.dstAddr()
        val targetPort = msg.dstPort()
        onLogUpdate("Connecting to $targetHost:$targetPort...")

        val clientBootstrap = Bootstrap()
            .group(ctx.channel().eventLoop())
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(TargetResponseHandler(ctx, onLogUpdate))
                }
            })

        clientBootstrap.connect(targetHost, targetPort).addListener { future ->
            if (future.isSuccess) {
                val outboundChannel = (future as ChannelFuture).channel()
                ctx.writeAndFlush(
                    DefaultSocks5CommandResponse(
                        Socks5CommandStatus.SUCCESS,
                        msg.dstAddrType(),
                        targetHost,
                        targetPort
                    )
                )
                ctx.pipeline().remove(this)
                ctx.pipeline().addLast(RelayHandler(outboundChannel))
                outboundChannel.pipeline().addLast(RelayHandler(ctx.channel()))
                onLogUpdate("Connected to $targetHost:$targetPort")
            } else {
                onLogUpdate("Failed to connect to $targetHost:$targetPort")
                ctx.writeAndFlush(
                    DefaultSocks5CommandResponse(
                        Socks5CommandStatus.FAILURE,
                        msg.dstAddrType()
                    )
                )
                ctx.close()
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        onLogUpdate("Error: ${cause.message}")
        cause.printStackTrace()
        ctx.close()
    }

    override fun messageReceived(ctx: ChannelHandlerContext?, msg: Socks5Message?) {
        TODO("Not yet implemented")
    }
}

class RelayHandler(private val targetChannel: Channel) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        targetChannel.writeAndFlush(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        targetChannel.close()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
        targetChannel.close()
    }
}

class TargetResponseHandler(private val clientChannel: ChannelHandlerContext, private val onLogUpdate: (String) -> Unit) :
    ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        clientChannel.writeAndFlush(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        clientChannel.close()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        onLogUpdate("Error: ${cause.message}")
        cause.printStackTrace()
        ctx.close()
        clientChannel.close()
    }
}
