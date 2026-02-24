# 环境配置

项目主要支持在 Linux 上编译，不过大部分内容也可以在 Windows 上编译。具体环境配置的步骤如下：

## 安装 Bazel

建议采用 [Bazelisk](https://bazel.build/install/bazelisk) 安装 Bazel，以获得自动更新功能。

如果是在 Windows 上构建，你还需要安装 [MSYS2](https://www.msys2.org/) 和 [Visual Studio Build Tools](https://aka.ms/buildtools)。

## 安装 Android SDK & NDK

项目中有部分 Android 相关代码要求 Android SDK 和 NDK。

你需要安装一份 Android SDK，包括 build-tools 和 platform，还需要安装一份 NDK。具体安装方法这里不再赘述。

安装好后，设置 ANDROID_HOME 和 ANDROID_NDK_HOME 环境变量，指向安装好的 Android SDK 和 NDK 路径即可。

## 准备就绪

接下来你就可以尝试编译了。

例如，运行以下命令可以编译 ArmorStand：

```shell
bazel build //armorstand:mod_fabric //armorstand:mod_neoforge
```

你可能需要把上面命令的 bazel 换成 bazelisk 才能运行。
