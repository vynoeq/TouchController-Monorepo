"""Rules for generating legacy text resources."""

def _generate_legacy_text_impl(ctx):
    output_files = []
    for file in ctx.files.deps:
        filename = file.basename.removesuffix("." + file.extension)
        output_file = ctx.actions.declare_file(filename + ".lang")

        args = ctx.actions.args()
        args.add(file)
        args.add(output_file)

        ctx.actions.run(
            inputs = [file],
            outputs = [output_file],
            executable = ctx.executable._generator_bin,
            arguments = [args],
            progress_message = "Generating legacy language file %s" % filename,
        )
        output_files.append(output_file)

    return [DefaultInfo(files = depset(output_files))]

generate_legacy_text = rule(
    implementation = _generate_legacy_text_impl,
    attrs = {
        "deps": attr.label_list(
            allow_files = [".json"],
            mandatory = True,
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/touchcontroller/legacy_text_generator"),
            executable = True,
            cfg = "exec",
        ),
    },
)
