package top.fifthlight.combine.backend.minecraft.identifier.resourcelocation

import net.minecraft.resources.ResourceLocation
import top.fifthlight.combine.data.Identifier as CombineIdentifier

fun CombineIdentifier.toMinecraft(): ResourceLocation = when (this) {
    is CombineIdentifier.Vanilla -> ResourceLocation.withDefaultNamespace(id)
    is CombineIdentifier.Namespaced -> ResourceLocation.fromNamespaceAndPath(namespace, id)
}

fun ResourceLocation.toCombine() = if (this.namespace == "minecraft") {
    CombineIdentifier.Vanilla(path)
} else {
    CombineIdentifier.Namespaced(namespace, path)
}