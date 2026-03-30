package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WelcomeMessage {
    private Long id;
    private String content;          // 欢迎消息内容（支持多行）
    private Boolean enabled = true;  // 是否启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}