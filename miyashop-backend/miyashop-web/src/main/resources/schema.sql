-- ========================================
-- MiyaShop 电商系统数据库表结构
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS miyashop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE miyashop;

-- ========================================
-- 系统管理模块
-- ========================================

-- 管理员用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    role_id BIGINT COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单权限表
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    url VARCHAR(200) COMMENT '菜单URL',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '排序',
    type TINYINT DEFAULT 1 COMMENT '类型 1目录 2菜单 3按钮',
    permission VARCHAR(100) COMMENT '权限标识',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_id (role_id),
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ========================================
-- 用户模块
-- ========================================

-- 会员表
CREATE TABLE ums_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会员ID',
    user_id VARCHAR(50) NOT NULL UNIQUE COMMENT '用户ID',
    mobile VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像',
    gender TINYINT COMMENT '性别 0未知 1男 2女',
    birthday DATE COMMENT '生日',
    openid VARCHAR(100) COMMENT '微信OpenID',
    level INT DEFAULT 1 COMMENT '会员等级',
    points INT DEFAULT 0 COMMENT '积分',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    total_spent DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_mobile (mobile),
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 用户收货地址表
CREATE TABLE ums_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区/县',
    detail VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认 0否 1是',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ========================================
-- 商品模块
-- ========================================

-- 商品分类表
CREATE TABLE pms_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(200) COMMENT '分类图标',
    image VARCHAR(500) COMMENT '分类图片',
    level TINYINT NOT NULL COMMENT '分类层级 1一级 2二级 3三级',
    sort INT DEFAULT 0 COMMENT '排序',
    show_status TINYINT DEFAULT 1 COMMENT '显示状态 0隐藏 1显示',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 品牌表
CREATE TABLE pms_brand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '品牌ID',
    name VARCHAR(50) NOT NULL COMMENT '品牌名称',
    logo VARCHAR(500) COMMENT '品牌logo',
    desc VARCHAR(500) COMMENT '品牌描述',
    sort INT DEFAULT 0 COMMENT '排序',
    show_status TINYINT DEFAULT 1 COMMENT '显示状态 0隐藏 1显示',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- 商品表
