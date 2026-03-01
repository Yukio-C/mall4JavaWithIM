-- ==========================================
-- 4. 支付模块 (Payment Module)
-- ==========================================

USE `mall_db`;

-- 支付流水表
CREATE TABLE IF NOT EXISTS `payment_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '系统内部订单号',
  `trade_no` VARCHAR(128) DEFAULT '' COMMENT '支付平台流水号(外部交易号)',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联用户ID',
  `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  `pay_type` TINYINT UNSIGNED NOT NULL DEFAULT 3 COMMENT '支付方式: 1-支付宝, 2-微信, 3-余额',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态: 0-进行中, 1-支付成功, 2-支付失败',
  `raw_response` TEXT COMMENT '支付结果详细记录(JSON字符串)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '支付请求发起时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`) COMMENT '每个订单业务上对应一个终态流水',
  KEY `idx_trade_no` (`trade_no`) COMMENT '外部流水号索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用户支付记录索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水记录表';
