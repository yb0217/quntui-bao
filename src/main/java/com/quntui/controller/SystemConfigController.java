package com.quntui.controller;

import com.quntui.model.SystemConfig;
import com.quntui.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService service;
    
    // 获取所有配置
    @GetMapping
    public Map<String, Object> getAll() {
        List<SystemConfig> list = service.findAll();
        
        // 转换为 key-value 形式
        Map<String, Object> configMap = new HashMap<>();
        for (SystemConfig c : list) {
            configMap.put(c.getConfigKey(), c.getConfigValue());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", configMap);
        return result;
    }
    
    // 获取单个配置
    @GetMapping("/{key}")
    public Map<String, Object> getByKey(@PathVariable String key) {
        String value = service.getValue(key);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", value);
        return result;
    }
    
    // 保存配置
    @PostMapping
    public Map<String, Object> save(@RequestBody Map<String, String> params) {
        String key = params.get("key");
        String value = params.get("value");
        
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        
        service.save(config);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "保存成功");
        return result;
    }
}