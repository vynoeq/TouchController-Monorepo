package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.tabs.TabManager
import net.minecraft.client.gui.components.tabs.TabNavigationBar
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import top.fifthlight.armorstand.ArmorStandClient
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.manage.ModelManager
import top.fifthlight.armorstand.ui.component.*
import top.fifthlight.armorstand.ui.model.ConfigViewModel
import top.fifthlight.armorstand.ui.util.autoWidthButton
import top.fifthlight.armorstand.ui.util.checkbox
import top.fifthlight.armorstand.ui.util.slider
import top.fifthlight.armorstand.ui.util.textField
import top.fifthlight.armorstand.util.ceilDiv

class ConfigScreen(parent: Screen? = null) : ArmorStandScreen<ConfigScreen, ConfigViewModel>(
    parent = parent,
    viewModelFactory = ::ConfigViewModel,
    title = Component.translatable("armorstand.config"),
) {
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_1 && hasControlDown() && ArmorStandClient.instance.debug) {
            minecraft?.setScreen(DebugScreen(this))
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private val topBar by lazy {
        StringWidget(width, 32, title, currentMinecraft.font)
    }

    private val closeButton = Button.builder(CommonComponents.GUI_BACK) { onClose() }.build()

    private val openModelDirectoryButton =
        Button.builder(Component.translatable("armorstand.config.open_model_directory")) {
            viewModel.openModelDir()
        }.build()

    private val sortButton = run {
        fun sortText(order: ModelManager.Order, ascend: Boolean): String {
            val order = when (order) {
                ModelManager.Order.NAME -> "name"
                ModelManager.Order.LAST_CHANGED -> "last_changed"
            }
            val sort = when (ascend) {
                true -> "asc"
                false -> "desc"
            }
            return "armorstand.config.sort.$order.$sort"
        }

        Button.builder(Component.translatable("armorstand.config.sort.name.asc")) {
            val (order, ascend) = viewModel.uiState.value.let { Pair(it.order, it.sortAscend) }
            if (ascend) {
                // switch ascend
                viewModel.updateSearchParam(order, false)
            } else {
                // next order
                val index = (ModelManager.Order.entries.indexOf(order) + 1) % ModelManager.Order.entries.size
                val newOrder = ModelManager.Order.entries[index]
                viewModel.updateSearchParam(newOrder, true)
            }
        }.size(100, 20).build().also { button ->
            scope.launch {
                viewModel.uiState.collect { state ->
                    button.message = Component.translatable(sortText(state.order, state.sortAscend))
                }
            }
        }
    }

    private val refreshButton by lazy {
        autoWidthButton(Component.translatable("armorstand.config.refresh")) {
            viewModel.refreshModels()
        }
    }

    private val clearButton by lazy {
        autoWidthButton(Component.translatable("armorstand.config.clear")) {
            viewModel.selectModel(null)
        }.also { button ->
            scope.launch {
                ConfigHolder.config.collect { config ->
                    button.active = config.model != null
                }
            }
        }
    }

    private val searchBox by lazy {
        textField(
            placeHolder = Component.translatable("armorstand.config.search_placeholder")
                .withStyle(ChatFormatting.ITALIC)
                .withStyle(ChatFormatting.GRAY),
            text = viewModel.uiState.map { it.searchString }.distinctUntilChanged(),
            onChanged = viewModel::updateSearchString,
        )
    }

    private val pager by lazy {
        PagingWidget(
            textRenderer = font,
            currentPage = viewModel.uiState.value.currentOffset,
            totalPages = viewModel.uiState.value.totalItems,
            onPrevPage = {
                viewModel.updatePageIndex(-1)
            },
            onNextPage = {
                viewModel.updatePageIndex(1)
            },
        ).also { pager ->
            scope.launch {
                viewModel.uiState.map { state ->
                    Triple(state.currentOffset, state.totalItems, state.pageSize)
                }.distinctUntilChanged().collect { (currentOffset, totalItems, pageSize) ->
                    pageSize?.let { pageSize ->
                        pager.currentPage = (currentOffset / pageSize) + 1
                        pager.totalPages = totalItems ceilDiv pageSize
                        pager.refresh()
                    }
                }
            }
        }
    }

    private val modelGrid by lazy {
        AutoHeightGridLayout(
            cellWidth = 64,
            cellHeightRange = 60..80,
            padding = Insets(horizonal = 8),
            verticalGap = 8,
        ).also { grid ->
            scope.launch {
                viewModel.uiState.map { it.currentPageItems }.distinctUntilChanged().collect { items ->
                    grid.visitWidgets {
                        if (it is AutoCloseable) {
                            it.close()
                        }
                        removeWidget(it)
                    }
                    grid.clear()
                    items?.let {
                        for (item in items) {
                            val button = ModelButton(
                                modelItem = item,
                                font = font,
                                padding = Insets(8),
                                onPressAction = {
                                    viewModel.selectModel(it.path)
                                },
                                onFavoriteAction = {
                                    viewModel.setFavoriteModel(it.path, !it.favorite)
                                },
                            )
                            grid.add(button)
                        }
                        grid.arrangeElements()
                        grid.visitWidgets { addRenderableWidget(it) }
                    }
                }
            }
        }
    }

    private val loadingOverlay by lazy {
        LoadingOverlay(modelGrid).also { overlay ->
            scope.launch {
                viewModel.uiState.collect { state ->
                    overlay.loading = state.currentPageItems == null
                }
            }
        }
    }

    private val sendModelDataButton by lazy {
        checkbox(
            text = Component.translatable("armorstand.config.send_model_data"),
            value = viewModel.uiState.map { it.sendModelData },
            onValueChanged = viewModel::updateSendModelData,
        )
    }

    private val hidePlayerShadowButton by lazy {
        checkbox(
            text = Component.translatable("armorstand.config.hide_player_shadow"),
            value = viewModel.uiState.map { it.hidePlayerShadow },
            onValueChanged = viewModel::updateHidePlayerShadow,
        )
    }

    private val hidePlayerArmorButton by lazy {
        checkbox(
            text = Component.translatable("armorstand.config.hide_player_armor"),
            value = viewModel.uiState.map { it.hidePlayerArmor },
            onValueChanged = viewModel::updateHidePlayerArmor,
        )
    }

    private val showOtherPlayersButton by lazy {
        checkbox(
            text = Component.translatable("armorstand.config.show_other_players"),
            value = viewModel.uiState.map { it.showOtherPlayerModel },
            onValueChanged = viewModel::updateShowOtherPlayerModel,
        )
    }

    private val modelScaleSlider by lazy {
        slider(
            textFactory = { slider, text -> Component.translatable("armorstand.config.model_scale", text) },
            min = 0.0,
            max = 4.0,
            value = viewModel.uiState.map { it.modelScale.toDouble() },
            onValueChanged = { userTriggered, value ->
                viewModel.updateModelScale(value.toFloat())
            },
        )
    }

    private val thirdPersonDistanceScaleSlider = slider(
        textFactory = { slider, text -> Component.translatable("armorstand.config.third_person_distance_scale", text) },
        min = 0.05,
        max = 2.0,
        value = viewModel.uiState.map { it.thirdPersonDistanceScale.toDouble() },
        onValueChanged = { userTriggered, value ->
            viewModel.updateThirdPersonDistanceScale(value.toFloat())
        },
    )

    private val rendererSelectButton = Button.builder(Component.translatable("armorstand.config.renderer_select")) {
        currentMinecraft.setScreen(RendererSelectScreen(this))
    }.build()

    private val vmcButton = Button.builder(Component.translatable("armorstand.config.vmc")) {
        currentMinecraft.setScreen(VmcConfigScreen(this))
    }.build()

    private val previewTab = LayoutScreenTab(
        title = Component.translatable("armorstand.config.tab.preview"),
        padding = Insets(8),
    ) {
        BorderLayout(
            direction = BorderLayout.Direction.VERTICAL,
        ).apply {
            setCenterElement(
                ModelWidget(
                    surface = Surface.color(0xFF383838u) + Surface.border(0xFF161616u)
                ),
            )
            val gap = 8
            val padding = 8
            setSecondElement(
                LinearLayout(
                    direction = LinearLayout.Direction.VERTICAL,
                    padding = Insets(top = padding),
                    gap = gap,
                ).apply {
                    listOf(
                        modelScaleSlider,
                    ).forEach {
                        add(
                            it,
                            expand = true
                        )
                    }
                    pack()
                }
            )
        }
    }

    private val settingsTab = LayoutScreenTab(
        title = Component.translatable("armorstand.config.tab.settings"),
        padding = Insets(8),
    ) {
        BorderLayout(
            direction = BorderLayout.Direction.VERTICAL,
        ).apply {
            setCenterElement(
                LinearLayout(
                    direction = LinearLayout.Direction.VERTICAL,
                    padding = Insets(top = 8),
                    gap = 8,
                ).apply {
                    listOf(
                        rendererSelectButton,
                        vmcButton,
                        sendModelDataButton,
                        showOtherPlayersButton,
                        hidePlayerShadowButton,
                        hidePlayerArmorButton,
                        thirdPersonDistanceScaleSlider,
                    ).forEach {
                        add(
                            it,
                            expand = true
                        )
                    }
                }
            )
        }
    }

    private val metadataTab = LayoutScreenTab(
        title = Component.translatable("armorstand.config.tab.metadata"),
        padding = Insets(8),
    ) {
        BorderLayout().apply {
            setCenterElement(
                MetadataWidget(
                    minecraft = currentMinecraft,
                    textClickHandler = ::handleComponentClicked,
                ).also {
                    scope.launch {
                        viewModel.uiState
                            .map { state -> state.currentMetadata }
                            .distinctUntilChanged()
                            .collect { metadata ->
                                it.metadata = metadata
                            }
                    }
                }, LayoutSettings.defaults().padding(8)
            )
        }
    }

    private val tabManager = TabManager(
        { addRenderableWidget(it) },
        { removeWidget(it) },
    )

    private var tabNavigationLayoutElement = TabNavigationBar.builder(tabManager, width)
        .addTabs(previewTab, settingsTab, metadataTab)
        .build()

    private var initialized = false
    override fun init() {
        val layout = BorderLayout(
            width = width,
            height = height,
            direction = BorderLayout.Direction.VERTICAL,
        )
        layout.setFirstElement(topBar) { topBar, width, height -> topBar.width = width }
        val padding = 8
        val gap = 8
        layout.setCenterElement(
            SpreadLayout(
                width = width,
                height = height - 32 * 2,
                padding = Insets(horizonal = padding),
                gap = gap,
            ).apply {
                add(
                    widget = BorderLayout(
                        direction = BorderLayout.Direction.VERTICAL,
                        surface = Surface.listBackgroundWithSeparator(),
                    ).apply {
                        setFirstElement(
                            LinearLayout(
                                direction = LinearLayout.Direction.VERTICAL,
                                padding = Insets(padding),
                                gap = gap,
                            ).apply {
                                val toolbarActions = listOf(
                                    sortButton,
                                    refreshButton,
                                    clearButton,
                                )
                                if (this@ConfigScreen.width >= 700) {
                                    add(
                                        widget = BorderLayout(
                                            height = 20,
                                            direction = BorderLayout.Direction.HORIZONTAL,
                                        ).apply {
                                            setCenterElement(searchBox)
                                            setSecondElement(
                                                LinearLayout(
                                                    direction = LinearLayout.Direction.HORIZONTAL,
                                                    align = LinearLayout.Align.END,
                                                    height = 20,
                                                    gap = gap,
                                                    padding = Insets(left = padding),
                                                ).apply {
                                                    toolbarActions.forEach {
                                                        add(it, LayoutSettings.defaults().apply { alignVerticallyMiddle() })
                                                    }
                                                    pack()
                                                }
                                            )
                                        },
                                        expand = true,
                                    )
                                } else {
                                    add(searchBox, expand = true)
                                    add(
                                        widget = LinearLayout(
                                            direction = LinearLayout.Direction.HORIZONTAL,
                                            height = 20,
                                            align = LinearLayout.Align.END,
                                            gap = gap,
                                        ).apply {
                                            toolbarActions.forEach {
                                                add(it, LayoutSettings.defaults().apply { alignVerticallyMiddle() })
                                            }
                                        },
                                        expand = true,
                                    )
                                }
                                pack()
                            }
                        )
                        setCenterElement(loadingOverlay)
                        setSecondElement(pager, LayoutSettings.defaults().padding(padding))
                        addRenderableOnly(this)
                        addRenderableOnly(loadingOverlay)
                    },
                    weight = 2
                )
                add(
                    TabNavigationWrapper(
                        tabManager = tabManager,
                        inner = tabNavigationLayoutElement,
                        surface = Surface.combine(
                            Surface.padding(Insets(bottom = 2), Surface.listBackground()),
                            Surface.footerSeparator(),
                        ),
                    ).also {
                        addRenderableWidget(it)
                    }
                )
            }
        )
        layout.setSecondElement(
            LinearLayout(
                width = width,
                height = 32,
                direction = LinearLayout.Direction.HORIZONTAL,
                align = LinearLayout.Align.CENTER,
                gap = gap,
            ).apply {
                add(openModelDirectoryButton, LayoutSettings.defaults().apply { alignVerticallyMiddle() })
                add(closeButton, LayoutSettings.defaults().apply { alignVerticallyMiddle() })
            }
        )

        layout.arrangeElements()

        val (rows, columns) = modelGrid.calculateSize()
        viewModel.updatePageSize((rows * columns).takeIf { it > 0 })
        modelGrid.visitWidgets { removeWidget(it) }

        layout.visitWidgets { addRenderableWidget(it) }

        pager.init()
        addRenderableWidget(pager)
        if (!initialized) {
            initialized = true
            tabNavigationLayoutElement.selectTab(0, false)
        } else {
            tabManager.currentTab?.visitChildren { addRenderableWidget(it) }
        }
    }

    override fun tick() {
        viewModel.tick()
    }

    override fun onClose() {
        modelGrid.visitWidgets {
            if (it is AutoCloseable) {
                it.close()
            }
        }
        super.onClose()
    }
}
