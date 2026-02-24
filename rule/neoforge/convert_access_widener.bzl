"""Rules for converting access wideners to Forge access transformer format."""

def _convert_access_widener_impl(ctx):
    output_file = ctx.actions.declare_file(ctx.attr.output_name)
    args = ctx.actions.args()

    args.add(ctx.file.src.path)
    args.add(output_file.path)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = [ctx.file.src],
        outputs = [output_file],
        executable = ctx.executable._converter_bin,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        progress_message = "Converting access widener to access transformer for %s" % ctx.label.name,
    )

    return [DefaultInfo(files = depset([output_file]))]

convert_access_widener = rule(
    implementation = _convert_access_widener_impl,
    attrs = {
        "src": attr.label(
            allow_single_file = [".accesswidener", ".classtweaker"],
            mandatory = True,
            doc = "Access widener file",
        ),
        "output_name": attr.string(
            mandatory = True,
        ),
        "_converter_bin": attr.label(
            default = Label("//rule/neoforge/access_widener_converter"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Convert Fabric access widener to Forge access transformer.",
)
