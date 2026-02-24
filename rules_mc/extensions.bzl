"Module extension to download Minecraft artifacts"

load("@bazel_tools//tools/build_defs/repo:http.bzl", _http_file = "http_file")
load("//private:bytes_util.bzl", _hex_sha1_to_sri = "hex_sha1_to_sri")
load("//private:maven_coordinate.bzl", _convert_maven_coordinate_to_repo = "convert_maven_coordinate_to_repo")
load("//private:version_util.bzl", _version_to_repo_name = "version_to_repo_name")
load("//private/repo:assets.bzl", _minecraft_assets_repo = "minecraft_assets_repo")
load("//private/repo:library.bzl", _minecraft_library_repo = "minecraft_library_repo")
load("//private/repo:repo.bzl", _minecraft_repo = "minecraft_repo")

_total_platforms = [
    "windows",
    "linux",
    "osx",
]
_total_arches = [
    "32",
    "64",
]

def _download_version_manifest(mctx):
    manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    manifest_path = "version_manifest.json"

    mctx.report_progress("Downloading version manifest")
    mctx.download(
        url = manifest_url,
        output = manifest_path,
    )
    manifest = json.decode(mctx.read(manifest_path))

    return manifest

def _append_version_entry(version, version_manifest, entry_type):
    downloads = version_manifest["downloads"].get(entry_type)
    if not downloads:
        fail("Type '%s' not found in version %s's data" % (entry_type, version))

    url = downloads["url"]
    filename = url.split("/")[-1]
    _http_file(
        name = _version_to_repo_name(version, entry_type),
        url = url,
        integrity = _hex_sha1_to_sri(downloads["sha1"]),
        downloaded_file_path = filename,
    )

def _append_library_entry(name, manifest):
    downloads = manifest["downloads"]
    artifact = downloads.get("artifact")
    manifest_classifiers = downloads.get("classifiers")

    classifiers = {}

    def add_artifact(id, artifact):
        classifiers[id] = struct(
            path = artifact["path"],
            url = artifact["url"],
            sha1 = artifact["sha1"],
        )

    if artifact:
        add_artifact("default", artifact)
    if manifest_classifiers:
        for classifier, artifact in manifest_classifiers.items():
            add_artifact(classifier, artifact)

    natives = manifest.get("natives")
    rules = manifest.get("rules")
    common = True
    allow_platforms = []
    if rules:
        for platform in _total_platforms:
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

    # platforms -> classifiers
    platforms = {}

    def add_native_platform(platform):
        classifier = natives.get(platform)
        if classifier:
            def add_classifier(classifier, suffix = ""):
                download = manifest_classifiers.get(classifier)
                if download:
                    platforms[platform + suffix] = classifier
                else:
                    print("WARNING: unknown classifier %s for version %s's library %s on platform %s" % (classifier, version, name, platform))

            if "${arch}" in classifier:
                for arch in _total_arches:
                    real_classifier = classifier.replace("${arch}", arch)
                    add_classifier(real_classifier, "-" + arch)
            else:
                add_classifier(classifier)

    if "default" in classifiers:
        if common:
            platforms["common"] = "default"
        else:
            for platform in allow_platforms:
                platforms[platform] = "default"

    if natives:
        if rules:
            for platform in allow_platforms:
                add_native_platform(platform)
        else:
            for platform in natives.keys():
                add_native_platform(platform)

    extract = None
    if "extract" in manifest:
        extract_manifest = manifest["extract"]
        extract = struct(
            exclude = extract_manifest.get("exclude", []),
        )

    return struct(
        classifiers = classifiers,
        platforms = platforms,
        extract = extract,
    )

