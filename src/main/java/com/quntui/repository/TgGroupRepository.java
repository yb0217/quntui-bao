package com.quntui.repository;

import com.quntui.model.TgGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TgGroupRepository {
    void save(TgGroup group);
    void update(TgGroup group);
    TgGroup findByGroupId(@Param("groupId") Long groupId);
}