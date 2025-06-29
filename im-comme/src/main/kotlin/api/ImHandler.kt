package api

import model.ImContext
import java.io.Serializable

interface ImHandler {
    /**
     * 发消息前的调用
     * 返回是否执行成功
     * 如果返回false即代表执行失败 后续的过程也不会执行
     */
    fun preMessage(msgType: Int, message: ByteArray, msgId:Long): Boolean

    /**
     * 发消息
     */
    fun onMessage(ctx: ImContext, msgType: Int, message: ByteArray, msgId:Long): Serializable

    /**
     * 发消息后的调用
     */
    fun postMessage(ctx: ImContext, msgType: Int, message: ByteArray, msgId:Long, obj: Serializable): Boolean
}