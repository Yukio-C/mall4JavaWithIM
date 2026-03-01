CREATE DATABASE IF NOT EXISTS `mall_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mall_db`;

-- ==========================================
-- 1. 用户模块 (User Module)
-- ==========================================

-- 用户表
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
  `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `balance` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '余额',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户表';

-- 收货地址表
CREATE TABLE `addresses` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(20) DEFAULT '' COMMENT '省',
  `city` VARCHAR(20) DEFAULT '' COMMENT '市',
  `district` VARCHAR(20) DEFAULT '' COMMENT '区',
  `detail` VARCHAR(100) NOT NULL COMMENT '详细地址',
  `tag` VARCHAR(10) DEFAULT '' COMMENT '标签：家/公司',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认：0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='收货地址表';


-- ==========================================
-- 2. 商品模块 (Product Module)
-- ==========================================

-- 商品分类表
CREATE TABLE `categories` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id` INT UNSIGNED DEFAULT 0 COMMENT '父分类ID，0为根分类',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序权重',
  PRIMARY KEY (`id`)
) COMMENT='商品分类表';

-- 商品表
CREATE TABLE `products` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `description` VARCHAR(255) DEFAULT '' COMMENT '商品简介',
  `cover` VARCHAR(255) NOT NULL COMMENT '封面图',
  `slider_imgs` TEXT COMMENT '轮播大图，JSON数组格式',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '当前售价',
  `original_price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '原价',
  `stock` INT UNSIGNED DEFAULT 0 COMMENT '库存',
  `sales` INT UNSIGNED DEFAULT 0 COMMENT '销量',
  `rating` DECIMAL(2, 1) DEFAULT 5.0 COMMENT '综合评分',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1上架 0下架',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_title` (`title`) -- 用于搜索
) COMMENT='商品表';


-- ==========================================
-- 3. 交易模块 (Order Module)
-- ==========================================

-- 购物车表
CREATE TABLE `carts` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `product_title` VARCHAR(100) NOT NULL COMMENT '商品标题冗余',
  `product_cover` VARCHAR(255) NOT NULL COMMENT '商品封面图冗余',
  `product_price` DECIMAL(10, 2) NOT NULL COMMENT '加入时价格冗余',
  `count` INT UNSIGNED DEFAULT 1 COMMENT '购买数量',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_cart` (`user_id`)
) COMMENT='购物车表';

-- 订单主表
CREATE TABLE `orders` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号(业务唯一)',
  `user_id` BIGINT UNSIGNED NOT NULL,
  `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '实付金额(减去优惠后)',

  -- 支付与状态
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待付款 1待发货 2已发货 3已完成 4已关闭 5售后中',
  `pay_type` TINYINT DEFAULT NULL COMMENT '1支付宝 2微信',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',

  -- 物流信息
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人快照',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '电话快照',
  `receiver_address` VARCHAR(200) NOT NULL COMMENT '完整地址快照',
  `logistics_company` VARCHAR(50) DEFAULT '' COMMENT '物流公司',
  `logistics_no` VARCHAR(64) DEFAULT '' COMMENT '物流单号',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_orders` (`user_id`)
) COMMENT='订单主表';

-- 订单明细表 (商品快照)
CREATE TABLE `order_items` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '关联订单ID',
  `product_id` BIGINT UNSIGNED NOT NULL,
  `product_title` VARCHAR(100) NOT NULL COMMENT '购买时标题快照',
  `product_cover` VARCHAR(255) NOT NULL COMMENT '购买时图片快照',
  `product_price` DECIMAL(10, 2) NOT NULL COMMENT '购买时单价快照',
  `count` INT UNSIGNED NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) COMMENT='订单商品明细表';


-- ==========================================
-- 4. 互动与售后 (Interaction Module)
-- ==========================================

-- 售后申请表
CREATE TABLE `after_sales` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1退款(仅退款) 2退货退款',
  `reason` VARCHAR(200) DEFAULT '' COMMENT '申请原因',
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1审核通过 2审核拒绝 3退款成功',
  `refund_amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='售后申请表';

-- 商品评价表
CREATE TABLE `reviews` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
  `content` TEXT COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片 JSON',
  `reply` TEXT COMMENT '商家回复',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_review` (`product_id`)
) COMMENT='评价管理表';

-- 预插入管理员账号 (对应前端 admin/admin)
-- 密码 'admin' 经过 BCrypt 加密后的哈希值为: $2a$10$76/97zE/u0XbVq1fP5fGxe1Xn5lXfW8fUv1z5/1Xn5lXfW8fUv1z5 (示例值，请确保后端一致)
-- 建议在开发环境开启特定初始化逻辑，或者使用如下语句手动插入：
INSERT INTO `users` (`username`, `password`, `nickname`, `role`) VALUES ('admin', '$2a$10$76/97zE/u0XbVq1fP5fGxe1Xn5lXfW8fUv1z5/1Xn5lXfW8fUv1z5', '超级管理员', 'ADMIN');
