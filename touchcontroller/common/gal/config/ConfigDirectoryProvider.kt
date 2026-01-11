package top.fifthlight.touchcontroller.common.gal.config

import top.fifthlight.mergetools.api.ExpectFactory
import java.nio.file.Path

interface ConfigDirectoryProvider {
    val configDirectory: Path

    @ExpectFactory
    interface Factory {
        fun of(): ConfigDirectoryProvider
    }
}
