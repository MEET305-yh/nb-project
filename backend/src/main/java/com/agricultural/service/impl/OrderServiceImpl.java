package com.agricultural.service.impl;

import com.agricultural.entity.*;
import com.agricultural.mapper.*;
import com.agricultural.service.OrderService;
import com.agricultural.service.ProductService;
import com.agricultural.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Override
    @Transactional
    public Order createOrder(Long userId, Long addressId, List<Long> cartIds) {
        // 获取地址信息
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或不属于当前用户");
        }

        // 获取购物车商品
        List<ShoppingCart> cartItems = shoppingCartService.listByIds(cartIds);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setStatus("PENDING_PAYMENT");
        order.setShippingAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);

        class PendingOrderItem {
            OrderItem item;
            Long productId;
            Integer quantity;
        }

        List<PendingOrderItem> pendingItems = new java.util.ArrayList<>();

        // 预生成订单项并计算总金额
        for (ShoppingCart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || product.getStock() < cart.getQuantity()) {
                throw new IllegalArgumentException("商品不存在或库存不足: " + (product != null ? product.getName() : cart.getProductId()));
            }

            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(cart.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setCreateTime(LocalDateTime.now());

            PendingOrderItem pending = new PendingOrderItem();
            pending.item = orderItem;
            pending.productId = product.getId();
            pending.quantity = cart.getQuantity();
            pendingItems.add(pending);
        }

        order.setTotalAmount(totalAmount);
        save(order);

        // 保存订单项并更新库存
        for (PendingOrderItem pending : pendingItems) {
            pending.item.setOrderId(order.getId());
            orderItemMapper.insert(pending.item);
            productService.updateStock(pending.productId, pending.quantity);
        }

        // 删除购物车
        shoppingCartService.removeByIds(cartIds);

        return order;
    }

    @Override
    @Transactional
    public Order createOrderDirect(Long userId, Long addressId, List<Map<String, Object>> items) {
        // 获取地址信息
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或不属于当前用户");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("商品列表为空");
        }

        // 先验证商品并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            if (item.get("productId") == null || item.get("quantity") == null) {
                throw new IllegalArgumentException("商品信息不完整：productId 或 quantity 为空");
            }
            
            Long productId;
            Integer quantity;
            try {
                Object productIdObj = item.get("productId");
                if (productIdObj instanceof Number) {
                    productId = ((Number) productIdObj).longValue();
                } else {
                    productId = Long.valueOf(productIdObj.toString());
                }
                
                Object quantityObj = item.get("quantity");
                if (quantityObj instanceof Number) {
                    quantity = ((Number) quantityObj).intValue();
                } else {
                    quantity = Integer.valueOf(quantityObj.toString());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("商品信息格式错误: " + e.getMessage());
            }

            Product product = productService.getById(productId);
            if (product == null) {
                throw new IllegalArgumentException("商品不存在，ID: " + productId);
            }
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("商品库存不足: " + product.getName() + "，当前库存: " + product.getStock() + "，需要: " + quantity);
            }

            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(subtotal);
        }

        // 创建订单对象并设置总金额
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setStatus("PENDING_PAYMENT");
        order.setShippingAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setTotalAmount(totalAmount);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);

        // 保存订单以获取ID
        save(order);

        // 创建订单项（商品已在前面验证过）
        for (Map<String, Object> item : items) {
            Long productId;
            Integer quantity;
            try {
                Object productIdObj = item.get("productId");
                if (productIdObj instanceof Number) {
                    productId = ((Number) productIdObj).longValue();
                } else {
                    productId = Long.valueOf(productIdObj.toString());
                }
                
                Object quantityObj = item.get("quantity");
                if (quantityObj instanceof Number) {
                    quantity = ((Number) quantityObj).intValue();
                } else {
                    quantity = Integer.valueOf(quantityObj.toString());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("商品信息格式错误: " + e.getMessage());
            }

            Product product = productService.getById(productId);
            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(quantity));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(subtotal);
            orderItem.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(orderItem);

            // 更新库存
            productService.updateStock(product.getId(), quantity);
        }

        return order;
    }

    @Override
    public boolean payOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单状态不正确");
        }

        order.setStatus("PAID");
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean shipOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单状态不正确");
        }

        order.setStatus("SHIPPED");
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean completeOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单状态不正确");
        }

        order.setStatus("COMPLETED");
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.eq(Order::getDeleted, 0);
        wrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = list(wrapper);
        for (Order order : orders) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setItems(items);
        }
        return orders;
    }
}




