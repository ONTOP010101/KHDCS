package com.app.service;

import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.entity.Manufacturer;
import com.app.mapper.ManufacturerMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ManufacturerService {

    private static final Logger log = LoggerFactory.getLogger(ManufacturerService.class);

    private static final ConcurrentHashMap<String, Boolean> CACHED_EXISTING_CODES = new ConcurrentHashMap<>();
    private static volatile boolean codesLoaded = false;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    private static final Map<String, SFunction<Manufacturer, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("manufacturerCode", Manufacturer::getManufacturerCode);
        SORT_FIELD_MAP.put("name", Manufacturer::getName);
        SORT_FIELD_MAP.put("boothNo", Manufacturer::getBoothNo);
        SORT_FIELD_MAP.put("createTime", Manufacturer::getCreateTime);
    }

    public PageResult<Manufacturer> list(long current, long size, String keyword, String sortField, String sortOrder) {
        Page<Manufacturer> page = new Page<>(current, size);
        LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                .like(Manufacturer::getName, keyword)
                .or()
                .like(Manufacturer::getManufacturerCode, keyword)
                .or()
                .like(Manufacturer::getContact1, keyword));
        }

        applySort(page, qw, sortField, sortOrder);
        manufacturerMapper.selectPage(page, qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    private void applySort(Page<Manufacturer> page, LambdaQueryWrapper<Manufacturer> qw, String sortField, String sortOrder) {
        if (StringUtils.hasText(sortField)) {
            SFunction<Manufacturer, ?> sortFunc = SORT_FIELD_MAP.get(sortField);
            if (sortFunc != null) {
                boolean asc = !"desc".equalsIgnoreCase(sortOrder);
                if (asc) {
                    qw.orderByAsc(sortFunc);
                } else {
                    qw.orderByDesc(sortFunc);
                }
            }
        }
    }

    public Manufacturer getById(Long id) {
        return manufacturerMapper.selectById(id);
    }

    public Manufacturer create(Manufacturer manufacturer) {
        Long userId = UserContext.getUserId();
        manufacturer.setCreateBy(userId);
        manufacturer.setUpdateBy(userId);
        manufacturerMapper.insert(manufacturer);
        if (StringUtils.hasText(manufacturer.getManufacturerCode())) {
            CACHED_EXISTING_CODES.put(manufacturer.getManufacturerCode().trim(), Boolean.TRUE);
        }
        return manufacturer;
    }

    public void update(Long id, Manufacturer manufacturer) {
        manufacturer.setId(id);
        manufacturer.setUpdateBy(UserContext.getUserId());
        manufacturerMapper.updateById(manufacturer);
    }

    public void delete(Long id) {
        manufacturerMapper.deleteById(id);
    }

    public void deleteBatch(Long[] ids) {
        manufacturerMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Transactional
    public ImportResult batchInsert(List<Manufacturer> manufacturers, boolean updateMode) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;
        int duplicateCount = 0;
        int updatedCount = 0;

        if (manufacturers == null || manufacturers.isEmpty()) {
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setDuplicateCount(0);
            result.setUpdatedCount(0);
            result.setFailedRows(failedRows);
            return result;
        }

        ensureCodesCacheLoaded();
        Set<String> importedCodes = new HashSet<>();

        for (int i = 0; i < manufacturers.size(); i++) {
            Manufacturer m = manufacturers.get(i);
            try {
                StringBuilder rowErrors = new StringBuilder();
                boolean isDuplicate = false;

                if (!StringUtils.hasText(m.getManufacturerCode()) && !StringUtils.hasText(m.getName())) {
                    rowErrors.append("厂商编号和厂商名称均为空; ");
                }

                if (StringUtils.hasText(m.getManufacturerCode())) {
                    String code = m.getManufacturerCode().trim();
                    if (CACHED_EXISTING_CODES.containsKey(code)) {
                        if (updateMode) {
                            truncateFields(m);
                            Long userId = UserContext.getUserId();
                            m.setUpdateBy(userId);
                            LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();
                            qw.eq(Manufacturer::getManufacturerCode, code).last("LIMIT 1");
                            Manufacturer existing = manufacturerMapper.selectOne(qw);
                            if (existing != null) {
                                m.setId(existing.getId());
                                m.setCreateBy(existing.getCreateBy());
                                m.setCreateTime(existing.getCreateTime());
                                manufacturerMapper.updateById(m);
                                updatedCount++;
                                continue;
                            }
                        } else {
                            rowErrors.append("厂商编号[").append(code).append("]已存在于数据库; ");
                            isDuplicate = true;
                        }
                    } else if (importedCodes.contains(code)) {
                        rowErrors.append("厂商编号[").append(code).append("]在导入数据中重复; ");
                        isDuplicate = true;
                    }
                }

                if (rowErrors.length() > 0) {
                    Map<String, String> failRow = new LinkedHashMap<>();
                    failRow.put("row", String.valueOf(i + 1));
                    failRow.put("厂商编号", m.getManufacturerCode() != null ? m.getManufacturerCode() : "");
                    failRow.put("厂商名称", m.getName() != null ? m.getName() : "");
                    failRow.put("失败原因", rowErrors.toString());
                    failRow.put("类型", isDuplicate ? "重复" : "校验失败");
                    failedRows.add(failRow);
                    if (isDuplicate) duplicateCount++;
                    continue;
                }

                truncateFields(m);

                Long userId = UserContext.getUserId();
                m.setCreateBy(userId);
                m.setUpdateBy(userId);
                m.setId(null);
                manufacturerMapper.insert(m);

                if (StringUtils.hasText(m.getManufacturerCode())) {
                    String code = m.getManufacturerCode().trim();
                    importedCodes.add(code);
                    CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("批量导入厂商第{}条失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                failRow.put("厂商编号", m.getManufacturerCode() != null ? m.getManufacturerCode() : "");
                failRow.put("厂商名称", m.getName() != null ? m.getName() : "");
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failRow.put("类型", "异常");
                failedRows.add(failRow);
            }
        }

        result.setTotalCount(manufacturers.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size() - duplicateCount);
        result.setDuplicateCount(duplicateCount);
        result.setUpdatedCount(updatedCount);
        result.setFailedRows(failedRows);
        return result;
    }

    private void ensureCodesCacheLoaded() {
        if (codesLoaded) return;
        synchronized (ManufacturerService.class) {
            if (codesLoaded) return;
            List<Manufacturer> all = manufacturerMapper.selectList(null);
            for (Manufacturer m : all) {
                if (StringUtils.hasText(m.getManufacturerCode())) {
                    CACHED_EXISTING_CODES.put(m.getManufacturerCode().trim(), Boolean.TRUE);
                }
            }
            codesLoaded = true;
        }
    }

    private void truncateFields(Manufacturer m) {
        int maxLen;
        maxLen = 20; if (m.getQq() != null && m.getQq().length() > maxLen) { m.setQq(m.getQq().substring(0, maxLen)); }
        maxLen = 20; if (m.getMobile1() != null && m.getMobile1().length() > maxLen) { m.setMobile1(m.getMobile1().substring(0, maxLen)); }
        maxLen = 20; if (m.getMobile2() != null && m.getMobile2().length() > maxLen) { m.setMobile2(m.getMobile2().substring(0, maxLen)); }
        maxLen = 20; if (m.getMobile3() != null && m.getMobile3().length() > maxLen) { m.setMobile3(m.getMobile3().substring(0, maxLen)); }
        maxLen = 20; if (m.getPhone1() != null && m.getPhone1().length() > maxLen) { m.setPhone1(m.getPhone1().substring(0, maxLen)); }
        maxLen = 20; if (m.getPhone2() != null && m.getPhone2().length() > maxLen) { m.setPhone2(m.getPhone2().substring(0, maxLen)); }
        maxLen = 20; if (m.getPhone3() != null && m.getPhone3().length() > maxLen) { m.setPhone3(m.getPhone3().substring(0, maxLen)); }
        maxLen = 200; if (m.getAddress() != null && m.getAddress().length() > maxLen) { m.setAddress(m.getAddress().substring(0, maxLen)); }
        maxLen = 100; if (m.getRemark() != null && m.getRemark().length() > maxLen) { m.setRemark(m.getRemark().substring(0, maxLen)); }
        maxLen = 100; if (m.getOtherRemark() != null && m.getOtherRemark().length() > maxLen) { m.setOtherRemark(m.getOtherRemark().substring(0, maxLen)); }
        maxLen = 100; if (m.getCertificate() != null && m.getCertificate().length() > maxLen) { m.setCertificate(m.getCertificate().substring(0, maxLen)); }
        maxLen = 50; if (m.getSmsNumber() != null && m.getSmsNumber().length() > maxLen) { m.setSmsNumber(m.getSmsNumber().substring(0, maxLen)); }
        maxLen = 100; if (m.getName() != null && m.getName().length() > maxLen) { m.setName(m.getName().substring(0, maxLen)); }
    }
}
