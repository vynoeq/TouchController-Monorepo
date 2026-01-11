package top.fifthlight.touchcontroller.common.gal.library

import top.fifthlight.mergetools.api.ExpectFactory
import java.io.InputStream
import java.nio.file.Path

interface NativeLibraryPathGetter {
    fun getNativeLibraryPath(path: String, debugPath: Path?): InputStream?

    @ExpectFactory
    interface Factory {
        fun of(): NativeLibraryPathGetter
    }
}