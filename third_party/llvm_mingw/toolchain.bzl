"""LLVM-MinGW toolchain definitions."""

load("@rules_cc//cc/toolchains:cc_toolchain.bzl", "cc_toolchain")
load(":config.bzl", "config")

def _llvm_mingw_toolchain_impl(name, visibility, include_files, bin_files, lib_files, triple, target_cpu, target_cpu_name, exec_compatible_with, execroot):
    native.filegroup(
        name = "%s_gcc" % name,
        srcs = [
            "bin/%s-c++" % triple,
            "bin/%s-g++" % triple,
            "bin/%s-gcc" % triple,
        ],
    )

    native.filegroup(
        name = "%s_clang" % name,
        srcs = [
            "bin/%s-clang" % triple,
            "bin/%s-clang++" % triple,
            "bin/clang-cpp",
        ],
    )

    native.filegroup(
        name = "%s_ld" % name,
        srcs = [
            "bin/%s-ld" % triple,
        ],
    )

    native.filegroup(
        name = "%s_include" % name,
        srcs = include_files,
    )

    native.filegroup(
        name = "%s_bin" % name,
        srcs = bin_files,
    )

    native.filegroup(
        name = "%s_lib" % name,
        srcs = lib_files,
    )

    native.filegroup(
        name = "%s_ar" % name,
        srcs = ["bin/%s-ar" % triple],
    )

    native.filegroup(
        name = "%s_as" % name,
        srcs = ["bin/%s-as" % triple],
    )

    native.filegroup(
        name = "%s_nm" % name,
        srcs = ["bin/%s-nm" % triple],
    )

    native.filegroup(
        name = "%s_objcopy" % name,
        srcs = ["bin/%s-objcopy" % triple],
    )

    native.filegroup(
        name = "%s_objdump" % name,
        srcs = ["bin/%s-objdump" % triple],
    )

    native.filegroup(
        name = "%s_ranlib" % name,
        srcs = ["bin/%s-ranlib" % triple],
    )

    native.filegroup(
        name = "%s_strip" % name,
        srcs = ["bin/%s-strip" % triple],
    )

    native.filegroup(
        name = "%s_ar_files" % name,
        srcs = [
            ":%s_ar" % name,
            "//:wrapper_files",
        ],
    )

    native.filegroup(
        name = "%s_as_files" % name,
        srcs = [
            ":%s_as" % name,
            "//:wrapper_files",
        ],
    )

    native.filegroup(
        name = "%s_compiler_files" % name,
        srcs = [
            ":%s_bin" % name,
            ":%s_include" % name,
            "//:wrapper_files",
        ],
    )

    native.filegroup(
        name = "%s_linker_files" % name,
        srcs = [
            ":%s_ar" % name,
            ":%s_clang" % name,
            ":%s_gcc" % name,
            ":%s_ld" % name,
            ":%s_lib" % name,
            "//:wrapper_files",
        ],
    )

    native.filegroup(
        name = "%s_objcopy_files" % name,
        srcs = [
            ":%s_objcopy" % name,
            "//:wrapper_files",
        ],
    )

    native.filegroup(
        name = "%s_strip_files" % name,
        srcs = [
            ":%s_strip" % name,
            "//:wrapper_files",
        ],
    )

    native.filegroup(
        name = "%s_all_files" % name,
        srcs = [
            ":%s_bin" % name,
            ":%s_compiler_files" % name,
            ":%s_linker_files" % name,
        ],
    )

    config(
        name = "%s_config" % name,
        triple = triple,
        target_cpu = target_cpu_name,
        execroot = execroot,
    )

    cc_toolchain(
        name = "%s_cc_toolchain" % name,
        all_files = ":%s_all_files" % name,
        ar_files = ":%s_ar_files" % name,
        as_files = ":%s_as_files" % name,
        compiler_files = ":%s_compiler_files" % name,
        dwp_files = "//:empty",
        linker_files = ":%s_linker_files" % name,
        objcopy_files = ":%s_objcopy_files" % name,
        strip_files = ":%s_strip_files" % name,
        supports_param_files = 1,
        toolchain_config = ":%s_config" % name,
        toolchain_identifier = "llvm-mingw-%s" % triple,
    )

    native.toolchain(
        name = name,
        exec_compatible_with = exec_compatible_with,
        target_compatible_with = [
            target_cpu,
            "@platforms//os:windows",
        ],
        target_settings = None,
        toolchain = ":%s_cc_toolchain" % name,
        toolchain_type = "@bazel_tools//tools/cpp:toolchain_type",
        visibility = visibility,
    )

_llvm_mingw_toolchain_symbol = macro(
    attrs = {
        "triple": attr.string(mandatory = True, configurable = False),
        "target_cpu": attr.label(mandatory = True, configurable = False),
        "target_cpu_name": attr.string(mandatory = True, configurable = False),
        "exec_compatible_with": attr.label_list(mandatory = True, configurable = False),
        "execroot": attr.string(mandatory = True, configurable = False),
        "include_files": attr.label_list(mandatory = True),
        "bin_files": attr.label_list(mandatory = True),
        "lib_files": attr.label_list(mandatory = True),
    },
    implementation = _llvm_mingw_toolchain_impl,
)

def llvm_mingw_toolchain(name, triple, **kwargs):
    _llvm_mingw_toolchain_symbol(
        name = name,
        triple = triple,
        include_files = native.glob([
            "%s/include/**" % triple,
            "lib/clang/*/include/**",
        ], allow_empty = True),
        bin_files = native.glob([
            "bin/**",
            "%s/**" % triple,
        ]),
        lib_files = native.glob([
            "lib/**/lib*.a",
            "lib/clang/*/lib/**/*.a",
            "%s/lib/*.a" % triple,
        ]),
        **kwargs
    )
