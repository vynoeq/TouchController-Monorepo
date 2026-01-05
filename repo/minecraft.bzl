"Module extension to download Minecraft artifacts"

load("//private:bytes_util.bzl", "hex_sha1_to_sri")

total_platforms = [
    "windows",
    "linux",
    "osx",
]
total_arches = [
    "32",
    "64",
]

version = tag_class(
    attrs = {
        "version": attr.string(
            doc = "The Minecraft version to be used",
        ),
        "assets": attr.bool(
            doc = "Download assets",
            default = False,
        ),
        "client": attr.bool(
            doc = "Download client",
            default = False,
        ),
        "server": attr.bool(
            doc = "Download server",
            default = False,
        ),
        "client_mappings": attr.bool(
            doc = "Download client mappings",
            default = False,
        ),
        "server_mappings": attr.bool(
            doc = "Download server mappings",
            default = False,
        ),
    },
)

exclude_library = tag_class(
    attrs = {
        "names": attr.string_list(
            doc = "Names to exclude",
            default = [],
        ),
    },
)

def _minecraft_repo_impl(rctx):
    version_entries = rctx.attr.version_entries
    version_entry_urls = rctx.attr.version_entry_urls
    version_entry_sha1 = rctx.attr.version_entry_sha1
    version_libraries = rctx.attr.version_libraries
    library_paths = rctx.attr.library_paths
    library_urls = rctx.attr.library_urls
    library_sha1 = rctx.attr.library_sha1

    download_tokens = []

    def library_id_to_target_name(id):
        return id.replace("-", "_").replace(":", "_").replace(".", "_").replace("#", "_")

    library_build_content = [
        'package(default_visibility = ["//:__subpackages__"])',
        "",
    ]
    for id, path in library_paths.items():
        url = library_urls[id]
        sha1 = library_sha1[id]

        download_tokens.append(rctx.download(
            url = url,
            output = "libraries/%s" % path,
            integrity = hex_sha1_to_sri(sha1),
            block = False,
        ))

        library_build_content += [
            "alias(",
            '    name = "%s",' % library_id_to_target_name(id),
            '    actual = "%s",' % path,
            ")",
        ]
    rctx.file("libraries/BUILD.bazel", content = "\n".join(library_build_content))

    for version, entries in version_entries.items():
        build_content = [
            'load("@rules_java//java:defs.bzl", "java_import")',
            'package(default_visibility = ["//visibility:public"])',
            "",
        ]

        # Platform mapping for select statements
        # TODO: add cpu mapping
        platform_mapping = {
            "windows": "@platforms//os:windows",
            "linux": "@platforms//os:linux",
            "osx": "@platforms//os:macos",
            "windows-32": "@platforms//os:windows",
            "windows-64": "@platforms//os:windows",
            "linux-32": "@platforms//os:linux",
            "linux-64": "@platforms//os:linux",
            "osx-64": "@platforms//os:macos",
        }

        for entry in entries:
            entry_key = "%s#%s" % (version, entry)
            entry_url = version_entry_urls[entry_key]
            entry_sha1 = version_entry_sha1[entry_key]
            entry_ext = entry_url.split(".")[-1]
            entry_filename = "%s.%s" % (entry, entry_ext)
            download_tokens.append(rctx.download(
                url = entry_url,
                output = "%s/%s" % (version, entry_filename),
                integrity = hex_sha1_to_sri(entry_sha1),
                block = False,
            ))
            if entry.endswith("_mappings"):
                build_content += [
                    "alias(",
                    '    name = "%s",' % entry,
                    '    actual = "%s",' % entry_filename,
                    ")",
                ]
            elif entry != "client":
                build_content += [
                    "java_import(",
                    '    name = "%s",' % entry,
                    '    jars = ["%s"],' % entry_filename,
                    ")",
                ]
            else:
                build_content += [
                    "java_import(",
                    '    name = "%s_libraries",' % entry,
                    "    jars = [",
                ]

                platform_libs_dict = {}
                common_library_name_set = set()
                platform_library_name_sets = {}
                for lib_name in version_libraries[version]:
                    separator_index = lib_name.index('#')
                    platform = lib_name[0:separator_index]
                    library_name = lib_name[separator_index + 1:]
                    library_target = library_id_to_target_name(library_name)
                    if platform == "common":
                        if library_name in library_paths and library_target not in common_library_name_set:
                            common_library_name_set.add(library_target)
                            build_content.append('        "//libraries:%s",' % library_target)
                    else:
                        if library_name in library_paths:
                            if platform not in platform_libs_dict:
                                platform_libs_dict[platform] = []
                            if platform not in platform_library_name_sets:
                                platform_library_name_sets[platform] = set()
                            if library_target not in platform_library_name_sets[platform]:
                                platform_library_name_sets[platform].add(library_target)
                                platform_libs_dict[platform].append("//libraries:%s" % library_target)

                build_content.append("    ]")

                if platform_libs_dict:
                    build_content.append("+ select({")
                    constraint_libs_dict = {}
                    for platform, libs in platform_libs_dict.items():
                        if libs:
                            platform_constraint = platform_mapping.get(platform)
                            if platform_constraint:
                                if platform_constraint not in constraint_libs_dict:
                                    constraint_libs_dict[platform_constraint] = []
                                constraint_libs_dict[platform_constraint] += libs
                    for constraint, libs in constraint_libs_dict.items():
                        condition_libs = ",\n".join(['            "%s"' % lib for lib in libs])
                        build_content.append('        "%s": [\n%s\n        ],' % (constraint, condition_libs))

                    build_content.append("})")

                build_content.append(",\n")
                build_content.append(")")

                build_content += [
                    "java_import(",
                    '    name = "%s",' % entry,
                    '    jars = ["%s"],' % entry_filename,
                    '    deps = [":%s_libraries"],' % entry,
                    ")",
                ]

        rctx.file(
            "%s/BUILD.bazel" % version,
            content = "\n".join(build_content),
        )

    for token in download_tokens:
        token.wait()

    return None

