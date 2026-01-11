"""Parchment mappings repository rule, fetches Parchment artifact from Maven"""

load("//private:maven_coordinate.bzl", _convert_maven_coordinate_to_url = "convert_maven_coordinate_to_url")

_PARCHMENT_REPO_URL = "https://maven.parchmentmc.org"

def _parchment_repo_impl(rctx):
    """Repository rule implementation for a single Parchment version"""
    mc_version = rctx.attr.mc_version
    parchment_version = rctx.attr.parchment_version
    parchment_sha256 = rctx.attr.sha256

    # Maven coordinate: org.parchmentmc.data:parchment-{mc_version}:{parchment_version}
    maven_coordinate = "org.parchmentmc.data:parchment-%s:%s" % (mc_version, parchment_version)

    maven_url = _convert_maven_coordinate_to_url(
        _PARCHMENT_REPO_URL,
        maven_coordinate,
        extension = "zip",
    )

    zip_filename = "parchment-%s-%s.zip" % (mc_version, parchment_version)

    rctx.report_progress("Downloading Parchment %s for Minecraft %s" % (parchment_version, mc_version))
    rctx.download(
        url = maven_url,
        output = zip_filename,
        sha256 = parchment_sha256,
    )

    rctx.extract(archive = zip_filename, output = ".")

    build_content = [
        'package(default_visibility = ["//visibility:public"])',
        "",
        "alias(",
        '    name = "zip",',
        '    actual = "%s",' % zip_filename,
        ")",
        "",
        "alias(",
        '    name = "json",',
        '    actual = "parchment.json",',
        ")",
    ]

    rctx.file(
        "BUILD.bazel",
        "\n".join(build_content),
    )

_parchment_repo = repository_rule(
    implementation = _parchment_repo_impl,
    attrs = {
        "mc_version": attr.string(
            doc = "Minecraft version (e.g., 1.20.2)",
            mandatory = True,
        ),
        "parchment_version": attr.string(
            doc = "Parchment version (e.g., 2023.10.08)",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA-256 checksum of the Parchment ZIP file",
            mandatory = True,
        ),
    },
)

version = tag_class(
    attrs = {
        "mc_version": attr.string(
            doc = "Minecraft version (e.g., 1.20.2)",
            mandatory = True,
        ),
        "parchment_version": attr.string(
            doc = "Parchment version (e.g., 2023.10.08)",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA-256 checksum of the Parchment ZIP file",
            mandatory = True,
        ),
    },
)

def _parchment_impl(mctx):
    """Module extension implementation"""

    # Collect all version tags
    versions = {}
    for module in mctx.modules:
        for tag in module.tags.version:
            key = (tag.mc_version, tag.parchment_version)
            if key in versions:
                # Duplicate check
                existing = versions[key]
                if existing.sha256 != tag.sha256:
                    fail("Parchment version %s for MC %s already exists with different SHA-256" % (
                        tag.parchment_version,
                        tag.mc_version,
                    ))
            else:
                versions[key] = {
                    "mc_version": tag.mc_version,
                    "parchment_version": tag.parchment_version,
                    "sha256": tag.sha256,
                }

    # Create repository for each version
    for (mc_version, parchment_version), attrs in versions.items():
        # parchment_1_20_2_2023_10_08
        repo_name = "parchment_%s" % (
            mc_version.replace(".", "_") + "_" + parchment_version.replace(".", "_").replace("-", "_")
        )

        _parchment_repo(
            name = repo_name,
            mc_version = mc_version,
            parchment_version = parchment_version,
            sha256 = attrs["sha256"],
        )

parchment = module_extension(
    implementation = _parchment_impl,
    tag_classes = {
        "version": version,
    },
)
