package com.agricultural.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码生成工具类
 */
public class CaptchaUtil {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除容易混淆的字符
    
    /**
     * 生成验证码
     * @return CaptchaResult 包含验证码图片的Base64编码和验证码字符串
     */
    public static CaptchaResult generateCaptcha() {
        // 创建画布
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        
        // 生成随机验证码
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        g.setFont(new Font("Arial", Font.BOLD, 28));
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            char c = CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length()));
            code.append(c);
            
            // 随机颜色
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            
            // 随机位置
            int x = 20 + i * 25;
            int y = 25 + random.nextInt(10);
            
            // 随机旋转
            double angle = (random.nextDouble() - 0.5) * 0.3;
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(c), x, y);
            g.rotate(-angle, x, y);
        }
        
        // 添加干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 添加干扰点
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
        
        g.dispose();
        
        // 转换为Base64
        String base64Image = imageToBase64(image);
        
        return new CaptchaResult(base64Image, code.toString());
    }
    
    /**
     * 将图片转换为Base64字符串
     */
    private static String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
    
    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private String imageBase64;
        private String code;
        
        public CaptchaResult(String imageBase64, String code) {
            this.imageBase64 = imageBase64;
            this.code = code;
        }
        
        public String getImageBase64() {
            return imageBase64;
        }
        
        public String getCode() {
            return code;
        }
    }
}


