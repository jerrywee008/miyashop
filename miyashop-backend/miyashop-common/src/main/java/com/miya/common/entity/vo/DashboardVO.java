package com.miya.common.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 首页仪表盘数据
 */
@Data
@Schema(description = "首页仪表盘数据")
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "统计卡片")
    private StatsVO stats;

    @Schema(description = "近7天销售趋势")
    private List<TrendItem> salesTrend;

    @Schema(description = "分类占比")
    private List<PieItem> categoryDistribution;

    @Schema(description = "最新订单")
    private List<OrderItem> recentOrders;

    @Schema(description = "热门商品")
    private List<ProductItem> hotProducts;

    // ── 内部类 ──

    @Data
    @Schema(description = "统计卡片数据")
    public static class StatsVO implements Serializable {
        @Schema(description = "今日订单数")
        private Long todayOrders = 0L;
        @Schema(description = "今日销售额")
        private BigDecimal todaySales = BigDecimal.ZERO;
        @Schema(description = "今日新增用户")
        private Long newUsers = 0L;
        @Schema(description = "待处理订单数")
        private Long pendingOrders = 0L;
    }

    @Data
    @Schema(description = "趋势数据项")
    public static class TrendItem implements Serializable {
        @Schema(description = "日期")
        private String date;
        @Schema(description = "订单数")
        private Long orders = 0L;
        @Schema(description = "销售额")
        private BigDecimal sales = BigDecimal.ZERO;
    }

    @Data
    @Schema(description = "分类占比")
    public static class PieItem implements Serializable {
        @Schema(description = "分类名称")
        private String name;
        @Schema(description = "商品数量")
        private Long value = 0L;
    }

    @Data
    @Schema(description = "订单摘要")
    public static class OrderItem implements Serializable {
        private String orderNo;
        private String productName;
        private BigDecimal amount;
        private Integer status;
    }

    @Data
    @Schema(description = "热门商品")
    public static class ProductItem implements Serializable {
        private String name;
        private BigDecimal price;
        private Integer sales;
        private Integer stock;
    }
}
