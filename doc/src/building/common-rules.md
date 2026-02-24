# 常用规则

如[构建流水线](./building-pipeline.md)所述，构建 Minecraft 模组需要一些复杂的流程，这些操作常常被封装在 Fabric Loom、NeoGradle、
ModDevGradle 或者是 ForgeGradle 中，而且每个插件的配置方法还不一致，为开发多加载器模组带来了不必要的麻烦。

从 Gradle 侧吸取经验，项目选择了包装主要来自于 Fabric 的工具，并提供对 Bazel 友好的构建规则：

- extract_jar：从 JAR 内提取指定的文件。常用来从 Yarn、Intermediary 的 JAR 内提取映射。
- merge_mapping：按一定的规则，合并多个映射文件。用于将官方映射、Intermediary 映射、Yarn 映射等映射表合并，并输出一个 Tiny v2 映射文件。
- remap_jar：重映射指定的输入 JAR 文件。

另外的，还有一些特定于模组加载器的规则：

- fabric_merge_jij：合并给定的 JAR，得到一个 Fabric Jar-In-Jar 模组。
- apply_access_widener：应用 Access Widener 到给定的 JAR 文件上。
- extract_access_widener：从给定的 JAR 文件提取 Access Widener 文件。
- remap_access_widener：重映射给定的 Access Widener 文件。
- neoforge_merge_jij：合并给定的 JAR，得到一个 Forge / NeoForge Jar-In-Jar 模组。
- convert_access_widener：把 Fabric 的 Access Widener 转换为 Forge / NeoForge 的 Access Transformer。

通过组合以上的规则，我们能构建一个完整的 Minecraft 模组构建流水线。
