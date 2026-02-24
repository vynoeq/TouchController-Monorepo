"""Repository rule to fetch Minecraft libraries"""

load("//private:bytes_util.bzl", _hex_sha1_to_sri = "hex_sha1_to_sri")

def _minecraft_library_repo_impl(rctx):
    rctx.download(
        url = rctx.attr.url,
        output = rctx.attr.path,
        integrity = _hex_sha1_to_sri(rctx.attr.sha1),
    )

    if rctx.attr.extract:
        build_content = """package(default_visibility = ["//visibility:public"])
load("@rules_mc//private/rules:extract_lib.bzl", "extract_lib")

extract_lib(
    name = "file",
    jar = "%s",
    excludes = %s,
)
""" % (rctx.attr.path, rctx.attr.extract_exclude)
    else:
        build_content = """package(default_visibility = ["//visibility:public"])

filegroup(
    name = "file",
    srcs = ["%s"],
)
""" % (rctx.attr.path)

    rctx.file("BUILD.bazel", build_content)

minecraft_library_repo = repository_rule(
    implementation = _minecraft_library_repo_impl,
    attrs = {
        "url": attr.string(mandatory = True),
        "sha1": attr.string(mandatory = True),
        "path": attr.string(mandatory = True),
        "extract": attr.bool(default = False),
        "extract_exclude": attr.string_list(default = []),
    },
)
