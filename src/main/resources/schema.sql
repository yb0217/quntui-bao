-- 群推宝数据库初始化脚本

-- 创建表

-- 1. 管理员表
CREATE TABLE IF NOT EXISTS admin_user  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    role VARCHAR(20) DEFAULT 'admin',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. TG群组表
CREATE TABLE IF NOT EXISTS tg_group  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL UNIQUE COMMENT 'TG群ID',
    group_name VARCHAR(255) COMMENT '群名称',
    invite_link VARCHAR(500) COMMENT '邀请链接',
    welcome_enabled BOOLEAN DEFAULT TRUE COMMENT '启用欢迎',
    welcome_message TEXT COMMENT '欢迎消息',
    ad_enabled BOOLEAN DEFAULT TRUE COMMENT '启用广告',
    ad_interval_minutes INT DEFAULT 60 COMMENT '广告间隔(分钟)',
    last_ad_time DATETIME COMMENT '上次发广告时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. 欢迎消息模板表
CREATE TABLE IF NOT EXISTS welcome_template  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    content TEXT NOT NULL COMMENT '欢迎消息内容',
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. 广告消息表
CREATE TABLE IF NOT EXISTS ad_message  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) COMMENT '标题',
    content TEXT NOT NULL COMMENT '消息内容',
    buttons TEXT COMMENT '按钮JSON(兼容旧数据)',
    button_layout VARCHAR(20) DEFAULT '1x2' COMMENT '按钮布局',
    button_config TEXT COMMENT '可视化按钮配置',
    enabled BOOLEAN DEFAULT TRUE,
    delay_seconds INT DEFAULT 5 COMMENT '发送间隔(秒)',
    max_per_minute INT DEFAULT 20 COMMENT '每分钟限流',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. 发送日志表
CREATE TABLE IF NOT EXISTS send_log  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT COMMENT '群ID',
    target_user_id BIGINT COMMENT '目标用户ID',
    message_type VARCHAR(20) COMMENT 'welcome/ad',
    message_id BIGINT COMMENT '广告消息ID',
    content TEXT COMMENT '发送内容',
    success BOOLEAN COMMENT '是否成功',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 初始化管理员账号 admin / admin123 (SHA256)
INSERT INTO admin_user (username, password, role) 
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin')
ON DUPLICATE KEY UPDATE username=username;



-- 全局欢迎消息表（只保留一条）
CREATE TABLE IF NOT EXISTS welcome_message  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL COMMENT '欢迎消息内容（支持多行）',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);



-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config  (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化默认配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('ad_cycle_minutes', '60', '广告发送周期(分钟)'),
('ad_delay_seconds', '2', '发送间隔秒数'),
('ad_max_per_minute', '20', '每分钟限流')
ON DUPLICATE KEY UPDATE config_value=config_value;
