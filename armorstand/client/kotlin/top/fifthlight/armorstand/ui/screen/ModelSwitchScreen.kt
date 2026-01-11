package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.CommonColors
import top.fifthlight.armorstand.ArmorStandClient
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.armorstand.ui.component.ModelIcon
import top.fifthlight.armorstand.ui.component.Surface
import top.fifthlight.armorstand.ui.model.ModelSwitchViewModel
import top.fifthlight.armorstand.ui.state.ModelSwitchScreenState

class ModelSwitchScreen(parent: Screen? = null) : ArmorStandScreen<ModelSwitchScreen, ModelSwitchViewModel>(
    parent = parent,
    viewModelFactory = ::ModelSwitchViewModel,
    title = Component.translatable("armorstand.model_switch"),
) {

    companion object {
        private val LOADING_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "loading")
        private const val LOADING_ICON_WIDTH = 32
        private const val LOADING_ICON_HEIGHT = 32
        private const val ITEM_SIZE = 72
        private const val ICON_SIZE = 56
        private const val ICON_PADDING = (ITEM_SIZE - ICON_SIZE) / 2
        private const val ITEM_GAP = 16
        const val TOTAL_ITEMS = ModelSwitchViewModel.TOTAL_ITEMS
        private const val TOTAL_WIDTH = ITEM_SIZE * TOTAL_ITEMS + ITEM_GAP * (TOTAL_ITEMS - 1)
        private val UNSELECTED_BACKGROUND
            get() = Surface.listBackgroundWithSeparator()
        private val SELECTED_BACKGROUND
            get() = Surface.listBackgroundWithSeparator() + Surface.color(0x66FFFFFFu)
        private val MODEL_NAME_LABEL_BACKGROUND
            get() = Surface.listBackgroundWithSeparator()
        private const val MODEL_NAME_LABEL_PADDING = 16
        private const val MODEL_NAME_LABEL_WIDTH = 320
        private const val MODEL_NAME_LABEL_HEIGHT = 32
        private const val MODEL_ICON_CACHE_SIZE = 32
        private val EMPTY_MESSAGE = Component.translatable("armorstand.model_switch.empty")
        private val NO_SELECTION_MESSAGE = Component.translatable("armorstand.model_switch.no_selection")
    }

    override fun isPauseScreen() = false
    override fun renderBlurredBackground(context: GuiGraphics) {}
    override fun renderMenuBackground(context: GuiGraphics) {}

    private val modelIconCache = LinkedHashMap<Int, Pair<ModelIcon, ModelItem>>()
    private var modelIcons = listOf<Pair<ModelIcon, ModelItem>>()

    init {
        val leftModels = TOTAL_ITEMS / 2
        val rightModels = TOTAL_ITEMS - leftModels - 1
        scope.launch {
            try {
                viewModel.uiState
                    .mapNotNull {
                        when (val content = it.content) {
                            is ModelSwitchScreenState.Content.Loaded -> {
                                Pair(content.currentIndex, content.totalModels)
                            }

                            else -> null
                        }
                    }
                    .distinctUntilChanged()
                    .collect { (currentIndex, totalModels) ->
                        if (totalModels.isEmpty()) {
                            return@collect
                        }
                        val realIndices = mutableListOf<Int>()
                        modelIcons = (-leftModels..rightModels).map {
                            val realIndex = (currentIndex + it + totalModels.size) % totalModels.size
                            realIndices.add(realIndex)
                            val modelItem = totalModels[realIndex]
                            modelIconCache.getOrPut(realIndex) {
                                val icon = ModelIcon(modelItem).apply { setDimensions(ICON_SIZE, ICON_SIZE) }
                                Pair(icon, modelItem)
                            }
                        }
                        while (modelIconCache.size > MODEL_ICON_CACHE_SIZE) {
                            val lastEntry = modelIconCache.entries.last()
                            if (lastEntry.value !in modelIcons) {
                                modelIconCache.remove(lastEntry.key)
                            } else {
                                break
                            }
                        }
                    }
            } finally {
                modelIconCache.values.forEach { it.first.close() }
                modelIconCache.clear()
            }
        }
    }

    override fun tick() {
        viewModel.clientTick()
        val uiState = viewModel.uiState.value
        if (uiState.content == ModelSwitchScreenState.Content.Empty) {
            currentMinecraft.player?.displayClientMessage(EMPTY_MESSAGE, true)
            onClose()
            return
        }
        if (uiState.needToBeClosed) {
            val currentModel = modelIcons.getOrNull(TOTAL_ITEMS / 2)
            if (currentModel != null) {
                val (_, item) = currentModel
                ModelInstanceManager.addFavoriteModelPath(item.path)
                ConfigHolder.update {
                    copy(model = item.path.toString())
                }
            }
            onClose()
            return
        }
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double,
    ): Boolean {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true
        }
        return when {
            verticalAmount > 0.0 -> {
                switchModel(false)
                true
            }

            verticalAmount < 0.0 -> {
                switchModel(true)
                true
            }

            else -> false
        }
    }

    private var totalMoveDelta = 0.0
    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        val mouseHandler = currentMinecraft.mouseHandler
        totalMoveDelta += mouseHandler.accumulatedDX
        when {
            totalMoveDelta > ITEM_SIZE -> {
                switchModel(true)
                totalMoveDelta = 0.0
            }

            totalMoveDelta < -ITEM_SIZE -> {
                switchModel(false)
                totalMoveDelta = 0.0
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ArmorStandClient.modelSwitchKeyBinding.matches(keyCode, scanCode)) {
            switchModel(true)
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun switchModel(next: Boolean) {
        currentMinecraft.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F))
        viewModel.switchModel(next)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val uiState = viewModel.uiState.value
        when (uiState.content) {
            ModelSwitchScreenState.Content.Loading -> {
                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    LOADING_ICON,
                    (width - LOADING_ICON_WIDTH) / 2,
                    (height - LOADING_ICON_HEIGHT) / 2,
                    LOADING_ICON_WIDTH,
                    LOADING_ICON_HEIGHT,
                )
            }

            ModelSwitchScreenState.Content.Empty -> {}

            is ModelSwitchScreenState.Content.Loaded -> {
                val left = (width - TOTAL_WIDTH) / 2
                val top = ITEM_GAP
                for (index in 0 until TOTAL_ITEMS) {
                    val itemLeft = left + index * (ITEM_SIZE + ITEM_GAP)
                    if (index == TOTAL_ITEMS / 2) {
                        SELECTED_BACKGROUND
                    } else {
                        UNSELECTED_BACKGROUND
                    }.draw(graphics, itemLeft, top, ITEM_SIZE, ITEM_SIZE)
                    val (icon) = modelIcons.getOrNull(index) ?: continue
                    icon.setPosition(itemLeft + ICON_PADDING, top + ICON_PADDING)
                    icon.render(graphics, mouseX, mouseY, deltaTicks)
                }

                val labelLeft = (width - MODEL_NAME_LABEL_WIDTH) / 2
                val labelTop = top + ITEM_SIZE + ITEM_GAP
                MODEL_NAME_LABEL_BACKGROUND.draw(
                    graphics,
                    labelLeft,
                    labelTop,
                    MODEL_NAME_LABEL_WIDTH,
                    MODEL_NAME_LABEL_HEIGHT
                )
                val currentModel = modelIcons.getOrNull(TOTAL_ITEMS / 2)
                val labelTextLeft = labelLeft + MODEL_NAME_LABEL_PADDING
                val labelTextWidth = MODEL_NAME_LABEL_WIDTH - MODEL_NAME_LABEL_PADDING * 2
                val font = currentMinecraft.font
                if (currentModel != null) {
                    val (_, item) = currentModel
                    val text = Component.literal(item.name)
                    val textLines = font.split(text, labelTextWidth)
                    val textHeight = font.lineHeight * textLines.size
                    val textWidth = textLines.maxOf { font.width(it) }
                    var textTextTop = labelTop + (MODEL_NAME_LABEL_HEIGHT - textHeight) / 2
                    for (line in textLines) {
                        graphics.drawString(
                            font,
                            line,
                            labelTextLeft + (labelTextWidth - textWidth) / 2,
                            textTextTop,
                            CommonColors.WHITE,
                        )
                        textTextTop += font.lineHeight
                    }
                } else {
                    val text = NO_SELECTION_MESSAGE
                    val textWidth = font.width(text)
                    val textTextTop = labelTop + (MODEL_NAME_LABEL_HEIGHT - font.lineHeight) / 2
                    graphics.drawString(
                        font,
                        text,
                        labelTextLeft + (labelTextWidth - textWidth) / 2,
                        textTextTop,
                        CommonColors.WHITE,
                    )
                }
            }
        }

        super.render(graphics, mouseX, mouseY, deltaTicks)
    }
}
