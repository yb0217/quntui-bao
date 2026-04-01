#!/usr/bin/env python3
"""
群推宝 Python 机器人
- 监听群新成员加入，发送欢迎消息
- 定时发送广告消息
- 配置从 Java API 获取
"""
import asyncio
import os
import sys
import requests
from telethon import events, Button
from telethon.errors import FloodWaitError
from telethon.utils import get_display_name

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from python.api_client import QuntuiAPIClient
import gc


# 从 API 获取配置
def get_telegram_config():
    """从 Java API 获取 Telegram 配置"""
    api_url = os.environ.get('API_URL', 'http://localhost:8083')
    try:
        resp = requests.get(f"{api_url}/api/bot-config", timeout=5)
        if resp.status_code == 200:
            return resp.json()
    except Exception as e:
        print(f"⚠️ 获取配置失败，使用环境变量: {e}")
    
    # 回退到环境变量
    return {
        'botToken': os.environ.get('BOT_TOKEN', ''),
        'apiId': int(os.environ.get('API_ID', '0')),
        'apiHash': os.environ.get('API_HASH', ''),
        'proxyEnabled': False,
        'proxyHost': '127.0.0.1',
        'proxyPort': 7890
    }

class QuntuiBot:
    def __init__(self, api_url="http://localhost:8083"):
        self.api = QuntuiAPIClient(api_url)
        self.client = None
        self.running = True
        
        # 获取 Telegram 配置
        self.tg_config = get_telegram_config()
        
    async def start(self):
        """启动机器人"""
        print("🎲 群推宝机器人启动中...")
        
        # 使用内存 session 避免文件锁定问题
        self.client = self.api.get_telegram_client_with_session(':memory:')
        await self.client.start(bot_token=self.tg_config['botToken'])
        
        print(f"✅ 机器人已启动")
        
        # 初始化配置缓存（用于检测变化）
        self._config_cache = {
            'ad_cycle_minutes': self.api.get_ad_cycle_minutes(),
            'ad_delay_seconds': self.api.get_ad_delay_seconds(),
            'ad_max_per_minute': self.api.get_ad_max_per_minute()
        }
        
        # 注册事件处理器
        self.register_handlers()
        
        # 启动内存清理定时任务
        asyncio.create_task(self.memory_cleanup())
        
        # 启动配置监听任务（每30秒检查配置变化）
        asyncio.create_task(self.config_watcher())
        
        # 启动广告定时任务
        asyncio.create_task(self.ad_scheduler())
        
        # 保持运行
        await self.client.run_until_disconnected()
    
    def register_handlers(self):
        """注册事件处理器"""
        
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
            
            # 获取欢迎消息（从新的全局欢迎消息 API）
            welcome_msg = group_config.get('welcomeMessage')
            if not welcome_msg:
                welcome_data = self.api.get_active_welcome_message()
                if welcome_data:
                    welcome_msg = welcome_data.get('content')
                if not welcome_msg:
                    welcome_msg = '欢迎新朋友！🎉\n请先阅读群规，有问题随时@管理员~'
            
            if welcome_msg:
                # 发送欢迎消息
                await self.client.send_message(chat_id, welcome_msg)
                print(f"👋 欢迎消息已发送到群 {chat_id}")
                
                # TODO: 暂时屏蔽发送日志
                # self.api.record_send_log(
                #     group_id=chat_id,
                #     target_user_id=user.id,
                #     message_type='welcome',
                #     content=welcome_msg,
                #     success=True
                # )
                
        except Exception as e:
            print(f"❌ 处理新成员加入失败: {e}")
    
    async def memory_cleanup(self):
        """定期清理内存，防止内存泄漏"""
        while self.running:
            await asyncio.sleep(300)  # 每 5 分钟清理一次
            gc.collect()
            print("🧹 内存清理完成")
    
    async def config_watcher(self):
        """监听配置变化，发现变化立即刷新缓存"""
        while self.running:
            await asyncio.sleep(30)  # 每 30 秒检查一次
            
            try:
                # 获取最新配置
                new_cycle = self.api.get_ad_cycle_minutes()
                new_delay = self.api.get_ad_delay_seconds()
                new_max = self.api.get_ad_max_per_minute()
                
                # 检查是否有变化
                changed = False
                if new_cycle != self._config_cache['ad_cycle_minutes']:
                    print(f"⚡ 检测到配置变化: ad_cycle_minutes {self._config_cache['ad_cycle_minutes']} -> {new_cycle}")
                    self._config_cache['ad_cycle_minutes'] = new_cycle
                    changed = True
                if new_delay != self._config_cache['ad_delay_seconds']:
                    print(f"⚡ 检测到配置变化: ad_delay_seconds {self._config_cache['ad_delay_seconds']} -> {new_delay}")
                    self._config_cache['ad_delay_seconds'] = new_delay
                    changed = True
                if new_max != self._config_cache['ad_max_per_minute']:
                    print(f"⚡ 检测到配置变化: ad_max_per_minute {self._config_cache['ad_max_per_minute']} -> {new_max}")
                    self._config_cache['ad_max_per_minute'] = new_max
                    changed = True
                    
                if changed:
                    print("✅ 配置已更新，下次广告轮播将使用新配置")
            except Exception as e:
                print(f"⚠️ 配置监听失败: {e}")
    
    async def ad_scheduler(self):
        """广告定时发送任务 - 全局限流，一轮发完所有群"""
        import random
        
        await asyncio.sleep(5)  # 启动等待
        
        # 发送索引，记录上次发送到哪个群
        send_index = 0
        
        while True:
            try:
                # 获取广告配置（用于限流）
                ad_messages = self.api.get_ad_messages()
                if not ad_messages:
                    print("⏳ 无广告消息，60秒后重试")
                    await asyncio.sleep(60)
                    continue
                
                # 取第一条广告（现在只允许一条）
                ad = ad_messages[0]
                
                # 从系统配置获取限流参数
                max_per_minute = self.api.get_ad_max_per_minute()  # 每分钟限流
                delay_base = self.api.get_ad_delay_seconds()       # 间隔基数
                
                # 获取广告发送周期
                ad_cycle_minutes = self.api.get_ad_cycle_minutes()
                
                # 获取所有启用了广告的群
                all_groups = self.api.get_groups()
                enabled_groups = [g for g in all_groups if g.get('adEnabled', False)]
                
                if not enabled_groups:
                    print("⏳ 无启用广告的群，60秒后重试")
                    await asyncio.sleep(60)
                    continue
                
                groups_count = len(enabled_groups)
                print(f"📋 开始广告轮播，共 {groups_count} 个群，每分钟限流 {max_per_minute} 条，周期 {ad_cycle_minutes} 分钟")
                
                # 记录本轮开始时间，用于每分钟限流计数
                minute_start_time = asyncio.get_event_loop().time()
                sent_this_minute = 0
                
                # 一轮循环：发完所有群（受限于每分钟限流）
                while send_index < groups_count:
                    group = enabled_groups[send_index]
                    group_id = group.get('groupId')
                    ad_interval = group.get('adIntervalMinutes', 60)  # 群独立间隔
                    
                    # 检查距离上次发送是否达到该群的间隔要求
                    last_ad_time = group.get('lastAdTime')
                    if last_ad_time:
                        from datetime import datetime
                        try:
                            # 解析时间
                            last_str = last_ad_time.replace('Z', '+00:00')
                            last_time = datetime.fromisoformat(last_str.replace(tzinfo=None))
                            local_now = datetime.now()
                            minutes_since_last = (local_now - last_time).total_seconds() / 60
                            
                            if minutes_since_last < ad_interval:
                                # 未达到该群的间隔，跳到下一个群
                                print(f"⏭ 群 {group_id} 距离上次发送还差 {ad_interval - minutes_since_last:.1f} 分钟，跳过")
                                send_index += 1
                                continue
                        except Exception as e:
                            print(f"⚠️ 解析时间失败: {e}")
                    
                    # 检查每分钟限流
                    current_time = asyncio.get_event_loop().time()
                    if current_time - minute_start_time >= 60:
                        # 新的一分钟，重置计数
                        minute_start_time = current_time
                        sent_this_minute = 0
                        print("🔄 新一分钟开始")
                    
                    if sent_this_minute >= max_per_minute:
                        # 本分钟已达上限，等待
                        wait_seconds = 60 - (current_time - minute_start_time)
                        print(f"⏳ 本分钟已达上限({max_per_minute})，等待 {wait_seconds:.0f} 秒")
                        await asyncio.sleep(wait_seconds)
                        minute_start_time = asyncio.get_event_loop().time()
                        sent_this_minute = 0
                        continue
                    
                    # 构建按钮
                    buttons = self.build_buttons(ad)
                    
                    # 发送广告
                    try:
                        title = ad.get('title', '')
                        content = ad.get('content', '')
                        message = f"*{title}*\n\n{content}" if title else content
                        
                        # 确保 buttons 为 None 而不是空列表
                        await self.client.send_message(group_id, message, buttons=buttons if buttons else None)
                        
                        # TODO: 暂时屏蔽发送日志
                        # self.api.record_send_log(
                        #     group_id=group_id,
                        #     target_user_id=None,
                        #     message_type='ad',
                        #     ad_message_id=ad.get('id'),
                        #     content=content,
                        #     success=True
                        # )
                        
                        # 更新群的最后发送时间
                        self.api.update_group_last_ad_time(group_id)
                        
                        print(f"✅ [{sent_this_minute + 1}/{max_per_minute}] 广告已发送到群 {group_id}")
                        sent_this_minute += 1
                        
                    except FloodWaitError as e:
                        print(f"⏳ TG限流，等待 {e.seconds} 秒")
                        await asyncio.sleep(e.seconds)
                    except Exception as e:
                        print(f"❌ 发送广告到群 {group_id} 失败: {e}")
                        # TODO: 暂时屏蔽发送日志
                        # self.api.record_send_log(
                        #     group_id=group_id,
                        #     target_user_id=None,
                        #     message_type='ad',
                        #     ad_message_id=ad.get('id'),
                        #     content=ad.get('content', ''),
                        #     success=False,
                        #     error_msg=str(e)
                        # )
                    
                    # 随机间隔 2-(delay_base+2) 秒
                    interval = random.randint(2, delay_base + 2)
                    await asyncio.sleep(interval)
                    
                    # 移动到下一个群
                    send_index += 1
                
                # 一轮发完了，重置索引，等待配置的周期时间再发下一轮
                send_index = 0
                
                print(f"📊 本轮发送完成({groups_count}个群)，等待 {ad_cycle_minutes} 分钟后开始下一轮...")
                await asyncio.sleep(ad_cycle_minutes * 60)
                
            except Exception as e:
                print(f"❌ 广告任务异常: {e}")
                await asyncio.sleep(60)
    
    def build_buttons(self, ad_message):
        """构建 Inline 按钮 - 支持多行格式"""
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
            
            # 调试信息
            
            # 新格式：[{buttons: [{text, type, value}, ...]}, ...]
            # 兼容旧格式：[{text, type, value}, ...]
            result = []
            
            for row in buttons_data:
                # 判断是否是新格式
                if 'buttons' in row:
                    # 新格式：多行按钮
                    btn_list = row['buttons']
                    row_buttons = []
                    for btn in btn_list:
                        text = btn.get('text', '')
                        url = btn.get('value', '')
                        if text and url:
                            row_buttons.append(Button.url(text, url))
                    if row_buttons:
                        result.append(row_buttons)
                else:
                    # 旧格式：扁平结构，按layout分组
                    text = row.get('text', '')
                    url = row.get('value', '')
                    if text and url:
                        result.append([Button.url(text, url)])
            
            # 展平旧格式
            if result and isinstance(result[0], list) and len(result) > 0:
                first_elem = result[0][0] if result[0] else None
                if first_elem and not hasattr(first_elem, 'text'):
                    # 是扁平结构，需要重组
                    flat = [btn[0] for btn in result]
                    result = []
                    layout = ad_message.get('buttonLayout', '1x2')
                    if layout == '1x1':
                        for btn in flat:
                            result.append([btn])
                    else:
                        for i in range(0, len(flat), 2):
                            row = flat[i:i+2]
                            result.append(row)
            
            return result if result else None
                
        except Exception as e:
            print(f"❌ 构建按钮失败: {e}")
            import traceback
            traceback.print_exc()
            return None


async def main():
    bot = QuntuiBot()
    await bot.start()


if __name__ == '__main__':
    asyncio.run(main())