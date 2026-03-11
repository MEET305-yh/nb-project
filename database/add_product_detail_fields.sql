-- 为商品表添加详情相关字段
USE agricultural_db;

ALTER TABLE products 
ADD COLUMN specifications TEXT COMMENT '商品参数信息（JSON格式）' AFTER description,
ADD COLUMN detail_images TEXT COMMENT '详情图片（多个URL，逗号分隔）' AFTER image,
ADD COLUMN detail_text TEXT COMMENT '图文详情（富文本）' AFTER detail_images;




