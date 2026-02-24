package top.fifthlight.blazerod.render.version_1_21_8.extension

import com.mojang.blaze3d.systems.CommandEncoder
import top.fifthlight.blazerod.render.version_1_21_8.systems.ComputePass
import java.util.function.Supplier

fun CommandEncoder.createComputePass(label: Supplier<String>): ComputePass =
    (this as CommandEncoderExt).`blazerod$createComputePass`(label)

fun CommandEncoder.memoryBarrier(barriers: Int) =
    (this as CommandEncoderExt).`blazerod$memoryBarrier`(barriers)
