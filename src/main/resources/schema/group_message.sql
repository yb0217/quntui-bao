-- 群组消息记录表（用于删除旧消息）
CREATE TABLE IF NOT EXISTS group_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id VARCHAR(100) NOT NULL COMMENT 'TG群组ID',
    message_type ENUM('welcome', 'ad') NOT NULL COMMENT '消息类型',
    message_id BIGINT NOT NULL COMMENT 'TG消息ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_type (group_id, message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组消息记录';
