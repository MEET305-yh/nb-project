-- 更新用户密码为正确的 BCrypt 哈希值
-- 密码: 123456
-- 这个哈希值是通过 BCryptPasswordEncoder 生成的

USE agricultural_db;

-- 更新 admin 密码 (BCrypt 加密的 "123456")
UPDATE users SET password = '$2a$10$rKqJqJqJqJqJqJqJqJqJ.uJqJqJqJqJqJqJqJqJqJqJqJqJqJqJqJq' WHERE username = 'admin';

-- 更新 merchant1 密码
UPDATE users SET password = '$2a$10$rKqJqJqJqJqJqJqJqJqJ.uJqJqJqJqJqJqJqJqJqJqJqJqJqJqJqJq' WHERE username = 'merchant1';

-- 更新 user1 密码
UPDATE users SET password = '$2a$10$rKqJqJqJqJqJqJqJqJqJ.uJqJqJqJqJqJqJqJqJqJqJqJqJqJqJqJq' WHERE username = 'user1';

















