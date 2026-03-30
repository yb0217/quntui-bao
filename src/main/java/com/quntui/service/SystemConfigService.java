package com.quntui.service;

import com.quntui.model.SystemConfig;
import com.quntui.repository.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigMapper mapper;
    
    // 获取所有配置
    public List<SystemConfig> findAll() {
        return mapper.findAll();
    }
    
    // 根据键获取值
    public String getValue(String key) {
        return mapper.getValueByKey(key);
    }
    
    // 获取整数值
    public int getIntValue(String key, int defaultValue) {
        String value = mapper.getValueByKey(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // 保存或更新
    public void save(SystemConfig config) {
        mapper.save(config);
    }
}