import api.AkImSetting
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import netty.NettyServer
import kotlin.concurrent.thread

fun main() {
    // 在后台线程启动服务器
    thread {
        NettyServer().start(AkImSetting())
    }

    // 主线程等待服务器启动
    Thread.sleep(2000)

    // 启动客户端
    NettyClient().start(AkImSetting())
}

class NettyClient {

    private val workerGroup = NioEventLoopGroup(10)

    fun start(setting: AkImSetting) {
        try {
            Bootstrap().apply {
                group(workerGroup)
                channel(NioSocketChannel::class.java)
                handler(object: ChannelInitializer<SocketChannel>(){
                    override fun initChannel(ch: SocketChannel?) {
                        ch?.pipeline()?.addLast()
                    }
                });
            }.connect("127.0.0.1",8080).sync().channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}
