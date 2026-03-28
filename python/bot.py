#!/usr/bin/env python3
"""
群推宝 Python 机器人
- 监听群新成员加入，发送欢迎消息
- 定时发送广告消息
"""
import asyncio
import os
import sys
from telethon import events, Button
from telethon.errors import FloodWaitError
from telethon.utils import get_display_name

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from python.api_client import QuntuiAPIClient
from python.config import Config

class QuntuiBot:
    def __init__(self, api_url="http://localhost:8083"):
        self.api = QuntuiAPIClient(api_url)
        self.client = None
        self.config = Config()
        
    async def start(self):
        """启动机器人"""
        print("🎲 群推宝机器人启动中...")
        
        self.client = self.api.get_telegram_client()
        await self.client.start(bot_token=self.config.BOT_TOKEN)
        
        print(f"✅ 机器人已启动: @{self.config.BOT_USERNAME}")
        
        # 注册事件处理器
        self.register_handlers()
        
        # 启动广告定时任务
        asyncio.create_task(self.ad_scheduler())
        
        # 保持运行
        await self.client.run_until_disconnected()
    
    def register_handlers(self):
        """注册事件处理器"""
        
        @self.client.on(events.NewParticipant(event=events.ChatAction))
        async def on_new_member(event):
            """新成员加入群"""
            await self.handle_new_member(event)
        
        @self.client.on(events.ChatAction)
        async def on_chat_action(event):
            """处理群事件（成员加入等）"""
            if event.user_joined or event.user_added:
                await self.handle_new_member(event)
        
        print("✅ 事件处理器已注册")
    
    async def handle_new_member(self, event):
        """处理新成员加入"""
        try:
            chat_id = event.chat_id
            user = event.user
            
            if not user or user.is_self:
                return
            
            # 获取群组配置
            group_config = self.api.get_group(chat_id)
            if not group_config:
                return
            
            # 检查是否启用欢迎消息
            if not group_config.get('welcomeEnabled', True):
                return
            
            # 获取欢迎消息模板
            welcome_msg = group_config.get('welcomeMessage')
            if not welcome_msg:
                # 如果没有配置，从 API 获取默认模板
                templates = self.api.get_welcome_templates()
                if templates:
                    welcome_msg = templates[0].get('content', '欢迎新朋友！🎉')
            
            if welcome_msg:
                # 发送欢迎消息
                await self.client.send_message(chat_id, welcome_msg)
                print(f"✅ 欢迎消息已发送到群 {chat_id}")
                
                # 记录发送日志
                self.api.record_send_log(
                    group_id=chat_id,
                    target_user_id=user.id,
                    message_type='welcome',
                    content=welcome_msg,
                    success=True
                )
                
        except Exception as e:
            print(f"❌ 处理新成员加入失败: {e}")
    
    async def ad_scheduler(self):
        """广告定时发送任务"""
        await asyncio.sleep(5)  # 启动等待
        
        while True:
            try:
                # 获取所有启用了广告的群组
                groups = self.api.get_groups()
                
                for group in groups:
                    if not group.get('adEnabled', False):
                        continue
                    
                    group_id = group.get('groupId')
                    last_ad_time = group.get('lastAdTime')
                    ad_interval = group.get('adIntervalMinutes', 60)
                    
                    # 检查是否需要发送广告
                    should_send = self.api.check_can_send(group_id)
                    if not should_send.get('canSend'):
                        continue
                    
                    # 获取广告消息
                    ad_messages = self.api.get_ad_messages()
                    if not ad_messages:
                        continue
                    
                    # 随机选择一条广告消息
                    import random
                    ad = random.choice(ad_messages)
                    
                    # 构建按钮（如果有）
                    buttons = self.build_buttons(ad)
                    
                    # 发送广告消息
                    try:
                        msg = await self.client.send_message(
                            group_id,
                            ad.get('content', ''),
                            buttons=buttons
                        )
                        
                        # 记录发送日志
                        self.api.record_send_log(
                            group_id=group_id,
                            target_user_id=None,
                            message_type='ad',
                            ad_message_id=ad.get('id'),
                            content=ad.get('content', ''),
                            success=True
                        )
                        
                        # 更新群组最后发送时间
                        self.api.update_group_last_ad_time(group_id)
                        
                        print(f"✅ 广告已发送到群 {group_id}")
                        
                    except FloodWaitError as e:
                        print(f"⏳ 限流等待 {e.seconds} 秒")
                        await asyncio.sleep(e.seconds)
                    except Exception as e:
                        print(f"❌ 发送广告失败: {e}")
                        self.api.record_send_log(
                            group_id=group_id,
                            target_user_id=None,
                            message_type='ad',
                            ad_message_id=ad.get('id'),
                            content=ad.get('content', ''),
                            success=False,
                            error_msg=str(e)
                        )
                
            except Exception as e:
                print(f"❌ 广告任务异常: {e}")
            
            # 每分钟检查一次
            await asyncio.sleep(60)
    
    def build_buttons(self, ad_message):
        """构建 Inline 按钮"""
        try:
            button_config = ad_message.get('buttonConfig')
            if not button_config:
                return None
            
            import json
            if isinstance(button_config, str):
                buttons_data = json.loads(button_config)
            else:
                buttons_data = button_config
            
            if not buttons_data:
                return None
            
            layout = ad_message.get('buttonLayout', '1x2')
            buttons = []
            
            for btn in buttons_data:
                text = btn.get('text', '')
                url = btn.get('value', '')
                
                if text and url:
                    buttons.append(Button.url(text, url))
            
            # 根据布局组合按钮
            if layout == '1x1':
                # 一行一个按钮
                return [buttons]
            else:
                # 一行两个按钮
                result = []
                for i in range(0, len(buttons), 2):
                    row = buttons[i:i+2]
                    result.append(row)
                return result if result else None
                
        except Exception as e:
            print(f"❌ 构建按钮失败: {e}")
            return None


async def main():
    bot = QuntuiBot()
    await bot.start()


if __name__ == '__main__':
    asyncio.run(main())