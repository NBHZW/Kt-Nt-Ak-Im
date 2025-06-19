package handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec

class TcpServerInitializer: ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(TcpMessageDecoder())
        pipeline.addLast(TcpMessageEncoder())
    }

}
