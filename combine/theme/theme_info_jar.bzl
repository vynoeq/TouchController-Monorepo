"""Rules for generating mod loader metadata JAR for themes."""

load("//combine:properties.bzl", "combine_version", "home_page", "issue_tracker", "license", "sources_page")
load("//rule:mod_info_jar.bzl", "mod_info_jar")

def _theme_info_jar_impl(name, visibility, substitutions):
    predefined_substitutions = {
        "${version}": combine_version,
        "${license}": license,
        "${home_page}": home_page,
        "${sources_page}": sources_page,
        "${issue_tracker}": issue_tracker,
    }
    predefined_substitutions.update(substitutions)
    mod_info_jar(
        name = name,
        visibility = visibility,
        fabric = "//combine/theme:resources/fabric.mod.json",
        neoforge = "//combine/theme:resources/META-INF/neoforge.mods.toml",
        resource_strip_prefix = native.package_name(),
        substitutions = predefined_substitutions,
    )

theme_info_jar = macro(
    attrs = {
        "substitutions": attr.string_dict(
            mandatory = True,
            configurable = False,
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _theme_info_jar_impl,
)
