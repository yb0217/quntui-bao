package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdMessage {
    private Long id;
    private String title;            // 标题
    private String content;         // 消息内容
    private String buttons;         // 按钮JSON(兼容旧数据)
    private String buttonLayout;    // 按钮布局 1x2 或 1x1
    private String buttonConfig;    // 可视化按钮配置(JSON数组)
    private Boolean enabled = true;
    
    // 限流配置
    private Integer delaySeconds = 5;   // 发送间隔(秒)，默认5秒
    private Integer maxPerMinute = 20;  // 每分钟最多发几条
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}