def _minecraft_impl(mctx):
    manifest = _download_version_manifest(mctx)

    # Deduplicate version entries
    version_inputs = {}
    for mod in mctx.modules:
        for version_tag in mod.tags.version:
            version = version_tag.version
            if version in version_inputs:
                entry = version_inputs
                version_inputs[version] = struct(
                    assets = version_inputs[version].assets or version_tag.assets,
                    client = version_inputs[version].client or version_tag.client,
                    server = version_inputs[version].server or version_tag.server,
                    client_mappings = version_inputs[version].client_mappings or version_tag.client_mappings,
                    server_mappings = version_inputs[version].server_mappings or version_tag.server_mappings,
                )
            else:
                version_inputs[version] = struct(
                    assets = version_tag.assets,
                    client = version_tag.client,
                    server = version_tag.server,
                    client_mappings = version_tag.client_mappings,
                    server_mappings = version_tag.server_mappings,
                )

    exclude_library_names = []
    for mod in mctx.modules:
        for exclude_library in mod.tags.exclude_library:
            for name in exclude_library.names:
                exclude_library_names.append(name)

    version_manifests = {}

    for version in version_inputs.keys():
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
        version_manifests[version] = struct(
            path = version_manifest_path,
            token = mctx.download(
                url = version_manifest_entry["url"],
                output = version_manifest_path,
                integrity = _hex_sha1_to_sri(version_manifest_entry["sha1"]),
                block = False,
            ),
        )

    version_entries = {}
    library_entries = {}
    asset_entries = {}

    for version, version_manifest in version_manifests.items():
        version_input = version_inputs[version]
        version_manifest.token.wait()
        version_manifest = json.decode(mctx.read(version_manifest.path))

        entries = []
        if version_input.client:
            _append_version_entry(version, version_manifest, "client")
            entries.append("client#file")
        if version_input.server:
            _append_version_entry(version, version_manifest, "server")
            entries.append("server#file")
        if version_input.client_mappings:
            _append_version_entry(version, version_manifest, "client_mappings")
            entries.append("client_mappings#file")
        if version_input.server_mappings:
            _append_version_entry(version, version_manifest, "server_mappings")
            entries.append("server_mappings#file")

        libraries = []
        if version_input.client:
            for library in version_manifest["libraries"]:
                name = library["name"]
                if name in exclude_library_names:
                    continue
                if not name in library_entries:
                    library_entries[name] = _append_library_entry(name, library)
                if name in libraries:
                    continue
                libraries.append(name)

        asset_manifest_id = None
        if version_input.assets:
            asset_info = version_manifest["assetIndex"]
            if asset_info == None:
                fail("No assets for version %s" % version)
            asset_id = asset_info["id"]
            asset_manifest_id = asset_id
            if not asset_id in asset_entries:
                asset_entries[asset_id] = struct(
                    sha1 = asset_info["sha1"],
                    url = asset_info["url"],
                )

        version_entries[version] = struct(
            entries = entries,
            libraries = libraries,
            asset_manifest_id = asset_manifest_id,
        )

    for library_name, library in library_entries.items():
        for classifier, artifact in library.classifiers.items():
            name = "%s_%s" % (_convert_maven_coordinate_to_repo("minecraft", library_name), classifier)
            if library.extract:
                _minecraft_library_repo(
                    name = name,
                    url = artifact.url,
                    sha1 = artifact.sha1,
                    path = artifact.path,
                    extract = True,
                    extract_exclude = library.extract.exclude,
                )
            else:
                _minecraft_library_repo(
                    name = name,
                    url = artifact.url,
                    sha1 = artifact.sha1,
                    path = artifact.path,
                )

    library_classifiers = {}
    library_platforms = {}
    for name, library in library_entries.items():
        library_classifiers[name] = ["%s#%s" % (classifier, artifact) for classifier, artifact in library.classifiers.items()]
        library_platforms[name] = ["%s#%s" % (platform, classifier) for platform, classifier in library.platforms.items()]

    _minecraft_repo(
        name = "minecraft",
        version_entries = {key: entry.entries for key, entry in version_entries.items()},
        version_libraries = {key: entry.libraries for key, entry in version_entries.items()},
        # library -> classifiers
        library_classifiers = library_classifiers,
        # library -> platform#classifier
        library_platforms = library_platforms,
        library_extracts = {key: entry.extract.exclude for key, entry in library_entries.items() if entry.extract},
    )

    _minecraft_assets_repo(
        name = "minecraft_assets",
        asset_sha1 = {key: entry.sha1 for key, entry in asset_entries.items()},
        asset_urls = {key: entry.url for key, entry in asset_entries.items()},
        version_assets = {version: entry.asset_manifest_id for version, entry in version_entries.items() if entry.asset_manifest_id},
    )

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

minecraft = module_extension(
    implementation = _minecraft_impl,
    tag_classes = {
        "version": version,
        "exclude_library": exclude_library,
    },
)
