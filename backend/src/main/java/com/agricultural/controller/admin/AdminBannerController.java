package com.agricultural.controller.admin;

import com.agricultural.common.Result;
import com.agricultural.entity.Banner;
import com.agricultural.mapper.BannerMapper;
import com.agricultural.service.BannerService;
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
@RequestMapping("/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerMapper bannerMapper;

    @GetMapping
    public Result<IPage<Banner>> getBannerList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Banner> page = new Page<>(current, size);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Banner::getCategoryName, keyword));
        }

        wrapper.orderByAsc(Banner::getSortOrder);
        wrapper.orderByDesc(Banner::getCreateTime);
        IPage<Banner> result = bannerService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<Banner>> getAllBanners() {
        List<Banner> banners = bannerService.list();
        return Result.success(banners);
    }

    @GetMapping("/{id}")
    public Result<Banner> getBannerById(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner == null || banner.getDeleted() == 1) {
            return Result.error("轮播图不存在");
        }
        return Result.success(banner);
    }

    @PostMapping
    public Result<Banner> createBanner(@RequestBody Banner banner) {
        banner.setDeleted(0);
        if (banner.getStatus() == null) {
            banner.setStatus(1); // 默认启用
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        bannerService.save(banner);
        return Result.success("创建成功", banner);
    }

    @PutMapping("/{id}")
    public Result<String> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        Banner existing = bannerService.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error("轮播图不存在");
        }

        banner.setId(id);
        banner.setUpdateTime(LocalDateTime.now());
        bannerService.updateById(banner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteBanner(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        
        if (banner.getDeleted() != null && banner.getDeleted() == 1) {
            return Result.error("轮播图已被删除");
        }

        // 使用 LambdaUpdateWrapper 直接更新，绕过 MyBatis-Plus 的逻辑删除限制
        LambdaUpdateWrapper<Banner> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Banner::getId, id)
                .set(Banner::getDeleted, 1)
                .set(Banner::getUpdateTime, LocalDateTime.now());
        
        int result = bannerMapper.update(null, updateWrapper);
        
        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateBannerStatus(@PathVariable Long id, @RequestParam Integer status) {
        Banner banner = bannerService.getById(id);
        if (banner == null || banner.getDeleted() == 1) {
            return Result.error("轮播图不存在");
        }
        banner.setStatus(status);
        banner.setUpdateTime(LocalDateTime.now());
        bannerService.updateById(banner);
        return Result.success("状态更新成功");
    }
}
















