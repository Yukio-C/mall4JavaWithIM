CREATE DATABASE IF NOT EXISTS `mall_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mall_db`;

-- ==========================================
-- 1. 鐢ㄦ埛妯″潡 (User Module)
-- ==========================================

-- 鐢ㄦ埛琛?
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `username` VARCHAR(50) NOT NULL COMMENT '鐢ㄦ埛鍚?,
  `password` VARCHAR(100) NOT NULL COMMENT '鍔犲瘑鍚庣殑瀵嗙爜',
  `nickname` VARCHAR(50) DEFAULT '' COMMENT '鏄电О',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '澶村儚URL',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '鎵嬫満鍙?,
  `balance` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '浣欓',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '瑙掕壊: USER, ADMIN',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '娉ㄥ唽鏃堕棿',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) COMMENT='鐢ㄦ埛琛?;

-- 鏀惰揣鍦板潃琛?
CREATE TABLE `addresses` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '鍏宠仈鐢ㄦ埛ID',
  `name` VARCHAR(50) NOT NULL COMMENT '鏀惰揣浜哄鍚?,
  `phone` VARCHAR(20) NOT NULL COMMENT '鏀惰揣浜虹數璇?,
  `province` VARCHAR(20) DEFAULT '' COMMENT '鐪?,
  `city` VARCHAR(20) DEFAULT '' COMMENT '甯?,
  `district` VARCHAR(20) DEFAULT '' COMMENT '鍖?,
  `detail` VARCHAR(100) NOT NULL COMMENT '璇︾粏鍦板潃',
  `tag` VARCHAR(10) DEFAULT '' COMMENT '鏍囩锛氬/鍏徃',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '鏄惁榛樿锛?鍚?1鏄?,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='鏀惰揣鍦板潃琛?;


-- ==========================================
-- 2. 鍟嗗搧妯″潡 (Product Module)
-- ==========================================

-- 鍟嗗搧鍒嗙被琛?
CREATE TABLE `categories` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id` INT UNSIGNED DEFAULT 0 COMMENT '鐖跺垎绫籌D锛?涓烘牴鍒嗙被',
  `name` VARCHAR(50) NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `sort` INT DEFAULT 0 COMMENT '鎺掑簭鏉冮噸',
  PRIMARY KEY (`id`)
) COMMENT='鍟嗗搧鍒嗙被琛?;

-- 鍟嗗搧琛?
CREATE TABLE `products` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id` INT UNSIGNED NOT NULL COMMENT '鍒嗙被ID',
  `title` VARCHAR(100) NOT NULL COMMENT '鍟嗗搧鏍囬',
  `description` VARCHAR(255) DEFAULT '' COMMENT '鍟嗗搧绠€浠?,
  `cover` VARCHAR(255) NOT NULL COMMENT '灏侀潰鍥?,
  `slider_imgs` TEXT COMMENT '杞挱澶у浘锛孞SON鏁扮粍鏍煎紡',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '褰撳墠鍞环',
  `original_price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '鍘熶环',
  `stock` INT UNSIGNED DEFAULT 0 COMMENT '搴撳瓨',
  `sales` INT UNSIGNED DEFAULT 0 COMMENT '閿€閲?,
  `rating` DECIMAL(2, 1) DEFAULT 5.0 COMMENT '缁煎悎璇勫垎',
  `status` TINYINT(1) DEFAULT 1 COMMENT '鐘舵€侊細1涓婃灦 0涓嬫灦',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_title` (`title`) -- 鐢ㄤ簬鎼滅储
) COMMENT='鍟嗗搧琛?;


-- ==========================================
-- 3. 浜ゆ槗妯″潡 (Order Module)
-- ==========================================

