package com.quntui.controller;

import com.quntui.model.WelcomeTemplate;
import com.quntui.service.WelcomeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/welcome")
public class WelcomeTemplateController {
    
    @Autowired
    private WelcomeTemplateService service;

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String keyword) {
        List<WelcomeTemplate> list = service.search(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        WelcomeTemplate template = service.findById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", template);
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody WelcomeTemplate template) {
        service.save(template);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody WelcomeTemplate template) {
        template.setId(id);
        service.save(template);
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