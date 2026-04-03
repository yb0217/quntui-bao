package com.quntui.controller;

import com.quntui.model.GroupMessage;
import com.quntui.service.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 群组消息记录 API
 */
@RestController
@RequestMapping("/api/group-messages")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;

    /**
     * 保存或更新消息记录
     */
    @PostMapping
    public Map<String, Object> saveMessage(@RequestBody GroupMessage message) {
        Map<String, Object> result = new HashMap<>();
        try {
            groupMessageService.saveMessage(message.getGroupId(), message.getMessageType(), message.getMessageId());
            result.put("code", 200);
            result.put("msg", "保存成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "保存失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取消息记录
     */
    @GetMapping
    public Map<String, Object> getMessage(@RequestParam String groupId, @RequestParam String messageType) {
        Map<String, Object> result = new HashMap<>();
        try {
            GroupMessage msg = groupMessageService.getMessage(groupId, messageType);
            result.put("code", 200);
            result.put("data", msg);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        return result;
    }
}
