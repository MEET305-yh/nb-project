package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.entity.Order;
import com.agricultural.service.OrderService;
import com.agricultural.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Result<Order> createOrder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Long addressId = Long.valueOf(request.get("addressId").toString());
        
        // 判断是立即购买还是从购物车结算
        if (request.containsKey("items") && request.get("items") != null) {
            // 立即购买模式
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            Order order = orderService.createOrderDirect(userId, addressId, items);
            return Result.success("订单创建成功", order);
        } else {
            // 从购物车结算模式
            @SuppressWarnings("unchecked")
            List<Long> cartIds = (List<Long>) request.get("cartIds");
            Order order = orderService.createOrder(userId, addressId, cartIds);
            return Result.success("订单创建成功", order);
        }
    }

    @GetMapping
    public Result<List<Order>> getUserOrders(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Order> orders = orderService.getUserOrders(userId);
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping("/{id}/pay")
    public Result<String> payOrder(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        orderService.payOrder(id);
        return Result.success("支付成功");
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('ADMIN')")
    public Result<String> shipOrder(@PathVariable Long id) {
        orderService.shipOrder(id);
        return Result.success("发货成功");
    }

    @PostMapping("/{id}/complete")
    public Result<String> completeOrder(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        orderService.completeOrder(id);
        return Result.success("订单完成");
    }
}









