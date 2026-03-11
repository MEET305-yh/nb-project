package com.agricultural.service.impl;

import com.agricultural.entity.Category;
import com.agricultural.mapper.CategoryMapper;
import com.agricultural.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSortOrder);
        wrapper.orderByDesc(Category::getCreateTime);
        return list(wrapper);
    }
}

















