-- ==========================================
-- 售后与实时通讯模块扩展脚本 (方案二：主从明细模式)
-- ==========================================

USE `mall_db`;

-- 1. 售后申请主表
DROP TABLE IF EXISTS `after_sales`;
CREATE TABLE `after_sales` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单号',
  `type` TINYINT NOT NULL COMMENT '1:仅退款, 2:退货退款, 3:换货',
  `reason` VARCHAR(200) NOT NULL COMMENT '申请原因',
  `description` TEXT COMMENT '问题描述',
  `images` TEXT COMMENT '凭证图片URL (JSON)',
  `status` TINYINT DEFAULT 0 COMMENT '0:待审核, 1:处理中, 2:已完成, 3:已拒绝',
  `refund_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '总退款金额',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME DEFAULT NULL,
  `handle_remark` VARCHAR(255) DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请主表';

-- 2. 售后申请明细表 (处理一个订单多个商品的情况)
DROP TABLE IF EXISTS `after_sale_items`;
CREATE TABLE `after_sale_items` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `after_sale_id` BIGINT UNSIGNED NOT NULL COMMENT '关联售后主表ID',
  `order_item_id` BIGINT UNSIGNED NOT NULL COMMENT '关联订单明细ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `product_title` VARCHAR(100) NOT NULL COMMENT '商品标题快照',
  `product_cover` VARCHAR(255) NOT NULL COMMENT '商品封面快照',
  `product_price` DECIMAL(10, 2) NOT NULL COMMENT '单价快照',
  `count` INT UNSIGNED NOT NULL COMMENT '售后数量',
  PRIMARY KEY (`id`),
  KEY `idx_after_sale_id` (`after_sale_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请明细表';

-- 3. 聊天消息表
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单号',
  `from_user_id` VARCHAR(64) NOT NULL,
  `to_user_id` VARCHAR(64) NOT NULL,
  `content` TEXT NOT NULL,
  `msg_type` TINYINT DEFAULT 1,
  `is_read` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后聊天消息记录表';
