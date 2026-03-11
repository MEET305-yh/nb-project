package com.agricultural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("banners")
public class Banner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String image; // 轮播图图片 URL
    private String categoryName; // 分类名称，对应分类管理里的数据
    private Integer sortOrder; // 排序顺序
    private Integer status; // 0-禁用, 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
















