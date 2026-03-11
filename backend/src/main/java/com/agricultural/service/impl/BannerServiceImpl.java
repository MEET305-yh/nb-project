package com.agricultural.service.impl;

import com.agricultural.entity.Banner;
import com.agricultural.mapper.BannerMapper;
import com.agricultural.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getActiveBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getDeleted, 0);
        wrapper.eq(Banner::getStatus, 1); // 只返回启用的轮播图
        wrapper.orderByAsc(Banner::getSortOrder); // 按排序顺序
        wrapper.orderByDesc(Banner::getCreateTime); // 再按创建时间倒序
        return list(wrapper);
    }
}
















