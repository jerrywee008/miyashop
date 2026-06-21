package com.miya.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.miya.common.entity.oms.OmsOrder;
import com.miya.common.entity.oms.OmsOrderItem;
import com.miya.common.entity.pms.PmsCategory;
import com.miya.common.entity.pms.PmsProduct;
import com.miya.common.entity.pms.PmsSku;
import com.miya.common.entity.ums.UmsMember;
import com.miya.common.entity.vo.DashboardVO;
import com.miya.mapper.oms.OmsOrderItemMapper;
import com.miya.mapper.oms.OmsOrderMapper;
import com.miya.mapper.pms.PmsCategoryMapper;
import com.miya.mapper.pms.PmsProductMapper;
import com.miya.mapper.ums.UmsMemberMapper;
import com.miya.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private UmsMemberMapper memberMapper;

    @Autowired
    private PmsProductMapper productMapper;

    @Autowired
    private PmsCategoryMapper categoryMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        try {
            vo.setStats(buildStats());
        } catch (Exception e) {
            log.warn("统计卡片查询失败", e);
            vo.setStats(new DashboardVO.StatsVO());
        }

        try {
            vo.setSalesTrend(buildSalesTrend());
        } catch (Exception e) {
            log.warn("销售趋势查询失败", e);
            vo.setSalesTrend(Collections.emptyList());
        }

        try {
            vo.setCategoryDistribution(buildCategoryDistribution());
        } catch (Exception e) {
            log.warn("分类占比查询失败", e);
            vo.setCategoryDistribution(Collections.emptyList());
        }

        try {
            vo.setRecentOrders(buildRecentOrders());
        } catch (Exception e) {
            log.warn("最新订单查询失败", e);
            vo.setRecentOrders(Collections.emptyList());
        }

        try {
            vo.setHotProducts(buildHotProducts());
        } catch (Exception e) {
            log.warn("热门商品查询失败", e);
            vo.setHotProducts(Collections.emptyList());
        }

        return vo;
    }

    // ── 统计卡片 ──
    private DashboardVO.StatsVO buildStats() {
        DashboardVO.StatsVO stats = new DashboardVO.StatsVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        // 今日订单
        stats.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<OmsOrder>()
                        .ge(OmsOrder::getCreatedTime, todayStart)
                        .lt(OmsOrder::getCreatedTime, todayEnd)));

        // 今日销售额（已支付订单的实付金额）
        List<OmsOrder> todayPaid = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .ge(OmsOrder::getCreatedTime, todayStart)
                        .lt(OmsOrder::getCreatedTime, todayEnd)
                        .gt(OmsOrder::getStatus, 0)); // status > 0 表示已支付
        stats.setTodaySales(todayPaid.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 今日新增用户
        stats.setNewUsers(memberMapper.selectCount(
                new LambdaQueryWrapper<UmsMember>()
                        .ge(UmsMember::getCreatedTime, todayStart)
                        .lt(UmsMember::getCreatedTime, todayEnd)));

        // 待处理订单（待发货）
        stats.setPendingOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getStatus, 1)));

        return stats;
    }

    // ── 近7天销售趋势 ──
    private List<DashboardVO.TrendItem> buildSalesTrend() {
        List<DashboardVO.TrendItem> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            List<OmsOrder> dayOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<OmsOrder>()
                            .ge(OmsOrder::getCreatedTime, dayStart)
                            .lt(OmsOrder::getCreatedTime, dayEnd)
                            .gt(OmsOrder::getStatus, 0));

            DashboardVO.TrendItem item = new DashboardVO.TrendItem();
            item.setDate(day.format(fmt));
            item.setOrders((long) dayOrders.size());
            item.setSales(dayOrders.stream()
                    .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            trend.add(item);
        }
        return trend;
    }

    // ── 分类占比 ──
    private List<DashboardVO.PieItem> buildCategoryDistribution() {
        List<DashboardVO.PieItem> items = new ArrayList<>();

        // 查询所有启用的一级分类
        List<PmsCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<PmsCategory>()
                        .eq(PmsCategory::getLevel, 1)
                        .eq(PmsCategory::getShowStatus, 1));

        for (PmsCategory cat : categories) {
            long count = productMapper.selectCount(
                    new LambdaQueryWrapper<PmsProduct>()
                            .eq(PmsProduct::getCategoryId, cat.getId())
                            .eq(PmsProduct::getStatus, 1));
            if (count > 0) {
                DashboardVO.PieItem item = new DashboardVO.PieItem();
                item.setName(cat.getName());
                item.setValue(count);
                items.add(item);
            }
        }
        return items;
    }

    // ── 最新订单（近 5 条） ──
    private List<DashboardVO.OrderItem> buildRecentOrders() {
        List<OmsOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .orderByDesc(OmsOrder::getCreatedTime)
                        .last("LIMIT 5"));

        List<DashboardVO.OrderItem> result = new ArrayList<>();
        for (OmsOrder order : orders) {
            // 查订单明细取第一个商品名
            List<OmsOrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>()
                            .eq(OmsOrderItem::getOrderId, order.getId())
                            .last("LIMIT 1"));
            String productName = items.isEmpty() ? "-" : items.get(0).getProductName();

            DashboardVO.OrderItem oi = new DashboardVO.OrderItem();
            oi.setOrderNo(order.getOrderNo());
            oi.setProductName(productName);
            oi.setAmount(order.getPayAmount() != null ? order.getPayAmount() : order.getTotalAmount());
            oi.setStatus(order.getStatus());
            result.add(oi);
        }
        return result;
    }

    // ── 热门商品（销量 Top 5） ──
    private List<DashboardVO.ProductItem> buildHotProducts() {
        List<PmsProduct> products = productMapper.selectList(
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getStatus, 1)
                        .orderByDesc(PmsProduct::getSales)
                        .last("LIMIT 5"));

        return products.stream().map(p -> {
            DashboardVO.ProductItem item = new DashboardVO.ProductItem();
            item.setName(p.getName());
            item.setPrice(p.getPrice());
            item.setSales(p.getSales());
            item.setStock(p.getStock());
            return item;
        }).collect(Collectors.toList());
    }
}
