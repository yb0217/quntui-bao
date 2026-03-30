package com.quntui.controller;

import com.quntui.config.TelegramConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bot-config")
public class BotConfigController {

    @Autowired
    private TelegramConfig telegramConfig;

    @GetMapping
    public Map<String, Object> getBotConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("botToken", telegramConfig.getBotToken());
        config.put("apiId", telegramConfig.getApiId());
        config.put("apiHash", telegramConfig.getApiHash());
        config.put("proxyEnabled", telegramConfig.getProxy().isEnabled());
        config.put("proxyHost", telegramConfig.getProxy().getHost());
        config.put("proxyPort", telegramConfig.getProxy().getPort());
        return config;
    }
}