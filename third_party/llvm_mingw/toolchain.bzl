"""Filegroups generation macro - only generates filegroups."""

load(":config.bzl", "config")
load("@rules_cc//cc/toolchains:cc_toolchain.bzl", "cc_toolchain")

def _llvm_mingw_toolchain_impl(name, visibility, include_files, bin_files, lib_files, triple, target_cpu_name, execroot, binary_extension, use_wrapper):
    wrapper_files = ["//:wrapper_files"] if use_wrapper else []

    native.filegroup(
        name = "%s_gcc" % name,
        visibility = visibility,
        srcs = [
            "bin/%s-c++%s" % (triple, binary_extension),
            "bin/%s-g++%s" % (triple, binary_extension),
            "bin/%s-gcc%s" % (triple, binary_extension),
        ],
    )

    native.filegroup(
        name = "%s_clang" % name,
        visibility = visibility,
        srcs = [
            "bin/%s-clang%s" % (triple, binary_extension),
            "bin/%s-clang++%s" % (triple, binary_extension),
            "bin/clang%s" % binary_extension,
        ],
    )

    native.filegroup(
        name = "%s_ld" % name,
        visibility = visibility,
        srcs = [
            "bin/%s-ld" % triple,
        ],
    )

    native.filegroup(
        name = "%s_include" % name,
        visibility = visibility,
        srcs = include_files,
    )

    native.filegroup(
        name = "%s_bin" % name,
        visibility = visibility,
        srcs = bin_files,
    )

    native.filegroup(
        name = "%s_lib" % name,
        visibility = visibility,
        srcs = lib_files,
    )

    native.filegroup(
        name = "%s_ar" % name,
        visibility = visibility,
        srcs = ["bin/%s-ar%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_as" % name,
        visibility = visibility,
        srcs = ["bin/%s-as%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_nm" % name,
        visibility = visibility,
        srcs = ["bin/%s-nm%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_objcopy" % name,
        visibility = visibility,
        srcs = ["bin/%s-objcopy%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_objdump" % name,
        visibility = visibility,
        srcs = ["bin/%s-objdump%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_ranlib" % name,
        visibility = visibility,
        srcs = ["bin/%s-ranlib%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_strip" % name,
        visibility = visibility,
        srcs = ["bin/%s-strip%s" % (triple, binary_extension)],
    )

    native.filegroup(
        name = "%s_ar_files" % name,
        visibility = visibility,
        srcs = [
            ":%s_ar" % name,
        ] + wrapper_files,
    )

    native.filegroup(
        name = "%s_as_files" % name,
        visibility = visibility,
        srcs = [
            ":%s_as" % name,
        ] + wrapper_files,
    )

    native.filegroup(
        name = "%s_compiler_files" % name,
        visibility = visibility,
        srcs = [
            ":%s_bin" % name,
            ":%s_include" % name,
        ] + wrapper_files,
    )

    native.filegroup(
        name = "%s_linker_files" % name,
        visibility = visibility,
        srcs = [
            ":%s_ar" % name,
            ":%s_clang" % name,
            ":%s_gcc" % name,
            ":%s_ld" % name,
            ":%s_lib" % name,
        ] + wrapper_files,
    )

    native.filegroup(
        name = "%s_objcopy_files" % name,
        visibility = visibility,
        srcs = [
            ":%s_objcopy" % name,
        ] + wrapper_files,
    )

    native.filegroup(
        name = "%s_strip_files" % name,
        visibility = visibility,
        srcs = [
            ":%s_strip" % name,
        ] + wrapper_files,
    )

    native.filegroup(
        name = "%s_all_files" % name,
        visibility = visibility,
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
        binary_extension = binary_extension,
        use_wrapper = use_wrapper,
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
        supports_param_files = True,
        supports_header_parsing = True,
        toolchain_config = ":%s_config" % name,
        toolchain_identifier = "llvm-mingw-%s" % triple,
    )

_llvm_mingw_toolchain_symbol = macro(
    attrs = {
        "triple": attr.string(mandatory = True, configurable = False),
        "target_cpu_name": attr.string(mandatory = True, configurable = False),
        "execroot": attr.string(mandatory = True, configurable = False),
        "include_files": attr.label_list(mandatory = True),
        "bin_files": attr.label_list(mandatory = True),
        "lib_files": attr.label_list(mandatory = True),
        "binary_extension": attr.string(mandatory = True, configurable = False),
        "use_wrapper": attr.bool(default = True, configurable = False),
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
