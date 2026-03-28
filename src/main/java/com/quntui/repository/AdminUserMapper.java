package com.quntui.repository;

import com.quntui.model.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminUserMapper {
    AdminUser findByUsername(@Param("username") String username);
    AdminUser findById(@Param("id") Long id);
    int insert(AdminUser user);
    int update(AdminUser user);
}