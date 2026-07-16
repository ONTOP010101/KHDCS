package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.entity.Outbound;
import com.app.entity.OutboundCode;
import com.app.entity.Sample;
import com.app.entity.SampleThumbnail;
import com.app.mapper.OutboundCodeMapper;
import com.app.mapper.OutboundMapper;
import com.app.mapper.SampleMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OutboundService {

    private static final Logger log = LoggerFactory.getLogger(OutboundService.class);

    @Autowired
    private OutboundMapper outboundMapper;

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    @Autowired
    private OutboundCodeMapper outboundCodeMapper;

    private static final Map<String, SFunction<Outbound, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("outboundCode", Outbound::getOutboundCode);
        SORT_FIELD_MAP.put("codeName", Outbound::getCodeName);
        SORT_FIELD_MAP.put("createDate", Outbound::getCreateDate);
        SORT_FIELD_MAP.put("creator", Outbound::getCreator);
        SORT_FIELD_MAP.put("floor", Outbound::getFloor);
    }

    public PageResult<Outbound> list(long current, long size, String keyword, String codeName, String sortField, String sortOrder) {
        LambdaQueryWrapper<Outbound> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(codeName)) {
            qw.eq(Outbound::getCodeName, codeName);
        } else if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                    .like(Outbound::getOutboundCode, keyword)
                    .or().like(Outbound::getCodeName, keyword));
        }
        applySort(qw, sortField, sortOrder);

        Page<Outbound> page = new Page<>(current, size);
        Page<Outbound> result = outboundMapper.selectPage(page, qw);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void applySort(LambdaQueryWrapper<Outbound> qw, String sortField, String sortOrder) {
        if (!StringUtils.hasText(sortField)) {
            qw.orderByDesc(Outbound::getCreateTime);
            return;
        }
        SFunction<Outbound, ?> field = SORT_FIELD_MAP.get(sortField);
        if (field == null) {
            qw.orderByDesc(Outbound::getCreateTime);
            return;
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            qw.orderByAsc(field);
        } else {
            qw.orderByDesc(field);
        }
    }

    public Outbound getById(Long id) {
        return outboundMapper.selectById(id);
    }

    public Outbound create(Outbound outbound) {
        outboundMapper.insert(outbound);
        return outbound;
    }

    public void update(Long id, Outbound outbound) {
        outbound.setId(id);
        outboundMapper.updateById(outbound);
    }

    public void delete(Long id) {
        outboundMapper.deleteById(id);
    }

    /**
     * 批量提交 - 标记 submitted=1
     */
    public int batchSubmit(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        int count = 0;
        for (Long id : ids) {
            Outbound item = outboundMapper.selectById(id);
            if (item != null && (item.getSubmitted() == null || item.getSubmitted() == 0)) {
                item.setSubmitted(1);
                outboundMapper.updateById(item);
                count++;
            }
        }
        return count;
    }

    /**
     * 按代号组批量提交 - 将指定 codeName 下所有未提交条目标记为已提交
     */
    public int batchSubmitByCodeNames(List<String> codeNames) {
        if (codeNames == null || codeNames.isEmpty()) return 0;
        int count = 0;
        for (String codeName : codeNames) {
            List<Outbound> items = outboundMapper.selectByCodeName(codeName);
            for (Outbound item : items) {
                if (item.getSubmitted() == null || item.getSubmitted() == 0) {
                    item.setSubmitted(1);
                    outboundMapper.updateById(item);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 按公司编号查询所有出库明细记录
     */
    public List<Outbound> listByCompanyCode(String companyCode) {
        LambdaQueryWrapper<Outbound> qw = new LambdaQueryWrapper<>();
        qw.eq(Outbound::getCompanyCode, companyCode)
          .eq(Outbound::getDeleted, 0)
          .eq(Outbound::getSubmitted, 1)
          .orderByAsc(Outbound::getStockOutTime);
        return outboundMapper.selectList(qw);
    }

    // ==================== 明细操作 ====================

    public List<Outbound> listByCodeName(String codeName) {
        return outboundMapper.selectByCodeName(codeName);
    }

    public Outbound addItem(String codeName, String companyCode, String creator, String floor, boolean submitted) {
        if (!StringUtils.hasText(codeName)) {
            throw new BusinessException(400, "代号不能为空");
        }
        if (!StringUtils.hasText(companyCode)) {
            throw new BusinessException(400, "公司编号不能为空");
        }

        Sample sample = sampleMapper.findBySampleCode(companyCode);
        if (sample == null) {
            throw new BusinessException(400, "输入的公司编号不存在");
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

        Outbound item = new Outbound();
        item.setCodeName(codeName);
        item.setOutboundCode(today + "-" + codeName);
        item.setCompanyCode(companyCode);
        item.setCreateDate(now);
        item.setStockOutTime(now);
        item.setCreator(StringUtils.hasText(creator) ? creator : "");
        if (StringUtils.hasText(floor)) {
            item.setFloor(floor);
        } else {
            OutboundCode code = outboundCodeMapper.selectOne(
                new LambdaQueryWrapper<OutboundCode>().eq(OutboundCode::getCodeName, codeName)
            );
            item.setFloor(code != null && StringUtils.hasText(code.getFloor()) ? code.getFloor() : "");
        }

        item.setImage("");
        item.setFactoryNo(nvl(sample.getFactoryCode()));
        item.setSampleName(nvl(sample.getSampleName()));
        item.setChinesePackage(nvl(sample.getPackagingCn()));
        item.setBoothNumber(nvl(sample.getBoothNo()));
        item.setManufacturerName(nvl(sample.getName()));
        item.setMobile(nvl(sample.getMobile1()));
        item.setTelephone(nvl(sample.getPhone1()));
        item.setManufacturerCode(nvl(sample.getManufacturerCode()));

        // 提交状态由调用方控制：web 端暂存，手动提交
        item.setSubmitted(submitted ? 1 : 0);

        SampleThumbnail thumbnail = sampleThumbnailMapper.selectById(sample.getId());
        if (thumbnail != null && thumbnail.getImageId() != null) {
            if (thumbnail.getThumbnail() != null && !thumbnail.getThumbnail().isEmpty()) {
                item.setImage("/thumbnails/" + thumbnail.getThumbnail());
            } else {
                item.setImage("/images/thumbnail/" + thumbnail.getImageId());
            }
            item.setImageId(thumbnail.getImageId());
        }

        outboundMapper.insert(item);
        return item;
    }

    public void removeItem(Long id) {
        outboundMapper.deleteById(id);
    }

    public void removeItems(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            outboundMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 删除指定代号下的所有出库明细（用于撤销提交）
     */
    public int removeByCodeName(String codeName) {
        LambdaQueryWrapper<Outbound> qw = new LambdaQueryWrapper<>();
        qw.eq(Outbound::getCodeName, codeName);
        return outboundMapper.delete(qw);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
