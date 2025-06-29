package util

/**
 * MessageId生成器总接口
 * 提供了默认的ID生成策略，默认策略是支持分布式ID生成的
 *
 */
interface DistributedMsgIdConstructor {

    companion object {
        fun getId(): Long? {

            return null
        }
    }
}
