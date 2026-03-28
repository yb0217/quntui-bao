package com.quntui.repository;

import com.quntui.model.WelcomeTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WelcomeTemplateMapper {
    List<WelcomeTemplate> findAll();
    WelcomeTemplate findById(@Param("id") Long id);
    WelcomeTemplate findEnabled();
    int insert(WelcomeTemplate template);
    int update(WelcomeTemplate template);
    int delete(@Param("id") Long id);
    
    // 搜索
    List<WelcomeTemplate> search(@Param("keyword") String keyword);
}