CREATE TABLE pms_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    brand_id BIGINT COMMENT '品牌ID',
    images VARCHAR(1000) COMMENT '商品图片(多张逗号分隔)',
    detail TEXT COMMENT '商品详情',
    price DECIMAL(10,2) COMMENT '参考价格',
    stock INT DEFAULT 0 COMMENT '总库存',
    sales INT DEFAULT 0 COMMENT '销量',
    score DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    review_count INT DEFAULT 0 COMMENT '评价数',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_category_id (category_id),
    INDEX idx_brand_id (brand_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品规格参数表
CREATE TABLE pms_product_attr (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规格ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(50) NOT NULL COMMENT '规格名称',
    values VARCHAR(500) NOT NULL COMMENT '规格值(多个逗号分隔)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格参数表';

-- 商品SKU表
CREATE TABLE pms_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'SKU ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    specs VARCHAR(500) NOT NULL COMMENT '规格参数(JSON格式)',
    image VARCHAR(500) COMMENT 'SKU图片',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) COMMENT '原价',
    stock INT DEFAULT 0 COMMENT '库存',
    sales INT DEFAULT 0 COMMENT '销量',
    barcode VARCHAR(50) COMMENT '条形码',
    status TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_product_id (product_id),
    INDEX idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 商品SPU表
CREATE TABLE pms_spu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'SPU ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    spec_params TEXT COMMENT '规格参数定义(JSON格式)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

-- 商品评价表
CREATE TABLE pms_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT COMMENT 'SKU ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    score TINYINT NOT NULL COMMENT '评分 1-5分',
    content VARCHAR(500) COMMENT '评价内容',
    images VARCHAR(1000) COMMENT '评价图片(多张逗号分隔)',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名 0否 1是',
    reply_content VARCHAR(500) COMMENT '商家回复',
    reply_time DATETIME COMMENT '回复时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ========================================
-- 订单模块
-- ========================================

-- 购物车表
CREATE TABLE oms_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT NOT NULL COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中 0未选中 1已选中',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE oms_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    freight DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    status TINYINT DEFAULT 0 COMMENT '订单状态 0待支付 1待发货 2待收货 3已完成 4已取消 5已退款 6售后处理',
    pay_type TINYINT COMMENT '支付方式 1微信 2支付宝',
    pay_time DATETIME COMMENT '支付时间',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    receiver_address VARCHAR(500) COMMENT '收货地址',
    remark VARCHAR(500) COMMENT '订单备注',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    cancel_time DATETIME COMMENT '取消时间',
    finish_time DATETIME COMMENT '完成时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE oms_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    sku_image VARCHAR(500) COMMENT 'SKU图片',
    sku_specs VARCHAR(500) COMMENT 'SKU规格',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_product_id (product_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 支付记录表
CREATE TABLE oms_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    payment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '支付单号',
    third_party_no VARCHAR(100) COMMENT '第三方支付单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_type TINYINT NOT NULL COMMENT '支付方式 1微信 2支付宝',
    status TINYINT DEFAULT 0 COMMENT '支付状态 0待支付 1已支付 2支付失败 3已退款',
    pay_time DATETIME COMMENT '支付时间',
    refund_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '退款金额',
    refund_time DATETIME COMMENT '退款时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id),
    INDEX idx_payment_no (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 退款记录表
CREATE TABLE oms_refund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_item_id BIGINT COMMENT '订单明细ID',
    refund_no VARCHAR(50) NOT NULL UNIQUE COMMENT '退款单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(500) NOT NULL COMMENT '退款原因',
    type TINYINT NOT NULL COMMENT '退款类型 1仅退款 2退货退款',
    status TINYINT DEFAULT 0 COMMENT '退款状态 0待处理 1同意 2拒绝 3退款成功 4退款失败',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    handle_time DATETIME COMMENT '处理时间',
    refund_time DATETIME COMMENT '退款时间',
    remark VARCHAR(500) COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id),
    INDEX idx_refund_no (refund_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ========================================
-- 秒杀模块
-- ========================================

-- 秒杀活动表
CREATE TABLE sms_seckill_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '活动状态 0未开始 1进行中 2已结束',
    sort INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 秒杀商品关联表
CREATE TABLE sms_seckill_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
    stock INT NOT NULL COMMENT '秒杀库存',
    sold INT DEFAULT 0 COMMENT '已售数量',
    limit_per_user INT DEFAULT 1 COMMENT '每人限购数量',
    sort INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_activity_id (activity_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品关联表';

-- 秒杀订单表
CREATE TABLE sms_seckill_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    order_id BIGINT COMMENT '订单ID',
    quantity INT NOT NULL COMMENT '购买数量',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    status TINYINT DEFAULT 0 COMMENT '状态 0待支付 1已支付 2已取消',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- ========================================
-- 团购模块
-- ========================================

-- 团购活动表
CREATE TABLE sms_groupbuy_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    groupbuy_price DECIMAL(10,2) NOT NULL COMMENT '团购价',
    min_people INT NOT NULL COMMENT '成团人数',
    max_people INT COMMENT '最大人数',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    valid_hours INT DEFAULT 24 COMMENT '有效时长(小时)',
    status TINYINT DEFAULT 0 COMMENT '活动状态 0未开始 1进行中 2已结束',
    sort INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
    INDEX idx_sku_id (sku_id),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

-- 团购队伍表
CREATE TABLE sms_groupbuy_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '队伍ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    leader_id BIGINT NOT NULL COMMENT '团长ID',
    current_people INT DEFAULT 1 COMMENT '当前人数',
    max_people INT NOT NULL COMMENT '最大人数',
    status TINYINT DEFAULT 0 COMMENT '状态 0待成团 1进行中 2成功 3失败',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    success_time DATETIME COMMENT '成团时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_activity_id (activity_id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_leader_id (leader_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购队伍表';

-- 团购成员表
CREATE TABLE sms_groupbuy_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    team_id BIGINT NOT NULL COMMENT '队伍ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '订单ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    is_leader TINYINT DEFAULT 0 COMMENT '是否团长 0否 1是',
    INDEX idx_team_id (team_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购成员表';

-- ========================================
-- 营销模块
-- ========================================

-- 优惠券表
CREATE TABLE pms_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type TINYINT NOT NULL COMMENT '类型 1满减券 2折扣券',
    amount DECIMAL(10,2) COMMENT '面额(满减券)',
    discount INT COMMENT '折扣(折扣券，如85表示85折)',
    min_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
    total_count INT NOT NULL COMMENT '发放总数',
    used_count INT DEFAULT 0 COMMENT '已使用数量',
    per_limit INT COMMENT '每人限领数量',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 会员优惠券表
CREATE TABLE ums_member_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    order_id BIGINT COMMENT '使用的订单ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0未使用 1已使用 2已过期',
    receive_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    use_time DATETIME COMMENT '使用时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    INDEX idx_member_id (member_id),
    INDEX idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员优惠券表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入管理员用户
INSERT INTO sys_user (username, password, real_name, role_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, 1);

-- 插入角色
INSERT INTO sys_role (name, code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限'),
('运营管理员', 'OPERATION_ADMIN', '商品、订单等运营权限'),
('客服管理员', 'SERVICE_ADMIN', '客服权限');

-- 插入菜单
INSERT INTO sys_menu (parent_id, name, url, icon, sort, type, permission) VALUES
(0, '系统管理', '', 'setting', 1, 1, ''),
(1, '用户管理', '/user', 'user', 1, 2, 'user:list'),
(1, '角色管理', '/role', 'team', 2, 2, 'role:list'),
(1, '菜单管理', '/menu', 'menu', 3, 2, 'menu:list'),
(0, '商品管理', '', 'shopping', 2, 1, ''),
(5, '分类管理', '/category', 'list', 1, 2, 'category:list'),
(5, '品牌管理', '/brand', 'star', 2, 2, 'brand:list'),
(5, '商品管理', '/product', 'goods', 3, 2, 'product:list'),
(0, '订单管理', '', 'file-text', 3, 1, ''),
(9, '订单列表', '/order', 'order', 1, 2, 'order:list'),
(9, '退款管理', '/refund', 'return', 2, 2, 'refund:list'),
(0, '营销管理', '', 'gift', 4, 1, ''),
(12, '秒杀活动', '/seckill', 'thunderbolt', 1, 2, 'seckill:list'),
(12, '团购活动', '/groupbuy', 'team', 2, 2, 'groupbuy:list'),
(12, '优惠券', '/coupon', 'coupon', 3, 2, 'coupon:list'),
(0, '数据统计', '', 'chart', 5, 1, ''),
(16, '销售统计', '/statistics/sales', 'line-chart', 1, 2, 'statistics:sales'),
(16, '用户统计', '/statistics/user', 'pie-chart', 2, 2, 'statistics:user');

-- 关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 插入商品分类
INSERT INTO pms_category (parent_id, name, level, sort) VALUES
(0, '女装', 1, 1),
(1, '连衣裙', 2, 1),
(1, '衬衫', 2, 2),
(1, 'T恤', 2, 3),
(1, '裤子', 2, 4),
(1, '外套', 2, 5),
(0, '首饰', 1, 2),
(7, '项链', 2, 1),
(7, '耳环', 2, 2),
(7, '手镯', 2, 3),
(7, '戒指', 2, 4),
(0, '彩妆', 1, 3),
(11, '口红', 2, 1),
(11, '眼影', 2, 2),
(11, '粉底', 2, 3),
(11, '眉笔', 2, 4);

-- ========================================
-- 兼容性补丁：添加 BaseEntity 必需的字段
-- (如果 CREATE TABLE 中未定义则补充)
-- ========================================

-- 缺少 updated_time 的表
ALTER TABLE sms_seckill_order ADD COLUMN IF NOT EXISTS updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE sms_groupbuy_member ADD COLUMN IF NOT EXISTS updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE ums_member_coupon ADD COLUMN IF NOT EXISTS updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 缺少 deleted 的表
ALTER TABLE oms_cart ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE oms_payment ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE oms_refund ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE sms_seckill_order ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE sms_groupbuy_team ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE sms_groupbuy_member ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';
ALTER TABLE ums_member_coupon ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '删除标记 0未删除 1已删除';