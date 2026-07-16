-- 迁移：client_sample_items 添加 snapshot_data 快照字段
-- 目的：每个代号下的样品数据独立存储，修改互不影响

-- 1. 添加快照字段（如果列已存在会报错，忽略即可）
ALTER TABLE `client_sample_items` 
  ADD COLUMN `snapshot_data` JSON DEFAULT NULL COMMENT '样品数据快照(JSON)';

ALTER TABLE `client_sample_items` 
  ADD COLUMN `calculated_price` DECIMAL(10,4) DEFAULT NULL COMMENT '报价1计算值';

ALTER TABLE `client_sample_items` 
  ADD COLUMN `calculated_price2` DECIMAL(10,4) DEFAULT NULL COMMENT '报价2计算值';

-- 2. 为已有数据填充快照（从 samples 表复制当前数据）
UPDATE `client_sample_items` i
INNER JOIN `samples` s ON i.sample_id = s.id AND s.deleted = 0
SET i.snapshot_data = JSON_OBJECT(
  'sampleCode', IFNULL(s.sample_code, ''),
  'manufacturerCode', IFNULL(s.manufacturer_code, ''),
  'sampleName', IFNULL(s.sample_name, ''),
  'englishName', IFNULL(s.english_name, ''),
  'category', IFNULL(s.category, ''),
  'categoryCode', IFNULL(s.category_code, ''),
  'factoryCode', IFNULL(s.factory_code, ''),
  'sampleUnit', IFNULL(s.sample_unit, ''),
  'sampleUnitEn', IFNULL(s.sample_unit_en, ''),
  'packagingCn', IFNULL(s.packaging_cn, ''),
  'packagingEn', IFNULL(s.packaging_en, ''),
  'packageCode', IFNULL(s.package_code, ''),
  'color', IFNULL(s.color, ''),
  'colorEn', IFNULL(s.color_en, ''),
  'size', IFNULL(s.size, ''),
  'origin', IFNULL(s.origin, ''),
  'supplier', IFNULL(s.supplier, ''),
  'boothNo', IFNULL(s.booth_no, ''),
  'contactPerson', IFNULL(s.contact_person, ''),
  'contactPhone', IFNULL(s.contact_phone, ''),
  'mobile', IFNULL(s.mobile, ''),
  'fax', IFNULL(s.fax, ''),
  'qq', IFNULL(s.qq, ''),
  'factoryPrice', s.factory_price,
  'taxPrice', s.tax_price,
  'sampleLength', s.sample_length,
  'sampleWidth', s.sample_width,
  'sampleHeight', s.sample_height,
  'sampleGrossWeight', s.sample_gross_weight,
  'sampleNetWeight', s.sample_net_weight,
  'cartonLength', s.carton_length,
  'cartonWidth', s.carton_width,
  'cartonHeight', s.carton_height,
  'cartonMaterialVolume', s.carton_material_volume,
  'cartonVolume', s.carton_volume,
  'innerBoxCount', s.inner_box_count,
  'cartonCapacity', s.carton_capacity,
  'packingUnit', IFNULL(s.packing_unit, ''),
  'cartonGrossWeight', s.carton_gross_weight,
  'cartonNetWeight', s.carton_net_weight,
  'packageLength', s.package_length,
  'packageWidth', s.package_width,
  'packageHeight', s.package_height,
  'certification', IFNULL(s.certification, ''),
  'certificationCount', s.certification_count,
  'remark', IFNULL(s.remark, ''),
  'remarkEn', IFNULL(s.remark_en, ''),
  'registrant', IFNULL(s.registrant, ''),
  'modifier', IFNULL(s.modifier, ''),
  'status', s.status,
  'infringement', IFNULL(s.infringement, ''),
  'batteryInfo', IFNULL(s.battery_info, ''),
  'hideFromXzx', IFNULL(s.hide_from_xzx, '')
)
WHERE i.snapshot_data IS NULL AND i.deleted = 0;