-- 璐墿杞﹁〃
CREATE TABLE `carts` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `product_title` VARCHAR(100) NOT NULL COMMENT '鍟嗗搧鏍囬鍐椾綑',
  `product_cover` VARCHAR(255) NOT NULL COMMENT '鍟嗗搧灏侀潰鍥惧啑浣?,
  `product_price` DECIMAL(10, 2) NOT NULL COMMENT '鍔犲叆鏃朵环鏍煎啑浣?,
  `count` INT UNSIGNED DEFAULT 1 COMMENT '璐拱鏁伴噺',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_cart` (`user_id`)
) COMMENT='璐墿杞﹁〃';

-- 璁㈠崟涓昏〃
CREATE TABLE `orders` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '璁㈠崟鍙?涓氬姟鍞竴)',
  `user_id` BIGINT UNSIGNED NOT NULL,
  `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '璁㈠崟鎬婚噾棰?,
  `pay_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '瀹炰粯閲戦(鍑忓幓浼樻儬鍚?',

  -- 鏀粯涓庣姸鎬?
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0寰呬粯娆?1寰呭彂璐?2宸插彂璐?3宸插畬鎴?4宸插叧闂?5鍞悗涓?,
  `pay_type` TINYINT DEFAULT NULL COMMENT '1鏀粯瀹?2寰俊',
  `pay_time` DATETIME DEFAULT NULL COMMENT '鏀粯鏃堕棿',

  -- 鐗╂祦淇℃伅
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '鏀惰揣浜哄揩鐓?,
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '鐢佃瘽蹇収',
  `receiver_address` VARCHAR(200) NOT NULL COMMENT '瀹屾暣鍦板潃蹇収',
  `logistics_company` VARCHAR(50) DEFAULT '' COMMENT '鐗╂祦鍏徃',
  `logistics_no` VARCHAR(64) DEFAULT '' COMMENT '鐗╂祦鍗曞彿',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '鍙戣揣鏃堕棿',

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_orders` (`user_id`)
) COMMENT='璁㈠崟涓昏〃';

-- 璁㈠崟鏄庣粏琛?(鍟嗗搧蹇収)
CREATE TABLE `order_items` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '鍏宠仈璁㈠崟ID',
  `product_id` BIGINT UNSIGNED NOT NULL,
  `product_title` VARCHAR(100) NOT NULL COMMENT '璐拱鏃舵爣棰樺揩鐓?,
  `product_cover` VARCHAR(255) NOT NULL COMMENT '璐拱鏃跺浘鐗囧揩鐓?,
  `product_price` DECIMAL(10, 2) NOT NULL COMMENT '璐拱鏃跺崟浠峰揩鐓?,
  `count` INT UNSIGNED NOT NULL COMMENT '璐拱鏁伴噺',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) COMMENT='璁㈠崟鍟嗗搧鏄庣粏琛?;


-- ==========================================
-- 4. 浜掑姩涓庡敭鍚?(Interaction Module)
-- ==========================================

-- 鍞悗鐢宠琛?
CREATE TABLE `after_sales` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1閫€娆?浠呴€€娆? 2閫€璐ч€€娆?,
  `reason` VARCHAR(200) DEFAULT '' COMMENT '鐢宠鍘熷洜',
  `status` TINYINT DEFAULT 0 COMMENT '0寰呭鏍?1瀹℃牳閫氳繃 2瀹℃牳鎷掔粷 3閫€娆炬垚鍔?,
  `refund_amount` DECIMAL(10, 2) NOT NULL COMMENT '閫€娆鹃噾棰?,
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='鍞悗鐢宠琛?;

