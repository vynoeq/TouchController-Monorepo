ProguardExtractionInfo = provider(fields = {
    "rules": "depset of extracted .pro files",
})

_DEPS_ATTRS = [
    "deps",
    "runtime_deps",
    "merge_deps",
    "merge_only_deps",
    "merge_runtime_deps",
]

def _proguard_extract_impl(target, ctx):
    transitive_rules = []

    if hasattr(ctx.rule.attr, "deps"):
        for dep in ctx.rule.attr.deps:
            if ProguardExtractionInfo in dep:
                transitive_rules.append(dep[ProguardExtractionInfo].rules)

    if hasattr(ctx.rule.attr, "runtime_deps"):
        for dep in ctx.rule.attr.runtime_deps:
            if ProguardExtractionInfo in dep:
                transitive_rules.append(dep[ProguardExtractionInfo].rules)

    if hasattr(ctx.rule.attr, "merge_only_deps"):
        for dep in ctx.rule.attr.merge_only_deps:
            if ProguardExtractionInfo in dep:
                transitive_rules.append(dep[ProguardExtractionInfo].rules)

    current_rules = []
    if JavaInfo in target:
        for jar in target[JavaInfo].runtime_output_jars:
            out_pro = ctx.actions.declare_file(jar.basename + ".extracted.pro")

            args = ctx.actions.args()

            args.add(jar)
            args.add(out_pro)

            args.use_param_file("@%s", use_always = True)
            args.set_param_file_format("multiline")

            ctx.actions.run(
                executable = ctx.executable._extractor,
                arguments = [args],
                inputs = [jar],
                outputs = [out_pro],
                mnemonic = "ExtractProguard",
                execution_requirements = {
                    "supports-workers": "1",
                    "supports-multiplex-workers": "1",
                    "supports-multiplex-sandboxing": "1",
                    "requires-worker-protocol": "proto",
                },
            )
            current_rules.append(out_pro)

    res_depset = depset(direct = current_rules, transitive = transitive_rules)
    return [
        ProguardExtractionInfo(rules = res_depset),
        OutputGroupInfo(proguard_rule = res_depset),
    ]

proguard_extract = aspect(
    implementation = _proguard_extract_impl,
    attr_aspects = _DEPS_ATTRS,
    attrs = {
        "_extractor": attr.label(
            default = Label("//fastmerger/proguard/extractor"),
            executable = True,
            cfg = "exec",
        ),
    },
)
