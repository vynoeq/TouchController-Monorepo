AntlrInfo = provider(
    fields = {
        "src": "The source files of the antlr rule.",
    },
)

def _antlr_lib_impl(ctx):
    return [
        AntlrInfo(src = ctx.file.src),
        DefaultInfo(files = depset([ctx.file.src])),
    ]

antlr_lib = rule(
    implementation = _antlr_lib_impl,
    attrs = {
        "src": attr.label(
            doc = "The source files of the antlr rule.",
            allow_single_file = [".g4"],
        ),
    },
)
