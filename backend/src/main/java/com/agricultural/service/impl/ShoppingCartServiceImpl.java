package com.agricultural.service.impl;

import com.agricultural.entity.ShoppingCart;
import com.agricultural.mapper.ShoppingCartMapper;
import com.agricultural.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Override
    public List<ShoppingCart> getUserCart(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        wrapper.orderByDesc(ShoppingCart::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        // 检查是否已存在
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        wrapper.eq(ShoppingCart::getProductId, productId);
        ShoppingCart existing = getOne(wrapper);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdateTime(LocalDateTime.now());
            return updateById(existing);
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            return save(cart);
        }
    }

    @Override
    public boolean updateCartQuantity(Long cartId, Integer quantity) {
        ShoppingCart cart = getById(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }

        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        return updateById(cart);
    }

    @Override
    public boolean removeFromCart(Long cartId) {
        return removeById(cartId);
    }
}

















