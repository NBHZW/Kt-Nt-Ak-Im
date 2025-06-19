package netty

import api.AkImSetting
import api.enums.ConnectModeEnum
import handler.TcpServerInitializer
import handler.WebSocketServerInitializer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler


class NettyServer {

    private val bossGroup = NioEventLoopGroup(10)
    private val workerGroup = NioEventLoopGroup(10)
    private val webSocketServerInitializer = WebSocketServerInitializer()
    private val tcpServerInitializer = TcpServerInitializer()

    fun start(setting: AkImSetting) {
        try {
            initHandler(setting)
            ServerBootstrap().apply {
                group(bossGroup, workerGroup)
                channel(NioServerSocketChannel::class.java)
                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                option(ChannelOption.SO_BACKLOG, 1024)
                //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                childOption(ChannelOption.SO_KEEPALIVE, true)
                //将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                childOption(ChannelOption.TCP_NODELAY, true)
                handler(LoggingHandler(LogLevel.INFO))
                childHandler(if(setting.mode == ConnectModeEnum.WEBSOCKET) webSocketServerInitializer else tcpServerInitializer )

            }.bind(setting.port).sync().channel().closeFuture().sync()
        } catch (e: Exception) {
            println(e.message)
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    private fun initHandler(setting: AkImSetting) {

    }
}
