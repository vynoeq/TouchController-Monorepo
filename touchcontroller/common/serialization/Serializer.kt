package top.fifthlight.touchcontroller.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val jsonFormat = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
    allowTrailingComma = true
    prettyPrint = true
    prettyPrintIndent = "  "
    isLenient = true
}
