load("@rules_mc//minecraft:defs.bzl", "EextractLibInfo")

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
