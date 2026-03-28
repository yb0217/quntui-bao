# 群推宝 Python 机器人

## 功能
1. **监听新成员加入** - 自动发送欢迎消息
2. **定时发送广告** - 根据配置的间隔自动发送广告
3. **TG限流保护** - 按每分钟20条限制发送

## 配置

### 1. 环境变量
```bash
# TG Bot Token（从 @BotFather 获取）
export BOT_TOKEN="your_bot_token_here"

# TG API 配置（从 my.telegram.org 获取）
export API_ID=12345678
export API_HASH="your_api_hash"

# Java API 地址
export API_URL="http://localhost:8083"
```

### 2. 安装依赖
```bash
cd python
pip install -r requirements.txt
```

### 3. 运行
```bash
python bot.py
```

## 目录结构
```
python/
├── __init__.py      # 模块入口
├── bot.py           # 主程序
├── api_client.py    # API 通信
├── config.py        # 配置
└── requirements.txt # 依赖
```

## 工作原理

1. **启动时**
   - 连接 TG 机器人
   - 注册群事件监听器
   - 启动广告定时任务

2. **新成员加入**
   - 监听群事件
   - 调用 Java API 获取群配置
   - 发送欢迎消息
   - 记录发送日志

3. **定时广告**
   - 每分钟检查所有启用了广告的群组
   - 调用限流检查 API
   - 发送广告消息（带按钮）
   - 记录发送日志

## 按钮配置格式

在管理后台配置的按钮会自动转换为 TG Inline 按钮：
```json
[
  {"text": "加入社群", "type": "url", "value": "https://t.me/xxx"},
  {"text": "咨询客服", "type": "chat", "value": "https://t.me/xxx"}
]
```