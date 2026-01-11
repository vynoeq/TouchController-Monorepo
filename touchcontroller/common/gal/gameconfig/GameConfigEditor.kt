package top.fifthlight.touchcontroller.common.gal.gameconfig

import top.fifthlight.mergetools.api.ExpectFactory

interface GameConfigEditor {
    interface Editor {
        var autoJump: Boolean
    }

    fun interface Callback {
        fun invoke(editor: Editor)
    }

    fun submit(callback: Callback)

    @ExpectFactory
    interface Factory {
        fun of(): GameConfigEditor
    }
}
