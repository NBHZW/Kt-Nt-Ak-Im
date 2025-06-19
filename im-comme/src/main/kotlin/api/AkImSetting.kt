package api

import api.enums.ConnectModeEnum

class AkImSetting {
    // 业务信息提供者
    private val businessInfoProvider:BusinessInfoProvider? = null

    // 节点活动状态监听者
    private val nodeStatusListener:NodeStatusListener? = null

    // Netty服务监听端口 默认为8888
    val port:Int = 8888

    // 连接模式 TCP or WebSocket 默认为websocket
    val mode = ConnectModeEnum.WEBSOCKET
}