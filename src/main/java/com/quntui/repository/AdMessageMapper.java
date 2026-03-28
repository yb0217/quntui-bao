package com.quntui.repository;

import com.quntui.model.AdMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdMessageMapper {
    List<AdMessage> findAll();
    AdMessage findById(@Param("id") Long id);
    List<AdMessage> findEnabled();
    int insert(AdMessage message);
    int update(AdMessage message);
    int delete(@Param("id") Long id);
    
    // 搜索
    List<AdMessage> search(@Param("keyword") String keyword);
}