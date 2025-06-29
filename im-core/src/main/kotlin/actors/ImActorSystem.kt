package actors

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.SmallestMailboxPool
import api.NodeStatusListener
import com.typesafe.config.Config
import constant.ImInfo.Companion.CLUSTER_LISTENER
import constant.ImInfo.Companion.CONSOLE_ACTOR_NAME
import constant.ImInfo.Companion.INIT_SIZE
import constant.ImInfo.Companion.MODE_CLUSTER
import constant.ImInfo.Companion.REVIVE_ROUTE
import constant.ImInfo.Companion.SEND_ROUTE
import constant.ImInfo.Companion.SYSTEM_NAME
import constant.ImInfo.Companion.USER_PREFIX
import model.ImSession
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.toKotlinUuid

class ImActorSystem private constructor() {
    companion object{
        private val systemInstance: ImActorSystem = ImActorSystem()
        fun getInstance(): ImActorSystem{
            return systemInstance
        }

        /**
         * 用户路由表，发送消息时用
         */
        var userRouteMap: ConcurrentHashMap<String, RouteNode> = ConcurrentHashMap(INIT_SIZE)
    }

    /**
     * 系统Actor对象
     */
    private var systemActor: ActorSystem? = null

    /**
     * 连接用户缓存
     * userId - <sessionId,actor> 的键值对关系
     */
    private val userCacheMap: ConcurrentHashMap<String, MutableMap<String,Connector>> = ConcurrentHashMap()

    var sendRoute: ActorRef? = null
    var receiveRoute: ActorRef? = null
    var consoleActor: ActorRef? = null
    private val POOL_SIZE = 6
    private val taskTimes: Timer? = null
    private var selfAddr: String? = null

    // 系统节点session，发送系统级消息用
    private var nodeSysSession: ImSession? = null
    private var nodeSysActor: ActorRef? = null



    fun getSystemActor(): ActorSystem?{
        return systemActor
    }

    fun init(config: Config, listener: NodeStatusListener){
        // 启动系统Actor
        systemActor = ActorSystem.create(SYSTEM_NAME,config)
        // 启动AKKA集群监听Actor
        systemActor?.actorOf(Props.create(ImClusterListener::class.java,listener),CLUSTER_LISTENER)
        sendRoute = actorSystem.actorOf(SmallestMailboxPool(POOL_SIZE).props(Props.create(SendRoute::class.java)), SEND_ROUTE)
        reciveRoute = actorSystem.actorOf(
            SmallestMailboxPool(POOL_SIZE).props(Props.create(ReceiveRoute::class.java)),
            REVIVE_ROUTE
        )
        consoleActor = actorSystem.actorOf(Props.create(ConsoleActor::class.java), CONSOLE_ACTOR_NAME)
        // 读取配置中的mode模式进行不同的初始化操作
        val mode = config.getConfig("akka").getConfig("node").getString("mode")
        //如果是集群模式，链接zookeeper
        if (MODE_CLUSTER == mode) {
            val configConnector: ConfigConnector = ConfigConnector()
            configConnector.init(config.getConfig("akka"), null)
        } else {
            val nodes: MutableList<String?> = ArrayList<String?>()
            nodes.add("akka://${SYSTEM_NAME}")
            ServerNodeManager.refreshServers(nodes)
        }
        val id = UUID.randomUUID().toString()
        nodeSysSession = ImSession(systemActor?.name()+id,id,"system")
        nodeSysActor = createActorBySession(nodeSysSession)
    }

    fun createActorBySession(nodeSysSession: ImSession?): ActorRef? {
        val name = USER_PREFIX + nodeSysSession?.getUserId() + "_" + nodeSysSession?.getSessionId()
        return systemActor?.actorOf(Props.create(UserActor::class.java,nodeSysSession),name)
    }
}