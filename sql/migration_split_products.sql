-- ==========================================
-- 数据库迁移脚本: 拆分 products 表
-- 日期: 2026-02-08
-- 描述: 将 slider_imgs 迁移到 products_detail 表，并新增详情页字段
-- ==========================================

USE `mall_db`;

-- 1. 创建商品详情表 (products_detail)
CREATE TABLE IF NOT EXISTS `products_detail` (
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '关联商品ID',
  `slider_imgs` TEXT COMMENT '轮播大图，JSON数组格式',
  `detail_html` LONGTEXT COMMENT '商品图文详情 (富文本)',
  `specs` JSON COMMENT '规格参数 (JSON格式)',
  `service_info` VARCHAR(255) DEFAULT '7天无理由退货 · 48小时发货 · 正品保障' COMMENT '售后服务说明',
  PRIMARY KEY (`product_id`),
  CONSTRAINT `fk_products_detail_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) COMMENT='商品详情表';

-- 2. 数据迁移
-- 将原 products 表中的 slider_imgs 迁移过来
-- 同时将 description 简单包装后作为初始的 detail_html，防止详情页为空
INSERT INTO `products_detail` (`product_id`, `slider_imgs`, `detail_html`)
SELECT 
    `id`, 
    `slider_imgs`, 
    CONCAT('<div class="product-intro"><h3>商品介绍</h3><p>', `description`, '</p><p>此处为默认生成的图文详情，请后续在后台编辑。</p></div>') 
FROM `products`;

-- 3. 清理原表字段
-- 删除 products 表中不再需要的 slider_imgs 字段
ALTER TABLE `products` DROP COLUMN `slider_imgs`;

-- 验证操作
-- SELECT * FROM products_detail;
