package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TgGroup {
    private Long id;
    private Long groupId;           // TG群ID
    private String groupName;        // 群名称
    private String inviteLink;       // 邀请链接
    private Boolean welcomeEnabled = true;   // 启用欢迎
    private String welcomeMessage;  // 欢迎消息
    private Boolean adEnabled = true;        // 启用广告
    private Integer adIntervalMinutes = 60;   // 广告间隔(分钟)
    private LocalDateTime lastAdTime;         // 上次发广告时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}