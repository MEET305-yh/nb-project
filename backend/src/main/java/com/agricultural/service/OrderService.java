package com.agricultural.service;

import com.agricultural.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    Order createOrder(Long userId, Long addressId, List<Long> cartIds);
    Order createOrderDirect(Long userId, Long addressId, List<Map<String, Object>> items);
    boolean payOrder(Long orderId);
    boolean shipOrder(Long orderId);
    boolean completeOrder(Long orderId);
    List<Order> getUserOrders(Long userId);
}


