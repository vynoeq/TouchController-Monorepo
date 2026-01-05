# TouchController Monorepo

这是 TouchController 组织的代码集中存储仓库。这里有：

- TouchController：一款 Minecraft：Java 版的模组，添加了类基岩版的触摸控制系统。
- Combine：一个为 Minecraft：Java 版制作的 GUI 库。支持以 Compose 的风格构建跨 Minecraft 版本的 UI。
- BlazeRod：一个为 Minecraft：Java 版制作的模型渲染库。支持渲染 glTF、PMX 等复杂模型，也支持渲染基岩版实体模型，并支持骨骼动画、形态键（变形目标）等高级渲染特性。
- ArmorStand：一款 Minecraft：Java 版的模型渲染库，使用 BlazeRod 进行渲染，支持替换玩家模型。
- AuthProxy: 一款 Minecraft：Java 版的模组，允许为服务器验证使用代理。
- PathFlow: 一个 Kotlin 库，用于定义数据的特征和转换规则，从而自动求解转换路径，完成数据转换。计划用于 BlazeRod 库，以自动完成不同格式的动画转换。

## 目前状态

整体项目正在从各自的仓库逐步迁移到本仓库中。进度大致如下：

- Combine：迁移中
- TouchController：暂未迁移
- TouchControllerWiki：迁移完毕
- BlazeRod：迁移完毕
- ArmorStand：迁移完毕
- AuthProxy：迁移完毕

待所有代码迁移后，这个仓库会和 TouchController 仓库合并作为一个分支，然后这个仓库会进入归档状态。

---

# TouchController Monorepo

This is the central repository of TouchController organization. Now there are:

- TouchController: A mod for Minecraft: Java Edition, adding touch control system like the Bedrock Edition.
- Combine: A GUI library for Minecraft: Java Edition. It supports building cross-version Minecraft UIs in a Compose-like style.
- BlazeRod: A model rendering library for Minecraft: Java Edition. It supports rendering complex models such as glTF and PMX, as well as Bedrock Edition entity models, and includes advanced rendering features like skeletal animation and morph targets (shape keys).
- ArmorStand: A model rendering library for Minecraft: Java Edition, using BlazeRod for rendering, and supports replacing player models.
- AuthProxy: A mod for Minecraft: Java Edition, allowing using proxies for server authentication.
- PathFlow: A Kotlin library for defining data features and transformation rules, thereby automatically solving transformation paths and completing data transformations. It is planned to be used in the BlazeRod library to automate animation conversions of different formats.

## Current Status

Projects are gradually being migrated from their respective repositories to here. The progress is roughly as follows:

- Combine: Migrating
- TouchController: Not yet migrated
- TouchControllerWiki：Migrated
- BlazeRod: Migrated
- ArmorStand: Migrated
- AuthProxy: Migrated

After all code is migrated, this repository will be merged with the TouchController repository as a branch, and this repository will be archived.
