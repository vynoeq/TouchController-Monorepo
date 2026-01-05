# TouchController 中的锚点机制

<!-- ANCHOR: p1 -->
锚点机制旨在适配不同大小的屏幕，使得控件布局在任何设备上都可用。
<!-- ANCHOR_END: p1 -->

## 锚点机制

<!-- ANCHOR: p2 -->
- 锚点共有 9 种，分别为屏幕四角、屏幕四边的中心、屏幕中心。
- 无论画面边框如何变化，控件相对其锚点的位置始终不变。
<!-- ANCHOR_END: p2 -->

## 自动锚点

> [!WARNING]
> 自动锚点功能正在开发中！[#318](https://github.com/TouchController/TouchController/issues/318)

- 自动锚点通常是默认开启的。
- 开启自动锚点的控件能够根据自身与锚点的距离自动切换锚点。
