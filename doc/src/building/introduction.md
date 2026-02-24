# 介绍

虽然大部分 Minecraft 模组采用 Gradle，但是本项目采用 Bazel 构建系统。主要原因有：

- 在同一套系统内，完成 Fabric、NeoForge、Forge（待完善）的多加载器构建
- 更方便编写自定义规则，相对于 Gradle 更不容易出错
- 采用 Aspects，为构建时做一些更激进的优化
- 方便构建 C++、Rust 等多语言的库，也方便交叉编译 Windows、Linux、Android 等平台的库

不过，Minecraft modding 社区生态几乎都在 Gradle 上，因此我们需要自己从头在 Bazel 上从头编写一套自定义规则，满足 Minecraft 模组开发的需求。

这部分文档旨在介绍自定义规则的使用，以及如何使用这些规则来准备模组开发环境、构建模组、发布模组。
