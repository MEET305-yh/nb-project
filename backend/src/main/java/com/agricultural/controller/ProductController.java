package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.entity.AdminProductDetail;
import com.agricultural.entity.Product;
import com.agricultural.service.AdminProductDetailService;
import com.agricultural.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AdminProductDetailService adminProductDetailService;

    @GetMapping("/public/list")
    public Result<IPage<Product>> getProducts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<Product> page = new Page<>(current, size);
        IPage<Product> result = productService.getProducts(page, category, keyword);
        return Result.success(result);
    }

    @GetMapping("/public/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getDeleted() == 1 || product.getStatus() == 0) {
            return Result.error("商品不存在或已下架");
        }
        AdminProductDetail detail = adminProductDetailService.getById(id);
        if (detail != null) {
            product.setSpecifications(detail.getSpecifications());
            product.setDetailImages(detail.getDetailImages());
            product.setDetailText(detail.getDetailText());
            product.setEnvironmentImages(detail.getEnvironmentImages());
            product.setEnvironmentVideo(detail.getEnvironmentVideo());
        }
        return Result.success(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT') or hasRole('ADMIN')")
    public Result<Product> createProduct(@RequestBody Product product) {
        product.setStatus(1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setDeleted(0);
        productService.save(product);
        saveProductDetail(product);
        return Result.success("创建成功", product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('ADMIN')")
    public Result<String> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);
        saveProductDetail(product);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('ADMIN')")
    public Result<String> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);
        return Result.success("状态更新成功");
    }

    private void saveProductDetail(Product product) {
        if (product == null || product.getId() == null) {
            return;
        }
        AdminProductDetail detail = new AdminProductDetail();
        detail.setProductId(product.getId());
        detail.setSpecifications(product.getSpecifications());
        detail.setDetailImages(product.getDetailImages());
        detail.setDetailText(product.getDetailText());
        detail.setEnvironmentImages(product.getEnvironmentImages());
        detail.setEnvironmentVideo(product.getEnvironmentVideo());
        adminProductDetailService.saveOrUpdate(detail);
    }
}


