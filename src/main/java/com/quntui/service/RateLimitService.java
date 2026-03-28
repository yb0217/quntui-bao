package com.quntui.service;

import com.quntui.model.TgGroup;
import com.quntui.model.AdMessage;
import com.quntui.model.SendLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quntui.repository.SendLogMapper;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class RateLimitService {
    
    @Autowired
    private SendLogMapper sendLogMapper;

    /**
     * TG机器人对群发消息限流规则:
     * - 每分钟最多 20 条消息
     * - 每次发送间隔至少 3-5 秒
     */
    
    /**
     * 检查是否可以发送广告
     * @param group 群组
     * @param adMessage 广告消息
     * @return null=可以发送, String=不能发送的原因
     */
    public String checkCanSend(TgGroup group, AdMessage adMessage) {
        if (group == null || !group.getAdEnabled()) {
            return "群广告未启用";
        }
        if (adMessage == null || !adMessage.getEnabled()) {
            return "广告消息未启用";
        }
        
        // 检查上次发送时间间隔(防止触发TG限流)
        LocalDateTime lastAdTime = group.getLastAdTime();
        if (lastAdTime != null) {
            long secondsSinceLastAd = ChronoUnit.SECONDS.between(lastAdTime, LocalDateTime.now());
            Integer delaySeconds = adMessage.getDelaySeconds();
            if (delaySeconds == null) delaySeconds = 5;
            
            if (secondsSinceLastAd < delaySeconds) {
                return "发送间隔不足，需等待 " + (delaySeconds - secondsSinceLastAd) + " 秒";
            }
        }
        
        // 检查本分钟发送次数(TG限流: 每分钟20条)
        int thisMinuteCount = getThisMinuteSendCount(group.getGroupId());
        Integer maxPerMinute = adMessage.getMaxPerMinute();
        if (maxPerMinute == null) maxPerMinute = 20;
        
        if (thisMinuteCount >= maxPerMinute) {
            return "本分钟已发送 " + thisMinuteCount + " 条，达到上限(" + maxPerMinute + ")";
        }
        
        return null; // 可以发送
    }
    
    /**
     * 获取本分钟发送次数
     */
    public int getThisMinuteSendCount(Long groupId) {
        List<Map<String, Object>> list = sendLogMapper.getThisMinuteCountByGroup(groupId);
        if (list.isEmpty() || list.get(0).get("cnt") == null) {
            return 0;
        }
        return ((Number) list.get(0).get("cnt")).intValue();
    }
    
    /**
     * 获取今日发送次数
     */
    public int getTodaySendCount(Long groupId) {
        List<Map<String, Object>> list = sendLogMapper.getTodayCountByGroup(groupId);
        if (list.isEmpty() || list.get(0).get("cnt") == null) {
            return 0;
        }
        return ((Number) list.get(0).get("cnt")).intValue();
    }
    
    /**
     * 记录发送结果
     */
    public void recordSend(Long groupId, Long adMessageId, String content, boolean success, String errorMsg) {
        SendLog log = new SendLog();
        log.setGroupId(groupId);
        log.setMessageType("ad");
        log.setMessageId(adMessageId);
        log.setContent(content);
        log.setSuccess(success);
        log.setErrorMsg(errorMsg);
        sendLogMapper.insert(log);
    }
}