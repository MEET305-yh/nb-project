package com.agricultural.service;

import com.agricultural.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> getAllCategories();
}

















