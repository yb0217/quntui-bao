package com.quntui.repository;

import com.quntui.model.WelcomeMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WelcomeMessageMapper {
    // 获取当前启用的欢迎消息（只有一条）
    WelcomeMessage getActive();
    
    // 获取所有欢迎消息
    List<WelcomeMessage> findAll();
    
    // 根据ID查询
    WelcomeMessage findById(@Param("id") Long id);
    
    // 保存（插入或更新）
    int save(WelcomeMessage message);
    
    // 删除
    int deleteById(@Param("id") Long id);
}