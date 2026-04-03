package com.quntui.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Bot 启动服务
 * JAR 启动时自动启动 Python 机器人
 */
@Slf4j
@Component
public class BotStarterService implements ApplicationRunner {

    @Value("${python.bot.workdir:/tmp/python}")
    private String pythonWorkDir;

    @Value("${python.bot.script:bot.py}")
    private String pythonScript;

    @Value("${telegram.bot.token:}")
    private String botTokenFromConfig = "";

    @Override
    public void run(ApplicationArguments args) {
        log.info("正在启动 Python 机器人...");
        log.info("Python工作目录: {}", pythonWorkDir);
        log.info("Python脚本: {}", pythonScript);
        log.info("BotTokenFromConfig是否为null: {}", botTokenFromConfig == null);
        log.info("BotTokenFromConfig长度: {}", botTokenFromConfig != null ? botTokenFromConfig.length() : 0);

        // 检查 Python 脚本目录
        File scriptDir = new File(pythonWorkDir);
        if (!scriptDir.exists()) {
            log.error("Python 脚本目录不存在: {}", pythonWorkDir);
            return;
        }

        // 检查 bot.py 是否存在
        File botFile = new File(scriptDir, pythonScript);
        if (!botFile.exists()) {
            log.error("{} 不存在: {}", pythonScript, botFile.getAbsolutePath());
            return;
        }

        // 获取 Bot Token（优先配置，其次环境变量）
        String botToken = botTokenFromConfig;
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Bot Token 未在配置中设置，尝试从环境变量获取");
            botToken = System.getenv("BOT_TOKEN");
        }
        
        if (botToken == null || botToken.isEmpty()) {
            log.error("Bot Token 未配置，无法启动 Python 机器人");
            return;
        }

        // 启动 Python 机器人
        try {
            log.info("使用 Bot Token: {}...", botToken.substring(0, Math.min(10, botToken.length())));
            
            String[] cmd = new String[]{"python3", "-u", "bot.py", botToken};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(scriptDir);
            pb.environment().put("API_ID", "21791619");
            pb.environment().put("API_HASH", "282e7c380c97f10391003d88c48701fe");
            pb.environment().put("API_BASE", "http://localhost:8083");
            pb.environment().put("PROXY_ENABLED", "true");
            
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(scriptDir, "bot.log")));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(scriptDir, "bot.log")));

            Process process = pb.start();

            log.info("Python 机器人进程已启动，PID: {}", process.pid());

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.contains("indexOf") || msg.contains("Cannot invoke")) {
                msg = "Python 机器人启动失败";
            }
            log.error("{}", msg, e);
        }
    }
}
