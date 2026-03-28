package com.quntui.controller;

import com.quntui.model.TgGroup;
import com.quntui.service.TgGroupService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class TgGroupController {
    
    @Autowired
    private TgGroupService tgGroupService;

    // 获取群组列表（支持搜索）
    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String keyword) {
        List<TgGroup> groups = tgGroupService.search(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", groups);
        result.put("total", groups.size());
        return result;
    }

    // 获取单个群组
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        TgGroup group = tgGroupService.findById(id);
        Map<String, Object> result = new HashMap<>();
        if (group != null) {
            result.put("code", 0);
            result.put("data", group);
        } else {
            result.put("code", 1);
            result.put("msg", "群组不存在");
        }
        return result;
    }

    // 创建/更新群组
    @PostMapping
    public Map<String, Object> save(@RequestBody TgGroup group) {
        tgGroupService.save(group);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody TgGroup group) {
        group.setId(id);
        tgGroupService.save(group);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }

    // 删除群组
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        tgGroupService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        return result;
    }
    
    // 下载导入模板 (CSV)
    @GetMapping("/template")
    public ResponseEntity<String> downloadTemplate() throws Exception {
        String template = tgGroupService.getTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "群组导入模板.csv");
        return new ResponseEntity<>(template, headers, HttpStatus.OK);
    }
    
    // 导入群组 (CSV)
    @PostMapping("/import")
    public Map<String, Object> importGroups(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "请选择文件");
            return result;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".csv") && !filename.toLowerCase().endsWith(".txt"))) {
            result.put("code", 1);
            result.put("msg", "请上传 CSV 格式文件(.csv)");
            return result;
        }
        
        try {
            int count = tgGroupService.importGroups(file);
            result.put("code", 0);
            result.put("msg", "导入成功，共 " + count + " 条记录");
            result.put("count", count);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "导入失败: " + e.getMessage());
        }
        
        return result;
    }
}