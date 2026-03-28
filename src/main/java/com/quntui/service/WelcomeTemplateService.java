package com.quntui.service;

import com.quntui.model.WelcomeTemplate;
import com.quntui.repository.WelcomeTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WelcomeTemplateService {
    
    @Autowired
    private WelcomeTemplateMapper mapper;

    public List<WelcomeTemplate> findAll() {
        return mapper.findAll();
    }
    
    public List<WelcomeTemplate> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return mapper.findAll();
        }
        return mapper.search(keyword.trim());
    }

    public WelcomeTemplate findById(Long id) {
        return mapper.findById(id);
    }

    public WelcomeTemplate findEnabled() {
        return mapper.findEnabled();
    }

    public int save(WelcomeTemplate template) {
        if (template.getId() == null) {
            return mapper.insert(template);
        } else {
            return mapper.update(template);
        }
    }

    public int delete(Long id) {
        return mapper.delete(id);
    }
}