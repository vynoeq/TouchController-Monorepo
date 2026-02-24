package top.fifthlight.touchcontroller.common.control.action.provider

import top.fifthlight.mergetools.api.ExpectFactory

interface ChatScreenProvider {
    fun openChatScreen()

    @ExpectFactory
    interface Factory {
        fun of(): ChatScreenProvider
    }

    companion object : ChatScreenProvider by ChatScreenProviderFactory.of()
}
