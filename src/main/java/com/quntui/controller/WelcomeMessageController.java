package com.quntui.controller;

import com.quntui.model.WelcomeMessage;
import com.quntui.service.WelcomeMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/welcome-message")
public class WelcomeMessageController {
    
    @Autowired
    private WelcomeMessageService service;
    
    // 获取当前启用的欢迎消息
    @GetMapping("/active")
    public Map<String, Object> getActive() {
        WelcomeMessage message = service.getActive();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", message);
        return result;
    }
    
    // 获取所有欢迎消息
    @GetMapping
    public Map<String, Object> getAll() {
        List<WelcomeMessage> list = service.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", list);
        return result;
    }
    
    // 根据ID获取
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        WelcomeMessage message = service.findById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", message);
        return result;
    }
    
    // 保存欢迎消息
    @PostMapping
    public Map<String, Object> save(@RequestBody WelcomeMessage message) {
        service.save(message);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "保存成功");
        return result;
    }
    
    // 更新欢迎消息
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody WelcomeMessage message) {
        message.setId(id);
        service.update(message);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "更新成功");
        return result;
    }
    
    // 删除
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        service.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "删除成功");
        return result;
    }
}