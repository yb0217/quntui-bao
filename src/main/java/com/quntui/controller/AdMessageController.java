package com.quntui.controller;

import com.quntui.model.AdMessage;
import com.quntui.service.AdMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ad-messages")
public class AdMessageController {
    
    @Autowired
    private AdMessageService service;

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String keyword) {
        List<AdMessage> list = service.search(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        AdMessage message = service.findById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", message);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody AdMessage message) {
        service.save(message);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody AdMessage message) {
        message.setId(id);
        service.save(message);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        service.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }
}