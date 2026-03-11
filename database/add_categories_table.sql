-- 添加分类表
USE agricultural_db;

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    INDEX idx_name (name),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 插入初始分类数据
INSERT INTO categories (name, description, sort_order, deleted) VALUES
('水果', '新鲜水果', 1, 0),
('蔬菜', '绿色蔬菜', 2, 0),
('粮食', '优质粮食', 3, 0),
('其他', '其他农产品', 4, 0);

















