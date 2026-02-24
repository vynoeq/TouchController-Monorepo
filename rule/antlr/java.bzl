load("@rules_java//java:defs.bzl", "java_library")
load("//rule:jar.bzl", "jar")
load("//rule/antlr:antlr.bzl", "AntlrInfo")

def _java_antlr_library_sources_impl(ctx):
    output_dir = ctx.actions.declare_directory("_antlr_gen_" + ctx.label.name)

    src_info = ctx.attr.src[AntlrInfo]
    src_file = src_info.src

    args = ctx.actions.args()
    args.add("-Dlanguage=Java")
    args.add("-visitor")
    args.add("-package", ctx.attr.package)
    args.add("-o", output_dir.path)
    args.add(src_file)

    ctx.actions.run(
        inputs = [src_file],
        outputs = [output_dir],
        executable = ctx.executable._antlr_tool,
        arguments = [args],
    )

    return [
        DefaultInfo(files = depset([output_dir])),
    ]

_java_antlr_library_sources = rule(
    implementation = _java_antlr_library_sources_impl,
    attrs = {
        "src": attr.label(
            mandatory = True,
            providers = [AntlrInfo],
        ),
        "package": attr.string(
            mandatory = True,
        ),
        "_antlr_tool": attr.label(
            default = "//rule/antlr:antlr4",
            executable = True,
            cfg = "exec",
        ),
    },
)

def _zipper_impl(ctx):
    args = ctx.actions.args()
    args.add("c", ctx.outputs.output)
    args.add_all(ctx.files.srcs)

    ctx.actions.run(
        inputs = ctx.files.srcs,
        outputs = [ctx.outputs.output],
        executable = ctx.executable._zipper_tool,
        arguments = [args],
    )

    return [
        DefaultInfo(
            files = depset([ctx.outputs.output]),
        ),
    ]

_zipper = rule(
    implementation = _zipper_impl,
    attrs = {
        "srcs": attr.label_list(
            mandatory = True,
        ),
        "output": attr.output(
            mandatory = True,
        ),
        "_zipper_tool": attr.label(
            default = "@bazel_tools//tools/zip:zipper",
            executable = True,
            cfg = "exec",
        ),
    },
)

def _java_antlr_library_impl(name, visibility, src, package):
    source_label = name + "_sources"
    srcjar_label = name + "_srcjar"

    _java_antlr_library_sources(
        name = source_label,
        src = src,
        package = package,
    )

    _zipper(
        name = srcjar_label,
        srcs = [":" + source_label],
        output = name + ".srcjar",
    )

    java_library(
        name = name,
        srcs = [":" + srcjar_label],
        visibility = visibility,
        deps = [
            "@maven//:org_antlr_antlr4_runtime",
        ],
    )

java_antlr_library = macro(
    implementation = _java_antlr_library_impl,
    attrs = {
        "src": attr.label(
            mandatory = True,
            providers = [AntlrInfo],
        ),
        "package": attr.string(
            mandatory = True,
        ),
    },
)
