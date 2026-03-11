package com.agricultural.service;

import com.agricultural.entity.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    List<ShoppingCart> getUserCart(Long userId);
    boolean addToCart(Long userId, Long productId, Integer quantity);
    boolean updateCartQuantity(Long cartId, Integer quantity);
    boolean removeFromCart(Long cartId);
}


