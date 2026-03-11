-- 重置所有测试用户密码为 123456
-- 使用正确的 BCrypt 哈希值

USE agricultural_db;

-- 方法1: 如果用户已存在，更新密码
UPDATE users 
SET password = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' 
WHERE username IN ('admin', 'merchant1', 'user1');

-- 如果上面的更新没有影响任何行（用户不存在），则插入新用户
INSERT INTO users (username, password, phone, nickname, role, deleted) 
SELECT 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138000', '管理员', 'ADMIN', 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, phone, nickname, role, deleted) 
SELECT 'merchant1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138001', '商家1', 'MERCHANT', 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'merchant1');

INSERT INTO users (username, password, phone, nickname, role, deleted) 
SELECT 'user1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138002', '用户1', 'USER', 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');

















