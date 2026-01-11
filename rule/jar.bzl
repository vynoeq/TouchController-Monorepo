"""Rules for creating JAR file."""

load("@rules_java//java:defs.bzl", "JavaInfo")

def _impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")

    args = ctx.actions.args()
    args.add(output_jar.path)

    input_files = []
    for file_label, path in ctx.attr.data.items():
        files = file_label.files.to_list()
        if not files:
            fail("No files provided for path: " + path)

        if len(files) > 1:
            fail("Multiple files provided for path: " + path + ". Only one file per path is supported.")

        input_file = files[0]
        input_files.append(input_file)
        args.add("--entry")
        args.add(path)
        args.add(input_file.path)

    ctx.actions.run(
        inputs = input_files,
        outputs = [output_jar],
        executable = ctx.executable._create_jar_executable,
        arguments = [args],
        progress_message = "Creating JAR %s" % ctx.label.name,
    )

    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        DefaultInfo(files = depset([output_jar])),
    ]

jar = rule(
    implementation = _impl,
    attrs = {
        "data": attr.label_keyed_string_dict(
            mandatory = True,
            allow_files = True,
            default = {},
            doc = "Files and path. File is the key, path is the value",
        ),
        "_create_jar_executable": attr.label(
            default = Label("@//rule/create_jar"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Create a JAR file from files",
)
