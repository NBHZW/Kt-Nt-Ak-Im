package actors

import api.Dispatcher

class WebSocketDispatcher: Dispatcher() {
    init{
        this.sender = DefaultSender()
    }
}