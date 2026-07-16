-- 短信模板表
CREATE TABLE IF NOT EXISTS sms_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    type VARCHAR(20) NOT NULL COMMENT '类型: sms/wechat/wework',
    sms_type VARCHAR(20) DEFAULT NULL COMMENT '短信类型: 通知/营销 (仅sms)',
    sign_id INT DEFAULT NULL COMMENT '联麓签名ID',
    apply_purpose VARCHAR(100) DEFAULT NULL COMMENT '用途类型 (如 B06-其他业务管理服务类)',
    variable_type VARCHAR(50) DEFAULT 'number_letter' COMMENT '变量类型: number_letter=数字+字母, letter=仅字母',
    lianlu_template_id INT DEFAULT NULL COMMENT '联麓模板ID',
    content TEXT COMMENT '模板内容(可含变量)',
    status INT DEFAULT 2 COMMENT '状态: 0=待审核,1=审核通过,2=审核驳回',
    refuse_reason VARCHAR(500) DEFAULT NULL COMMENT '审核拒绝原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='短信/微信/企业微信模板';

-- 发送记录表
CREATE TABLE IF NOT EXISTS send_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code_name VARCHAR(50) NOT NULL COMMENT '本次代号',
    client_name VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
    type VARCHAR(20) NOT NULL COMMENT '类型: sms/wechat/wework',
    lianlu_template_id INT DEFAULT NULL COMMENT '联麓模板ID',
    template_name VARCHAR(200) DEFAULT NULL COMMENT '模板名称',
    manufacturer_code VARCHAR(100) DEFAULT NULL COMMENT '厂商编号',
    manufacturer_name VARCHAR(200) DEFAULT NULL COMMENT '厂商名称',
    booth_no VARCHAR(100) DEFAULT NULL COMMENT '摊位号',
    phone VARCHAR(50) DEFAULT NULL COMMENT '手机号',
    content TEXT COMMENT '实际发送内容',
    send_time DATETIME DEFAULT NULL COMMENT '发送时间',
    send_status VARCHAR(20) DEFAULT 'pending' COMMENT '发送状态: pending/success/fail',
    read_status INT DEFAULT 0 COMMENT '是否阅读: 0=未读,1=已读',
    task_id VARCHAR(100) DEFAULT NULL COMMENT '联麓任务ID',
    tag VARCHAR(100) DEFAULT NULL COMMENT '自定义标签',
    resp_code VARCHAR(50) DEFAULT NULL COMMENT '联麓回执码',
    code_desc VARCHAR(200) DEFAULT NULL COMMENT '回执描述',
    resp_time DATETIME DEFAULT NULL COMMENT '回执时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_code_name (code_name),
    INDEX idx_type (type),
    INDEX idx_send_status (send_status),
    INDEX idx_send_time (send_time),
    INDEX idx_task_id (task_id)
) COMMENT='发送记录表';
