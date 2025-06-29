package api

import api.enums.ConnectModeEnum
import java.util.concurrent.ConcurrentHashMap

open class AkImSetting {
    // 业务信息提供者 - 允许子类重写
    open var businessInfoProvider: BusinessInfoProvider? = null

    // 节点活动状态监听者 - 允许子类重写
    open var nodeStatusListener: NodeStatusListener? = null

    // Handler处理器集合 - 允许子类重写
    open var handlerMap: ConcurrentHashMap<String, ImHandler>? = null

    // port为Netty监听端口 默认8888
    open var port: Int = 8888

    // mode为连接模式 ConnectModeEnum中包含了websocket和tcp两种模式 默认websocket模式
    open var mode: ConnectModeEnum = ConnectModeEnum.WEBSOCKET

    // 心跳检测模式是否开启 默认不开启
    open var hearBeat: Boolean = false
    /*
    fun setBusinessInfoProvider(businessInfoProvider: BusinessInfoProvider){
        this.businessInfoProvider = businessInfoProvider
    }

    fun setNodeStatusListener(nodeStatusListener: NodeStatusListener){
        this.nodeStatusListener = nodeStatusListener
    }

    fun setHandlerMap(handlerMap: ConcurrentHashMap<String, ImHandler>){
        this.handlerMap = handlerMap
    }

    fun setPort(port: Int){
        this.port = port
    }

    fun setMode(mode: ConnectModeEnum){
        this.mode = mode
    }
    */

    companion object {
        // 获取不可修改的默认配置
        fun getDefaultAkImSetting(): AkImSetting = DefaultAkImSettingHolder.DEFAULT
    }

    // 私有内部类封装默认实现
    private class DefaultAkImSetting : AkImSetting() {
        // 初始化时设置默认值，不提供修改方法
        override var businessInfoProvider: BusinessInfoProvider? = DefaultBusinessInfoProvider()
        override var nodeStatusListener: NodeStatusListener? = DefaultNodeStatusListener()
        override var handlerMap: ConcurrentHashMap<String, ImHandler>? = DefaultImHandler()

        // 重写基础属性并设为final
        override var port: Int = 8888
        override var mode: ConnectModeEnum = ConnectModeEnum.WEBSOCKET
        override var hearBeat: Boolean = true
    }

    // 通过Holder模式确保线程安全的单例
    private object DefaultAkImSettingHolder {
        val DEFAULT: AkImSetting = DefaultAkImSetting()
    }
}