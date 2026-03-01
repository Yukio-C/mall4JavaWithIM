USE `mall_db`;

-- 1. 给订单明细表增加评价状态
ALTER TABLE `order_items` ADD COLUMN `is_commented` TINYINT(1) DEFAULT 0 COMMENT '是否已评价';

-- 2. 确保 reviews 表结构对齐 (如果之前没成功，这里是最后加固)
-- ALTER TABLE `reviews` ADD COLUMN `order_item_id` BIGINT NOT NULL COMMENT '关联订单明细ID' AFTER `order_id`;
-- ALTER TABLE `reviews` ADD COLUMN `is_anonymous` TINYINT(1) DEFAULT 0 COMMENT '是否匿名' AFTER `images`;
