#!/bin/bash
# 部署脚本

cd /tmp

# 1. 创建数据库表
echo "创建数据库表..."
mysql -u fish -p'qtz#923812' -h 127.0.0.1 -P 3306 quntui_bao < group_message.sql

# 2. 停止旧服务
echo "停止旧服务..."
pkill -f 'java.*quntui-bao' 2>/dev/null
pkill -f 'python.*bot.py' 2>/dev/null
sleep 2

# 3. 启动新服务
echo "启动新服务..."
nohup java -jar -Dspring.profiles.active=prod quntui-bao-1.0.0.jar > /tmp/quntui-bao.log 2>&1 &

sleep 5
echo "部署完成，检查状态..."
ps aux | grep java | grep -v grep
ps aux | grep python | grep -v grep
tail -20 /tmp/quntui-bao.log
