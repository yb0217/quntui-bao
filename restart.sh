#!/bin/bash
pkill -f 'java.*quntui-bao' 2>/dev/null
sleep 2
cd /tmp
nohup java -jar -Dspring.profiles.active=prod quntui-bao-1.0.0.jar > /tmp/quntui-bao.log 2>&1 &
sleep 5
ps aux | grep java | grep -v grep
tail -20 /tmp/quntui-bao.log
