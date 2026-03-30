package com.quntui.repository;

import com.quntui.model.SendLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface SendLogMapper {
    int insert(SendLog log);
    List<SendLog> findByGroupId(@Param("groupId") Long groupId);
    List<Map<String, Object>> getStatistics();
    List<Map<String, Object>> getStatisticsByGroup(@Param("groupId") Long groupId);
    
    // 获取所有群组统计
    List<Map<String, Object>> getAllGroupStatistics();
    
    // 本分钟发送次数(TG限流检查)
    List<Map<String, Object>> getThisMinuteCountByGroup(@Param("groupId") Long groupId);
    
    // 今日发送次数(统计用)
    List<Map<String, Object>> getTodayCountByGroup(@Param("groupId") Long groupId);
}