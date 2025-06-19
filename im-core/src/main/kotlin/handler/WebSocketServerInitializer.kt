package handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler


class WebSocketServerInitializer: ChannelInitializer<SocketChannel>(){

    var hearBeat: Boolean = false
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpServerCodec())
        pipeline.addLast(HttpObjectAggregator(65536))
        pipeline.addLast(WebSocketServerCompressionHandler())
        pipeline.addLast(WebSocketHandler())
    }

}