-- 鍟嗗搧璇勪环琛?
CREATE TABLE `reviews` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '璇勫垎 1-5',
  `content` TEXT COMMENT '璇勪环鍐呭',
  `images` TEXT COMMENT '璇勪环鍥剧墖 JSON',
  `reply` TEXT COMMENT '鍟嗗鍥炲',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_review` (`product_id`)
) COMMENT='璇勪环绠＄悊琛?;

-- 棰勬彃鍏ョ鐞嗗憳璐﹀彿 (瀵瑰簲鍓嶇 admin/admin)
-- 瀵嗙爜 'admin' 缁忚繃 BCrypt 鍔犲瘑鍚庣殑鍝堝笇鍊间负: $2a$10$76/97zE/u0XbVq1fP5fGxe1Xn5lXfW8fUv1z5/1Xn5lXfW8fUv1z5 (绀轰緥鍊硷紝璇风‘淇濆悗绔竴鑷?
-- 寤鸿鍦ㄥ紑鍙戠幆澧冨紑鍚壒瀹氬垵濮嬪寲閫昏緫锛屾垨鑰呬娇鐢ㄥ涓嬭鍙ユ墜鍔ㄦ彃鍏ワ細
INSERT INTO `users` (`username`, `password`, `nickname`, `role`) VALUES ('admin', '$2a$10$76/97zE/u0XbVq1fP5fGxe1Xn5lXfW8fUv1z5/1Xn5lXfW8fUv1z5', '瓒呯骇绠＄悊鍛?, 'ADMIN');
-- ==========================================
-- 4. 鏀粯妯″潡 (Payment Module)
-- ==========================================

USE `mall_db`;

-- 鏀粯娴佹按琛?CREATE TABLE IF NOT EXISTS `payment_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '绯荤粺鍐呴儴璁㈠崟鍙?,
  `trade_no` VARCHAR(128) DEFAULT '' COMMENT '鏀粯骞冲彴娴佹按鍙?澶栭儴浜ゆ槗鍙?',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '鍏宠仈鐢ㄦ埛ID',
  `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '鏀粯閲戦',
  `pay_type` TINYINT UNSIGNED NOT NULL DEFAULT 3 COMMENT '鏀粯鏂瑰紡: 1-鏀粯瀹? 2-寰俊, 3-浣欓',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '鐘舵€? 0-杩涜涓? 1-鏀粯鎴愬姛, 2-鏀粯澶辫触',
  `raw_response` TEXT COMMENT '鏀粯缁撴灉璇︾粏璁板綍(JSON瀛楃涓?',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鏀粯璇锋眰鍙戣捣鏃堕棿',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`) COMMENT '姣忎釜璁㈠崟涓氬姟涓婂搴斾竴涓粓鎬佹祦姘?,
  KEY `idx_trade_no` (`trade_no`) COMMENT '澶栭儴娴佹按鍙风储寮?,
  KEY `idx_user_id` (`user_id`) COMMENT '鐢ㄦ埛鏀粯璁板綍绱㈠紩'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏀粯娴佹按璁板綍琛?;
-- ==========================================
-- 鍞悗涓庡疄鏃堕€氳妯″潡鎵╁睍鑴氭湰 (鏂规浜岋細涓讳粠鏄庣粏妯″紡)
-- ==========================================

USE `mall_db`;

-- 1. 鍞悗鐢宠涓昏〃
DROP TABLE IF EXISTS `after_sales`;
CREATE TABLE `after_sales` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '鐢ㄦ埛ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '璁㈠崟ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '鍏宠仈璁㈠崟鍙?,
  `type` TINYINT NOT NULL COMMENT '1:浠呴€€娆? 2:閫€璐ч€€娆? 3:鎹㈣揣',
  `reason` VARCHAR(200) NOT NULL COMMENT '鐢宠鍘熷洜',
  `description` TEXT COMMENT '闂鎻忚堪',
  `images` TEXT COMMENT '鍑瘉鍥剧墖URL (JSON)',
  `status` TINYINT DEFAULT 0 COMMENT '0:寰呭鏍? 1:澶勭悊涓? 2:宸插畬鎴? 3:宸叉嫆缁?,
  `refund_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '鎬婚€€娆鹃噾棰?,
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME DEFAULT NULL,
  `handle_remark` VARCHAR(255) DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍞悗鐢宠涓昏〃';

-- 2. 鍞悗鐢宠鏄庣粏琛?(澶勭悊涓€涓鍗曞涓晢鍝佺殑鎯呭喌)
DROP TABLE IF EXISTS `after_sale_items`;
CREATE TABLE `after_sale_items` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `after_sale_id` BIGINT UNSIGNED NOT NULL COMMENT '鍏宠仈鍞悗涓昏〃ID',
  `order_item_id` BIGINT UNSIGNED NOT NULL COMMENT '鍏宠仈璁㈠崟鏄庣粏ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '鍟嗗搧ID',
  `product_title` VARCHAR(100) NOT NULL COMMENT '鍟嗗搧鏍囬蹇収',
  `product_cover` VARCHAR(255) NOT NULL COMMENT '鍟嗗搧灏侀潰蹇収',
  `product_price` DECIMAL(10, 2) NOT NULL COMMENT '鍗曚环蹇収',
  `count` INT UNSIGNED NOT NULL COMMENT '鍞悗鏁伴噺',
  PRIMARY KEY (`id`),
  KEY `idx_after_sale_id` (`after_sale_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍞悗鐢宠鏄庣粏琛?;

