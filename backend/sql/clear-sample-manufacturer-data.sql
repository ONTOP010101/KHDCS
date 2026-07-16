-- ============================================================
-- 清空样品资料数据 & 厂商资料数据
-- 执行前请先备份数据库！
-- 执行顺序：先删子表/关联表，再删主表
-- ============================================================

-- 1. 客户选样价格设置（关联 client_samples）
DELETE FROM client_sample_price_settings;

-- 2. 客户选样明细（关联 samples、client_samples）
DELETE FROM client_sample_items;

-- 3. 客户选样主表
DELETE FROM client_samples;

-- 4. 样品缩略图（关联 samples）
DELETE FROM sample_thumbnail;

-- 5. 样品图片（关联 samples）
DELETE FROM images;

-- 6. 样品视频（关联 samples）
DELETE FROM videos;

-- 7. 库存明细（关联 manufacturers）
DELETE FROM inventory;

-- 8. 库存条码
DELETE FROM inventory_code;

-- 9. 库存分组
DELETE FROM inventory_group;

-- 10. 出库明细（关联 manufacturers）
DELETE FROM outbound;

-- 11. 出库条码
DELETE FROM outbound_code;

-- 12. 样品主表
DELETE FROM samples;

-- 13. 厂商主表
DELETE FROM manufacturers;

-- ============================================================
-- 清空完成后，还需手动清理：
-- 1. ES 索引中的 sample 数据（见下方 curl 命令）
-- 2. 上传的图片/视频文件目录（如 uploads/ 下对应文件）
-- ============================================================

-- ============================================================
-- 清空 ES 样品索引（在服务器终端执行）:
-- curl -X POST "http://localhost:9200/samples/_delete_by_query" -H "Content-Type: application/json" -d "{\"query\":{\"match_all\":{}}}"
-- ============================================================
