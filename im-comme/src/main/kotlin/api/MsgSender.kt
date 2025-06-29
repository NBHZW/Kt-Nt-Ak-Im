package api

import model.ImContext
import model.ImSession

interface MsgSender {
    fun sendPointMsg(msgId: Long, session: ImSession, message: String, msgType: Int, to: String)

    fun sendGroupMessage(msgId: Long, session: ImSession, message: String, msgType: Int, groupId: String)

    fun disConnect(session: ImSession)

    fun connect(session: ImSession, ctx: ImContext?)

    fun setInfoProvider(provider: BusinessInfoProvider)

    fun writeMsg(context: ImContext, msgType: Int, msg: String)
}