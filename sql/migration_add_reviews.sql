USE `mall_db`;

-- 1. 修正评价表结构：在现有的 reviews 表基础上增加字段
ALTER TABLE `reviews` ADD COLUMN `order_item_id` BIGINT NOT NULL COMMENT '关联订单明细ID' AFTER `order_id`;
ALTER TABLE `reviews` ADD COLUMN `is_anonymous` TINYINT(1) DEFAULT 0 COMMENT '是否匿名' AFTER `images`;

-- 2. 给 orders 表增加一个评价标识，防止重复评价 (如果还没加的话)
ALTER TABLE `orders` ADD COLUMN `is_commented` TINYINT(1) DEFAULT 0 COMMENT '是否已评价';

-- 3. (可选) 如果之前误创了 product_reviews 表，可以执行删除
-- DROP TABLE IF EXISTS `product_reviews`;
