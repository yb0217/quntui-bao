package com.quntui.service;

import com.quntui.model.TgGroup;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportExportService {

    /**
     * 导入群组 CSV
     * CSV格式：group_id,group_name,invite_link,welcome_enabled,welcome_message,ad_enabled,ad_interval_minutes
     */
    public List<TgGroup> importGroups(MultipartFile file) throws Exception {
        List<TgGroup> groups = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // 跳过表头
                }
                
                if (line.trim().isEmpty()) continue;
                
                String[] parts = parseCSVLine(line);
                if (parts.length < 1) continue;
                
                TgGroup group = new TgGroup();
                
                // group_id (必填)
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    try {
                        group.setGroupId(Long.parseLong(parts[0].trim()));
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else {
                    continue;
                }
                
                // group_name
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    group.setGroupName(parts[1].trim());
                }
                
                // invite_link
                if (parts.length > 2 && !parts[2].isEmpty()) {
                    group.setInviteLink(parts[2].trim());
                }
                
                // welcome_enabled (默认 true)
                if (parts.length > 3 && !parts[3].isEmpty()) {
                    group.setWelcomeEnabled(parseBoolean(parts[3].trim()));
                } else {
                    group.setWelcomeEnabled(true);
                }
                
                // welcome_message
                if (parts.length > 4 && !parts[4].isEmpty()) {
                    group.setWelcomeMessage(parts[4].trim());
                }
                
                // ad_enabled (默认 true)
                if (parts.length > 5 && !parts[5].isEmpty()) {
                    group.setAdEnabled(parseBoolean(parts[5].trim()));
                } else {
                    group.setAdEnabled(true);
                }
                
                // ad_interval_minutes (默认 60)
                if (parts.length > 6 && !parts[6].isEmpty()) {
                    try {
                        group.setAdIntervalMinutes(Integer.parseInt(parts[6].trim()));
                    } catch (NumberFormatException e) {
                        group.setAdIntervalMinutes(60);
                    }
                } else {
                    group.setAdIntervalMinutes(60);
                }
                
                groups.add(group);
            }
        }
        
        return groups;
    }
    
    /**
     * 解析CSV行（处理引号包裹的字段）
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        
        return fields.toArray(new String[0]);
    }
    
    private boolean parseBoolean(String val) {
        String v = val.toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("是") || v.equals("yes") || v.equals("y");
    }
    
    /**
     * 生成群组导入模板 CSV
     */
    public String generateGroupTemplate() {
        return "group_id,group_name,invite_link,welcome_enabled,welcome_message,ad_enabled,ad_interval_minutes\n" +
               "-1001234567890,营销群1,https://t.me/xxx,true,欢迎新朋友！,true,60\n" +
               "-1001234567891,营销群2,https://t.me/yyy,true,欢迎加入我们的群~,true,60\n";
    }
}