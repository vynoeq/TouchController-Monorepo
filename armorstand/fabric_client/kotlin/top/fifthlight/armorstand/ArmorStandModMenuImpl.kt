package top.fifthlight.armorstand

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import top.fifthlight.armorstand.ui.screen.ConfigScreen

@Suppress("unused")
class ArmorStandModMenuImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory(::ConfigScreen)
}
