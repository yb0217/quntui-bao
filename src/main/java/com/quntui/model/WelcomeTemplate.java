package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WelcomeTemplate {
    private Long id;
    private String name;             // 模板名称
    private String content;         // 欢迎消息内容
    private Boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}