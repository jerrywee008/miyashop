package com.miya.service.ums;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.ums.UmsMember;
import java.util.Map;

public interface MemberService extends IService<UmsMember> {
    Map<String, Object> login(String mobile, String code);
    Page<UmsMember> pageList(Integer page, Integer size, String keyword, String mobile, Integer status);
}
