package com.agricultural.service.impl;


import com.agricultural.entity.Product;
import com.agricultural.mapper.ProductMapper;
import com.agricultural.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public IPage<Product> getProducts(Page<Product> page, String category, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0);
        wrapper.eq(Product::getStatus, 1); // 只显示上架商品

        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }

        wrapper.orderByDesc(Product::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean updateStock(Long productId, Integer quantity) {
        Product product = getById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        int newStock = product.getStock() - quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("库存不足");
        }

        product.setStock(newStock);
        // 库存为 0 时自动下架
        if (newStock == 0) {
            product.setStatus(0);
        }
        return updateById(product);
    }

}

















