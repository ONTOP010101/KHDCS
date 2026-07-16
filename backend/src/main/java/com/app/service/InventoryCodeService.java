package com.app.service;

import com.app.common.PageResult;
import com.app.entity.Inventory;
import com.app.entity.InventoryCode;
import com.app.mapper.InventoryCodeMapper;
import com.app.mapper.InventoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryCodeService {

    @Autowired
    private InventoryCodeMapper inventoryCodeMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    private static final Map<String, SFunction<InventoryCode, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("inventoryCode", InventoryCode::getInventoryCode);
        SORT_FIELD_MAP.put("codeName", InventoryCode::getCodeName);
        SORT_FIELD_MAP.put("createDate", InventoryCode::getCreateDate);
        SORT_FIELD_MAP.put("creator", InventoryCode::getCreator);
        SORT_FIELD_MAP.put("floor", InventoryCode::getFloor);
    }

    public PageResult<InventoryCode> list(long current, long size, String keyword, String sortField, String sortOrder) {
        LambdaQueryWrapper<InventoryCode> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                    .like(InventoryCode::getInventoryCode, keyword)
                    .or().like(InventoryCode::getCodeName, keyword));
        }
        applySort(qw, sortField, sortOrder);
        Page<InventoryCode> page = new Page<>(current, size);
        Page<InventoryCode> result = inventoryCodeMapper.selectPage(page, qw);
        // 填充每个代号组的明细总数和已提交数
        for (InventoryCode code : result.getRecords()) {
            if (code.getCodeName() != null) {
                LambdaQueryWrapper<Inventory> cw = new LambdaQueryWrapper<>();
                cw.eq(Inventory::getCodeName, code.getCodeName()).eq(Inventory::getDeleted, 0);
                code.setTotalCount(inventoryMapper.selectCount(cw).intValue());
                cw.eq(Inventory::getSubmitted, 1);
                code.setSubmittedCount(inventoryMapper.selectCount(cw).intValue());
            }
        }
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void applySort(LambdaQueryWrapper<InventoryCode> qw, String sortField, String sortOrder) {
        if (!StringUtils.hasText(sortField)) {
            qw.orderByDesc(InventoryCode::getCreateTime);
            return;
        }
        SFunction<InventoryCode, ?> field = SORT_FIELD_MAP.get(sortField);
        if (field == null) {
            qw.orderByDesc(InventoryCode::getCreateTime);
            return;
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            qw.orderByAsc(field);
        } else {
            qw.orderByDesc(field);
        }
    }

    public InventoryCode getById(Long id) {
        return inventoryCodeMapper.selectById(id);
    }

    public InventoryCode create(InventoryCode inventoryCode) {
        if (!StringUtils.hasText(inventoryCode.getCodeName())) {
            throw new RuntimeException("本次代号不能为空");
        }
        // 检查代号是否已存在
        LambdaQueryWrapper<InventoryCode> qw = new LambdaQueryWrapper<>();
        qw.eq(InventoryCode::getCodeName, inventoryCode.getCodeName());
        Long count = inventoryCodeMapper.selectCount(qw);
        if (count != null && count > 0) {
            throw new RuntimeException("代号[" + inventoryCode.getCodeName() + "]已存在");
        }
        if (!StringUtils.hasText(inventoryCode.getInventoryCode())) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            inventoryCode.setInventoryCode(today + "-" + inventoryCode.getCodeName());
        }
        inventoryCodeMapper.insert(inventoryCode);
        return inventoryCode;
    }

    public void update(Long id, InventoryCode inventoryCode) {
        inventoryCode.setId(id);
        inventoryCodeMapper.updateById(inventoryCode);
    }

    public void delete(Long id) {
        inventoryCodeMapper.deleteById(id);
    }
}
