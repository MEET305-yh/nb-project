-- 为分类表增加上下架状态字段
ALTER TABLE categories
ADD COLUMN status INT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架' AFTER description;

-- 兼容历史数据：空值统一为上架
UPDATE categories
SET status = 1
WHERE status IS NULL;

