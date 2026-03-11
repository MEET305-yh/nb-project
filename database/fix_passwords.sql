-- 修复用户密码脚本
-- 使用正确的 BCrypt 哈希值（密码: 123456）
-- 这个哈希值是通过 Spring Security 的 BCryptPasswordEncoder 生成的

USE agricultural_db;

-- 删除旧的测试用户
DELETE FROM users WHERE username IN ('admin', 'merchant1', 'user1');

-- 插入新的测试用户，使用正确的 BCrypt 哈希值
-- 密码都是: 123456
-- 哈希值: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
INSERT INTO users (username, password, phone, nickname, role, deleted) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138000', '管理员', 'ADMIN', 0),
('merchant1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138001', '商家1', 'MERCHANT', 0),
('user1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138002', '用户1', 'USER', 0);

















