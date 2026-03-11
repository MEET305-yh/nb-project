package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.entity.ShoppingCart;
import com.agricultural.service.ShoppingCartService;
import com.agricultural.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<ShoppingCart>> getCart(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<ShoppingCart> cart = shoppingCartService.getUserCart(userId);
        return Result.success(cart);
    }

    @PostMapping
    public Result<String> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        shoppingCartService.addToCart(userId, productId, quantity);
        return Result.success("添加成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateCartQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer quantity = request.get("quantity");
        shoppingCartService.updateCartQuantity(id, quantity);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> removeFromCart(@PathVariable Long id) {
        shoppingCartService.removeFromCart(id);
        return Result.success("删除成功");
    }
}

















