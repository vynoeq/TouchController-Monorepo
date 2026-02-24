package top.fifthlight.touchcontroller.version_1_21_11.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import top.fifthlight.touchcontroller.common.ui.config.screen.getConfigScreen

class TouchControllerModMenuApiImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> =
        ConfigScreenFactory<Screen> { parent -> getConfigScreen(parent) as Screen }
}
