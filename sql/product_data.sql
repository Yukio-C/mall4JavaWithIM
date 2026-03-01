-- 1. 清理旧数据 (可选)
-- TRUNCATE TABLE `categories`;
-- TRUNCATE TABLE `products`;

-- 2. 插入商品分类
INSERT INTO `categories` (`id`, `parent_id`, `name`, `icon`, `sort`) VALUES
(1, 0, '数码电子', 'phone', 1),
(2, 0, '时尚服装', 't-shirt', 2),
(3, 0, '美味食品', 'food', 3),
(4, 0, '家居生活', 'home', 4);

-- 3. 插入商品数据

-- ================= 数码电子 (Category 1) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(1, '2026新款 智能手机 Pro Max', '搭载最新一代处理器，超强夜景拍摄，持久续航。', 
 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500&q=80', 
 '["https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800", "https://images.unsplash.com/photo-1598327770170-6691674de46c?w=800"]',
 5999.00, 6999.00, 100, 1024, 4.9, 1),

(1, '无线降噪蓝牙耳机 Headset X', '主动降噪，沉浸式音质体验，30小时超长续航。', 
 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80', 
 '["https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800"]',
 899.00, 1299.00, 50, 2300, 4.8, 1),

(1, '轻薄笔记本电脑 Ultra 14', '1.2kg超轻机身，2.8K OLED屏幕，办公娱乐两不误。', 
 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=500&q=80', 
 '["https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800", "https://images.unsplash.com/photo-1531297420492-6188abc8a96b?w=800"]',
 4599.00, 5299.00, 20, 560, 4.7, 1),

(1, '智能运动手表 Watch GT', '全天候健康监测，100+运动模式，50米防水。', 
 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80', 
 '["https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800", "https://images.unsplash.com/photo-1542496658-e33a6d0d50f6?w=800"]',
 1299.00, 1599.00, 200, 890, 4.6, 1);


-- ================= 时尚服装 (Category 2) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(2, '男士商务休闲纯棉衬衫', '精选长绒棉，免烫工艺，修身剪裁，舒适透气。', 
 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&q=80', 
 '["https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800"]',
 199.00, 399.00, 300, 500, 4.7, 1),

(2, '法式复古碎花连衣裙', '浪漫碎花设计，收腰显瘦，适合春夏出游。', 
 'https://images.unsplash.com/photo-1618244972963-dbee1a7edc95?w=500&q=80', 
 '["https://images.unsplash.com/photo-1618244972963-dbee1a7edc95?w=800", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800"]',
 299.00, 459.00, 150, 880, 4.8, 1),

(2, '纯色连帽卫衣 Unisex', '重磅纯棉，宽松版型，百搭潮流单品。', 
 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&q=80', 
 '["https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800"]',
 159.00, 299.00, 500, 1200, 4.5, 1);


-- ================= 美味食品 (Category 3) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(3, '特级有机初榨橄榄油 500ml', '西班牙原装进口，冷压初榨，健康生活首选。', 
 'https://images.unsplash.com/photo-1474979266404-7caddbed77a3?w=500&q=80', 
 '["https://images.unsplash.com/photo-1474979266404-7caddbed77a3?w=800"]',
 128.00, 198.00, 300, 88, 5.0, 1),

(3, '手工黑巧克力礼盒', '70%可可含量，丝滑浓郁，情人节送礼佳品。', 
 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=500&q=80', 
 '["https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800", "https://images.unsplash.com/photo-1481391319719-61d281219f77?w=800"]',
 188.00, 258.00, 100, 340, 4.9, 1),

(3, '混合坚果每日礼包', '七种坚果科学配比，低温烘焙，锁住营养。', 
 'https://images.unsplash.com/photo-1623428187969-5da2dcea5ebf?w=500&q=80', 
 '["https://images.unsplash.com/photo-1623428187969-5da2dcea5ebf?w=800"]',
 89.00, 129.00, 1000, 5600, 4.8, 1);


-- ================= 家居生活 (Category 4) =================

INSERT INTO `products` 
(`category_id`, `title`, `description`, `cover`, `slider_imgs`, `price`, `original_price`, `stock`, `sales`, `rating`, `status`) 
VALUES 
(4, '复古机械键盘 青轴', '全键无冲，PBT键帽，清脆手感，打字如飞。', 
 'https://images.unsplash.com/photo-1587829741301-dc798b91add1?w=500&q=80', 
 '["https://images.unsplash.com/photo-1587829741301-dc798b91add1?w=800", "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=800"]',
 359.00, 499.00, 50, 450, 4.6, 1),

(4, '极简实木床头柜', '北美黑胡桃木，榫卯结构，环保木蜡油涂装。', 
 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=500&q=80', 
 '["https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=800"]',
 680.00, 980.00, 20, 120, 4.8, 1),
 
(4, '下架商品-测试不可见', '这个商品已经下架了，前端普通用户应该看不到。', 
 'https://via.placeholder.com/300?text=Off+Shelf', 
 '["https://via.placeholder.com/800?text=Off+Shelf"]',
 9999.00, 9999.00, 0, 0, 1.0, 0);
