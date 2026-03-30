#!/usr/bin/env python3
"""
群推宝配置
"""
import os
from socks import SOCKS5

class Config:
    """配置类"""
    # TG Bot 配置
    BOT_TOKEN = os.environ.get('BOT_TOKEN', '8380175815:AAG-ynueRiEkNWDFULpc8VNhRD5b3B3f0Cc')
    BOT_USERNAME = os.environ.get('BOT_USERNAME', '')
    
    # Telethon 配置（用于监听群事件）
    API_ID = int(os.environ.get('API_ID', '21791619'))
    API_HASH = os.environ.get('API_HASH', '282e7c380c97f10391003d88c48701fe')
    
    # SOCKS5 代理配置
    PROXY = (SOCKS5, '127.0.0.1', 7890)
    
    # Java API 地址
    API_URL = os.environ.get('API_URL', 'http://localhost:8083')
    
    # 日志配置
    LOG_LEVEL = os.environ.get('LOG_LEVEL', 'INFO')