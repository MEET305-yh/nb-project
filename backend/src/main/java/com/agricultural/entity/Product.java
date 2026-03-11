package com.agricultural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    /**
     * 商品展示标签，多个标签使用逗号分隔，例如："产地直供,时令优选"
     */
    private String tags;
    @TableField(exist = false)
    private String specifications; // 商品参数信息（JSON或文本）
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String image;
    @TableField(exist = false)
    private String detailImages; // 详情图片（多个URL，逗号分隔）
    @TableField(exist = false)
    private String detailText; // 图文详情（富文本）
    @TableField(exist = false)
    private String environmentImages; // 生长环境图片（多个URL，逗号分隔）
    @TableField(exist = false)
    private String environmentVideo; // 生长环境视频URL
    private Long merchantId;
    private Integer status; // 0-下架, 1-上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}




