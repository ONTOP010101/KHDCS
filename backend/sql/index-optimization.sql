USE photo_management;

ALTER TABLE images ADD INDEX idx_gallery_sort (gallery_id, sort_order, create_time);

ALTER TABLE images ADD INDEX idx_gallery_create (gallery_id, create_time);

ALTER TABLE chat_messages ADD INDEX idx_conv_time (conversation_id, create_time);

ALTER TABLE samples ADD FULLTEXT INDEX ftx_sample_search (sample_name, material, description);

ALTER TABLE users ADD INDEX idx_real_name (real_name);

ALTER TABLE gallery ADD INDEX idx_category_time (category, create_time);

ALTER TABLE login_logs ADD INDEX idx_user_time (user_id, create_time);

ALTER TABLE operation_logs ADD INDEX idx_module_time (module, create_time);
