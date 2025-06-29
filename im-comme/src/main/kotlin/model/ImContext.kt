package model

import api.MsgSender
import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey

/**
 *  封装和IM会话相关的信息
 *  包括当前连接的[ChannelHandlerContext],当前连接的Session信息[ImSession]
 *  消息发送器[MsgSender] 和 心跳次数管理
 *
 *  主要作用
 *  维护状态和利用消息发送器发送单点消息和群聊消息
 */
class ImContext {
    private var heartNumber: Int = 0;
    private var sender: MsgSender? = null
    private var channelHandlerContext: ChannelHandlerContext? = null

    companion object{
        val IM_SESSION_KEY = AttributeKey.valueOf<ImSession>("kt.nt.ak.im.session")
        val IM_CONTEXT_KEY = AttributeKey.valueOf<ImContext>("kt.nt.ak.im.context")
    }


    constructor(sender: MsgSender? , ctx: ChannelHandlerContext){
        this.sender = sender
        this.channelHandlerContext = ctx
    }

    fun getSession(): ImSession? {
        return channelHandlerContext!!.channel().attr<ImSession?>(IM_SESSION_KEY).get()
    }

    fun heartNum(): Int{
        return heartNumber++
    }

    fun resetHeartNum() {
        heartNumber = 0;
    }

    fun disConnect(){
        val session = getSession()
        if(session!=null){
            sender?.disConnect(session)
        }
        channelHandlerContext?.channel()?.close()
    }
}