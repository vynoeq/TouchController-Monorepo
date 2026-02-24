"""Rules to extract native libraris for Minecraft versions using LWJGL2."""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

ExtractLibInfo = provider(
    "Provider for JAR to be extracted with exclude patterns",
    fields = ["jar_file", "excludes"],
)

def _extract_lib_impl(ctx):
    return [ExtractLibInfo(
        jar_file = ctx.file.jar,
        excludes = ctx.attr.excludes,
    ), DefaultInfo(
        files = depset([ctx.file.jar]),
    )]

extract_lib = rule(
    doc = "Define a JAR file to be extracted with exclude patterns",
    implementation = _extract_lib_impl,
    attrs = {
        "jar": attr.label(
            doc = "The JAR file to be extracted",
            mandatory = True,
            allow_single_file = ["jar"],
        ),
        "excludes": attr.string_list(
            doc = "List of exclude patterns",
            mandatory = False,
            default = [],
        ),
    },
)

def _extract_manifest_impl(ctx):
    output_file = ctx.actions.declare_file(ctx.label.name + ".txt")
    entries = []
    for dep in ctx.attr.deps:
        default_info = dep[DefaultInfo]
        extract_info = dep[ExtractLibInfo]
        file = default_info.files.to_list()
        if len(file) != 1:
            fail("Bad dep: inputs can only contain 1 file")
        entries.append("%s:%s" % (file[0].path, ":".join(extract_info.excludes)))
    ctx.actions.write(
        output = output_file,
        content = "\n".join(entries),
    )

    return [DefaultInfo(
        files = depset([output_file]),
    )]

extract_manifest = rule(
    doc = "Generate a manifest file for files to be extracted",
    implementation = _extract_manifest_impl,
    attrs = {
        "deps": attr.label_list(
            doc = "List of files to be extracted",
            providers = [ExtractLibInfo],
            mandatory = True,
        ),
    },
)
