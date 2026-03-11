package com.agricultural.service;

import com.agricultural.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    User register(User user);
    boolean updateUserInfo(Long userId, User user);
}


