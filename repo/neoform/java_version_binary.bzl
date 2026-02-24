load("@rules_java//java:java_binary.bzl", "java_binary")
load("@with_cfg.bzl", "with_cfg")

java_8_binary, _java_8_binary_internal = with_cfg(java_binary).set("java_language_version", "8").set("java_runtime_version", "remotejdk_8").build()
java_17_binary, _java_17_binary_internal = with_cfg(java_binary).set("java_language_version", "21").set("java_runtime_version", "remotejdk_21").build()
java_21_binary, _java_21_binary_internal = with_cfg(java_binary).set("java_language_version", "21").set("java_runtime_version", "remotejdk_21").build()
java_25_binary, _java_25_binary_internal = with_cfg(java_binary).set("java_language_version", "25").set("java_runtime_version", "remotejdk_25").build()
java_16_binary = java_21_binary
