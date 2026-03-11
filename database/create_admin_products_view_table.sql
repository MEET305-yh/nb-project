-- 管理端商品详情附表（存放规格、图文详情等信息）
USE agricultural_db;

DROP TABLE IF EXISTS admin_products;

CREATE TABLE admin_products (
    product_id     BIGINT PRIMARY KEY COMMENT '商品ID，对应 products.id',
    specifications TEXT COMMENT '商品参数信息（JSON或文本）',
    detail_images  TEXT COMMENT '详情图片URL（逗号分隔）',
    detail_text    TEXT COMMENT '图文详情（富文本或纯文本）',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理后台商品详情扩展表';

