package com.quntui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class BotStarter {

    private Process botProcess;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private TelegramConfig telegramConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() {
        // 延迟 5 秒启动机器人，等待服务完全就绪
        scheduler.schedule(() -> {
            try {
                // 传递配置给 Python
                Map<String, String> env = new HashMap<>();
                env.put("BOT_TOKEN", telegramConfig.getBotToken());
                env.put("API_ID", String.valueOf(telegramConfig.getApiId()));
                env.put("API_HASH", telegramConfig.getApiHash());
                env.put("API_URL", "http://localhost:8083");
                
                String botPath = System.getProperty("user.dir") + "/python/bot.py";
                ProcessBuilder pb = new ProcessBuilder(
                    "python3", "-u", botPath
                );
                pb.directory(new java.io.File(System.getProperty("user.dir") + "/python"));
                pb.environment().putAll(env);
                pb.redirectErrorStream(true);
                
                botProcess = pb.start();
                
                // 读取日志输出
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(botProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("[Bot] " + line);
                        }
                    } catch (Exception e) {
                        System.err.println("Bot log error: " + e.getMessage());
                    }
                }).start();
                
                System.out.println("✅ Telegram 机器人已启动 (PID: " + botProcess.pid() + ")");
                
            } catch (Exception e) {
                System.err.println("❌ 启动机器人失败: " + e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }

    public void stopBot() {
        if (botProcess != null && botProcess.isAlive()) {
            botProcess.destroy();
            System.out.println("✅ Telegram 机器人已停止");
        }
    }
}