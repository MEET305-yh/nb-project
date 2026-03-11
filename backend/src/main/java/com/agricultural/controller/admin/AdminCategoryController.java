package com.agricultural.controller.admin;

import com.agricultural.common.Result;
import com.agricultural.entity.Category;
import com.agricultural.mapper.CategoryMapper;
import com.agricultural.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN','MERCHANT')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping
    public Result<IPage<Category>> getCategoryList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Category> page = new Page<>(current, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Category::getName, keyword)
                    .or().like(Category::getDescription, keyword));
        }

        wrapper.orderByAsc(Category::getSortOrder);
        wrapper.orderByDesc(Category::getCreateTime);
        IPage<Category> result = categoryService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null || category.getDeleted() == 1) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        // 检查分类名称是否已存在
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName());
        wrapper.eq(Category::getDeleted, 0);
        Category existing = categoryService.getOne(wrapper);
        if (existing != null) {
            return Result.error("分类名称已存在");
        }

        category.setDeleted(0);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryService.save(category);
        return Result.success("创建成功", category);
    }

    @PutMapping("/{id}")
    public Result<String> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryService.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        // 检查分类名称是否与其他分类重复
        if (category.getName() != null && !category.getName().equals(existing.getName())) {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getName, category.getName());
            wrapper.eq(Category::getDeleted, 0);
            wrapper.ne(Category::getId, id);
            Category duplicate = categoryService.getOne(wrapper);
            if (duplicate != null) {
                return Result.error("分类名称已存在");
            }
        }

        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        categoryService.updateById(category);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        
        if (category.getDeleted() != null && category.getDeleted() == 1) {
            return Result.error("分类已被删除");
        }

        // 检查是否有商品使用此分类
        // 这里可以添加检查逻辑，如果有商品使用此分类，可以提示用户

        // 使用 LambdaUpdateWrapper 直接更新，绕过 MyBatis-Plus 的逻辑删除限制
        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Category::getId, id)
                .set(Category::getDeleted, 1)
                .set(Category::getUpdateTime, LocalDateTime.now());
        
        int result = categoryMapper.update(null, updateWrapper);
        
        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}

