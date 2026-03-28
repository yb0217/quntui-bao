package com.quntui.repository;

import com.quntui.model.TgGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TgGroupMapper {
    List<TgGroup> findAll();
    TgGroup findById(@Param("id") Long id);
    TgGroup findByGroupId(@Param("groupId") Long groupId);
    int insert(TgGroup group);
    int update(TgGroup group);
    int delete(@Param("id") Long id);
    
    // 搜索支持
    List<TgGroup> search(@Param("keyword") String keyword);
}