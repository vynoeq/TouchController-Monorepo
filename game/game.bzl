"Macro to declare game versions"

load("@rules_java//java:defs.bzl", "java_binary")
load("@rules_java//java:java_import.bzl", "java_import")
load("//rule:decompile_jar.bzl", "decompile_jar")
load("//rule:extract_jar.bzl", "extract_jar")
load("//rule:jar.bzl", "jar")
load("//rule:merge_mapping.bzl", "merge_mapping", "merge_mapping_input")
load("//rule:remap_jar.bzl", "remap_jar")

def _game_version_impl(
        name,
        visibility,
        version,
        client,
        client_mappings,
        client_assets,
        client_libraries,
        server,
        server_legacy,
        neoforge,
        intermediary,
        sodium_intermediary,
        iris_intermediary):
    intermediary_mapping = name + "_intermediary_mapping"
    intermediary_input = name + "_intermediary_input"
    named_input = name + "_named_input"
    merged_mapping = name + "_merged_mapping"
    mapping_jar = name + "_mapping_jar"
    client_intermediary = name + "_client_intermediary"
    client_named = name + "_client_named"
    client_source = name + "_client_source"
    client_named_source = name + "_client_named_source"
    client_neoforge = name + "_client_neoforge"
    server_jar_file = name + "_server_jar_file"
    server_jar = name + "_server_jar"
    server_named = name + "_server_named"
    sodium_named = name + "_sodium_named"
    iris_named = name + "_iris_named"
    vanilla_client = name + "_vanilla_client"

    if intermediary:
        extract_jar(
            name = intermediary_mapping,
            entry_path = "mappings/mappings.tiny",
            filename = "intermediary.tiny",
            input = intermediary,
        )

        merge_mapping_input(
            name = intermediary_input,
            file = ":" + intermediary_mapping,
            format = "tinyv2",
            source_namespace = "official",
        )

    if client_mappings:
        merge_mapping_input(
            name = named_input,
            file = client_mappings,
            format = "proguard",
            namespace_mappings = {
                "source": "named",
                "target": "official",
            },
            source_namespace = "official",
        )

    decompile_jar(
        name = client_source,
        inputs = [client],
    )

    if intermediary or client_mappings:
        intermediary_input_item = [":" + intermediary_input] if intermediary else []
        named_input_item = [":" + named_input] if client_mappings else []

        merge_mapping(
            name = merged_mapping,
            complete_namespace = {
                "named": "intermediary",
            },
            inputs = intermediary_input_item + named_input_item,
            output = "merged.tiny",
            output_source_namespace = "official",
            visibility = visibility,
        )

        jar(
            name = mapping_jar,
            data = {
                (":" + merged_mapping): "mappings/mappings.tiny",
            },
            visibility = visibility,
        )

        remap_jar(
            name = client_intermediary,
            from_namespace = "official",
            inputs = [client],
            mapping = ":" + merged_mapping,
            to_namespace = "intermediary",
            visibility = visibility,
        )

        remap_jar(
            name = client_named,
            from_namespace = "official",
            inputs = [client],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
        )

        decompile_jar(
            name = client_named_source,
            inputs = [":" + client_named],
        )

    if neoforge:
        native.alias(
            name = client_neoforge,
            actual = neoforge,
            visibility = visibility,
        )

    if server_legacy:
        native.alias(
            name = server_jar,
            actual = server,
            visibility = visibility,
        )
    else:
        extract_jar(
            name = server_jar_file,
            entry_path = "META-INF/versions/%s/server-%s.jar" % (version, version),
            filename = "_minecraft/server.jar",
            input = server,
        )

        java_import(
            name = server_jar,
            jars = [
                ":" + server_jar_file,
            ],
            visibility = visibility,
        )

    if intermediary:
        remap_jar(
            name = server_named,
            from_namespace = "official",
            inputs = [":" + server_jar],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
        )

    if intermediary and sodium_intermediary:
        remap_jar(
            name = sodium_named,
            from_namespace = "intermediary",
            inputs = [sodium_intermediary],
            classpath = [":" + client_intermediary],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
            mixin = True,
            remove_jar_in_jar = True,
        )

    if intermediary and sodium_intermediary and iris_intermediary:
        remap_jar(
            name = iris_named,
            from_namespace = "intermediary",
            inputs = [iris_intermediary],
            classpath = [
                ":" + client_intermediary,
                sodium_intermediary,
            ],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
            mixin = True,
            remove_jar_in_jar = True,
        )

    if client_assets and client_libraries:
        java_binary(
            name = vanilla_client,
            srcs = [],
            data = [
                "@minecraft_assets//:assets",
                client_assets,
            ],
            env = {
                "LANG": "en_US.UTF8",
            },
            jvm_flags = [
                "-Ddev.launch.version=%s" % version,
                "-Ddev.launch.type=client",
                "-Ddev.launch.assetsPath=$(rlocationpath @minecraft_assets//:assets)",
                "-Ddev.launch.mainClass=net.minecraft.client.main.Main",
                "-Xmx4G",
            ],
            main_class = "top.fifthlight.fabazel.devlaunchwrapper.DevLaunchWrapper",
            runtime_deps = [
                client,
                client_libraries,
                "//rule/dev_launch_wrapper",
            ],
        )

game_version = macro(
    implementation = _game_version_impl,
    attrs = {
        "version": attr.string(
            mandatory = True,
            doc = "Minecraft version",
            configurable = False,
        ),
        "client": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "Client JAR file",
            configurable = False,
        ),
        "client_mappings": attr.label(
            mandatory = False,
            allow_single_file = True,
            doc = "Client mappings file",
            configurable = False,
        ),
        "client_assets": attr.label(
            mandatory = False,
            doc = "Client assets for the game",
            configurable = False,
        ),
        "client_libraries": attr.label(
            mandatory = False,
            doc = "Client libraries for the game",
            configurable = False,
        ),
        "server": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "Server JAR file",
            configurable = False,
        ),
        "server_legacy": attr.bool(
            mandatory = False,
            default = False,
            doc = "Mark the version's server JAR as legacy JARs that directly shadows libraries into main JAR",
            configurable = False,
        ),
        "neoforge": attr.label(
            mandatory = False,
            doc = "NeoForge compiled target",
            configurable = False,
        ),
        "intermediary": attr.label(
            mandatory = False,
            doc = "Intermediary mappings",
            configurable = False,
        ),
        "sodium_intermediary": attr.label(
            mandatory = False,
            doc = "Sodium to be remapped from intermediary",
            configurable = False,
        ),
        "iris_intermediary": attr.label(
            mandatory = False,
            doc = "Sodium to be remapped from intermediary",
            configurable = False,
        ),
    },
)
