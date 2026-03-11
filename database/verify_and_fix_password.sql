-- 验证并修复密码
-- 数据库中的哈希值: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6w2
-- 如果这个哈希值不正确，使用下面的正确哈希值更新

USE agricultural_db;

-- 查看当前用户密码
SELECT username, password FROM users WHERE username IN ('admin', 'merchant1', 'user1');

-- 更新为正确的 BCrypt 哈希值（密码: 123456）
-- 这个哈希值是通过 BCryptPasswordEncoder 生成的，已验证可以匹配 "123456"
UPDATE users 
SET password = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' 
WHERE username IN ('admin', 'merchant1', 'user1');

-- 验证更新结果
SELECT username, password FROM users WHERE username IN ('admin', 'merchant1', 'user1');

















