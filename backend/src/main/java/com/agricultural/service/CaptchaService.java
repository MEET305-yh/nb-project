package com.agricultural.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     * @return 包含验证码图片Base64和验证码ID的Map
     */
    Map<String, String> generateCaptcha();
    
    /**
     * 验证验证码
     * @param captchaId 验证码ID
     * @param code 用户输入的验证码
     * @return 验证是否通过
     */
    boolean verifyCaptcha(String captchaId, String code);
}


