#!/usr/bin/env python3
"""
群推宝配置
"""
import os

# TG Bot 配置
BOT_TOKEN = os.environ.get('BOT_TOKEN', '')
BOT_USERNAME = os.environ.get('BOT_USERNAME', '')

# Telethon 配置（用于监听群事件）
API_ID = int(os.environ.get('API_ID', '12345678'))
API_HASH = os.environ.get('API_HASH', '')

# Java API 地址
API_URL = os.environ.get('API_URL', 'http://localhost:8083')

# 日志配置
LOG_LEVEL = os.environ.get('LOG_LEVEL', 'INFO')