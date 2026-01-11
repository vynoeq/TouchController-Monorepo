"Module extension to download and configure LLVM-MinGW toolchains"

def _llvm_mingw_toolchain_impl(rctx):
    build_file_header = rctx.read(Label("@//third_party/llvm_mingw:BUILD.llvm_mingw_toolchain.bazel"))
    build_file = build_file_header + "\n".join([
        "\n".join([
            "",
            "llvm_mingw_toolchain(",
            '    name = "%s_toolchain",' % name,
            '    target_cpu = "@platforms//cpu:%s",' % name,
            '    target_cpu_name = "%s",' % name,
            '    exec_compatible_with = %s,' % rctx.attr.exec_compatible_with,
            '    execroot = "%s",' % rctx.path(""),
            '    triple = "%s",' % triple,
            '    visibility = ["//visibility:public"],',
            ")",
        ])
        for name, triple in rctx.attr.targets.items()
    ])
    rctx.file(
        "BUILD.bazel",
        content = build_file,
        executable = False,
    )

    wrapper_files = ["ar", "as", "cpp", "gcc", "g++", "gcov", "ld", "nm", "objcopy", "ranlib", "strip"]
    for triple in rctx.attr.targets.values():
        for wrapper_file in wrapper_files:
            real_tool = triple + "-" + wrapper_file
            rctx.template(
                "wrapper/" + real_tool,
                Label("@//third_party/llvm_mingw:wrapper.sh"),
                substitutions = {
                    "%{REAL_TOOL}": real_tool,
                },
            )

    rctx.download_and_extract(
        url = rctx.attr.url,
        sha256 = rctx.attr.sha256,
        strip_prefix = rctx.attr.strip_prefix,
        type = rctx.attr.type,
    )

_llvm_mingw_toolchain = repository_rule(
    implementation = _llvm_mingw_toolchain_impl,
    attrs = {
        "url": attr.string(
            doc = "URL to download the toolchain from",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA256 hash of the toolchain archive",
            mandatory = True,
        ),
        "strip_prefix": attr.string(
            doc = "Prefix to strip from the toolchain archive",
            mandatory = True,
        ),
        "type": attr.string(
            doc = "Archive type, such as .zip or .tar.xz",
            mandatory = True,
        ),
        "targets": attr.string_dict(
            doc = "Toolchain chain to triple mappings",
            mandatory = True,
        ),
        "exec_compatible_with": attr.string_list(
            doc = "Executable constraints",
            mandatory = True,
        ),
    },
)

toolchain = tag_class(
    attrs = {
        "name": attr.string(
            doc = "Generated repository name",
            mandatory = True,
        ),
        "url": attr.string(
            doc = "URL to download the toolchain from",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA256 hash of the toolchain archive",
            mandatory = True,
        ),
        "strip_prefix": attr.string(
            doc = "Prefix to strip from the toolchain archive",
            mandatory = True,
        ),
        "type": attr.string(
            doc = "Archive type, such as .zip or .tar.xz",
            mandatory = True,
        ),
        "targets": attr.string_dict(
            doc = "Toolchain chain to triple mappings",
            mandatory = True,
        ),
        "exec_compatible_with": attr.string_list(
            doc = "Executable constraints",
            mandatory = True,
        ),
    },
)

def _llvm_mingw_impl(mctx):
    toolchain_entries = {}
    for mod in mctx.modules:
        for toolchain in mod.tags.toolchain:
            if toolchain.name in toolchain_entries:
                entry = toolchain_entries[toolchain.name]
                if toolchain.url != entry.url:
                    fail("Toolchain URL mismatch for %s: %s != %s" % (toolchain.name, toolchain.url, entry.url))
                if toolchain.sha256 != entry.sha256:
                    fail("Toolchain SHA256 mismatch for %s: %s != %s" % (toolchain.name, toolchain.sha256, entry.sha256))
                if toolchain.strip_prefix != entry.strip_prefix:
                    fail("Toolchain strip_prefix mismatch for %s: %s != %s" % (toolchain.name, toolchain.strip_prefix, entry.strip_prefix))
                if toolchain.type != entry.type:
                    fail("Toolchain type mismatch for %s: %s != %s" % (toolchain.name, toolchain.type, entry.type))
                if toolchain.targets != entry.targets:
                    fail("Toolchain targets mismatch for %s: %s != %s" % (toolchain.name, toolchain.targets, entry.targets))
                if toolchain.exec_compatible_with != entry.exec_compatible_with:
                    fail("Toolchain exec_compatible_with mismatch for %s: %s != %s" % (toolchain.name, toolchain.exec_compatible_with, entry.exec_compatible_with))
            else:
                toolchain_entries[toolchain.name] = struct(
                    url = toolchain.url,
                    sha256 = toolchain.sha256,
                    strip_prefix = toolchain.strip_prefix,
                    type = toolchain.type,
                    targets = toolchain.targets,
                    exec_compatible_with = toolchain.exec_compatible_with,
                )
    for name, toolchain in toolchain_entries.items():
        _llvm_mingw_toolchain(
            name = name,
            url = toolchain.url,
            sha256 = toolchain.sha256,
            strip_prefix = toolchain.strip_prefix,
            type = toolchain.type,
            targets = toolchain.targets,
            exec_compatible_with = toolchain.exec_compatible_with,
        )

llvm_mingw = module_extension(
    implementation = _llvm_mingw_impl,
    tag_classes = {
        "toolchain": toolchain,
    },
)