#!/usr/bin/env python3
"""
与 Java 后端 API 通信的客户端
"""
import os
import sys
import json
import requests
from telethon import TelegramClient

# 尝试导入配置，如果不存在则使用默认值
try:
    from .config import Config
except ImportError:
    class Config:
        BOT_TOKEN = os.environ.get('BOT_TOKEN', '')
        API_ID = int(os.environ.get('API_ID', '12345678'))
        API_HASH = os.environ.get('API_HASH', 'abcdef123456')


class QuntuiAPIClient:
    def __init__(self, base_url="http://localhost:8083"):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()
        self.session.cookies.set('JSESSIONID', 'mock-session')
    
    def get_telegram_client(self):
        """获取 Telethon 客户端"""
        return TelegramClient(
            'quntui_bot',
            Config.API_ID,
            Config.API_HASH,
            bot_token=Config.BOT_TOKEN
        )
    
    # ========== 群组管理 ==========
    
    def get_groups(self, keyword=None):
        """获取群组列表"""
        url = f"{self.base_url}/api/groups"
        if keyword:
            url += f"?keyword={keyword}"
        
        try:
            resp = self.session.get(url)
            data = resp.json()
            return data.get('data', [])
        except Exception as e:
            print(f"获取群组失败: {e}")
            return []
    
    def get_group(self, group_id):
        """获取单个群组"""
        # 先获取所有群组，然后查找
        groups = self.get_groups()
        for g in groups:
            if g.get('groupId') == group_id:
                return g
        return None
    
    def create_group(self, group_data):
        """创建群组"""
        url = f"{self.base_url}/api/groups"
        try:
            resp = self.session.post(url, json=group_data)
            return resp.json()
        except Exception as e:
            print(f"创建群组失败: {e}")
            return None
    
    def update_group(self, group_id, group_data):
        """更新群组"""
        url = f"{self.base_url}/api/groups/{group_id}"
        try:
            resp = self.session.put(url, json=group_data)
            return resp.json()
        except Exception as e:
            print(f"更新群组失败: {e}")
            return None
    
    def update_group_last_ad_time(self, group_id):
        """更新群组最后发送广告时间"""
        group = self.get_group(group_id)
        if group:
            from datetime import datetime
            group['lastAdTime'] = datetime.now().isoformat()
            return self.update_group(group_id, group)
        return None
    
    # ========== 欢迎消息 ==========
    
    def get_welcome_templates(self, keyword=None):
        """获取欢迎消息模板"""
        url = f"{self.base_url}/api/welcome"
        if keyword:
            url += f"?keyword={keyword}"
        
        try:
            resp = self.session.get(url)
            data = resp.json()
            return data.get('data', [])
        except Exception as e:
            print(f"获取欢迎模板失败: {e}")
            return []
    
    # ========== 广告消息 ==========
    
    def get_ad_messages(self, keyword=None, enabled_only=True):
        """获取广告消息"""
        url = f"{self.base_url}/api/ad-messages"
        if keyword:
            url += f"?keyword={keyword}"
        
        try:
            resp = self.session.get(url)
            data = resp.json()
            messages = data.get('data', [])
            
            if enabled_only:
                messages = [m for m in messages if m.get('enabled', False)]
            
            return messages
        except Exception as e:
            print(f"获取广告消息失败: {e}")
            return []
    
    # ========== 限流检查 ==========
    
    def check_can_send(self, group_id):
        """检查是否可以发送广告"""
        url = f"{self.base_url}/api/send/check/{group_id}"
        
        try:
            resp = self.session.get(url)
            return resp.json()
        except Exception as e:
            print(f"检查限流失败: {e}")
            return {'canSend': True}
    
    # ========== 发送日志 ==========
    
    def record_send_log(self, group_id, target_user_id, message_type, 
                       ad_message_id=None, content='', success=True, error_msg=None):
        """记录发送日志"""
        url = f"{self.base_url}/api/send/record"
        
        data = {
            'groupId': group_id,
            'targetUserId': target_user_id,
            'messageType': message_type,
            'content': content,
            'success': success
        }
        
        if ad_message_id:
            data['adMessageId'] = ad_message_id
        
        if error_msg:
            data['errorMsg'] = error_msg
        
        try:
            self.session.post(url, json=data)
        except Exception as e:
            print(f"记录日志失败: {e}")


# 测试用
if __name__ == '__main__':
    client = QuntuiAPIClient()
    groups = client.get_groups()
    print(f"群组数量: {len(groups)}")