-- 3. 鑱婂ぉ娑堟伅琛?
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '鍏宠仈璁㈠崟鍙?,
  `from_user_id` VARCHAR(64) NOT NULL,
  `to_user_id` VARCHAR(64) NOT NULL,
  `content` TEXT NOT NULL,
  `msg_type` TINYINT DEFAULT 1,
  `is_read` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍞悗鑱婂ぉ娑堟伅璁板綍琛?;
ALTER TABLE `users` ADD COLUMN `permissions` TEXT DEFAULT NULL COMMENT '鏉冮檺鍒楄〃(JSON瀛樺偍椤甸潰鏍囪瘑)';
-- ==========================================
-- 鏁版嵁搴撹縼绉昏剼鏈? 鎷嗗垎 products 琛?-- 鏃ユ湡: 2026-02-08
-- 鎻忚堪: 灏?slider_imgs 杩佺Щ鍒?products_detail 琛紝骞舵柊澧炶鎯呴〉瀛楁
-- ==========================================

USE `mall_db`;

-- 1. 鍒涘缓鍟嗗搧璇︽儏琛?(products_detail)
CREATE TABLE IF NOT EXISTS `products_detail` (
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '鍏宠仈鍟嗗搧ID',
  `slider_imgs` TEXT COMMENT '杞挱澶у浘锛孞SON鏁扮粍鏍煎紡',
  `detail_html` LONGTEXT COMMENT '鍟嗗搧鍥炬枃璇︽儏 (瀵屾枃鏈?',
  `specs` JSON COMMENT '瑙勬牸鍙傛暟 (JSON鏍煎紡)',
  `service_info` VARCHAR(255) DEFAULT '7澶╂棤鐞嗙敱閫€璐?路 48灏忔椂鍙戣揣 路 姝ｅ搧淇濋殰' COMMENT '鍞悗鏈嶅姟璇存槑',
  PRIMARY KEY (`product_id`),
  CONSTRAINT `fk_products_detail_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) COMMENT='鍟嗗搧璇︽儏琛?;

-- 2. 鏁版嵁杩佺Щ
-- 灏嗗師 products 琛ㄤ腑鐨?slider_imgs 杩佺Щ杩囨潵
-- 鍚屾椂灏?description 绠€鍗曞寘瑁呭悗浣滀负鍒濆鐨?detail_html锛岄槻姝㈣鎯呴〉涓虹┖
INSERT INTO `products_detail` (`product_id`, `slider_imgs`, `detail_html`)
SELECT 
    `id`, 
    `slider_imgs`, 
    CONCAT('<div class="product-intro"><h3>鍟嗗搧浠嬬粛</h3><p>', `description`, '</p><p>姝ゅ涓洪粯璁ょ敓鎴愮殑鍥炬枃璇︽儏锛岃鍚庣画鍦ㄥ悗鍙扮紪杈戙€?/p></div>') 
FROM `products`;

-- 3. 娓呯悊鍘熻〃瀛楁
-- 鍒犻櫎 products 琛ㄤ腑涓嶅啀闇€瑕佺殑 slider_imgs 瀛楁
ALTER TABLE `products` DROP COLUMN `slider_imgs`;

-- 楠岃瘉鎿嶄綔
-- SELECT * FROM products_detail;
ALTER TABLE `orders` ADD COLUMN `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '鍙栨秷鍘熷洜';
ALTER TABLE `orders` ADD COLUMN `cancel_time` DATETIME DEFAULT NULL COMMENT '鍙栨秷鏃堕棿';
USE `mall_db`;

-- 1. 鏂板缁煎悎璇勪环瀛楁
ALTER TABLE `products` ADD COLUMN `total_rating` INT UNSIGNED DEFAULT 0 COMMENT '璇勪环鎬诲垎';
ALTER TABLE `products` ADD COLUMN `rating_count` INT UNSIGNED DEFAULT 0 COMMENT '璇勪环鎬讳汉鏁?;

-- 2. (鍙€? 鍒濆鍖栫幇鏈夋暟鎹細鏍规嵁 reviews 琛ㄥ悓姝ヤ竴娆℃棫鏁版嵁
-- UPDATE products p SET 
--   p.rating_count = (SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.id),
--   p.total_rating = (SELECT IFNULL(SUM(rating), 0) FROM reviews r WHERE r.product_id = p.id);
USE `mall_db`;

-- 1. 淇璇勪环琛ㄧ粨鏋勶細鍦ㄧ幇鏈夌殑 reviews 琛ㄥ熀纭€涓婂鍔犲瓧娈?
ALTER TABLE `reviews` ADD COLUMN `order_item_id` BIGINT NOT NULL COMMENT '鍏宠仈璁㈠崟鏄庣粏ID' AFTER `order_id`;
ALTER TABLE `reviews` ADD COLUMN `is_anonymous` TINYINT(1) DEFAULT 0 COMMENT '鏄惁鍖垮悕' AFTER `images`;

-- 2. 缁?orders 琛ㄥ鍔犱竴涓瘎浠锋爣璇嗭紝闃叉閲嶅璇勪环 (濡傛灉杩樻病鍔犵殑璇?
ALTER TABLE `orders` ADD COLUMN `is_commented` TINYINT(1) DEFAULT 0 COMMENT '鏄惁宸茶瘎浠?;

-- 3. (鍙€? 濡傛灉涔嬪墠璇垱浜?product_reviews 琛紝鍙互鎵ц鍒犻櫎
-- DROP TABLE IF EXISTS `product_reviews`;
USE `mall_db`;

-- 1. 缁欒鍗曟槑缁嗚〃澧炲姞璇勪环鐘舵€?
ALTER TABLE `order_items` ADD COLUMN `is_commented` TINYINT(1) DEFAULT 0 COMMENT '鏄惁宸茶瘎浠?;

-- 2. 纭繚 reviews 琛ㄧ粨鏋勫榻?(濡傛灉涔嬪墠娌℃垚鍔燂紝杩欓噷鏄渶鍚庡姞鍥?
-- ALTER TABLE `reviews` ADD COLUMN `order_item_id` BIGINT NOT NULL COMMENT '鍏宠仈璁㈠崟鏄庣粏ID' AFTER `order_id`;
-- ALTER TABLE `reviews` ADD COLUMN `is_anonymous` TINYINT(1) DEFAULT 0 COMMENT '鏄惁鍖垮悕' AFTER `images`;
-- 1. 娓呯悊鏃ф暟鎹?(鍙€?
-- TRUNCATE TABLE `categories`;
-- TRUNCATE TABLE `products`;

-- 2. 鎻掑叆鍟嗗搧鍒嗙被
INSERT INTO `categories` (`id`, `parent_id`, `name`, `icon`, `sort`) VALUES
(1, 0, '鏁扮爜鐢靛瓙', 'phone', 1),
(2, 0, '鏃跺皻鏈嶈', 't-shirt', 2),
(3, 0, '缇庡懗椋熷搧', 'food', 3),
(4, 0, '瀹跺眳鐢熸椿', 'home', 4);

-- 3. 鎻掑叆鍟嗗搧鏁版嵁

-- ================= 鏁扮爜鐢靛瓙 (Category 1) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(1, '2026鏂版 鏅鸿兘鎵嬫満 Pro Max', '鎼浇鏈€鏂颁竴浠ｅ鐞嗗櫒锛岃秴寮哄鏅媿鎽勶紝鎸佷箙缁埅銆?, 
 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500&q=80', 
 '["https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800", "https://images.unsplash.com/photo-1598327770170-6691674de46c?w=800"]',
 5999.00, 6999.00, 100, 1024, 4.9, 1),

(1, '鏃犵嚎闄嶅櫔钃濈墮鑰虫満 Headset X', '涓诲姩闄嶅櫔锛屾矇娴稿紡闊宠川浣撻獙锛?0灏忔椂瓒呴暱缁埅銆?, 
 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80', 
 '["https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800"]',
 899.00, 1299.00, 50, 2300, 4.8, 1),

(1, '杞昏杽绗旇鏈數鑴?Ultra 14', '1.2kg瓒呰交鏈鸿韩锛?.8K OLED灞忓箷锛屽姙鍏ū涔愪袱涓嶈銆?, 
 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=500&q=80', 
 '["https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800", "https://images.unsplash.com/photo-1531297420492-6188abc8a96b?w=800"]',
 4599.00, 5299.00, 20, 560, 4.7, 1),

(1, '鏅鸿兘杩愬姩鎵嬭〃 Watch GT', '鍏ㄥぉ鍊欏仴搴风洃娴嬶紝100+杩愬姩妯″紡锛?0绫抽槻姘淬€?, 
 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80', 
 '["https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800", "https://images.unsplash.com/photo-1542496658-e33a6d0d50f6?w=800"]',
 1299.00, 1599.00, 200, 890, 4.6, 1);


-- ================= 鏃跺皻鏈嶈 (Category 2) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(2, '鐢峰＋鍟嗗姟浼戦棽绾琛～', '绮鹃€夐暱缁掓锛屽厤鐑伐鑹猴紝淇韩鍓锛岃垝閫傞€忔皵銆?, 
 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&q=80', 
 '["https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800"]',
 199.00, 399.00, 300, 500, 4.7, 1),

(2, '娉曞紡澶嶅彜纰庤姳杩炶。瑁?, '娴极纰庤姳璁捐锛屾敹鑵版樉鐦︼紝閫傚悎鏄ュ鍑烘父銆?, 
 'https://images.unsplash.com/photo-1618244972963-dbee1a7edc95?w=500&q=80', 
 '["https://images.unsplash.com/photo-1618244972963-dbee1a7edc95?w=800", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800"]',
 299.00, 459.00, 150, 880, 4.8, 1),

(2, '绾壊杩炲附鍗。 Unisex', '閲嶇绾锛屽鏉剧増鍨嬶紝鐧炬惌娼祦鍗曞搧銆?, 
 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&q=80', 
 '["https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800"]',
 159.00, 299.00, 500, 1200, 4.5, 1);


-- ================= 缇庡懗椋熷搧 (Category 3) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(3, '鐗圭骇鏈夋満鍒濇Θ姗勬娌?500ml', '瑗跨彮鐗欏師瑁呰繘鍙ｏ紝鍐峰帇鍒濇Θ锛屽仴搴风敓娲婚閫夈€?, 
 'https://images.unsplash.com/photo-1474979266404-7caddbed77a3?w=500&q=80', 
 '["https://images.unsplash.com/photo-1474979266404-7caddbed77a3?w=800"]',
 128.00, 198.00, 300, 88, 5.0, 1),

(3, '鎵嬪伐榛戝阀鍏嬪姏绀肩洅', '70%鍙彲鍚噺锛屼笣婊戞祿閮侊紝鎯呬汉鑺傞€佺ぜ浣冲搧銆?, 
 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=500&q=80', 
 '["https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800", "https://images.unsplash.com/photo-1481391319719-61d281219f77?w=800"]',
 188.00, 258.00, 100, 340, 4.9, 1),

(3, '娣峰悎鍧氭灉姣忔棩绀煎寘', '涓冪鍧氭灉绉戝閰嶆瘮锛屼綆娓╃儤鐒欙紝閿佷綇钀ュ吇銆?, 
 'https://images.unsplash.com/photo-1623428187969-5da2dcea5ebf?w=500&q=80', 
 '["https://images.unsplash.com/photo-1623428187969-5da2dcea5ebf?w=800"]',
 89.00, 129.00, 1000, 5600, 4.8, 1);


-- ================= 瀹跺眳鐢熸椿 (Category 4) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(4, '澶嶅彜鏈烘閿洏 闈掕酱', '鍏ㄩ敭鏃犲啿锛孭BT閿附锛屾竻鑴嗘墜鎰燂紝鎵撳瓧濡傞銆?, 
 'https://images.unsplash.com/photo-1587829741301-dc798b91add1?w=500&q=80', 
 '["https://images.unsplash.com/photo-1587829741301-dc798b91add1?w=800", "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=800"]',
 359.00, 499.00, 50, 450, 4.6, 1),

(4, '鏋佺畝瀹炴湪搴婂ご鏌?, '鍖楃編榛戣儭妗冩湪锛屾Λ鍗粨鏋勶紝鐜繚鏈ㄨ湣娌规秱瑁呫€?, 
 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=500&q=80', 
 '["https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=800"]',
 680.00, 980.00, 20, 120, 4.8, 1),
 
(4, '涓嬫灦鍟嗗搧-娴嬭瘯涓嶅彲瑙?, '杩欎釜鍟嗗搧宸茬粡涓嬫灦浜嗭紝鍓嶇鏅€氱敤鎴峰簲璇ョ湅涓嶅埌銆?, 
 'https://via.placeholder.com/300?text=Off+Shelf', 
 '["https://via.placeholder.com/800?text=Off+Shelf"]',
 9999.00, 9999.00, 0, 0, 1.0, 0);
