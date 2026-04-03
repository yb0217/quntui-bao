# 群推宝 TODO

## 2026-04-02 完成

### 代码修改
- [x] 新增 BotStarterService.java - 自动启动 Python 机器人
- [x] 修改 application.yml - 添加配置
- [x] 修改 application-prod.yml - 生产环境配置
- [x] 新增 deploy.sh - 部署脚本

### 明日待办（2026-04-03）
- [ ] 打包：`./mvnw clean package -DskipTests`
- [ ] 部署到生产环境
- [ ] 验证 Python 机器人自动启动
- [ ] 测试广告发送功能

## 问题记录

### 已修复
- Python 机器人未自动启动 → 添加 BotStarterService

### 待验证
- 广告是否正常发送
- 欢迎消息是否正常