minecraft_repo = repository_rule(
    implementation = _minecraft_repo_impl,
    attrs = {
        "version_entries": attr.string_list_dict(),
        "version_entry_urls": attr.string_dict(),
        "version_entry_sha1": attr.string_dict(),
        "version_libraries": attr.string_list_dict(),
        "library_paths": attr.string_dict(),
        "library_urls": attr.string_dict(),
        "library_sha1": attr.string_dict(),
    },
)

def _minecraft_assets_repo_impl(rctx):
    asset_sha1 = rctx.attr.asset_sha1
    asset_urls = rctx.attr.asset_urls
    version_assets = rctx.attr.version_assets

    build_content = [
        'package(default_visibility = ["//visibility:public"])',
        "",
    ]

    manifest_tokens = []
    for index_id, asset_url in asset_urls.items():
        manifest_tokens.append(rctx.download(
            url = asset_url,
            output = "indexes/%s.json" % index_id,
            integrity = hex_sha1_to_sri(asset_sha1[index_id]),
            block = False,
        ))
    for token in manifest_tokens:
        token.wait()

    object_hashes = {}
    for manifest_id in asset_sha1.keys():
        manifest_path = "indexes/%s.json" % manifest_id
        manifest_text = rctx.read(manifest_path)
        asset_manifest = json.decode(manifest_text)
        manifest_paths = {}
        for asset_item in asset_manifest["objects"].values():
            asset_hash = asset_item["hash"]
            asset_path = "{}/{}".format(asset_hash[0:2], asset_hash)
            object_hashes[asset_hash] = asset_path
            manifest_paths[asset_hash] = asset_path
        build_content.append("filegroup(")
        build_content.append('    name = "objects_%s",' % manifest_id)
        build_content.append("    srcs = [")
        for asset_hash, asset_path in manifest_paths.items():
            build_content.append('        "objects/%s",' % asset_path)
        build_content.append("    ],")
        build_content.append(")")

    object_tokens = []
    for object_hash, object_path in object_hashes.items():
        object_path = object_hashes[object_hash]
        object_tokens.append(rctx.download(
            url = "https://resources.download.minecraft.net/%s" % object_path,
            output = "objects/%s" % object_path,
            integrity = hex_sha1_to_sri(object_hash),
            block = False,
        ))
    for token in object_tokens:
        token.wait()

    for version_id in version_assets.keys():
        version_manifest = version_assets[version_id]
        rctx.file(
            "versions/%s" % version_id,
            content = version_manifest,
        )
        build_content += [
            "alias(",
            '    name = "indexes_%s",' % version_id,
            '    actual = "indexes/%s.json",' % version_manifest,
            ")",
            "filegroup(",
            '    name = "assets_%s",' % version_id,
            "    srcs = [",
            '        ":indexes_%s",' % version_id,
            '        ":objects_%s",' % version_id,
            '        "versions/%s",' % version_id,
            "    ],",
            ")",
        ]
        if version_id != version_manifest:
            build_content += [
                "alias(",
                '    name = "objects_%s",' % version_id,
                '    actual = ":objects_%s",' % version_manifest,
                ")",
            ]

    build_content += [
        "alias(",
        '    name = "assets",',
        '    actual = ".",',
        ")",
    ]

    rctx.file(
        "BUILD.bazel",
        content = "\n".join(build_content),
    )

    return None

