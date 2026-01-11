package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.ui.component.BorderLayout
import top.fifthlight.armorstand.ui.component.LinearLayout
import top.fifthlight.armorstand.ui.component.ResultTable
import top.fifthlight.armorstand.ui.model.DatabaseViewModel
import top.fifthlight.armorstand.ui.util.autoWidthButton
import top.fifthlight.armorstand.ui.util.textField

class DatabaseScreen(parent: Screen? = null) : ArmorStandScreen<DatabaseScreen, DatabaseViewModel>(
    parent = parent,
    viewModelFactory = ::DatabaseViewModel,
    title = Component.translatable("armorstand.debug_screen.database")
) {
    private val topBar by lazy {
        StringWidget(width, 32, title, currentMinecraft.font)
    }
    private val closeButton = Button.builder(CommonComponents.GUI_BACK) { onClose() }.build()
    private val queryInput by lazy {
        textField(
            placeHolder = Component.literal("Enter SQL…").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY),
            text = viewModel.uiState.map { it.query }.distinctUntilChanged(),
            onChanged = viewModel::updateQuery,
        )
    }
    private val executeButton by lazy {
        autoWidthButton(
            text = Component.translatable("armorstand.debug_database.execute_query"),
        ) {
            viewModel.submitQuery()
        }
    }
    private val resultTable by lazy {
        ResultTable(textRenderer = currentMinecraft.font).also {
            scope.launch {
                viewModel.uiState.collect { state ->
                    it.setContent(state.state)
                }
            }
        }
    }

    override fun init() {
        val rootLayout = BorderLayout(
            width = width,
            height = height,
            direction = BorderLayout.Direction.VERTICAL,
        )
        rootLayout.setFirstElement(topBar) { topBar, width, height -> topBar.width = width }
        rootLayout.setCenterElement(
            BorderLayout(
                width = width,
                direction = BorderLayout.Direction.VERTICAL,
            ).apply {
                setFirstElement(
                    BorderLayout(
                        width = width,
                        height = 36,
                        direction = BorderLayout.Direction.HORIZONTAL,
                    ).apply {
                        setCenterElement(
                            widget = queryInput,
                            layoutSettings = LayoutSettings.defaults().apply { padding(8) },
                        )
                        setSecondElement(
                            widget = executeButton,
                            layoutSettings = LayoutSettings.defaults().apply {
                                padding(0, 8, 8, 8)
                            },
                        )
                    },
                )
                setCenterElement(
                    widget = resultTable,
                    layoutSettings = LayoutSettings.defaults().apply { padding(8, 0, 8, 8) },
                )
            }
        )
        rootLayout.setSecondElement(
            LinearLayout(
                width = width,
                height = 32,
                direction = LinearLayout.Direction.HORIZONTAL,
                align = LinearLayout.Align.CENTER,
                gap = 8,
            ).apply {
                add(closeButton, LayoutSettings.defaults().apply { alignVerticallyMiddle() })
            }
        )
        rootLayout.arrangeElements()
        rootLayout.visitWidgets { addRenderableWidget(it) }
    }
}
