package com.app.service;

import com.app.common.PageResult;
import com.app.entity.Outbound;
import com.app.entity.OutboundCode;
import com.app.mapper.OutboundCodeMapper;
import com.app.mapper.OutboundMapper;
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
public class OutboundCodeService {

    @Autowired
    private OutboundCodeMapper outboundCodeMapper;

    @Autowired
    private OutboundMapper outboundMapper;

    private static final Map<String, SFunction<OutboundCode, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("outboundCode", OutboundCode::getOutboundCode);
        SORT_FIELD_MAP.put("codeName", OutboundCode::getCodeName);
        SORT_FIELD_MAP.put("createDate", OutboundCode::getCreateDate);
        SORT_FIELD_MAP.put("creator", OutboundCode::getCreator);
        SORT_FIELD_MAP.put("floor", OutboundCode::getFloor);
    }

    public PageResult<OutboundCode> list(long current, long size, String keyword, String sortField, String sortOrder) {
        LambdaQueryWrapper<OutboundCode> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                    .like(OutboundCode::getOutboundCode, keyword)
                    .or().like(OutboundCode::getCodeName, keyword));
        }
        applySort(qw, sortField, sortOrder);
        Page<OutboundCode> page = new Page<>(current, size);
        Page<OutboundCode> result = outboundCodeMapper.selectPage(page, qw);
        // 填充每个代号组的明细总数和已提交数
        for (OutboundCode code : result.getRecords()) {
            if (code.getCodeName() != null) {
                LambdaQueryWrapper<Outbound> cw = new LambdaQueryWrapper<>();
                cw.eq(Outbound::getCodeName, code.getCodeName()).eq(Outbound::getDeleted, 0);
                code.setTotalCount(outboundMapper.selectCount(cw).intValue());
                cw.eq(Outbound::getSubmitted, 1);
                code.setSubmittedCount(outboundMapper.selectCount(cw).intValue());
            }
        }
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void applySort(LambdaQueryWrapper<OutboundCode> qw, String sortField, String sortOrder) {
        if (!StringUtils.hasText(sortField)) {
            qw.orderByDesc(OutboundCode::getCreateTime);
            return;
        }
        SFunction<OutboundCode, ?> field = SORT_FIELD_MAP.get(sortField);
        if (field == null) {
            qw.orderByDesc(OutboundCode::getCreateTime);
            return;
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            qw.orderByAsc(field);
        } else {
            qw.orderByDesc(field);
        }
    }

    public OutboundCode getById(Long id) {
        return outboundCodeMapper.selectById(id);
    }

    public OutboundCode create(OutboundCode outboundCode) {
        if (!StringUtils.hasText(outboundCode.getCodeName())) {
            throw new RuntimeException("本次代号不能为空");
        }
        LambdaQueryWrapper<OutboundCode> qw = new LambdaQueryWrapper<>();
        qw.eq(OutboundCode::getCodeName, outboundCode.getCodeName());
        Long count = outboundCodeMapper.selectCount(qw);
        if (count != null && count > 0) {
            throw new RuntimeException("代号[" + outboundCode.getCodeName() + "]已存在");
        }
        if (!StringUtils.hasText(outboundCode.getOutboundCode())) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            outboundCode.setOutboundCode(today + "-" + outboundCode.getCodeName());
        }
        outboundCodeMapper.insert(outboundCode);
        return outboundCode;
    }

    public void update(Long id, OutboundCode outboundCode) {
        outboundCode.setId(id);
        outboundCodeMapper.updateById(outboundCode);
    }

    public void delete(Long id) {
        outboundCodeMapper.deleteById(id);
    }
}
