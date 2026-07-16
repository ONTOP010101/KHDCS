-- 为择样明细表添加 checked 字段（批量勾选标记，服务端共享）
ALTER TABLE `client_sample_items`
  ADD COLUMN `checked` TINYINT DEFAULT 0 COMMENT '是否选中 (0=未选,1=已选)' AFTER `borrowed_sample`;
