package com.agricultural.controller.admin;

import com.agricultural.common.Result;
import com.agricultural.entity.User;
import com.agricultural.mapper.UserMapper;
import com.agricultural.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userService.page(page, wrapper);
        
        // 不返回密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null || user.getDeleted() == 1) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        // 检查用户名是否已存在（包括已删除的用户，因为数据库唯一约束仍然存在）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        // 不检查 deleted 字段，因为即使软删除，数据库唯一约束仍然存在
        User existing = userService.getOne(wrapper);
        if (existing != null) {
            return Result.error("用户名已存在");
        }

        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        }

        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);
        user.setPassword(null);
        return Result.success("创建成功", user);
    }

    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error("用户不存在");
        }

        // 如果用户名被修改，检查新用户名是否已存在
        if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, user.getUsername());
            User duplicateUser = userService.getOne(wrapper);
            if (duplicateUser != null && !duplicateUser.getId().equals(id)) {
                return Result.error("用户名已存在");
            }
        }

        user.setId(id);
        
        // 如果提供了新密码，则加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码，从现有用户中获取
            user.setPassword(existing.getPassword());
        }

        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            return Result.error("用户已被删除");
        }

        // 使用 LambdaUpdateWrapper 直接更新，绕过 MyBatis-Plus 的逻辑删除限制
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getDeleted, 1)
                .set(User::getUpdateTime, LocalDateTime.now());
        
        int result = userMapper.update(null, updateWrapper);
        
        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getUserStats() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        
        long totalUsers = userService.count(wrapper);
        
        wrapper.clear();
        wrapper.eq(User::getDeleted, 0).eq(User::getRole, "USER");
        long normalUsers = userService.count(wrapper);
        
        wrapper.clear();
        wrapper.eq(User::getDeleted, 0).eq(User::getRole, "MERCHANT");
        long merchants = userService.count(wrapper);
        
        wrapper.clear();
        wrapper.eq(User::getDeleted, 0).eq(User::getRole, "ADMIN");
        long admins = userService.count(wrapper);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", totalUsers);
        stats.put("normalUsers", normalUsers);
        stats.put("merchants", merchants);
        stats.put("admins", admins);

        return Result.success(stats);
    }
}

