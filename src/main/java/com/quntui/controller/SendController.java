package com.quntui.controller;

import com.quntui.service.RateLimitService;
import com.quntui.model.TgGroup;
import com.quntui.model.AdMessage;
import com.quntui.service.TgGroupService;
import com.quntui.service.AdMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/send")
public class SendController {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private TgGroupService tgGroupService;
    
    @Autowired
    private AdMessageService adMessageService;

    /**
     * 检查是否可以发送广告
     */
    @GetMapping("/check/{groupId}")
    public Map<String, Object> checkCanSend(@PathVariable Long groupId) {
        TgGroup group = tgGroupService.findByGroupId(groupId);
        List<AdMessage> adMessages = adMessageService.findEnabled();
        
        Map<String, Object> result = new HashMap<>();
        
        if (group == null) {
            result.put("code", 1);
            result.put("msg", "群组不存在");
            return result;
        }
        
        // 找一条可用的广告消息
        AdMessage adMessage = adMessages.isEmpty() ? null : adMessages.get(0);
        
        String reason = rateLimitService.checkCanSend(group, adMessage);
        
        if (reason == null) {
            result.put("code", 0);
            result.put("msg", "可以发送");
            result.put("canSend", true);
            result.put("todayCount", rateLimitService.getTodaySendCount(groupId));
        } else {
            result.put("code", 1);
            result.put("msg", reason);
            result.put("canSend", false);
            result.put("todayCount", rateLimitService.getTodaySendCount(groupId));
        }
        
        return result;
    }
    
    /**
     * 手动触发发送广告(给Python调用的)
     */
    @PostMapping("/trigger/{groupId}")
    public Map<String, Object> triggerSend(@PathVariable Long groupId) {
        TgGroup group = tgGroupService.findByGroupId(groupId);
        List<AdMessage> adMessages = adMessageService.findEnabled();
        
        Map<String, Object> result = new HashMap<>();
        
        if (group == null) {
            result.put("code", 1);
            result.put("msg", "群组不存在");
            return result;
        }
        
        if (adMessages.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "没有启用的广告消息");
            return result;
        }
        
        // 检查限流
        AdMessage adMessage = adMessages.get(0);
        String reason = rateLimitService.checkCanSend(group, adMessage);
        if (reason != null) {
            result.put("code", 1);
            result.put("msg", reason);
            return result;
        }
        
        // 返回发送所需的参数给Python
        result.put("code", 0);
        result.put("msg", "可以发送");
        result.put("data", Map.of(
            "groupId", group.getGroupId(),
            "groupName", group.getGroupName() != null ? group.getGroupName() : "",
            "title", adMessage.getTitle(),
            "content", adMessage.getContent(),
            "buttons", convertButtonsToJson(adMessage),
            "buttonLayout", adMessage.getButtonLayout() != null ? adMessage.getButtonLayout() : "1x2",
            "delaySeconds", adMessage.getDelaySeconds() != null ? adMessage.getDelaySeconds() : 20
        ));
        
        return result;
    }
    
    /**
     * 转换按钮配置为JSON(兼容旧格式)
     */
    private String convertButtonsToJson(AdMessage adMessage) {
        // 优先使用可视化配置
        String buttonConfig = adMessage.getButtonConfig();
        if (buttonConfig != null && !isEmpty(buttonConfig)) {
            return buttonConfig;
        }
        // 兼容旧格式
        return adMessage.getButtons();
    }
    
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.equals("[]");
    }
    
    /**
     * 记录发送结果
     */
    @PostMapping("/record")
    public Map<String, Object> recordSend(@RequestBody Map<String, Object> params) {
        Long groupId = Long.valueOf(params.get("groupId").toString());
        Long adMessageId = Long.valueOf(params.get("adMessageId").toString());
        String content = params.get("content").toString();
        boolean success = Boolean.parseBoolean(params.get("success").toString());
        String errorMsg = (String) params.get("errorMsg");
        
        rateLimitService.recordSend(groupId, adMessageId, content, success, errorMsg);
        
        // 更新群组最后发送时间
        if (success) {
            TgGroup group = tgGroupService.findByGroupId(groupId);
            if (group != null) {
                group.setLastAdTime(java.time.LocalDateTime.now());
                tgGroupService.save(group);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }
}