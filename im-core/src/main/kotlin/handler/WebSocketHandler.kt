package handler

import actors.WebSocketDispatcher
import constant.ImMsgCode
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.HOST
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.util.Attribute
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import model.ImContext.Companion.IM_SESSION_KEY
import model.ImSession
import java.net.HttpURLConnection

/**
 * WebSocket本质上是现由HTTP通过一些特殊的Header头标识后
 * 客户端和服务端一起upgrade为WebSocket
 * 所以在WebSocket的连接Handler中，首先处理的消息为HttpRequest类型的消息
 * 升级协议之后才是WebSocketFrame
 */
class WebSocketHandler: SimpleChannelInboundHandler<Any>() {
    private var webSocketServerHandshaker: WebSocketServerHandshaker? = null
    private val webSocketDispatcher: WebSocketDispatcher? = null
    companion object{
        const val HTTP_REQUEST_POINT_PATH = "/im/api/point"
        const val HTTP_REQUEST_GROUP_PATH = "/im/api/group"
        const val WEBSOCKET_PATH = "/im/websocket"
    }
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        when(msg){
            is FullHttpRequest ->{handlerFullHttpRequest(ctx, msg)}
            is WebSocketFrame -> {handlerWebSocketFrame(ctx, msg)}
        }
    }

    private fun handlerFullHttpRequest(ctx: ChannelHandlerContext, requestMsg: FullHttpRequest) {
        val path = requestMsg.uri()
        // 解析Rest API 接口
        if(path == HTTP_REQUEST_POINT_PATH && HttpMethod.POST == requestMsg.method()){
            handlerHttpFrame(ctx, requestMsg, ImMsgCode.IM_MSG_POINT)
        }else if(path == HTTP_REQUEST_GROUP_PATH && HttpMethod.POST == requestMsg.method()){
            handlerHttpFrame(ctx, requestMsg, ImMsgCode.IM_MSG_GROUP)
        }else if(path == WEBSOCKET_PATH){  // 如果是websocket握手请求 检测请求解码是否合法 和 upgrade字段
            if(!requestMsg.decoderResult().isSuccess || ("websocket"!=requestMsg.headers().get("upgrade"))){
                val fullHttpResponse = buildFullHttpResponse(BAD_REQUEST, "")
                sendHttpResponse(ctx,requestMsg,fullHttpResponse)
                return
            }
            // 到这里说明WebSocket握手请求合法 允许握手
            // 创建Netty自带的WebSocket握手处理器，利用握手处理器工厂创建
            // 三个参数 分别是：
            // 1：WS的连接路径
            // 2：允许的子协议（websocket允许在握手阶段使用子协议例如chat json mqtt） 只需要这里设置允许且客户端在Sec-Websocket-Protocol中声明子协议即可
            // 例如“chat” 如果需要同时支持多个，利用逗号分开 “chat,json”
            // 3：是否允许 WebSocket 扩展 所谓的拓展功能 是指对协议的压缩，分片优化等等
            val webSocketFactor = WebSocketServerHandshakerFactory(getWebSocketUrl(requestMsg),null,null)
            webSocketServerHandshaker = webSocketFactor.newHandshaker(requestMsg)
            if (webSocketServerHandshaker == null){
                // 说明不支持该websocket版本
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
            }else {
                // 如果支持则添加CORS头防止跨域
                val headers = DefaultHttpHeaders()
                headers.set("Access-Control-Allow-Origin", "*");
                headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                headers.set("Access-Control-Allow-Headers", "DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization");
                webSocketServerHandshaker!!.handshake(ctx.channel(), requestMsg, headers, ctx.channel().newPromise());
            }
        }else {
            // 所有路径都不匹配 恶意or垃圾消息 直接关闭
            ctx.channel().close()
        }
    }

    private fun getWebSocketUrl(requestMsg: FullHttpRequest): String {
        return "ws://"+requestMsg.headers().get(HOST)+WEBSOCKET_PATH
    }

    /**
     * 处理HTTP请求的核心方法。
     * 该方法解析HTTP请求的内容，提取请求体中的数据，并将其转换为字符串格式。
     * 同时，通过Channel的属性获取与当前连接关联的会话（ImSession），以便后续处理。
     * 参数说明：
     * - ctx: 当前通道的上下文，用于操作Netty的Channel和Pipeline。
     * - requestMsg: 完整的HTTP请求对象，包含请求头、请求体等信息。
     * - msgCode: 消息类型编码，用于区分不同的业务逻辑。
     */
    private fun handlerHttpFrame(ctx: ChannelHandlerContext, requestMsg: FullHttpRequest, msgCode:Int) {
        val byteBuf = requestMsg.content()
        val bytes = ByteArray(byteBuf.readableBytes())
        byteBuf.readBytes(bytes)
        val contentStr = bytes.toString(CharsetUtil.UTF_8)
        // 将对应的session对象保存
        val sessionAttribute: Attribute<ImSession?> = ctx.channel().attr(IM_SESSION_KEY)
        sessionAttribute.set(ImActorSystem)
        // 正式处理消息
        val flag = handleHttpMessage(ctx,contentStr, msgCode)
        var content = "{\"code\":200,\"msg\":\"ok\"}"
        if (!flag) {
            content = "{\"code\":500,\"msg\":\"fail\"}"
        }

    }

    private fun handleHttpMessage(
        ctx: ChannelHandlerContext,
        content:String,
        msgCode: Int,
    ): Boolean {
        val contentByteArr = content.toByteArray(CharsetUtil.UTF_8)
        return webSocketDispatcher.handle(msgCode, contentByteArr, ctx);
    }


    private fun sendHttpResponse(
        ctx: ChannelHandlerContext,
        request: FullHttpRequest,
        response: DefaultFullHttpResponse,
    ) {
        if (response.status().code() != HttpURLConnection.HTTP_OK) {
            val buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8)
            response.content().writeBytes(buf)
            buf.release()
            HttpUtil.setContentLength(response, response.content().readableBytes().toLong())
        }

        // 确保响应被发送出去
        val f = ctx.channel().writeAndFlush(response)
        if (!HttpUtil.isKeepAlive(request) || response.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE)
        } else {
            // 如果是Keep-Alive，确保响应完成后不关闭连接
            f.addListener(GenericFutureListener { future: Future<in Void?>? ->
                if (!future!!.isSuccess) {
                    ctx.close()
                }
            })
        }
    }

    private fun buildFullHttpResponse(responseStatus: HttpResponseStatus, content: String): DefaultFullHttpResponse {
        val response = DefaultFullHttpResponse(HTTP_1_1, responseStatus, Unpooled.wrappedBuffer(content.toByteArray(CharsetUtil.UTF_8)))
        response.headers().set("Access-Control-Allow-Origin", "*")
        response.headers().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.headers().set("Access-Control-Allow-Headers", "DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization"
        )
        response.headers().setInt("Content-Length", response.content().readableBytes())
        return response

    }

}
