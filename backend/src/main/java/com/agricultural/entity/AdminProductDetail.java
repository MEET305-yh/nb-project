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
    /**
     * 生长环境图片，多张图片 URL 使用逗号分隔
     */
    private String environmentImages;
    /**
     * 生长环境视频地址（例如上传到 OSS 后的播放 URL）
     */
    private String environmentVideo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}





