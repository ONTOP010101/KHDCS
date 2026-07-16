-- 厂商表增加企业微信外部联系人ID
ALTER TABLE manufacturers ADD COLUMN wework_external_userid VARCHAR(100) DEFAULT NULL COMMENT '企业微信外部联系人ID';
