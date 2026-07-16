CREATE TABLE IF NOT EXISTS `user_preferences` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `page_key` VARCHAR(100) NOT NULL,
  `preference_json` LONGTEXT,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_page` (`user_id`, `page_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
