package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群组消息记录（用于删除旧消息）
 */
@Data
public class GroupMessage {
    private Long id;
    private String groupId;
    private String messageType;  // welcome, ad
    private Long messageId;      // TG消息ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
