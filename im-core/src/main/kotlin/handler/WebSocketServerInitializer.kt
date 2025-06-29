package handler

import actors.WebSocketDispatcher
import api.BusinessInfoProvider
import api.ImHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket连接方式服务的初始化状态
 * 包括整个连接中负责的WebSocketDispatcher调度器，是否心跳检测，handler
 */
class WebSocketServerInitializer: ChannelInitializer<SocketChannel>(){
    private var webSocketDispatcher = WebSocketDispatcher()
    private var hearBeat: Boolean = false
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpServerCodec())
        pipeline.addLast(HttpObjectAggregator(65536))
        pipeline.addLast(WebSocketServerCompressionHandler())
        pipeline.addLast(WebSocketHandler())
    }



    fun setWebSocketDispatcherInformation(handlerMap: ConcurrentHashMap<String,ImHandler>?, businessInfoProvider: BusinessInfoProvider?){
        webSocketDispatcher.setHandlerMap(handlerMap)
        webSocketDispatcher.setBusinessInfoProvider(businessInfoProvider)
    }

    fun setHearBeat(hearBeat: Boolean){
        this.hearBeat = hearBeat
    }
}
