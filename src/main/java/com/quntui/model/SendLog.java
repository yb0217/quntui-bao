package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SendLog {
    private Long id;
    private Long groupId;           // 群ID
    private Long targetUserId;       // 目标用户ID(欢迎时用)
    private String messageType;      // welcome / ad
    private Long messageId;         // 广告消息ID
    private String content;          // 发送内容
    private Boolean success;        // 是否成功
    private String errorMsg;        // 错误信息
    private LocalDateTime createdAt;
}