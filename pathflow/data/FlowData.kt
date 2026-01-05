package top.fifthlight.pathflow.data

import top.fifthlight.pathflow.feature.FlowFeature

interface FlowData<T: FlowData<T>> {
    val features: Set<FlowFeature<T>>
}
