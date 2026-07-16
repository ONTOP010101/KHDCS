-- 添加 submitted 字段到 inventory 和 outbound 表
-- 用于标记记录是否已提交，只有已提交的记录会计入总库存

ALTER TABLE inventory ADD COLUMN submitted TINYINT(1) DEFAULT 0 COMMENT '是否已提交 0-未提交 1-已提交';
ALTER TABLE outbound ADD COLUMN submitted TINYINT(1) DEFAULT 0 COMMENT '是否已提交 0-未提交 1-已提交';

-- 建索引加速查询
CREATE INDEX idx_inventory_submitted ON inventory(submitted);
CREATE INDEX idx_outbound_submitted ON outbound(submitted);
