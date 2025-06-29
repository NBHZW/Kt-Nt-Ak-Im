package api

import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import model.ImContext
import model.ImContext.Companion.IM_CONTEXT_KEY
import util.DistributedMsgIdConstructor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


/**
 * 分发器 专门将请求分发给对应的Handler
 * handlerMap 保存 key-handler 的关系 根据Key分配给对应的Handler
 * postMsgExecutor 专门处理postMessage过程的线程池 主要是生产线程用于异步处理onMessage之后即postMessage()中的逻辑的处理 使其成为异步操作 提高效率
 * sender 是消息发送者
 */
abstract class Dispatcher {
    private var handlerMap: ConcurrentHashMap<String, ImHandler>?  = ConcurrentHashMap()
    private var businessInfoProvider: BusinessInfoProvider? = null
    private val postMsgExecutor = ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors()*2,30L,
        TimeUnit.SECONDS,
        LinkedBlockingDeque(1024),
        {Thread(it).apply {
            name = "im-postMsg-thread-${System.nanoTime()}"
            }
        }
    )
    protected var sender: MsgSender? = null


    fun handle(msgType: Int, msgContent: ByteArray, ctx: ChannelHandlerContext):Boolean{
        try{
            val handler = handlerMap?.get(msgType.toString())
            if(handler!=null){
                val msgId: Long? = DistributedMsgIdConstructor.getId()
                if(msgId == null) throw RuntimeException("Constructor MsgId is error")
                val preMessageOption = handler.preMessage(msgType, msgContent, msgId)
                if(!preMessageOption){
                    return false
                }
                var imContext = getImContext(ctx)
                if(imContext == null){
                    imContext = ImContext(sender,ctx)
                    ctx.channel().attr(IM_CONTEXT_KEY).set(imContext)
                }
                // 重置心跳超时次数
                imContext.resetHeartNum()
                val serializable = handler.onMessage(imContext, msgType, msgContent, msgId)
                val finalContext: ImContext = imContext
                // 异步完成后续的操作
                postMsgExecutor.execute { handler.postMessage(finalContext, msgType, msgContent, msgId, serializable) }
            }else{
                println("no handler for msgType:$msgType and now message content is: ${msgContent.toString(CharsetUtil.UTF_8)}")
            }
        }catch (e: Exception){
            println("handler error:${e.message}")
        }
        return false

    }

    /**
     * 获取上下文对象
     */
    private fun getImContext(ctx: ChannelHandlerContext): ImContext? {
        return ctx.channel().attr(IM_CONTEXT_KEY).get()
    }

    /**
     * 移除连接
     */
    fun handlerRemoved(ctx: ChannelHandlerContext) {
        val imContext = getImContext(ctx)
        if (imContext != null) {
            imContext.disConnect()
        }
        // 检查通道是否仍然打开 如果关闭则关闭 disConnect方法中已经有关闭逻辑了 这里属于二次关闭
        if (ctx.channel() != null && ctx.channel().isOpen) {
            ctx.channel().close()
        }
    }

    fun setHandlerMap(handlerMap: ConcurrentHashMap<String, ImHandler>?){
        this.handlerMap = handlerMap
    }

    fun setBusinessInfoProvider(businessInfoProvider: BusinessInfoProvider?){
        this.businessInfoProvider = businessInfoProvider
    }

    fun setSender(sender: MsgSender){
        this.sender = sender
    }

}