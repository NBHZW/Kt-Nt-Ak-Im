package model

import java.io.Serializable

/**
 * 封装后的session，封装了用户，会话，主机，设备的信息
 * 可序列化，方便进行网络传输
 * 重写toString方便打印展示
 */
class ImSession(): Serializable {
    private val serialVersionUID = 1L
    private val userId:String? = null
    private val sessionId:String? = null
    private val host:String? = null
    override fun toString(): String {
        return "ImSession(userId=$userId, sessionId=$sessionId, host=$host)"
    }
}
