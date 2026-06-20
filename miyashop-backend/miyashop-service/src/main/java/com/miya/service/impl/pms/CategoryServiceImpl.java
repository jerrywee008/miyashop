package com.miya.service.impl.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.pms.PmsCategory;
import com.miya.mapper.pms.PmsCategoryMapper;
import com.miya.service.pms.CategoryService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<PmsCategoryMapper, PmsCategory> implements CategoryService {

    @Override
    public List<PmsCategory> getTree() {
        List<PmsCategory> all = list(new LambdaQueryWrapper<PmsCategory>()
                .orderByAsc(PmsCategory::getSort));
        return buildTree(all, 0L);
    }

    @Override
    public List<PmsCategory> getList(Long parentId, Integer level) {
        LambdaQueryWrapper<PmsCategory> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) wrapper.eq(PmsCategory::getParentId, parentId);
        if (level != null) wrapper.eq(PmsCategory::getLevel, level);
        wrapper.orderByAsc(PmsCategory::getSort);
        return list(wrapper);
    }

    private List<PmsCategory> buildTree(List<PmsCategory> all, Long parentId) {
        List<PmsCategory> result = new ArrayList<>();
        List<PmsCategory> children = all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .collect(Collectors.toList());
        // MyBatis-Plus doesn't have children field, use a simple approach
        result.addAll(children);
        return result;
    }
}
