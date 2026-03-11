package com.agricultural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_products")
public class AdminProductDetail {
    @TableId(type = IdType.INPUT)
    private Long productId;
    private String specifications;
    private String detailImages;
    private String detailText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}





