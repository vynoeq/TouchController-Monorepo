package top.fifthlight.pathflow.resolver

import top.fifthlight.pathflow.data.FlowData
import top.fifthlight.pathflow.feature.FlowFeature
import top.fifthlight.pathflow.transformer.FlowTransformer
import top.fifthlight.pathflow.transformer.process

interface FlowResolver {
    fun <T : FlowData<T>> resolve(
        inputFeatures: Set<FlowFeature<T>>,
        targetFeatures: Set<FlowFeature<T>>,
        transformers: Set<FlowTransformer<T>>,
        maxSteps: Int,
    ): List<FlowTransformer<T>>?
}

fun <T : FlowData<T>> FlowResolver.process(
    input: T,
    targetFeatures: Set<FlowFeature<T>>,
    transformers: Set<FlowTransformer<T>>,
    maxSteps: Int,
): T? = resolve(
    inputFeatures = input.features,
    targetFeatures = targetFeatures,
    transformers = transformers,
    maxSteps = maxSteps,
)?.process(input)
