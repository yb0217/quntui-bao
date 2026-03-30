package com.quntui.service;

import com.quntui.model.SendLog;
import com.quntui.repository.SendLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class SendLogService {
    
    @Autowired
    private SendLogMapper mapper;

    public int log(SendLog log) {
        return mapper.insert(log);
    }

    public List<SendLog> findByGroupId(Long groupId) {
        return mapper.findByGroupId(groupId);
    }

    public Map<String, Object> getStatistics() {
        List<Map<String, Object>> list = mapper.getStatistics();
        return list.isEmpty() ? null : list.get(0);
    }

    public Map<String, Object> getStatisticsByGroup(Long groupId) {
        List<Map<String, Object>> list = mapper.getStatisticsByGroup(groupId);
        return list.isEmpty() ? null : list.get(0);
    }
    
    public List<Map<String, Object>> getAllGroupStatistics() {
        return mapper.getAllGroupStatistics();
    }
}