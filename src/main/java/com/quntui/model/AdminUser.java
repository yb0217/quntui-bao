package com.quntui.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUser {
    private Long id;
    private String username;
    private String password;
    private String role;             // admin
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}