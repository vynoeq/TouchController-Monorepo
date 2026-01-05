package top.fifthlight.pathflow.resolvers

import top.fifthlight.pathflow.data.FlowData
import top.fifthlight.pathflow.feature.FlowFeature
import top.fifthlight.pathflow.resolver.FlowResolver
import top.fifthlight.pathflow.transformer.FlowTransformer

object NoopResolver: FlowResolver {
    override fun <T : FlowData<T>> resolve(
        inputFeatures: Set<FlowFeature<T>>,
        targetFeatures: Set<FlowFeature<T>>,
        transformers: Set<FlowTransformer<T>>,
        maxSteps: Int,
    ) = null
}
