"""Rules for decompiling JAR files."""

load("@rules_java//java:defs.bzl", "JavaInfo", "java_common")

def _decompile_jar_impl(ctx):
    output_file = ctx.actions.declare_file("%s.jar" % ctx.label.name)
    merged_input = java_common.merge([dep[JavaInfo] for dep in ctx.attr.inputs])

    args = ctx.actions.args()
    if ctx.file.mappings:
        args.add("-m", ctx.file.mappings)
    args.add(output_file)
    args.add_all(merged_input.full_compile_jars)

    inputs = merged_input.full_compile_jars
    if ctx.file.mappings:
        inputs = depset([ctx.file.mappings], transitive = [inputs])

    ctx.actions.run(
        inputs = inputs,
        outputs = [output_file],
        executable = ctx.executable._vineflower_bin,
        arguments = [args],
        progress_message = "Decompiling %s" % ctx.label.name,
    )

    return [DefaultInfo(files = depset([output_file]))]

decompile_jar = rule(
    implementation = _decompile_jar_impl,
    attrs = {
        "inputs": attr.label_list(
            mandatory = True,
            doc = "Input JAR files",
        ),
        "mappings": attr.label(
            allow_single_file = [".tiny"],
            mandatory = False,
            doc = "Mapping file",
        ),
        "_vineflower_bin": attr.label(
            default = Label("//rule/vineflower"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Decompile JAR archives with vineflower.",
)
