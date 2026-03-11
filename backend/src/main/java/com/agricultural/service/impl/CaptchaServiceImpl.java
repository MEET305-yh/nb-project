package com.agricultural.service.impl;

import com.agricultural.service.CaptchaService;
import com.agricultural.util.CaptchaUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {
    
    // 存储验证码，key为captchaId，value为验证码字符串
    private final ConcurrentHashMap<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>();
    
    // 定时清理过期验证码
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 验证码过期时间（5分钟）
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000;
    
    public CaptchaServiceImpl() {
        // 每5分钟清理一次过期验证码
        scheduler.scheduleAtFixedRate(this::cleanExpiredCaptcha, 5, 5, TimeUnit.MINUTES);
    }
    
    @Override
    public Map<String, String> generateCaptcha() {
        // 生成验证码
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generateCaptcha();
        
        // 生成唯一ID
        String captchaId = UUID.randomUUID().toString();
        
        // 存储验证码（转换为大写，忽略大小写验证）
        captchaStore.put(captchaId, new CaptchaInfo(result.getCode().toUpperCase(), System.currentTimeMillis()));
        
        // 返回结果
        Map<String, String> response = new HashMap<>();
        response.put("captchaId", captchaId);
        response.put("imageBase64", result.getImageBase64());
        
        return response;
    }
    
    @Override
    public boolean verifyCaptcha(String captchaId, String code) {
        if (captchaId == null || code == null) {
            return false;
        }
        
        CaptchaInfo info = captchaStore.get(captchaId);
        if (info == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() - info.getCreateTime() > CAPTCHA_EXPIRE_TIME) {
            captchaStore.remove(captchaId);
            return false;
        }
        
        // 验证码验证（忽略大小写）
        boolean isValid = info.getCode().equalsIgnoreCase(code.trim());
        
        // 验证后删除验证码（一次性使用）
        if (isValid) {
            captchaStore.remove(captchaId);
        }
        
        return isValid;
    }
    
    /**
     * 清理过期的验证码
     */
    private void cleanExpiredCaptcha() {
        long currentTime = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getCreateTime() > CAPTCHA_EXPIRE_TIME
        );
    }
    
    /**
     * 验证码信息类
     */
    private static class CaptchaInfo {
        private final String code;
        private final long createTime;
        
        public CaptchaInfo(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getCreateTime() {
            return createTime;
        }
    }
}


