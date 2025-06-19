package model

import io.netty.util.Attribute
import io.netty.util.AttributeKey

class ImContext {
    companion object{
        val IM_SESSION_KEY = AttributeKey.valueOf<ImSession>("kt.nt.ak.im.session")
        val IM_CONTEXT_KEY = AttributeKey.valueOf<ImContext>("kt.nt.ak.im.context")
    }
}