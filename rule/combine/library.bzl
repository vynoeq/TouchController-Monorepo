"""Rules for Combine texture libraries."""

load("//rule:merge_library.bzl", "kt_merge_library")
load(":texture.bzl", "TextureLibraryInfo")

def _kt_texture_source_impl(ctx):
    texture_info = ctx.attr.dep[TextureLibraryInfo]
    output_file = ctx.actions.declare_file(ctx.attr.name + ".kt")

    args = ctx.actions.args()
    args.add("--output")
    args.add(output_file.path)
    args.add("--package")
    args.add(texture_info.package)
    args.add("--class_name")
    args.add(texture_info.class_name)
    for texture in texture_info.textures:
        if texture.background:
            args.add("--background")
        else:
            args.add("--texture")
        args.add(texture.identifier)
    for texture in texture_info.ninepatch_textures:
        args.add("--ninepatch")
        args.add(texture.identifier)

    ctx.actions.run(
        inputs = [],
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        arguments = [args],
    )

    return [DefaultInfo(files = depset([output_file]))]

_kt_texture_source = rule(
    implementation = _kt_texture_source_impl,
    attrs = {
        "dep": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/combine/kotlin"),
            cfg = "exec",
            executable = True,
        ),
    },
)

def _kt_texture_lib_impl(name, visibility, dep):
    source_lib = name + "_source"
    _kt_texture_source(
        name = source_lib,
        dep = dep,
        tags = ["manual"],
    )

    kt_merge_library(
        name = name,
        srcs = [source_lib],
        visibility = visibility,
        expect = True,
        deps = ["//combine/core/paint"],
    )

kt_texture_lib = macro(
    implementation = _kt_texture_lib_impl,
    attrs = {
        "dep": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
        ),
    },
)
