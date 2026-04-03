package com.quntui.service;

import com.quntui.model.GroupMessage;
import com.quntui.repository.GroupMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 群组消息记录服务
 */
@Slf4j
@Service
public class GroupMessageService {

    @Autowired
    private GroupMessageMapper mapper;

    /**
     * 保存或更新消息记录
     */
    public void saveMessage(String groupId, String messageType, Long messageId) {
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setMessageType(messageType);
        msg.setMessageId(messageId);
        mapper.saveOrUpdate(msg);
        log.debug("保存消息记录: group={}, type={}, msgId={}", groupId, messageType, messageId);
    }

    /**
     * 获取消息记录
     */
    public GroupMessage getMessage(String groupId, String messageType) {
        return mapper.findByGroupAndType(groupId, messageType);
    }

    /**
     * 删除消息记录
     */
    public void deleteMessage(String groupId, String messageType) {
        mapper.deleteByGroupAndType(groupId, messageType);
        log.debug("删除消息记录: group={}, type={}", groupId, messageType);
    }
}
