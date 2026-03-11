package com.agricultural.service;

import com.agricultural.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BannerService extends IService<Banner> {
    List<Banner> getActiveBanners();
}
















