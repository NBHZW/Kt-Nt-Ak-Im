package api

/**
 * Friend即好友
 * Group即群组
 * 从继承关系上来讲  Friend 和 Group 都属于是 Chat
 */
interface BusinessInfoProvider {
    // 获取好友列表
    fun getFriendList(): List<String>{
        return listOf("testUser1","testUser2","testUser3","testUser4")
    }

    // 获取群聊列表
    fun getGroupList(): List<String>{
        return listOf("testGroup1","testGroup2","testGroup3","testGroup4")
    }

    // 获取当前聊天列表
    fun getChatList(): List<String>{
        return listOf("testChat1","testChat2","testChat3","testChat4")
    }
}