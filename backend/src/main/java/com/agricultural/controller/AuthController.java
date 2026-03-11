package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.entity.User;
import com.agricultural.service.CaptchaService;
import com.agricultural.service.UserService;
import com.agricultural.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User user) {
        User registeredUser = userService.register(user);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", registeredUser.getId());
        data.put("username", registeredUser.getUsername());
        data.put("role", registeredUser.getRole());
        return Result.success("注册成功", data);
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captchaData = captchaService.generateCaptcha();
        return Result.success("获取验证码成功", captchaData);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String captchaId = loginRequest.get("captchaId");
        String captchaCode = loginRequest.get("captchaCode");

        // 验证验证码
        if (captchaId == null || captchaCode == null || !captchaService.verifyCaptcha(captchaId, captchaCode)) {
            return Result.error("验证码错误或已过期");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.findByUsername(username);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());

        return Result.success("登录成功", data);
    }
}

















