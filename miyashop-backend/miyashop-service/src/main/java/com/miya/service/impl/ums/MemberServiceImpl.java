package com.miya.service.impl.ums;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miya.common.entity.ums.UmsMember;
import com.miya.common.utils.JwtUtil;
import com.miya.mapper.ums.UmsMemberMapper;
import com.miya.service.ums.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements MemberService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String mobile, String code) {
        // Mock验证码检查
        UmsMember member;
        LambdaQueryWrapper<UmsMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsMember::getMobile, mobile);
        member = getOne(wrapper);

        if (member == null) {
            // 新用户自动注册
            member = new UmsMember();
            member.setUserId("U" + System.currentTimeMillis());
            member.setMobile(mobile);
            member.setNickname("用户" + mobile.substring(mobile.length() - 4));
            member.setLevel(1);
            member.setPoints(0);
            member.setBalance(java.math.BigDecimal.ZERO);
            member.setTotalSpent(java.math.BigDecimal.ZERO);
            member.setStatus(1);
            member.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            save(member);
        } else {
            member.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            updateById(member);
        }

        String token = jwtUtil.generateToken(member.getId(), member.getNickname(), "MEMBER");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", member);
        return result;
    }

    @Override
    public Page<UmsMember> pageList(Integer page, Integer size, String keyword, String mobile, Integer status) {
        LambdaQueryWrapper<UmsMember> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UmsMember::getNickname, keyword)
                    .or().like(UmsMember::getUserId, keyword));
        }
        if (StringUtils.hasText(mobile)) {
            wrapper.like(UmsMember::getMobile, mobile);
        }
        if (status != null) {
            wrapper.eq(UmsMember::getStatus, status);
        }
        wrapper.orderByDesc(UmsMember::getCreatedTime);
        return page(new Page<>(page, size), wrapper);
    }
}
