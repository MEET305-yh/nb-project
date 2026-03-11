package com.agricultural.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成 BCrypt 加密后的密码哈希值
 * 运行此类的 main 方法可以生成密码哈希
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        
        // 验证数据库中的哈希值
        String dbHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6w2";
        boolean dbMatches = encoder.matches(password, dbHash);
        System.out.println("========================================");
        System.out.println("验证数据库中的密码哈希值:");
        System.out.println("数据库哈希: " + dbHash);
        System.out.println("密码: " + password);
        System.out.println("验证结果: " + dbMatches);
        System.out.println("========================================");
        
        // 生成新的正确哈希值
        System.out.println("\n生成新的密码哈希值:");
        String encodedPassword = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("新的哈希值: " + encodedPassword);
        
        // 验证新生成的哈希值
        boolean newMatches = encoder.matches(password, encodedPassword);
        System.out.println("新哈希验证结果: " + newMatches);
        System.out.println("========================================");
        
        // 生成多个哈希值供选择
        System.out.println("\n生成多个哈希值（选择任意一个使用）:");
        for (int i = 0; i < 3; i++) {
            String hash = encoder.encode(password);
            System.out.println((i + 1) + ". " + hash);
        }
    }
}

