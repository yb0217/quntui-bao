package com.quntui.controller;

import com.quntui.service.SendLogService;
import com.quntui.service.AuthService;
import com.quntui.model.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IndexController {
    
    @Autowired
    private SendLogService sendLogService;
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params, HttpSession session) {
        String username = params.get("username");
        String password = params.get("password");
        
        AdminUser user = authService.login(username, password);
        Map<String, Object> result = new HashMap<>();
        
        if (user != null) {
            session.setAttribute("adminUser", user);
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", user.getUsername());
        } else {
            result.put("code", 1);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }

    @GetMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        Map<String, Object> stats = sendLogService.getStatistics();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", stats);
        return result;
    }
    
    @GetMapping("/statistics/by-group")
    public Map<String, Object> statisticsByGroup() {
        List<Map<String, Object>> stats = sendLogService.getAllGroupStatistics();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", stats);
        return result;
    }
}