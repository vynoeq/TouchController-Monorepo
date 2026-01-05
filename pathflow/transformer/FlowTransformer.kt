package top.fifthlight.pathflow.transformer

import top.fifthlight.pathflow.data.FlowData
import top.fifthlight.pathflow.feature.FlowFeature

interface FlowTransformer<T : FlowData<T>> {
    val removeFeatures: Set<FlowFeature<T>>
    val addFeatures: Set<FlowFeature<T>>
    val requireFeatures: Set<FlowFeature<T>>

    fun process(input: T): T
}

fun <T : FlowData<T>> List<FlowTransformer<T>>.process(input: T): T =
    fold(input) { acc, transformer -> transformer.process(acc) }
