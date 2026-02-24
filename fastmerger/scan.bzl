BdepsInfo = provider(fields = {
    "bdeps": ".bdeps files",
    "bdeps_pairs": "Array of jar -> bdeps pair",
    "transitive_bdeps": "Transitive depset of bdeps",
    "transitive_bdeps_pair": "Transitive depset of jar -> bdeps pair",
})

_DEPS_ATTRS = [
    "deps",
    "runtime_deps",
    "merge_deps",
    "merge_only_deps",
    "merge_runtime_deps",
]

def _bdeps_scan_impl(target, ctx):
    bdeps_depsets = []
    bdeps_pair_depsets = []

    if hasattr(ctx.rule.attr, "deps"):
        for dep in ctx.rule.attr.deps:
            if BdepsInfo in dep:
                bdeps_info = dep[BdepsInfo]
                bdeps_depsets.append(bdeps_info.transitive_bdeps)
                bdeps_pair_depsets.append(bdeps_info.transitive_bdeps_pair)

    if hasattr(ctx.rule.attr, "runtime_deps"):
        for dep in ctx.rule.attr.runtime_deps:
            if BdepsInfo in dep:
                bdeps_info = dep[BdepsInfo]
                bdeps_depsets.append(bdeps_info.transitive_bdeps)
                bdeps_pair_depsets.append(bdeps_info.transitive_bdeps_pair)

    if hasattr(ctx.rule.attr, "merge_only_deps"):
        for dep in ctx.rule.attr.merge_only_deps:
            if BdepsInfo in dep:
                bdeps_info = dep[BdepsInfo]
                bdeps_depsets.append(bdeps_info.transitive_bdeps)
                bdeps_pair_depsets.append(bdeps_info.transitive_bdeps_pair)

    current_bdeps = []
    current_bdeps_pairs = []
    if JavaInfo in target:
        for jar in target[JavaInfo].runtime_output_jars:
            out_bdeps = ctx.actions.declare_file(jar.basename + ".bdeps")

            args = ctx.actions.args()

            args.add(jar)
            args.add(out_bdeps)

            args.use_param_file("@%s", use_always = True)
            args.set_param_file_format("multiline")

            ctx.actions.run(
                executable = ctx.executable._extractor,
                arguments = [args],
                inputs = [jar],
                outputs = [out_bdeps],
                mnemonic = "ScanBdeps",
                execution_requirements = {
                    "supports-workers": "1",
                    "supports-multiplex-workers": "1",
                    "supports-multiplex-sandboxing": "1",
                    "requires-worker-protocol": "proto",
                },
            )
            current_bdeps.append(out_bdeps)
            current_bdeps_pairs.append(struct(
                jar = jar,
                bdeps = out_bdeps,
            ))

    result_bdeps_depset = depset(direct = current_bdeps, transitive = bdeps_depsets)
    result_bdeps_pair_depset = depset(direct = current_bdeps_pairs, transitive = bdeps_pair_depsets)
    return [
        BdepsInfo(
            bdeps = current_bdeps,
            bdeps_pairs = current_bdeps_pairs,
            transitive_bdeps = result_bdeps_depset,
            transitive_bdeps_pair = result_bdeps_pair_depset,
        ),
        OutputGroupInfo(
            bdeps = current_bdeps,
        ),
    ]

bdeps_scan = aspect(
    implementation = _bdeps_scan_impl,
    attr_aspects = _DEPS_ATTRS,
    attrs = {
        "_extractor": attr.label(
            default = Label("//fastmerger/scanner"),
            executable = True,
            cfg = "exec",
        ),
    },
)
