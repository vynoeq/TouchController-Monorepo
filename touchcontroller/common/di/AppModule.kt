package top.fifthlight.touchcontroller.common.di

import org.koin.dsl.module
import top.fifthlight.touchcontroller.common.ui.model.*

val appModule = module {
    factory { params -> ItemListScreenModel(params[0], params[1]) }
    factory { params -> ComponentScreenModel(params[0], params[1]) }
    factory { AboutScreenModel() }
    factory { params -> ManageControlPresetsTabModel(params[0]) }
    factory { params -> CustomControlLayoutTabModel(params[0]) }
    factory { params -> PresetsTabModel(params[0]) }
    factory { params -> LayersTabModel(params[0]) }
    factory { params -> ImportPresetScreenModel(params[0]) }
    factory { params -> WidgetsTabModel(params[0]) }
    factory { params -> LayerEditorScreenModel(params[0], params[1]) }
    factory { params -> LayoutEditorCustomTabModel(params[0]) }
    factory { ConfigScreenModel() }
    factory { ChatScreenModel() }
}
