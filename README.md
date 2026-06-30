# 抖音风格躲避逃跑小游戏

这是一个使用 Java Swing 编写的休闲小游戏：玩家操控角色在逃跑过程中躲避危险物，并尽可能吃到金币获得积分。

## 运行方式

确保已安装 JDK 17 或更高版本，然后执行：

```bash
javac -encoding UTF-8 -d out src/main/java/com/zhaohui/game/*.java
java -cp out com.zhaohui.game.DouyinRunnerGame
```

## 操作说明

- `←` / `A`：向左移动
- `→` / `D`：向右移动
- `↑` / `W`：向上移动
- `↓` / `S`：向下移动
- `空格`：游戏结束后重新开始

## 游戏规则

- 躲开红色危险物，碰到危险物游戏结束。
- 吃到金币可获得积分奖励。
- 生存时间越久，分数越高。
- 游戏速度会随着时间逐步提升。
