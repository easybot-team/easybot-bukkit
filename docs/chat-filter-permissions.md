# 消息过滤与命令权限

在 `plugins/EasyBot/config.yml` 中配置后运行 `/easybot reload`。旧配置省略新增项时保持原有行为；配置有语法错误或无效规则时重载失败，继续使用上一份有效配置。

```yaml
chat_filter:
  game_to_group:
    max_length: 100
    blocked_keywords: ["广告词"]
    blocked_player_names: ["PlayerName"]
    blocked_player_uuids: []
  group_to_game:
    max_length: 200
    blocked_keywords: ["广告词"]
```

默认长度为 `0`（不限）、列表为空。命中规则后整条消息不转发，不截断；游戏内聊天不受影响。关键词按普通文本包含匹配，忽略大小写；颜色代码不参与匹配或计数，长度按 Unicode 码点计算，组合 emoji 可能占多个码点。

游戏到群按发送包的 `player_name_raw` 和 UUID 屏蔽玩家，不使用显示昵称；玩家名忽略大小写，UUID 使用带连字符的完整格式。Floodgate 玩家沿用现有身份转换规则。原生 Bukkit、Paper、PlayerChat、RedisChat、VentureChat 以及 `/esay` 使用同一套过滤规则。

群到游戏同时检查主程序发送的纯文本和富文本段的文本表示，可能包含昵称、模板前缀和图片说明；不读取图片内容。连续文本段不能绕过关键词检查。过滤发生在显示、图片处理、@提醒和降级发送之前。此方向未收到发言人身份，因此不支持按账号/玩家名单过滤。

`/esay` 被拒绝时会明确提示，不显示成功、不扣 Vault 金币；控制台只受内容和长度限制。`skip_options.skip_chat` 仍仅控制自动聊天同步。

## 命令配置

```yaml
command:
  allow_bind: true
  enabled:
    help: true
    bind: true
    confirm: true
    status: true
    reload: true
    config: true
    esay: true
```

| 命令 | 权限节点 | 默认 |
| --- | --- | --- |
| `/easybot`、`/easybot help` | `easybot.command` | 所有人 |
| `/easybot bind`、`/easybot bind confirm` | `easybot.command` 和 `easybot.command.bind` | 所有人 |
| `/easybot confirm <code>` | 同上 | 所有人 |
| `/easybot status`（绑定状态） | 同上 | 所有人 |
| `/easybot reload` | `easybot.command` 和 `easybot.command.reload` | OP |
| `/easybot config <配置项> [秒数]` | `easybot.command` 和 `easybot.command.config` | OP |
| `/esay <消息>` | `easybot.command.esay` | 所有人 |

沿用 Bukkit 权限系统，可用现有权限插件授予或拒绝节点。重载权限不再硬编码为 OP。关闭 `allow_bind` 或禁用 `bind` 同时阻止确认绑定；`status` 可独立保留。帮助及补全只列出已开启且有权限的命令。禁用 `reload` 后需修改文件并重启插件/服务器恢复，建议保留此管理入口。