minecraft_assets_repo = repository_rule(
    implementation = _minecraft_assets_repo_impl,
    attrs = {
        "asset_sha1": attr.string_dict(),
        "asset_urls": attr.string_dict(),
        "version_assets": attr.string_dict(),
    },
)

def _minecraft_impl(mctx):
    manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    manifest_path = "version_manifest.json"

    mctx.report_progress("Downloading version manifest")
    mctx.download(
        url = manifest_url,
        output = manifest_path,
    )
    manifest = json.decode(mctx.read(manifest_path))

    # Deduplicate version entries
    version_entries = {}
    for mod in mctx.modules:
        for version_tag in mod.tags.version:
            version = version_tag.version
            if version in version_entries:
                version_entries[version]["assets"] |= version_tag.assets
                version_entries[version]["client"] |= version_tag.client
                version_entries[version]["server"] |= version_tag.server
                version_entries[version]["client_mappings"] |= version_tag.client_mappings
                version_entries[version]["server_mappings"] |= version_tag.server_mappings
            else:
                version_entries[version] = {
                    "assets": version_tag.assets,
                    "client": version_tag.client,
                    "server": version_tag.server,
                    "client_mappings": version_tag.client_mappings,
                    "server_mappings": version_tag.server_mappings,
                }

    exclude_library_names = []
    for mod in mctx.modules:
        for exclude_library in mod.tags.exclude_library:
            for name in exclude_library.names:
                exclude_library_names.append(name)

    target_version_entries = {}
    target_version_entry_urls = {}
    target_version_entry_sha1 = {}
    target_version_libraries = {}
    target_version_assets = {}
    library_paths = {}
    library_urls = {}
    library_sha1 = {}
    asset_sha1 = {}
    asset_urls = {}

    for version, entry in version_entries.items():
        target_assets = entry["assets"]
        target_client = entry["client"]
        target_server = entry["server"]
        target_client_mappings = entry["client_mappings"]
        target_server_mappings = entry["server_mappings"]

        # Find version metadata
        version_manifest_entry = None
        for entry in manifest["versions"]:
            if entry["id"] == version:
                version_manifest_entry = entry
                break
        if not version_manifest_entry:
            fail("Version %s not found in manifest" % version)

        # Download version JSON
        version_manifest_path = "version_{}.json".format(version)
        mctx.report_progress("Downloading %s manifest" % version)
        mctx.download(
            url = entry["url"],
            output = version_manifest_path,
            integrity = hex_sha1_to_sri(version_manifest_entry["sha1"]),
        )
        version_manifest = json.decode(mctx.read(version_manifest_path))

        entries = []

        def append_version_entry(entry_type):
            downloads = version_manifest["downloads"].get(entry_type)
            if not downloads:
                fail("Type '%s' not found in version %s's data" % (entry_type, version))
            entries.append(entry_type)
            entry_key = "%s#%s" % (version, entry_type)
            target_version_entry_urls[entry_key] = downloads["url"]
            target_version_entry_sha1[entry_key] = downloads["sha1"]

        if target_client:
            append_version_entry("client")
        if target_server:
            append_version_entry("server")
        if target_client_mappings:
            append_version_entry("client_mappings")
        if target_server_mappings:
            append_version_entry("server_mappings")

        target_version_entries[version] = entries

        if target_client:
            version_libraries = []
            for library in version_manifest["libraries"]:
                downloads = library["downloads"]
                name = library["name"]
                artifact = downloads.get("artifact")
                classifiers = downloads.get("classifiers")

                if name in exclude_library_names:
                    continue

                def add_artifact(id, artifact):
                    if id not in library_paths:
                        library_paths[id] = artifact["path"]
                        library_urls[id] = artifact["url"]
                        library_sha1[id] = artifact["sha1"]

                if artifact:
                    add_artifact(name, artifact)
                if classifiers:
                    for classifier, artifact in classifiers.items():
                        add_artifact("%s#%s" % (name, classifier), artifact)

                natives = library.get("natives")
                rules = library.get("rules")
                common = True
                allow_platforms = []
                if rules:
                    for platform in total_platforms:
                        allow = False
                        for rule in rules:
                            rule_pass = True
                            os = rule.get("os")
                            if os:
                                common = False
                                if os["name"] != platform:
                                    rule_pass = False
                            if rule_pass:
                                action = rule["action"]
                                if action == "allow":
                                    allow = True
                                elif action == "disallow":
                                    allow = False
                        if allow:
                            allow_platforms.append(platform)

                def add_native_platform(platform):
                    classifier = natives.get(platform)
                    if classifier:
                        def add_classifier(classifier, suffix = ""):
                            download = classifiers.get(classifier)
                            if download:
                                version_libraries.append("%s%s#%s#%s" % (platform, suffix, name, classifier))
                            else:
                                print("WARNING: unknown classifier %s for version %s's library %s on platform %s" % (classifier, version, name, platform))

                        if "${arch}" in classifier:
                            for arch in total_arches:
                                real_classifier = classifier.replace("${arch}", arch)
                                add_classifier(real_classifier, "-" + arch)
                        else:
                            add_classifier(classifier)

                if common:
                    if artifact:
                        version_libraries.append("common#%s" % name)
                    if natives:
                        for platform in total_platforms:
                            add_native_platform(platform)
                else:
                    for platform in allow_platforms:
                        if artifact:
                            version_libraries.append("%s#%s" % (platform, name))
                        if natives:
                            add_native_platform(platform)
            target_version_libraries[version] = version_libraries

        if target_assets:
            asset_info = version_manifest["assetIndex"]
            if asset_info == None:
                fail("No assets for version %s" % version)
            asset_id = asset_info["id"]
            target_version_assets[version] = asset_id
            if asset_id not in asset_sha1:
                asset_sha1[asset_id] = asset_info["sha1"]
                asset_urls[asset_id] = asset_info["url"]

    minecraft_repo(
        name = "minecraft",
        version_entries = target_version_entries,
        version_entry_urls = target_version_entry_urls,
        version_entry_sha1 = target_version_entry_sha1,
        version_libraries = target_version_libraries,
        library_paths = library_paths,
        library_urls = library_urls,
        library_sha1 = library_sha1,
    )

    minecraft_assets_repo(
        name = "minecraft_assets",
        asset_sha1 = asset_sha1,
        asset_urls = asset_urls,
        version_assets = target_version_assets,
    )

minecraft = module_extension(
    implementation = _minecraft_impl,
    tag_classes = {
        "version": version,
        "exclude_library": exclude_library,
    },
)
