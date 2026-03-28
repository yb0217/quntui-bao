package com.quntui.service;

import com.quntui.model.AdminUser;
import com.quntui.repository.AdminUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    
    @Autowired
    private AdminUserMapper mapper;

    public AdminUser login(String username, String password) {
        AdminUser user = mapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        String hashed = hashSHA256(password);
        if (hashed.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    public String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }

    public void initAdmin() {
        AdminUser admin = mapper.findByUsername("admin");
        if (admin == null) {
            admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(hashSHA256("admin123"));
            admin.setRole("admin");
            mapper.insert(admin);
        }
    }
}