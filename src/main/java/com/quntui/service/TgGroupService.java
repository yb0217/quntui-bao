package com.quntui.service;

import com.quntui.model.TgGroup;
import com.quntui.repository.TgGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TgGroupService {
    
    @Autowired
    private TgGroupMapper tgGroupMapper;
    
    @Autowired
    private ImportExportService importExportService;

    public List<TgGroup> findAll() {
        return tgGroupMapper.findAll();
    }
    
    public List<TgGroup> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return tgGroupMapper.findAll();
        }
        return tgGroupMapper.search(keyword.trim());
    }

    public TgGroup findById(Long id) {
        return tgGroupMapper.findById(id);
    }
    
    public TgGroup findByGroupId(Long groupId) {
        return tgGroupMapper.findByGroupId(groupId);
    }

    public void save(TgGroup group) {
        if (group.getId() == null) {
            tgGroupMapper.insert(group);
        } else {
            tgGroupMapper.update(group);
        }
    }

    public void delete(Long id) {
        tgGroupMapper.delete(id);
    }
    
    /**
     * 导入群组
     */
    public int importGroups(MultipartFile file) throws Exception {
        List<TgGroup> groups = importExportService.importGroups(file);
        int count = 0;
        for (TgGroup group : groups) {
            // 检查是否已存在
            TgGroup existing = tgGroupMapper.findByGroupId(group.getGroupId());
            if (existing == null) {
                tgGroupMapper.insert(group);
                count++;
            } else {
                // 更新已存在的
                group.setId(existing.getId());
                tgGroupMapper.update(group);
                count++;
            }
        }
        return count;
    }
    
    /**
     * 下载导入模板
     */
    public String getTemplate() throws Exception {
        return importExportService.generateGroupTemplate();
    }
}