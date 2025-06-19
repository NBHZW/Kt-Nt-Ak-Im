package api

import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import thread.PostThreadFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 分发器 专门将请求分发给对应的Handler
 * handlerMap 保存 key-handler 的关系 根据Key分配给对应的Handler
 * postMsgExecutor 专门处理postMessage过程的线程池 主要是生产线程用于异步处理onMessage之后即postMessage()中的逻辑的处理 使其成为异步操作 提高效率
 */
abstract class Dispatcher {
    private val handlerMap: ConcurrentHashMap<String, ImHandler>  = ConcurrentHashMap()
    private val businessInfoProvider: BusinessInfoProvider? = null
    private val postMsgExecutor = ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors()*2,30L,
        TimeUnit.SECONDS,
        LinkedBlockingDeque(1024),
        {Thread(it).apply {
            name = "im-postMsg-thread-${System.nanoTime()}"
            }
        }
    )


    fun handle(msgType: Int, msgContent: ByteArray, ctx: ChannelHandlerContext):Boolean{
        try{
            val handler = handlerMap.get(msgType.toString())
            if(handler!=null){
                val msgId: Long =
                handler.preMessage(msgType, msgContent,msgId)
            }else{
                println("no handler for msgType:$msgType and now message content is: ${msgContent.toString(CharsetUtil.UTF_8)}")
            }
        }catch (e: Exception){
            println("handler error:${e.message}")
        }
        return false

    }

}