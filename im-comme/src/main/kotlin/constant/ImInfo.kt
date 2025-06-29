package constant

class ImInfo {
    companion object{
        const val USER_PREFIX: String = "userActor_"
        
        const val ROUTE_PREFIX: String = "/zealsinger/user/"
        
        const val SEND_ROUTE: String = "SendRoute"
        
        const val REVIVE_ROUTE: String = "ReciveRoute"
        
        const val SYSTEM_NAME: String = "ZealSinger-Kt-Nt-Ak-System"

        const val CLUSTER_LISTENER: String = "clusterListener"

        const val MODE_CLUSTER: String = "cluster"
        
        const val SPLITE_CHAR: String = "#"
        
        const val CONSOLE_ACTOR_NAME: String = "consoleActor"
        
        const val COLLECT_ACTOR_NAME: String = "collectActor"
        /**
         * 路由节点默认容量
         */
        
        const val INIT_SIZE: Int = 400000
        /**
         * 心跳检测间隔 20s
         */
        
        const val HEARTBEAT_IDLE: Int = 20
        /**
         * 超时次数，超过检测次数则断开连接
         */
        
        const val CHECK_TIMES: Int = 3
        /**
         * 保留消息代码
         */
        
        const val LIMIT_CODE_MAX: Int = 100
    }
}