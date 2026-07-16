-- 千万级数据必备索引
CREATE INDEX IF NOT EXISTS idx_samples_sample_code ON samples(sample_code);
CREATE INDEX IF NOT EXISTS idx_samples_factory_code ON samples(factory_code);
CREATE INDEX IF NOT EXISTS idx_samples_manufacturer_code ON samples(manufacturer_code);
CREATE INDEX IF NOT EXISTS idx_samples_create_time ON samples(create_time);
CREATE INDEX IF NOT EXISTS idx_samples_deleted ON samples(deleted);
-- 联合索引：分页查询核心（WHERE deleted=0 + ORDER BY create_time），替换单列 idx_samples_create_time 的作用
CREATE INDEX IF NOT EXISTS idx_samples_deleted_create_time ON samples(deleted, create_time);

-- 综合查询高频字段索引
CREATE INDEX IF NOT EXISTS idx_samples_name ON samples(name);
CREATE INDEX IF NOT EXISTS idx_samples_category ON samples(category);
CREATE INDEX IF NOT EXISTS idx_samples_packaging_cn ON samples(packaging_cn);
CREATE INDEX IF NOT EXISTS idx_samples_contact1 ON samples(contact1);
CREATE INDEX IF NOT EXISTS idx_samples_booth_no ON samples(booth_no);
CREATE INDEX IF NOT EXISTS idx_samples_certification ON samples(certification);
CREATE INDEX IF NOT EXISTS idx_samples_infringement ON samples(infringement);
CREATE INDEX IF NOT EXISTS idx_samples_package_code ON samples(package_code);

-- 数值范围查询索引
CREATE INDEX IF NOT EXISTS idx_samples_factory_price ON samples(factory_price);
CREATE INDEX IF NOT EXISTS idx_samples_inner_box_count ON samples(inner_box_count);
CREATE INDEX IF NOT EXISTS idx_samples_carton_capacity ON samples(carton_capacity);

-- 复合索引：常用组合查询
CREATE INDEX IF NOT EXISTS idx_samples_sname_scode ON samples(sample_name, sample_code);

CREATE INDEX IF NOT EXISTS idx_images_sample_id ON images(sample_id);
CREATE INDEX IF NOT EXISTS idx_images_hash ON images(hash(32));

CREATE INDEX IF NOT EXISTS idx_videos_sample_id ON videos(sample_id);

-- 关键词全文搜索（中文分词用内置 parser）
ALTER TABLE samples ADD FULLTEXT INDEX ft_samples_keyword (sample_code, factory_code, sample_name, manufacturer_code);
-- 样品搜索全文索引（支持 3+ 字符关键词毫秒级搜索，对应 SampleService.list 搜索逻辑）
ALTER TABLE samples ADD FULLTEXT INDEX ftx_sample_search2 (sample_code, sample_name, manufacturer_code, factory_code);

