package com.quntui.repository;

import com.quntui.model.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SystemConfigMapper {
    // 获取所有配置
    List<SystemConfig> findAll();
    
    // 根据键获取值
    String getValueByKey(@Param("configKey") String configKey);
    
    // 保存或更新
    int save(SystemConfig config);
}