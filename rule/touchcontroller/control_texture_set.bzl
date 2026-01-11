"""Rules for generating control texture sets for TouchController."""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load("//rule:merge_library.bzl", "kt_merge_library")
load("//rule/combine:texture.bzl", "TextureGroupInfo", "TextureInfo", "TextureLibraryInfo", "generate_texture", "merge_texture_group_info", "path_to_identifier")

ControlTextureSetInfo = provider(
    doc = "Information about a control texture set.",
    fields = ["id", "textures", "metadata", "files"],
)
ControlTextureSetGroupInfo = provider(
    doc = "Information about a controltexture set group.",
    fields = ["sets"],
)

def _texture_set_impl(ctx):
    strip_prefix = ctx.attr.strip_prefix
    if strip_prefix == ".":
        strip_prefix = ctx.label.package

    output_textures = []
    output_files = []
    texture_entries = {}
    for src in ctx.files.srcs:
        metadata_file, texture_file = generate_texture(ctx.actions, ctx.executable._texture_generator, src)
        output_files.append(texture_file)
        output_files.append(metadata_file)

        if not src.short_path.startswith(strip_prefix):
            fail("Bad strip_prefix: want to strip %s from %s" % (strip_prefix, src.short_path))
        identifier = ctx.attr.id.lower() + "_" + path_to_identifier(src.short_path.removeprefix(strip_prefix))

        texture_info = TextureInfo(
            identifier = identifier,
            metadata = metadata_file,
            texture = texture_file,
            background = False,
        )

        texture_entry_id = src.basename.split(".")[0]
        if texture_entry_id in texture_entries:
            fail("Texture set %s is already existing" % texture_entry_id)
        texture_entries[texture_entry_id] = texture_info

        output_textures.append(texture_info)

    texture_files = depset(output_files)
    total_files = depset([ctx.file.metadata], transitive = [texture_files])
    return [
        TextureGroupInfo(
            textures = output_textures,
            ninepatch_textures = [],
            files = texture_files,
        ),
        ControlTextureSetInfo(
            id = ctx.attr.id,
            textures = texture_entries,
            metadata = ctx.file.metadata,
            files = total_files,
        ),
        DefaultInfo(files = total_files),
    ]

control_texture_set = rule(
    implementation = _texture_set_impl,
    attrs = {
        "id": attr.string(
            mandatory = True,
        ),
        "srcs": attr.label_list(
            mandatory = True,
            doc = "Input files. Must be .png files",
            allow_files = [".png"],
        ),
        "metadata": attr.label(
            mandatory = True,
            doc = "Metadata json",
            allow_single_file = [".json"],
        ),
        "strip_prefix": attr.string(
            mandatory = False,
            default = ".",
        ),
        "_texture_generator": attr.label(
            default = "//rule/combine/metadata/generator",
            executable = True,
            cfg = "exec",
        ),
    },
)

def _control_texture_group_impl(ctx):
    sets = [dep[ControlTextureSetInfo] for dep in ctx.attr.deps]
    return [
        ControlTextureSetGroupInfo(sets = sets),
        merge_texture_group_info([dep[TextureGroupInfo] for dep in ctx.attr.deps]),
        DefaultInfo(files = depset(transitive = [set.files for set in sets])),
    ]

control_texture_group = rule(
    implementation = _control_texture_group_impl,
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
            providers = [ControlTextureSetInfo, TextureGroupInfo],
        ),
    },
)

def _kt_control_texture_set_source_impl(ctx):
    texture_lib_info = ctx.attr.texture_lib[TextureLibraryInfo]
    control_group_info = ctx.attr.control_group[ControlTextureSetGroupInfo]

    output_file = ctx.actions.declare_file(ctx.attr.name + ".kt")

    args = ctx.actions.args()
    args.add("--output")
    args.add(output_file.path)
    args.add("--package")
    args.add(ctx.attr.package)
    args.add("--class_name")
    args.add(ctx.attr.class_name)
    args.add("--texture_package")
    args.add(texture_lib_info.package)
    args.add("--texture_class")
    args.add(texture_lib_info.class_name)
    args.add("--text_package")
    args.add(ctx.attr.text_binding_package)
    args.add("--text_class")
    args.add(ctx.attr.text_binding_class_name)

    for set_info in control_group_info.sets:
        args.add("--set")
        args.add(set_info.id)
        args.add(set_info.metadata.path)

        for texture_name, texture_info in set_info.textures.items():
            args.add("--texture")
            args.add("%s:%s" % (texture_name, texture_info.identifier))

    control_files = depset(transitive = [set_info.files for set_info in control_group_info.sets])
    input_files = depset(
        direct = texture_lib_info.files.to_list(),
        transitive = [control_files],
    )

    ctx.actions.run(
        inputs = input_files,
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        arguments = [args],
        progress_message = "Generating TextureSet.kt for %s.%s" % (ctx.attr.package, ctx.attr.class_name),
        mnemonic = "TextureSetGen",
    )

    return [DefaultInfo(files = depset([output_file]))]

_kt_control_texture_set_source = rule(
    implementation = _kt_control_texture_set_source_impl,
    attrs = {
        "texture_lib": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
            doc = "TextureLibraryInfo containing Textures class reference",
        ),
        "control_group": attr.label(
            providers = [ControlTextureSetGroupInfo],
            mandatory = True,
            doc = "ControlTextureSetGroupInfo containing texture set definitions",
        ),
        "package": attr.string(
            mandatory = True,
            doc = "Package name for generated TextureSet class",
        ),
        "class_name": attr.string(
            mandatory = True,
            doc = "Class name for generated TextureSet class",
        ),
        "text_binding_package": attr.string(
            mandatory = True,
        ),
        "text_binding_class_name": attr.string(
            mandatory = True,
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/touchcontroller/texture_set_generator"),
            executable = True,
            cfg = "exec",
        ),
    },
)

def _kt_control_texture_set_lib_impl(
        name,
        visibility,
        texture_lib,
        kt_texture_lib,
        kt_text_binding_lib,
        control_group,
        text_binding_package,
        text_binding_class_name,
        package,
        class_name):
    source_lib = name + "_source"
    _kt_control_texture_set_source(
        name = source_lib,
        texture_lib = texture_lib,
        control_group = control_group,
        text_binding_package = text_binding_package,
        text_binding_class_name = text_binding_class_name,
        package = package,
        class_name = class_name,
        tags = ["manual"],
    )

    kt_merge_library(
        name = name,
        srcs = [source_lib],
        visibility = visibility,
        merge_deps = [
            kt_texture_lib,
            kt_text_binding_lib,
        ],
        deps = [
            "//:kotlin_serialization",
            "//combine/core/data",
            "//combine/core/paint",
        ],
    )

kt_control_texture_set_lib = macro(
    implementation = _kt_control_texture_set_lib_impl,
    attrs = {
        "texture_lib": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
            configurable = False,
        ),
        "kt_texture_lib": attr.label(
            providers = [JavaInfo],
            mandatory = True,
            configurable = False,
        ),
        "kt_text_binding_lib": attr.label(
            providers = [JavaInfo],
            mandatory = True,
            configurable = False,
        ),
        "control_group": attr.label(
            providers = [ControlTextureSetGroupInfo],
            mandatory = True,
            configurable = False,
        ),
        "text_binding_package": attr.string(
            mandatory = True,
        ),
        "text_binding_class_name": attr.string(
            mandatory = True,
        ),
        "package": attr.string(
            mandatory = True,
        ),
        "class_name": attr.string(
            mandatory = False,
            default = "TextureSet",
        ),
    },
)
