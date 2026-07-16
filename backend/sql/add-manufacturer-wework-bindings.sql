-- 厂商微信绑定关联表（一个厂商可绑定多个微信）
CREATE TABLE IF NOT EXISTS manufacturer_wework_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id BIGINT NOT NULL COMMENT '厂商ID',
    wework_external_userid VARCHAR(100) NOT NULL COMMENT '企业微信外部联系人ID',
    phone VARCHAR(20) DEFAULT NULL COMMENT '绑定时对应用哪个手机号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    INDEX idx_manufacturer_id (manufacturer_id),
    UNIQUE KEY uk_external_userid (wework_external_userid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厂商微信绑定表';
