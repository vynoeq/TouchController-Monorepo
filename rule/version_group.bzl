load("//rule:merge_library.bzl", "java_merge_library", "kt_merge_library")

def _expand_versioned_label(label, version):
    parts = label.split(":")
    if len(parts) == 2:
        path, target = parts
        return "%s:%s_%s" % (path, target, version)
    else:
        path = parts[0]
        segments = path.split("/")
        if len(segments) == 0:
            fail("Invalid label: %s" % label)
        target = segments[-1]
        return "%s:%s_%s" % (path, target, version)

def _expand_versioned_labels(labels, version):
    return [_expand_versioned_label(label, version) for label in labels]

def version_entry(
        deps = [],
        merge_deps = [],
        merge_only_deps = [],
        merge_runtime_deps = []):
    return struct(
        deps = deps,
        merge_deps = merge_deps,
        merge_only_deps = merge_only_deps,
        merge_runtime_deps = merge_runtime_deps,
    )

def versioned_kt_merge_library(
        name,
        versions,
        enabled_versions,
        deps = [],
        merge_deps = [],
        merge_only_deps = [],
        merge_runtime_deps = [],
        version_deps = {},
        version_merge_deps = {},
        versioned_merge_deps = [],
        **kwargs):
    targets = []
    for version in enabled_versions:
        version_entry = versions[version]
        deps_item = version_deps.get(version, [])
        merge_deps_item = version_merge_deps.get(version, [])
        versioned_merge_deps_expanded = _expand_versioned_labels(versioned_merge_deps, version)
        target = "%s_%s" % (name, version)
        targets.append(":" + target)
        kt_merge_library(
            name = target,
            deps = deps + version_entry.deps + deps_item,
            merge_deps = merge_deps + version_entry.merge_deps + merge_deps_item + versioned_merge_deps_expanded,
            merge_only_deps = merge_only_deps + version_entry.merge_only_deps,
            merge_runtime_deps = merge_runtime_deps + version_entry.merge_runtime_deps,
            **kwargs
        )
    native.filegroup(
        name = name,
        srcs = targets,
    )

def versioned_java_merge_library(
        name,
        versions,
        enabled_versions,
        deps = [],
        merge_deps = [],
        merge_only_deps = [],
        merge_runtime_deps = [],
        version_merge_deps = {},
        versioned_merge_deps = [],
        **kwargs):
    targets = []
    for version in enabled_versions:
        version_entry = versions[version]
        merge_deps_item = version_merge_deps.get(version, [])
        versioned_merge_deps_expanded = _expand_versioned_labels(versioned_merge_deps, version)
        target = "%s_%s" % (name, version)
        targets.append(":" + target)
        java_merge_library(
            name = target,
            deps = deps + version_entry.deps,
            merge_deps = merge_deps + version_entry.merge_deps + merge_deps_item + versioned_merge_deps_expanded,
            merge_only_deps = merge_only_deps + version_entry.merge_only_deps,
            merge_runtime_deps = merge_runtime_deps + version_entry.merge_runtime_deps,
            **kwargs
        )
    native.filegroup(
        name = name,
        srcs = targets,
    )
