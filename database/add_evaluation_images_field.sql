-- 为评价表添加图片字段
USE agricultural_db;

ALTER TABLE evaluations
ADD COLUMN images TEXT COMMENT '评价图片URL（逗号分隔）' AFTER content;

