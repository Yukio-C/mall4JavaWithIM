USE `mall_db`;

-- 1. 新增综合评价字段
ALTER TABLE `products` ADD COLUMN `total_rating` INT UNSIGNED DEFAULT 0 COMMENT '评价总分';
ALTER TABLE `products` ADD COLUMN `rating_count` INT UNSIGNED DEFAULT 0 COMMENT '评价总人数';

-- 2. (可选) 初始化现有数据：根据 reviews 表同步一次旧数据
-- UPDATE products p SET 
--   p.rating_count = (SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.id),
--   p.total_rating = (SELECT IFNULL(SUM(rating), 0) FROM reviews r WHERE r.product_id = p.id);
