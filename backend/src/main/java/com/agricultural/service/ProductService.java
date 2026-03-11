package com.agricultural.service;

import com.agricultural.entity.Product;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ProductService extends IService<Product> {
    IPage<Product> getProducts(Page<Product> page, String category, String keyword);
    boolean updateStock(Long productId, Integer quantity);
}


