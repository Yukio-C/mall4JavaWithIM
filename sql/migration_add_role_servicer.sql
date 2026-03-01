ALTER TABLE `users` ADD COLUMN `permissions` TEXT DEFAULT NULL COMMENT '权限列表(JSON存储页面标识)';
