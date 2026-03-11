package com.agricultural.controller.admin;

import com.agricultural.common.Result;
import com.agricultural.entity.Order;
import com.agricultural.entity.OrderItem;
import com.agricultural.entity.Product;
import com.agricultural.entity.User;
import com.agricultural.mapper.OrderItemMapper;
import com.agricultural.service.OrderService;
import com.agricultural.service.ProductService;
import com.agricultural.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN','MERCHANT')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<IPage<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo) {
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }

        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }

        Long merchantId = getCurrentMerchantId();
        if (merchantId != null) {
            List<Long> orderIds = getOrderIdsByMerchant(merchantId);
            if (orderIds.isEmpty()) {
                return Result.success(page);
            }
            wrapper.in(Order::getId, orderIds);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> result = orderService.page(page, wrapper);
        for (Order order : result.getRecords()) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            order.setItems(orderItemMapper.selectList(itemWrapper));
        }
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getOrderStats() {
        Long merchantId = getCurrentMerchantId();
        List<Long> orderIds = merchantId != null ? getOrderIdsByMerchant(merchantId) : null;

        LambdaQueryWrapper<Order> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(Order::getDeleted, 0);
        if (orderIds != null) {
            if (orderIds.isEmpty()) {
                return Result.success(emptyStats());
            }
            baseWrapper.in(Order::getId, orderIds);
        }

        long totalOrders = orderService.count(baseWrapper);
        long pendingPayment = countByStatus(orderIds, "PENDING_PAYMENT");
        long paid = countByStatus(orderIds, "PAID");
        long shipped = countByStatus(orderIds, "SHIPPED");
        long completed = countByStatus(orderIds, "COMPLETED");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalOrders);
        stats.put("pendingPayment", pendingPayment);
        stats.put("paid", paid);
        stats.put("shipped", shipped);
        stats.put("completed", completed);

        return Result.success(stats);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam(required = false, defaultValue = "7") Integer days) {
        Long merchantId = getCurrentMerchantId();
        List<Long> orderIds = merchantId != null ? getOrderIdsByMerchant(merchantId) : null;

        if (orderIds != null && orderIds.isEmpty()) {
            return Result.success(emptyStatistics(days));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime;
        LocalDateTime startTime;
        
        // 如果是昨天，只统计昨天的数据（不包括今天）
        if (days == 1) {
            endTime = now.toLocalDate().atStartOfDay(); // 今天的开始时间
            startTime = endTime.minusDays(1); // 昨天的开始时间
        } else {
            endTime = now;
            startTime = now.minusDays(days);
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0)
                .in(Order::getStatus, "PAID", "SHIPPED", "COMPLETED")
                .ge(Order::getCreateTime, startTime)
                .lt(Order::getCreateTime, endTime);
        
        if (orderIds != null) {
            wrapper.in(Order::getId, orderIds);
        }

        List<Order> orders = orderService.list(wrapper);
        
        // 按日期分组统计营业额和订单数
        Map<String, BigDecimal> revenueByDate = new HashMap<>();
        Map<String, Long> orderCountByDate = new HashMap<>();
        
        for (Order order : orders) {
            String dateKey = order.getCreateTime().toLocalDate().toString();
            revenueByDate.merge(dateKey, order.getTotalAmount(), BigDecimal::add);
            orderCountByDate.merge(dateKey, 1L, Long::sum);
        }

        // 生成完整的日期序列
        List<String> dateLabels = new ArrayList<>();
        List<BigDecimal> revenueData = new ArrayList<>();
        List<Long> orderCountData = new ArrayList<>();
        
        LocalDateTime currentDate;
        if (days == 1) {
            // 只显示昨天
            currentDate = now.toLocalDate().minusDays(1).atStartOfDay();
            String dateKey = currentDate.toLocalDate().toString();
            String label = currentDate.getMonthValue() + "-" + currentDate.getDayOfMonth();
            dateLabels.add(label);
            revenueData.add(revenueByDate.getOrDefault(dateKey, BigDecimal.ZERO));
            orderCountData.add(orderCountByDate.getOrDefault(dateKey, 0L));
        } else {
            // 显示最近N天的数据
            for (int i = days - 1; i >= 0; i--) {
                currentDate = now.minusDays(i);
                String dateKey = currentDate.toLocalDate().toString();
                String label = currentDate.getMonthValue() + "-" + currentDate.getDayOfMonth();
                dateLabels.add(label);
                revenueData.add(revenueByDate.getOrDefault(dateKey, BigDecimal.ZERO));
                orderCountData.add(orderCountByDate.getOrDefault(dateKey, 0L));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dateLabels", dateLabels);
        result.put("revenue", revenueData);
        result.put("orderCount", orderCountData);
        result.put("totalRevenue", revenueData.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("totalOrders", orderCountData.stream().mapToLong(Long::longValue).sum());

        return Result.success(result);
    }

    @PostMapping("/{id}/ship")
    public Result<String> shipOrder(@PathVariable Long id) {
        orderService.shipOrder(id);
        return Result.success("发货成功");
    }

    @PostMapping("/{id}/cancel")
    public Result<String> cancelOrder(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null || order.getDeleted() == 1) {
            return Result.error("订单不存在");
        }

        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            return Result.error("订单状态不允许取消");
        }

        order.setStatus("CANCELLED");
        orderService.updateById(order);
        return Result.success("取消成功");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null || order.getDeleted() == 1) {
            return Result.error("订单不存在");
        }

        // 获取订单项
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);

        return Result.success(result);
    }

    @GetMapping("/product-sales")
    public Result<Map<String, Object>> getProductSales(@RequestParam(required = false, defaultValue = "7") Integer days) {
        Long merchantId = getCurrentMerchantId();
        
        // 获取商家的商品列表
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        if (merchantId != null) {
            productWrapper.eq(Product::getMerchantId, merchantId);
        }
        List<Product> products = productService.list(productWrapper);
        
        if (products.isEmpty()) {
            return Result.success(emptyProductSales());
        }
        
        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, String> productNameMap = products.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, Product::getName));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime;
        LocalDateTime startTime;
        
        // 如果是昨天，只统计昨天的数据（不包括今天）
        if (days == 1) {
            endTime = now.toLocalDate().atStartOfDay();
            startTime = endTime.minusDays(1);
        } else {
            endTime = now;
            startTime = now.minusDays(days);
        }

        // 获取时间范围内的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getDeleted, 0)
                .in(Order::getStatus, "PAID", "SHIPPED", "COMPLETED")
                .ge(Order::getCreateTime, startTime)
                .lt(Order::getCreateTime, endTime);
        
        List<Order> orders = orderService.list(orderWrapper);
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        
        if (orderIds.isEmpty()) {
            return Result.success(emptyProductSales());
        }

        // 获取这些订单的订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getProductId, productIds)
                .in(OrderItem::getOrderId, orderIds);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        // 按商品统计销量
        Map<Long, Integer> salesMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            salesMap.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        // 按销量排序，取前10名
        List<Map<String, Object>> productSalesList = salesMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productId", entry.getKey());
                    item.put("productName", productNameMap.get(entry.getKey()));
                    item.put("sales", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("sales"), (Integer) a.get("sales")))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        List<String> productNames = productSalesList.stream()
                .map(item -> (String) item.get("productName"))
                .collect(java.util.stream.Collectors.toList());
        List<Integer> sales = productSalesList.stream()
                .map(item -> (Integer) item.get("sales"))
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("productNames", productNames);
        result.put("sales", sales);
        result.put("totalSales", sales.stream().mapToInt(Integer::intValue).sum());

        return Result.success(result);
    }

    private Map<String, Object> emptyProductSales() {
        Map<String, Object> result = new HashMap<>();
        result.put("productNames", new ArrayList<>());
        result.put("sales", new ArrayList<>());
        result.put("totalSales", 0);
        return result;
    }

    private Map<String, Object> emptyStatistics(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dateLabels = new ArrayList<>();
        List<BigDecimal> revenueData = new ArrayList<>();
        List<Long> orderCountData = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        if (days == 1) {
            // 只显示昨天
            LocalDateTime date = now.toLocalDate().minusDays(1).atStartOfDay();
            String label = date.getMonthValue() + "-" + date.getDayOfMonth();
            dateLabels.add(label);
            revenueData.add(BigDecimal.ZERO);
            orderCountData.add(0L);
        } else {
            // 显示最近N天的数据
            for (int i = days - 1; i >= 0; i--) {
                LocalDateTime date = now.minusDays(i);
                String label = date.getMonthValue() + "-" + date.getDayOfMonth();
                dateLabels.add(label);
                revenueData.add(BigDecimal.ZERO);
                orderCountData.add(0L);
            }
        }
        
        result.put("dateLabels", dateLabels);
        result.put("revenue", revenueData);
        result.put("orderCount", orderCountData);
        result.put("totalRevenue", BigDecimal.ZERO);
        result.put("totalOrders", 0L);
        return result;
    }

    private Map<String, Object> emptyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 0L);
        stats.put("pendingPayment", 0L);
        stats.put("paid", 0L);
        stats.put("shipped", 0L);
        stats.put("completed", 0L);
        return stats;
    }

    private long countByStatus(List<Long> orderIds, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0).eq(Order::getStatus, status);
        if (orderIds != null) {
            if (orderIds.isEmpty()) {
                return 0L;
            }
            wrapper.in(Order::getId, orderIds);
        }
        return orderService.count(wrapper);
    }

    private Long getCurrentMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user != null && "MERCHANT".equalsIgnoreCase(user.getRole())) {
            return user.getId();
        }
        return null;
    }

    private List<Long> getOrderIdsByMerchant(Long merchantId) {
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getMerchantId, merchantId);
        List<Product> products = productService.list(productWrapper);
        if (products.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> productIds = products.stream().map(Product::getId).toList();

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getProductId, productIds);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        if (items.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return items.stream().map(OrderItem::getOrderId).distinct().toList();
    }
}




