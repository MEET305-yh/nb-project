package com.agricultural.controller.admin;

import com.agricultural.common.Result;
import com.agricultural.entity.AdminProductDetail;
import com.agricultural.entity.Product;
import com.agricultural.entity.User;
import com.agricultural.filter.AliOssUtil;
import com.agricultural.mapper.ProductMapper;
import com.agricultural.service.AdminProductDetailService;
import com.agricultural.service.ProductService;
import com.agricultural.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasAnyRole('ADMIN','MERCHANT')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AdminProductDetailService adminProductDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @GetMapping
    public Result<IPage<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getDescription, keyword));
        }

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        // 商家只可查看自己的商品
        if (isMerchant()) {
            Long merchantId = getCurrentUserId();
            if (merchantId != null) {
                wrapper.eq(Product::getMerchantId, merchantId);
            } else {
                // 如果无法获取商家信息，直接返回空列表
                return Result.success(page);
            }
        }

        wrapper.orderByDesc(Product::getCreateTime);
        IPage<Product> result = productService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getDeleted() == 1) {
            return Result.error("商品不存在");
        }
        if (!hasProductPermission(product)) {
            return Result.error("无权查看该商品");
        }
        fillProductDetail(product);
        return Result.success(product);
    }

    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        product.setStatus(1);
        product.setDeleted(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        if (isMerchant()) {
            Long merchantId = getCurrentUserId();
            if (merchantId == null) {
                return Result.error("无法获取商家信息");
            }
            product.setMerchantId(merchantId);
        }
        productService.save(product);
        saveOrUpdateProductDetail(product);
        return Result.success("创建成功", product);
    }

    @PutMapping("/{id}")
    public Result<String> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product existing = productService.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error("商品不存在");
        }

        if (!hasProductPermission(existing)) {
            return Result.error("无权操作该商品");
        }

        product.setId(id);
        if (isMerchant()) {
            Long merchantId = getCurrentUserId();
            if (merchantId == null) {
                return Result.error("无法获取商家信息");
            }
            product.setMerchantId(merchantId);
        } else if (product.getMerchantId() == null) {
            product.setMerchantId(existing.getMerchantId());
        }
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);
        product.setId(id);
        saveOrUpdateProductDetail(product);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        
        if (product.getDeleted() != null && product.getDeleted() == 1) {
            return Result.error("商品已被删除");
        }

        if (!hasProductPermission(product)) {
            return Result.error("无权操作该商品");
        }

        // 使用 LambdaUpdateWrapper 直接更新，绕过 MyBatis-Plus 的逻辑删除限制
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, id)
                .set(Product::getDeleted, 1)
                .set(Product::getUpdateTime, LocalDateTime.now());
        
        int result = productMapper.update(null, updateWrapper);
        
        if (result > 0) {
            adminProductDetailService.removeById(id);
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product product = productService.getById(id);
        if (product == null || product.getDeleted() == 1) {
            return Result.error("商品不存在");
        }
        if (!hasProductPermission(product)) {
            return Result.error("无权操作该商品");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);
        return Result.success("状态更新成功");
    }

    private void saveOrUpdateProductDetail(Product product) {
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

    private void fillProductDetail(Product product) {
        if (product == null || product.getId() == null) {
            return;
        }
        AdminProductDetail detail = adminProductDetailService.getById(product.getId());
        if (detail != null) {
            product.setSpecifications(detail.getSpecifications());
            product.setDetailImages(detail.getDetailImages());
            product.setDetailText(detail.getDetailText());
            product.setEnvironmentImages(detail.getEnvironmentImages());
            product.setEnvironmentVideo(detail.getEnvironmentVideo());
        }
    }

    private boolean hasProductPermission(Product product) {
        if (product == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        if (isMerchant()) {
            Long merchantId = getCurrentUserId();
            return merchantId != null && merchantId.equals(product.getMerchantId());
        }
        return false;
    }

    private boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    private boolean isMerchant() {
        return hasRole("ROLE_MERCHANT");
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (roleName.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        if (username == null) {
            return null;
        }
        User user = userService.findByUsername(username);
        return user != null ? user.getId() : null;
    }
}

