#!/bin/bash
cd /tmp
pkill -f 'java.*quntui-bao' 2>/dev/null
sleep 2
nohup java -jar -Dspring.profiles.active=prod quntui-bao-1.0.0.jar > /tmp/quntui-bao.log 2>&1 &
echo "启动中，查看日志: tail -f /tmp/quntui-bao.log"
