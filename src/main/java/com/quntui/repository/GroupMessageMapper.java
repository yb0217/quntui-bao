package com.quntui.repository;

import com.quntui.model.GroupMessage;
import org.apache.ibatis.annotations.*;

/**
 * 群组消息记录 Mapper
 */
@Mapper
public interface GroupMessageMapper {

    @Insert("INSERT INTO group_message (group_id, message_type, message_id) " +
            "VALUES (#{groupId}, #{messageType}, #{messageId}) " +
            "ON DUPLICATE KEY UPDATE message_id = #{messageId}, updated_at = NOW()")
    void saveOrUpdate(GroupMessage message);

    @Select("SELECT * FROM group_message WHERE group_id = #{groupId} AND message_type = #{messageType}")
    GroupMessage findByGroupAndType(@Param("groupId") String groupId, @Param("messageType") String messageType);

    @Delete("DELETE FROM group_message WHERE group_id = #{groupId} AND message_type = #{messageType}")
    void deleteByGroupAndType(@Param("groupId") String groupId, @Param("messageType") String messageType);

    @Delete("DELETE FROM group_message WHERE group_id = #{groupId}")
    void deleteByGroup(@Param("groupId") String groupId);
}
