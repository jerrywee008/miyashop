package com.miya.service.pms;

import com.baomidou.mybatisplus.extension.service.IService;
import com.miya.common.entity.pms.PmsCategory;
import java.util.List;

public interface CategoryService extends IService<PmsCategory> {
    List<PmsCategory> getTree();
    List<PmsCategory> getList(Long parentId, Integer level);
}
