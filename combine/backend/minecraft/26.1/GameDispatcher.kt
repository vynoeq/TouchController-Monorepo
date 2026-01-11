package top.fifthlight.combine.backend.minecraft_26_1

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import net.minecraft.client.Minecraft
import kotlin.coroutines.CoroutineContext

object GameDispatcher : CoroutineDispatcher() {
    private val client = Minecraft.getInstance()

    override fun isDispatchNeeded(context: CoroutineContext) = !client.isSameThread

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (client.isSameThread) {
            Dispatchers.Unconfined.dispatch(context, block)
        } else {
            client.execute(block)
        }
    }
}
