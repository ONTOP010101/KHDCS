-- client_sample_items 增加展厅已补、借样标记列
ALTER TABLE `client_sample_items`
  ADD COLUMN `showroom_replenished` TINYINT DEFAULT 0 COMMENT '展厅已补 (0=否,1=是)';

ALTER TABLE `client_sample_items`
  ADD COLUMN `borrowed_sample` TINYINT DEFAULT 0 COMMENT '借样 (0=否,1=是)';
