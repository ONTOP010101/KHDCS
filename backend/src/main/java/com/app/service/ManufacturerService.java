package com.app.service;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.dto.SearchCondition;
import com.app.entity.Manufacturer;
import com.app.mapper.ManufacturerMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ManufacturerService {

    private static final Logger log = LoggerFactory.getLogger(ManufacturerService.class);

    private static final ConcurrentHashMap<String, Boolean> CACHED_EXISTING_CODES = new ConcurrentHashMap<>();
    private static volatile boolean codesLoaded = false;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private WeworkService weworkService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.upload.image-path}")
    private String imagePath;

    private static final Map<String, SFunction<Manufacturer, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("manufacturerCode", Manufacturer::getManufacturerCode);
        SORT_FIELD_MAP.put("name", Manufacturer::getName);
        SORT_FIELD_MAP.put("boothNo", Manufacturer::getBoothNo);
        SORT_FIELD_MAP.put("createTime", Manufacturer::getCreateTime);
    }

    private static final Map<String, String> FIELD_COL_MAP = new LinkedHashMap<>();
    static {
        FIELD_COL_MAP.put("manufacturerCode", "manufacturer_code");
        FIELD_COL_MAP.put("name", "name");
        FIELD_COL_MAP.put("boothNo", "booth_no");
        FIELD_COL_MAP.put("boothType", "booth_type");
        FIELD_COL_MAP.put("contact1", "contact1");
        FIELD_COL_MAP.put("phone1", "phone1");
        FIELD_COL_MAP.put("mobile1", "mobile1");
        FIELD_COL_MAP.put("mainCard", "main_card");
        FIELD_COL_MAP.put("subCard", "sub_card");
        FIELD_COL_MAP.put("remark", "remark");
        FIELD_COL_MAP.put("address", "address");
        FIELD_COL_MAP.put("certificate", "certificate");
        FIELD_COL_MAP.put("registrant", "registrant");
        FIELD_COL_MAP.put("modifier", "modifier");
        FIELD_COL_MAP.put("television", "television");
        FIELD_COL_MAP.put("createTime", "create_time");
        FIELD_COL_MAP.put("updateTime", "update_time");
        FIELD_COL_MAP.put("expiryDate", "expiry_date");
    }

    public PageResult<Manufacturer> list(long current, long size, String keyword, String sortField, String sortOrder,
            String manufacturerCode, String boothNo, String boothType, String name, String contact1,
            String phone1, String mobile1, String fax, String email, String boothManager,
            String mainCard, String subCard, String remark, String summary, String address,
            String certificate, String registrant, String modifier,
            String createDateStart, String createDateEnd, String updateDateStart, String updateDateEnd,
            String expiryDateStart, String expiryDateEnd, String television) {
        Page<Manufacturer> page = new Page<>(current, size);
        LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                .like(Manufacturer::getName, keyword)
                .or()
                .like(Manufacturer::getManufacturerCode, keyword)
                .or()
                .like(Manufacturer::getBoothNo, keyword)
                .or()
                .like(Manufacturer::getMobile1, keyword)
                .or()
                .like(Manufacturer::getSmsNumber, keyword)
                .or()
                .like(Manufacturer::getContact1, keyword));
        }

        // 综合查询 - 精确字段模糊匹配（至少2个字符才生效）
        if (StringUtils.hasText(manufacturerCode) && manufacturerCode.trim().length() >= 2) qw.like(Manufacturer::getManufacturerCode, manufacturerCode.trim());
        if (StringUtils.hasText(boothNo) && boothNo.trim().length() >= 2) qw.like(Manufacturer::getBoothNo, boothNo.trim());
        if (StringUtils.hasText(boothType) && boothType.trim().length() >= 2) qw.like(Manufacturer::getBoothType, boothType.trim());
        if (StringUtils.hasText(name) && name.trim().length() >= 2) qw.like(Manufacturer::getName, name.trim());
        if (StringUtils.hasText(contact1) && contact1.trim().length() >= 2) qw.like(Manufacturer::getContact1, contact1.trim());
        if (StringUtils.hasText(phone1) && phone1.trim().length() >= 2) qw.like(Manufacturer::getPhone1, phone1.trim());
        if (StringUtils.hasText(mobile1) && mobile1.trim().length() >= 2) qw.like(Manufacturer::getMobile1, mobile1.trim());
        if (StringUtils.hasText(mainCard) && mainCard.trim().length() >= 2) qw.like(Manufacturer::getMainCard, mainCard.trim());
        if (StringUtils.hasText(subCard) && subCard.trim().length() >= 2) qw.like(Manufacturer::getSubCard, subCard.trim());
        if (StringUtils.hasText(remark) && remark.trim().length() >= 2) qw.like(Manufacturer::getRemark, remark.trim());
        if (StringUtils.hasText(address) && address.trim().length() >= 2) qw.like(Manufacturer::getAddress, address.trim());
        if (StringUtils.hasText(certificate) && certificate.trim().length() >= 2) qw.like(Manufacturer::getCertificate, certificate.trim());
        if (StringUtils.hasText(registrant) && registrant.trim().length() >= 2) qw.like(Manufacturer::getRegistrant, registrant.trim());
        if (StringUtils.hasText(modifier) && modifier.trim().length() >= 2) qw.like(Manufacturer::getModifier, modifier.trim());

        // 日期范围查询
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (StringUtils.hasText(createDateStart)) {
            LocalDateTime start = LocalDate.parse(createDateStart, dtf).atStartOfDay();
            qw.ge(Manufacturer::getCreateTime, start);
        }
        if (StringUtils.hasText(createDateEnd)) {
            LocalDateTime end = LocalDate.parse(createDateEnd, dtf).atTime(LocalTime.MAX);
            qw.le(Manufacturer::getCreateTime, end);
        }
        if (StringUtils.hasText(updateDateStart)) {
            LocalDateTime start = LocalDate.parse(updateDateStart, dtf).atStartOfDay();
            qw.ge(Manufacturer::getUpdateTime, start);
        }
        if (StringUtils.hasText(updateDateEnd)) {
            LocalDateTime end = LocalDate.parse(updateDateEnd, dtf).atTime(LocalTime.MAX);
            qw.le(Manufacturer::getUpdateTime, end);
        }
        if (StringUtils.hasText(expiryDateStart)) {
            qw.ge(Manufacturer::getExpiryDate, expiryDateStart);
        }
        if (StringUtils.hasText(expiryDateEnd)) {
            qw.le(Manufacturer::getExpiryDate, expiryDateEnd);
        }

        applySort(page, qw, sortField, sortOrder);
        manufacturerMapper.selectPage(page, qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public PageResult<Manufacturer> advancedSearch(long current, long size,
            List<SearchCondition> conditions, String sortField, String sortOrder) {
        if (conditions == null || conditions.isEmpty()) {
            return list(current, size, null, sortField, sortOrder,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
        // 将 conditions 转为独立参数，复用 list 方法（MyBatis-Plus 处理中文编码正确）
        String manufacturerCode = null, boothNo = null, boothType = null, name = null, contact1 = null;
        String phone1 = null, mobile1 = null, mainCard = null, subCard = null, remark = null;
        String address = null, certificate = null, registrant = null, modifier = null, television = null;
        String createDateStart = null, createDateEnd = null, updateDateStart = null, updateDateEnd = null;
        String expiryDateStart = null, expiryDateEnd = null;

        for (SearchCondition c : conditions) {
            if (!c.isValid()) continue;
            String val = c.getValue();
            String op = c.getOperator();
            switch (c.getField()) {
                case "manufacturerCode": if ("like".equals(op)) manufacturerCode = val; break;
                case "name": if ("like".equals(op)) name = val; break;
                case "boothNo": if ("like".equals(op)) boothNo = val; break;
                case "boothType": if ("like".equals(op)) boothType = val; break;
                case "contact1": if ("like".equals(op)) contact1 = val; break;
                case "phone1": if ("like".equals(op)) phone1 = val; break;
                case "mobile1": if ("like".equals(op)) mobile1 = val; break;
                case "mainCard": if ("like".equals(op)) mainCard = val; break;
                case "subCard": if ("like".equals(op)) subCard = val; break;
                case "remark": if ("like".equals(op)) remark = val; break;
                case "address": if ("like".equals(op)) address = val; break;
                case "certificate": if ("like".equals(op)) certificate = val; break;
                case "registrant": if ("like".equals(op)) registrant = val; break;
                case "modifier": if ("like".equals(op)) modifier = val; break;
                case "television": if ("like".equals(op)) television = val; break;
                case "createTime":
                    if ("ge".equals(op)) createDateStart = val;
                    else if ("le".equals(op)) createDateEnd = val;
                    break;
                case "updateTime":
                    if ("ge".equals(op)) updateDateStart = val;
                    else if ("le".equals(op)) updateDateEnd = val;
                    break;
                case "expiryDate":
                    if ("ge".equals(op)) expiryDateStart = val;
                    else if ("le".equals(op)) expiryDateEnd = val;
                    break;
            }
        }
        return list(current, size, null, sortField, sortOrder,
                manufacturerCode, boothNo, boothType, name, contact1, phone1, mobile1,
                null, null, null, mainCard, subCard, remark, null, address,
                certificate, registrant, modifier,
                createDateStart, createDateEnd, updateDateStart, updateDateEnd,
                expiryDateStart, expiryDateEnd, television);
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

    public List<String> getCodesWithSamples() {
        return jdbcTemplate.queryForList(
            "SELECT DISTINCT manufacturer_code FROM samples WHERE manufacturer_code IS NOT NULL AND manufacturer_code != '' AND deleted = 0",
            String.class);
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

        // 先查出现有记录以获取 manufacturerCode（用于级联同步 samples 表）和手机号快照（用于同步 wework 绑定）
        Manufacturer existing = manufacturerMapper.selectById(id);

        manufacturerMapper.updateById(manufacturer);

        // 级联同步冗余字段到 samples 表（保持样品中的厂商联系信息与厂商表一致）
        if (existing != null && StringUtils.hasText(existing.getManufacturerCode())) {
            syncManufacturerFieldsToSamples(existing.getManufacturerCode(), manufacturer);
        }

        // 同步 wework 绑定（手机号变更时自动解绑、超限清理）
        if (existing != null) {
            List<String> warnings = weworkService.syncBindingAfterUpdate(id, existing, manufacturer);
            if (!warnings.isEmpty()) {
                log.warn("厂商 [{}] 更新后 wework 绑定同步警告: {}", existing.getManufacturerCode(), warnings);
            }
        }
    }

    private void syncManufacturerFieldsToSamples(String manufacturerCode, Manufacturer m) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE samples SET ");

        if (m.getName() != null) { sql.append("name = ?, "); params.add(m.getName()); }
        if (m.getBoothNo() != null) { sql.append("booth_no = ?, "); params.add(m.getBoothNo()); }
        if (m.getPhone1() != null) { sql.append("phone1 = ?, "); params.add(m.getPhone1()); }
        if (m.getMobile1() != null) { sql.append("mobile1 = ?, "); params.add(m.getMobile1()); }
        if (m.getContact1() != null) { sql.append("contact1 = ?, "); params.add(m.getContact1()); }
        if (m.getSmsNumber() != null) { sql.append("sms_number = ?, "); params.add(m.getSmsNumber()); }
        if (m.getVisitorMobile() != null) { sql.append("visitor_mobile = ?, "); params.add(m.getVisitorMobile()); }

        if (params.isEmpty()) return;

        // 去掉末尾多余的 ", "
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE manufacturer_code = ? AND deleted = 0");
        params.add(manufacturerCode);

        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    public void delete(Long id) {
        // 先清理 wework 绑定记录，避免孤儿数据
        weworkService.deleteAllBindingsForManufacturer(id);
        manufacturerMapper.deleteById(id);
    }

    public void deleteBatch(Long[] ids) {
        // 先清理 wework 绑定记录
        for (Long id : ids) {
            weworkService.deleteAllBindingsForManufacturer(id);
        }
        manufacturerMapper.deleteBatchIds(Arrays.asList(ids));
    }

    public Result<Map<String, String>> uploadCertificate(Long id, MultipartFile file) {
        try {
            Manufacturer manufacturer = manufacturerMapper.selectById(id);
            if (manufacturer == null) {
                return Result.error(404, "厂商不存在");
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "certificate_" + id + "_" + System.currentTimeMillis() + ext;
            Path dir = Paths.get(imagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path filePath = dir.resolve(filename);
            file.transferTo(filePath.toFile());

            // Delete old certificate file if exists
            String oldCertificate = manufacturer.getCertificate();
            if (StringUtils.hasText(oldCertificate)) {
                try {
                    Path oldPath = Paths.get(imagePath, oldCertificate);
                    Files.deleteIfExists(oldPath);
                } catch (Exception ignored) {}
            }

            manufacturer.setCertificate(filename);
            manufacturer.setUpdateBy(UserContext.getUserId());
            manufacturerMapper.updateById(manufacturer);

            Map<String, String> result = new LinkedHashMap<>();
            result.put("filePath", filename);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("上传营业执照失败: {}", e.getMessage(), e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
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
                                // 同步 wework 绑定（手机号变更时自动解绑、超限清理）
                                weworkService.syncBindingAfterUpdate(existing.getId(), existing, m);
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
        maxLen = 20; if (m.getContact3() != null && m.getContact3().length() > maxLen) { m.setContact3(m.getContact3().substring(0, maxLen)); }
        maxLen = 50; if (m.getTelevision() != null && m.getTelevision().length() > maxLen) { m.setTelevision(m.getTelevision().substring(0, maxLen)); }
        maxLen = 20; if (m.getCanInvoice() != null && m.getCanInvoice().length() > maxLen) { m.setCanInvoice(m.getCanInvoice().substring(0, maxLen)); }
    }
}
