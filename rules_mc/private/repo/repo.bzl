"""Repository rules to fetch Minecraft files."""

load("//private:maven_coordinate.bzl", _convert_maven_coordinate = "convert_maven_coordinate")
load("//private:version_util.bzl", _version_to_repo_name = "version_to_repo_name")

platform_constraints = {
    "windows": "@platforms//os:windows",
    "linux": "@platforms//os:linux",
    "osx": "@platforms//os:macos",
}

def _find_constraint(platform):
    constraint = None
    for prefix, constraint in platform_constraints.items():
        if platform.startswith(prefix):
            return constraint
    print("WARNING: No platform constraint found for %s" % platform)
    return None

def _minecraft_repo_impl(rctx):
    version_entries = rctx.attr.version_entries
    version_libraries = rctx.attr.version_libraries
    library_extracts = rctx.attr.library_extracts
    library_platforms = {}

    for library, platforms_list in rctx.attr.library_platforms.items():
        platforms = {}
        for platform in platforms_list:
            (platform, classifier) = platform.split("#")
            platforms[platform] = classifier
        library_platforms[library] = platforms

    for version, entries in version_entries.items():
        build_content = [
            'load("@rules_java//java:defs.bzl", "java_import")',
            'load("@rules_mc//private/rules:extract_lib.bzl", "extract_manifest")',
            'package(default_visibility = ["//visibility:public"])',
            "",
        ]

        entry_dict = {}
        for version_entry in entries:
            entry, name = version_entry.split("#")
            entry_dict[entry] = name

        if "client" in entry_dict:
            common_libs = set()
            constraint_libs = {}
            extract_common_libs = set()
            constraint_extract_libs = {}

            for library_name in version_libraries.get(version, []):
                platforms = library_platforms.get(library_name, {})
                normalized_name = _convert_maven_coordinate(library_name)

                def get_lib_label(classifier):
                    return "@minecraft_%s_%s//:file" % (normalized_name, classifier)

                if "common" in platforms:
                    lib_label = get_lib_label(platforms["common"])
                    common_libs.add(lib_label)
                    if library_name in library_extracts:
                        extract_common_libs.add(lib_label)

                for platform in platforms:
                    if platform == "common":
                        continue
                    lib_label = get_lib_label(platforms[platform])
                    constraint = _find_constraint(platform)
                    if not constraint:
                        continue
                    if not constraint in constraint_libs:
                        constraint_libs[constraint] = set()
                    constraint_libs[constraint].add(lib_label)
                    if library_name in library_extracts:
                        if not constraint in constraint_extract_libs:
                            constraint_extract_libs[constraint] = set()
                        constraint_extract_libs[constraint].add(lib_label)

            if constraint_libs:
                build_content += [
                    "java_import(",
                    '    name = "client_libraries",',
                    "    jars = select({",
                    '        "//conditions:default": [',
                    ",\n".join(['            "%s"' % lib for lib in common_libs]),
                    "        ],",
                    "    }) + select({",
                ]
                for constraint, libs in constraint_libs.items():
                    libs_to_add = libs.difference(common_libs)
                    if libs_to_add:
                        build_content += [
                            '        "%s": [' % constraint,
                            ",\n".join(['            "%s"' % lib for lib in libs_to_add]),
                            "        ],",
                        ]
                build_content += [
                    '        "//conditions:default": [],',
                    "    }),",
                    ")",
                ]
            else:
                build_content += [
                    "java_import(",
                    '    name = "client_libraries",',
                    "    jars = [",
                ]
                if common_libs:
                    build_content.append(",\n".join(['            "%s"' % lib for lib in common_libs]))
                build_content.append("        ])")

            if constraint_extract_libs:
                build_content += [
                    "extract_manifest(",
                    '    name = "client_extract_libraries",',
                    "    deps = select({",
                    '        "//conditions:default": [',
                    ",\n".join(['            "%s"' % lib for lib in extract_common_libs]),
                    "        ],",
                    "    }) + select({",
                ]
                for constraint, libs in constraint_extract_libs.items():
                    libs_to_add = libs.difference(extract_common_libs)
                    if libs_to_add:
                        build_content += [
                            '        "%s": [' % constraint,
                            ",\n".join(['            "%s"' % lib for lib in libs_to_add]),
                            "        ],",
                        ]
                build_content += [
                    '        "//conditions:default": [],',
                    "    }),",
                    ")",
                ]
            elif extract_common_libs:
                build_content += [
                    "extract_manifest(",
                    '    name = "client_extract_libraries",',
                    "    deps = [",
                ]
                if common_libs:
                    build_content.append(",\n".join(['            "%s"' % lib for lib in extract_common_libs]))
                build_content.append("        ])")

        for entry, name in entry_dict.items():
            repo = _version_to_repo_name(version, entry)
            if entry in ["client", "server"]:
                build_content += [
                    "java_import(",
                    '    name = "%s",' % entry,
                    '    jars = ["@%s//file"],' % repo,
                    '    deps = [":client_libraries"],' if entry == "client" else "",
                    ")",
                ]
            else:
                build_content += [
                    "alias(",
                    '    name = "%s",' % entry,
                    '    actual = "@%s//file",' % repo,
                    ")",
                ]

        rctx.file(
            "%s/BUILD.bazel" % version,
            content = "\n".join(build_content),
        )

    return None

minecraft_repo = repository_rule(
    implementation = _minecraft_repo_impl,
    attrs = {
        "version_entries": attr.string_list_dict(),
        "version_libraries": attr.string_list_dict(),
        "library_classifiers": attr.string_list_dict(),
        "library_platforms": attr.string_list_dict(),
        "library_extracts": attr.string_list_dict(),
    },
)
