import subprocess

# First, copy the CREATE TABLE from add-report-templates.sql
create_sql = """CREATE TABLE IF NOT EXISTS `report_templates` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `title` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '备注描述',
    `template_data` MEDIUMTEXT NOT NULL COMMENT '模板完整数据(JSON)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_title` (`title`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表模板表';
"""

# Export data
r = subprocess.run(
    ['mysql', '-u', 'root', '-p123456', 'photo_management',
     '--default-character-set=utf8mb4', '--skip-column-names', '--batch',
     '-e', "SELECT CONCAT('INSERT INTO report_templates (id, title, description, template_data, create_time, update_time, deleted, create_by, update_by) VALUES (', id, ',', QUOTE(title), ',', QUOTE(COALESCE(description,'')), ',', QUOTE(COALESCE(template_data,'')), ',', QUOTE(create_time), ',', QUOTE(update_time), ',', deleted, ',', COALESCE(create_by,'NULL'), ',', COALESCE(update_by,'NULL'), ');') FROM report_templates WHERE id IN (32, 40) ORDER BY id"],
    capture_output=True, text=False
)
output = r.stdout.decode('utf-8')

with open(r'd:\客户端测试\backend\sql\export-report-templates.sql', 'w', encoding='utf-8') as f:
    f.write("SET NAMES utf8mb4;\n")
    f.write(create_sql)
    f.write("\n")
    f.write(output)
print("Done - table DDL + 2 templates.")
