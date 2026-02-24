"""Repository rule to fetch Minecraft assets."""

load("//private:bytes_util.bzl", _hex_sha1_to_sri = "hex_sha1_to_sri")

def _split_hash(hash):
    return "{}/{}".format(hash[0:2], hash)

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
            integrity = _hex_sha1_to_sri(asset_sha1[index_id]),
            block = False,
        ))
    for token in manifest_tokens:
        token.wait()

    object_entries = {}
    for manifest_id in asset_sha1.keys():
        manifest_path = "indexes/%s.json" % manifest_id
        manifest_text = rctx.read(manifest_path)
        asset_manifest = json.decode(manifest_text)
        map_to_resources = asset_manifest.get("map_to_resources", False)
        manifest_paths = {}
        for asset_name, asset_item in asset_manifest["objects"].items():
            asset_hash = asset_item["hash"]
            split_hash = _split_hash(asset_hash)
            asset_path = ("legacy/%s" % asset_name) if map_to_resources else ("objects/%s" % split_hash)
            manifest_paths[asset_hash] = asset_path
            object_entries[asset_path] = struct(
                hash = asset_hash,
                split_hash = split_hash,
            )
        build_content.append("filegroup(")
        build_content.append('    name = "objects_%s",' % manifest_id)
        build_content.append("    srcs = [")
        for asset_hash, asset_path in manifest_paths.items():
            build_content.append('        "%s",' % asset_path)
        build_content.append("    ],")
        build_content.append(")")

    object_tokens = []
    for asset_path, object_entry in object_entries.items():
        object_tokens.append(rctx.download(
            url = "https://resources.download.minecraft.net/%s" % object_entry.split_hash,
            output = asset_path,
            integrity = _hex_sha1_to_sri(object_entry.hash),
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
            '    name = "index_%s",' % version_id,
            '    actual = "indexes/%s.json",' % version_manifest,
            ")",
            "alias(",
            '    name = "version_%s",' % version_id,
            '    actual = "versions/%s",' % version_id,
            ")",
            "filegroup(",
            '    name = "assets_%s",' % version_id,
            "    srcs = [",
            '        ":index_%s",' % version_id,
            '        ":objects_%s",' % version_id,
            '        ":version_%s",' % version_id,
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
