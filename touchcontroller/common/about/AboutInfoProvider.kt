package top.fifthlight.touchcontroller.common.about

import top.fifthlight.mergetools.api.ExpectFactory

interface AboutInfoProvider {
    val aboutInfo: AboutInfo

    @ExpectFactory
    interface Factory {
        fun of(): AboutInfoProvider
    }

    companion object : AboutInfoProvider by AboutInfoProviderFactory.of()
}
