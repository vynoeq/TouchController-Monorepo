"""Rules for texture generation and processing."""

TextureInfo = provider(
    doc = "Information about a texture.",
    fields = ["identifier", "metadata", "texture", "background"],
)
NinePatchTextureInfo = provider(
    doc = "Information about a nine-patch texture.",
    fields = ["identifier", "metadata", "texture"],
)
TextureGroupInfo = provider(
    doc = "Information about a texture group.",
    fields = ["textures", "ninepatch_textures", "files"],
)

def path_to_identifier(path):
    # Strip leading "/" if present
    path = path.removeprefix("/")

    # Remove extension
    path = path.split(".")[0]

    # Replace all non-alphanumeric characters with underscores
    path = "".join([path[index] if path[index].isalnum() else "_" for index in range(len(path))])

    # Make all letters lowercase
    return path.lower()

def generate_ninepatch_texture(actions, texture_generator, src):
    """Generate metadata and compress a nine-patch texture.

    Args:
        actions: The action factory for declaring output files.
        texture_generator: The executable tool for texture generation.
        src: The source image file.

    Returns:
        A tuple of (metadata_file, compressed_file).
    """
    input_filename = src.basename.split(".")[0]
    metadata_file = actions.declare_file("%s.json" % input_filename, sibling = src)
    compressed_file = actions.declare_file("%s.compressed.9.png" % input_filename, sibling = src)

    args = actions.args()
    args.add("ninepatch")
    args.add(src)
    args.add(metadata_file)
    args.add(compressed_file)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    actions.run(
        inputs = [src],
        outputs = [compressed_file, metadata_file],
        executable = texture_generator,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        mnemonic = "CombineNinepatchTexture",
    )

    return metadata_file, compressed_file

def generate_texture(actions, texture_generator, src, background = False):
    """Generate metadata of a texture.

    Args:
        actions: The action factory for declaring output files.
        texture_generator: The executable tool for texture generation.
        src: The source image file.
        background: Whether this is a background texture (default: False).

    Returns:
        The metadata file for the generated texture.
    """
    input_filename = src.basename.split(".")[0]
    metadata_file = actions.declare_file("%s.json" % input_filename, sibling = src)

    args = actions.args()
    args.add("texture")
    if background:
        args.add("--background")
    args.add(src)
    args.add(metadata_file)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    actions.run(
        inputs = [src],
        outputs = [metadata_file],
        executable = texture_generator,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        mnemonic = "CombineTexture",
    )

    return metadata_file, src

def _ninepatch_texture_impl(ctx):
    strip_prefix = ctx.attr.strip_prefix
    if strip_prefix == ".":
        strip_prefix = ctx.label.package

    output_textures = []
    output_files = []
    for src in ctx.files.srcs:
        metadata_file, compressed_file = generate_ninepatch_texture(ctx.actions, ctx.executable._texture_generator, src)
        output_files.append(metadata_file)
        output_files.append(compressed_file)

        if not src.short_path.startswith(strip_prefix):
            fail("Bad strip_prefix: want to strip %s from %s" % (strip_prefix, src.short_path))

        identifier = path_to_identifier(src.short_path.removeprefix(strip_prefix))
        output_textures.append(NinePatchTextureInfo(
            identifier = identifier,
            metadata = metadata_file,
            texture = compressed_file,
        ))

    files = depset(output_files)
    return [TextureGroupInfo(
        textures = [],
        ninepatch_textures = output_textures,
        files = files,
    ), DefaultInfo(files = files)]

ninepatch_texture = rule(
    implementation = _ninepatch_texture_impl,
    provides = [TextureGroupInfo],
    attrs = {
        "srcs": attr.label_list(
            mandatory = True,
            doc = "Input files. Must be .9.png files",
            allow_files = [".9.png"],
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

def _texture_impl(ctx):
    strip_prefix = ctx.attr.strip_prefix
    if strip_prefix == ".":
        strip_prefix = ctx.label.package

    output_textures = []
    output_files = []
    for src in ctx.files.srcs:
        background = ctx.attr.background
        metadata_file, texture_file = generate_texture(ctx.actions, ctx.executable._texture_generator, src, background)
        output_files.append(texture_file)
        output_files.append(metadata_file)

        if not src.short_path.startswith(strip_prefix):
            fail("Bad strip_prefix: want to strip %s from %s" % (strip_prefix, src.short_path))
        identifier = path_to_identifier(src.short_path.removeprefix(strip_prefix))

        output_textures.append(TextureInfo(
            identifier = identifier,
            metadata = metadata_file,
            texture = texture_file,
            background = background,
        ))

    files = depset(output_files)
    return [TextureGroupInfo(
        textures = output_textures,
        ninepatch_textures = [],
        files = files,
    ), DefaultInfo(files = files)]

texture = rule(
    implementation = _texture_impl,
    provides = [TextureGroupInfo],
    attrs = {
        "srcs": attr.label_list(
            mandatory = True,
            doc = "Input files. Must be .png files",
            allow_files = [".png"],
        ),
        "background": attr.bool(
            mandatory = False,
            default = False,
            doc = "Specify whether this texture is a background texture. Background texture will not be packed in an altas.",
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

def merge_texture_group_info(groups):
    """Merge multiple texture group info objects into one.

    Args:
        groups: A list of TextureGroupInfo objects to merge.

    Returns:
        A merged TextureGroupInfo object containing all textures and files.
    """
    files_depsets = []
    textures = []
    ninepatch_textures = []
    for group in groups:
        textures += group.textures
        ninepatch_textures += group.ninepatch_textures
        files_depsets.append(group.files)
    files = depset(transitive = files_depsets)
    return TextureGroupInfo(
        textures = textures,
        ninepatch_textures = ninepatch_textures,
        files = files,
    )

def _texture_group_impl(ctx):
    groups_infos = [dep[TextureGroupInfo] for dep in ctx.attr.deps]
    merged_group = merge_texture_group_info(groups_infos)
    return [merged_group, DefaultInfo(files = merged_group.files)]

texture_group = rule(
    implementation = _texture_group_impl,
    provides = [TextureGroupInfo],
    attrs = {
        "deps": attr.label_list(
            providers = [TextureGroupInfo],
            default = [],
            mandatory = False,
        ),
    },
)

TextureLibraryInfo = provider(
    doc = "Information about a texture library.",
    fields = ["package", "class_name", "prefix", "textures", "ninepatch_textures", "files"],
)

def _texture_lib_impl(ctx):
    groups_infos = [dep[TextureGroupInfo] for dep in ctx.attr.deps]
    merged_group = merge_texture_group_info(groups_infos)
    return [
        TextureLibraryInfo(
            package = ctx.attr.package,
            class_name = ctx.attr.class_name,
            prefix = ctx.attr.prefix,
            textures = merged_group.textures,
            ninepatch_textures = merged_group.ninepatch_textures,
            files = merged_group.files,
        ),
        DefaultInfo(files = merged_group.files),
    ]

texture_lib = rule(
    implementation = _texture_lib_impl,
    provides = [TextureLibraryInfo],
    attrs = {
        "package": attr.string(
            mandatory = True,
        ),
        "class_name": attr.string(
            mandatory = True,
        ),
        "prefix": attr.string(
            mandatory = True,
        ),
        "deps": attr.label_list(
            providers = [TextureGroupInfo],
            default = [],
            mandatory = False,
        ),
    },
)
