#!/usr/bin/env python3
"""
与 Java 后端 API 通信的客户端
"""
import os
import sys
import json
import time
import requests
from telethon import TelegramClient
from socks import SOCKS5


# 从环境变量获取配置
def get_config():
    """获取 Telegram 配置"""
    from socks import SOCKS5
    return {
        'api_id': int(os.environ.get('API_ID', '21791619')),
        'api_hash': os.environ.get('API_HASH', '282e7c380c97f10391003d88c48701fe'),
        'proxy': (SOCKS5, '127.0.0.1', 7890) if os.environ.get('PROXY_ENABLED', 'false').lower() == 'true' else None
    }


class QuntuiAPIClient:
    # 类变量：所有实例共享缓存
    _group_cache = {}           # 群组缓存 {group_id: group_data}
    _all_groups_cache = []     # 全部群组列表缓存
    _welcome_cache = []         # 欢迎模板缓存
    _ad_messages_cache = []     # 广告消息缓存
    _initialized = False         # 是否已初始化
    
    def __init__(self, base_url="http://localhost:8083"):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()
        self.session.cookies.set('JSESSIONID', 'mock-session')
    
    def get_telegram_client(self):
        """获取 Telethon 客户端"""
        cfg = get_config()
        return TelegramClient(
            'quntui_bot',
            cfg['api_id'],
            cfg['api_hash'],
            proxy=cfg['proxy']
        )
    
    def get_telegram_client_with_session(self, session_path):
        """获取 Telethon 客户端（指定session路径）"""
        cfg = get_config()
        return TelegramClient(
            session_path,
            cfg['api_id'],
            cfg['api_hash'],
            proxy=cfg['proxy']
        )
    
    def initialize_cache(self):
        """启动时加载全量数据到缓存"""
        if self._initialized:
            return
        
        print("📦 初始化缓存数据...")
        
        # 加载群组
        self._all_groups_cache = self._fetch_groups()
        for g in self._all_groups_cache:
            self._group_cache[g['groupId']] = g
        
        # 加载欢迎模板
        self._welcome_cache = self._fetch_welcome_templates()
        
        # 加载广告消息
        self._ad_messages_cache = self._fetch_ad_messages()
        
        self._initialized = True
        print(f"✅ 缓存初始化完成: {len(self._group_cache)} 个群组")
    
    def _fetch_groups(self):
        """获取群组列表（内部方法）"""
        try:
            resp = self.session.get(f"{self.base_url}/api/groups")
            data = resp.json()
            return data.get('data', [])
        except Exception as e:
            print(f"获取群组失败: {e}")
            return []
    
    def _fetch_welcome_templates(self):
        """获取欢迎模板（内部方法）"""
        try:
            resp = self.session.get(f"{self.base_url}/api/welcome")
            data = resp.json()
            return data.get('data', [])
        except Exception as e:
            print(f"获取欢迎模板失败: {e}")
            return []
    
    def _fetch_ad_messages(self):
        """获取广告消息（内部方法）"""
        try:
            resp = self.session.get(f"{self.base_url}/api/ad-messages")
            data = resp.json()
            messages = data.get('data', [])
            return [m for m in messages if m.get('enabled', False)]
        except Exception as e:
            print(f"获取广告消息失败: {e}")
            return []
    
    # ========== 群组管理 ==========
    
    def get_groups(self, keyword=None):
        """获取群组列表"""
        # 懒加载：首次调用时初始化缓存
        if not self._initialized:
            self.initialize_cache()
        
        if keyword:
            # 有关键字搜索，查数据库
            groups = self._fetch_groups()
            return [g for g in groups if keyword in str(g.get('groupId', '')) or keyword in g.get('groupName', '')]
        
        return self._all_groups_cache
    
    def get_group(self, group_id):
        """获取单个群组"""
        if not self._initialized:
            self.initialize_cache()
        
        return self._group_cache.get(group_id)
    
    def create_group(self, group_data):
        """创建群组"""
        url = f"{self.base_url}/api/groups"
        try:
            resp = self.session.post(url, json=group_data)
            result = resp.json()
            
            # 更新缓存
            if result.get('code') == 200:
                group = result.get('data')
                if group:
                    self._group_cache[group['groupId']] = group
                    if group not in self._all_groups_cache:
                        self._all_groups_cache.append(group)
            
            return result
        except Exception as e:
            print(f"创建群组失败: {e}")
            return None
    
    def update_group(self, group_id, group_data):
        """更新群组"""
        url = f"{self.base_url}/api/groups/{group_id}"
        try:
            resp = self.session.put(url, json=group_data)
            result = resp.json()
            
            # 更新缓存
            if result.get('code') == 200:
                group = result.get('data')
                if group:
                    self._group_cache[group_id] = group
                    # 更新列表中的记录
                    for i, g in enumerate(self._all_groups_cache):
                        if g.get('groupId') == group_id:
                            self._all_groups_cache[i] = group
                            break
            
            return result
        except Exception as e:
            print(f"更新群组失败: {e}")
            return None
    
    def delete_group(self, group_id):
        """删除群组"""
        url = f"{self.base_url}/api/groups/{group_id}"
        try:
            resp = self.session.delete(url)
            result = resp.json()
            
            # 删除缓存
            if result.get('code') == 200:
                self._group_cache.pop(group_id, None)
                self._all_groups_cache = [g for g in self._all_groups_cache if g.get('groupId') != group_id]
            
            return result
        except Exception as e:
            print(f"删除群组失败: {e}")
            return None
    
    def update_group_last_ad_time(self, group_id):
        """更新群组最后发送广告时间"""
        # 先更新本地缓存
        if group_id in self._group_cache:
            from datetime import datetime
            self._group_cache[group_id]['lastAdTime'] = datetime.now().isoformat()
        
        # 再调 API 持久化
        return self.update_group(group_id, self._group_cache.get(group_id, {}))
    
    # ========== 欢迎消息 ==========
    
    def get_active_welcome_message(self):
        """获取当前启用的欢迎消息（全局单条）"""
        if not self._initialized:
            self.initialize_cache()
        
        try:
            resp = self.session.get(f"{self.base_url}/api/welcome-message/active")
            if resp.status_code == 200:
                data = resp.json()
                return data.get('data')
        except Exception as e:
            print(f"获取欢迎消息失败: {e}")
        return None
    
    def get_welcome_templates(self, keyword=None):
        """获取欢迎消息模板（兼容旧接口）"""
        if not self._initialized:
            self.initialize_cache()
        
        if keyword:
            # 有关键字搜索，查数据库
            templates = self._fetch_welcome_templates()
            return [t for t in templates if keyword in t.get('name', '') or keyword in t.get('content', '')]
        
        return self._welcome_cache
    
    def create_welcome_template(self, template_data):
        """创建欢迎模板"""
        url = f"{self.base_url}/api/welcome"
        try:
            resp = self.session.post(url, json=template_data)
            result = resp.json()
            
            # 更新缓存
            if result.get('code') == 200:
                template = result.get('data')
                if template:
                    self._welcome_cache.append(template)
            
            return result
        except Exception as e:
            print(f"创建欢迎模板失败: {e}")
            return None
    
    def update_welcome_template(self, template_id, template_data):
        """更新欢迎模板"""
        url = f"{self.base_url}/api/welcome/{template_id}"
        try:
            resp = self.session.put(url, json=template_data)
            result = resp.json()
            
            # 更新缓存
            if result.get('code') == 200:
                template = result.get('data')
                if template:
                    for i, t in enumerate(self._welcome_cache):
                        if t.get('id') == template_id:
                            self._welcome_cache[i] = template
                            break
            
            return result
        except Exception as e:
            print(f"更新欢迎模板失败: {e}")
            return None
    
    def delete_welcome_template(self, template_id):
        """删除欢迎模板"""
        url = f"{self.base_url}/api/welcome/{template_id}"
        try:
            resp = self.session.delete(url)
            result = resp.json()
            
            # 删除缓存
            if result.get('code') == 200:
                self._welcome_cache = [t for t in self._welcome_cache if t.get('id') != template_id]
            
            return result
        except Exception as e:
            print(f"删除欢迎模板失败: {e}")
            return None
    
    # ========== 广告消息 ==========
    
    def get_ad_messages(self, keyword=None, enabled_only=True):
        """获取广告消息"""
        if not self._initialized:
            self.initialize_cache()
        
        messages = self._ad_messages_cache
        
        if keyword:
            messages = [m for m in messages if keyword in m.get('title', '') or keyword in m.get('content', '')]
        
        if enabled_only:
            messages = [m for m in messages if m.get('enabled', False)]
        
        return messages
    
    def create_ad_message(self, ad_data):
        """创建广告消息"""
        url = f"{self.base_url}/api/ad-messages"
        try:
            resp = self.session.post(url, json=ad_data)
            result = resp.json()
            
            # 更新缓存
            if result.get('code') == 200:
                ad = result.get('data')
                if ad:
                    self._ad_messages_cache.append(ad)
            
            return result
        except Exception as e:
            print(f"创建广告消息失败: {e}")
            return None
    
    def update_ad_message(self, ad_id, ad_data):
        """更新广告消息"""
        url = f"{self.base_url}/api/ad-messages/{ad_id}"
        try:
            resp = self.session.put(url, json=ad_data)
            result = resp.json()
            
            # 更新缓存
            if result.get('code') == 200:
                ad = result.get('data')
                if ad:
                    for i, m in enumerate(self._ad_messages_cache):
                        if m.get('id') == ad_id:
                            self._ad_messages_cache[i] = ad
                            break
            
            return result
        except Exception as e:
            print(f"更新广告消息失败: {e}")
            return None
    
    def delete_ad_message(self, ad_id):
        """删除广告消息"""
        url = f"{self.base_url}/api/ad-messages/{ad_id}"
        try:
            resp = self.session.delete(url)
            result = resp.json()
            
            # 删除缓存
            if result.get('code') == 200:
                self._ad_messages_cache = [m for m in self._ad_messages_cache if m.get('id') != ad_id]
            
            return result
        except Exception as e:
            print(f"删除广告消息失败: {e}")
            return None
    
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
    
    # ========== 系统配置 ==========
    
    def get_system_config(self, key):
        """获取系统配置值"""
        try:
            resp = self.session.get(f"{self.base_url}/api/system-config/{key}")
            data = resp.json()
            return data.get('data')
        except Exception as e:
            print(f"获取系统配置失败: {e}")
            return None
    
    def get_ad_cycle_minutes(self):
        """获取广告发送周期（分钟）"""
        return int(self.get_system_config('ad_cycle_minutes') or 60)
    
    def get_ad_delay_seconds(self):
        """获取发送间隔（秒）"""
        return int(self.get_system_config('ad_delay_seconds') or 5)
    
    def get_ad_max_per_minute(self):
        """获取每分钟限流"""
        return int(self.get_system_config('ad_max_per_minute') or 20)


# 测试用
if __name__ == '__main__':
    client = QuntuiAPIClient()
    client.initialize_cache()
    groups = client.get_groups()
    print(f"群组数量: {len(groups)}")