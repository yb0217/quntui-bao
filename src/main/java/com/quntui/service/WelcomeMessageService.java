package com.quntui.service;

import com.quntui.model.WelcomeMessage;
import com.quntui.repository.WelcomeMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WelcomeMessageService {
    
    @Autowired
    private WelcomeMessageMapper mapper;
    
    // 获取当前启用的欢迎消息
    public WelcomeMessage getActive() {
        return mapper.getActive();
    }
    
    // 获取所有
    public List<WelcomeMessage> findAll() {
        return mapper.findAll();
    }
    
    // 根据ID查询
    public WelcomeMessage findById(Long id) {
        return mapper.findById(id);
    }
    
    // 保存（新增或更新）
    public WelcomeMessage save(WelcomeMessage message) {
        if (message.getId() == null) {
            // 新增：先禁用所有，再插入新记录
            message.setEnabled(true);
            mapper.save(message);
        } else {
            // 更新
            mapper.save(message);
        }
        return message;
    }
    
    // 删除
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}