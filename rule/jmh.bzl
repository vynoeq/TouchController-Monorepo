load("@rules_java//java:defs.bzl", "java_binary")

def _jmh_impl(name, visibility, srcs, deps, plugins):
    java_binary(
        name = name,
        visibility = visibility,
        srcs = srcs,
        deps = deps + select({
            "//conditions:default": ["@maven//:org_openjdk_jmh_jmh_core"],
        }),
        main_class = "org.openjdk.jmh.Main",
        plugins = plugins + select({
            "//conditions:default": ["//rule/jmh:plugin"],
        })
    )

jmh_benchmark = macro(
    implementation = _jmh_impl,
    attrs = {
        "srcs": attr.label_list(
            allow_files = True,
        ),
        "deps": attr.label_list(
            providers = [JavaInfo],
        ),
        "plugins": attr.label_list(
            providers = [JavaInfo],
        ),
    },
)
