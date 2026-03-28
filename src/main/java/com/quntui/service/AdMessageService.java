package com.quntui.service;

import com.quntui.model.AdMessage;
import com.quntui.repository.AdMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdMessageService {
    
    @Autowired
    private AdMessageMapper mapper;

    public List<AdMessage> findAll() {
        return mapper.findAll();
    }
    
    public List<AdMessage> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return mapper.findAll();
        }
        return mapper.search(keyword.trim());
    }

    public AdMessage findById(Long id) {
        return mapper.findById(id);
    }

    public List<AdMessage> findEnabled() {
        return mapper.findEnabled();
    }

    public int save(AdMessage message) {
        if (message.getId() == null) {
            return mapper.insert(message);
        } else {
            return mapper.update(message);
        }
    }

    public int delete(Long id) {
        return mapper.delete(id);
    }
}