package api

interface NodeStatusListener {
    // 节点上线的时候触发
    fun onMemberAdded(){

    }

    // 节点下线的时候触发
    fun onMemberRemoved(){
    }
}
