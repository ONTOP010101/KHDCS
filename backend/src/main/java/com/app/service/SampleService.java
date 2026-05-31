package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.entity.Sample;
import com.app.entity.SampleThumbnail;
import com.app.mapper.SampleMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SampleService {

    private static final Logger log = LoggerFactory.getLogger(SampleService.class);

    private static final ConcurrentHashMap<String, Boolean> CACHED_EXISTING_CODES = new ConcurrentHashMap<>();
    private static volatile boolean codesLoaded = false;

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    private static final Map<String, SFunction<Sample, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("id", Sample::getId);
        SORT_FIELD_MAP.put("manufacturerCode", Sample::getManufacturerCode);
        SORT_FIELD_MAP.put("sampleCode", Sample::getSampleCode);
        SORT_FIELD_MAP.put("category", Sample::getCategory);
        SORT_FIELD_MAP.put("sampleName", Sample::getSampleName);
        SORT_FIELD_MAP.put("englishName", Sample::getEnglishName);
        SORT_FIELD_MAP.put("factoryCode", Sample::getFactoryCode);
        SORT_FIELD_MAP.put("sampleUnit", Sample::getSampleUnit);
        SORT_FIELD_MAP.put("sampleUnitEn", Sample::getSampleUnitEn);
        SORT_FIELD_MAP.put("packagingCn", Sample::getPackagingCn);
        SORT_FIELD_MAP.put("packagingEn", Sample::getPackagingEn);
        SORT_FIELD_MAP.put("factoryPrice", Sample::getFactoryPrice);
        SORT_FIELD_MAP.put("taxPrice", Sample::getTaxPrice);
        SORT_FIELD_MAP.put("sampleLength", Sample::getSampleLength);
        SORT_FIELD_MAP.put("sampleWidth", Sample::getSampleWidth);
        SORT_FIELD_MAP.put("sampleHeight", Sample::getSampleHeight);
        SORT_FIELD_MAP.put("sampleGrossWeight", Sample::getSampleGrossWeight);
        SORT_FIELD_MAP.put("sampleNetWeight", Sample::getSampleNetWeight);
        SORT_FIELD_MAP.put("cartonLength", Sample::getCartonLength);
        SORT_FIELD_MAP.put("cartonWidth", Sample::getCartonWidth);
        SORT_FIELD_MAP.put("cartonHeight", Sample::getCartonHeight);
        SORT_FIELD_MAP.put("cartonMaterialVolume", Sample::getCartonMaterialVolume);
        SORT_FIELD_MAP.put("cartonVolume", Sample::getCartonVolume);
        SORT_FIELD_MAP.put("innerBoxCount", Sample::getInnerBoxCount);
        SORT_FIELD_MAP.put("cartonCapacity", Sample::getCartonCapacity);
        SORT_FIELD_MAP.put("packingUnit", Sample::getPackingUnit);
        SORT_FIELD_MAP.put("cartonGrossWeight", Sample::getCartonGrossWeight);
        SORT_FIELD_MAP.put("cartonNetWeight", Sample::getCartonNetWeight);
        SORT_FIELD_MAP.put("packageLength", Sample::getPackageLength);
        SORT_FIELD_MAP.put("packageWidth", Sample::getPackageWidth);
        SORT_FIELD_MAP.put("packageHeight", Sample::getPackageHeight);
        SORT_FIELD_MAP.put("certification", Sample::getCertification);
        SORT_FIELD_MAP.put("certificationCount", Sample::getCertificationCount);
        SORT_FIELD_MAP.put("color", Sample::getColor);
        SORT_FIELD_MAP.put("colorEn", Sample::getColorEn);
        SORT_FIELD_MAP.put("remark", Sample::getRemark);
        SORT_FIELD_MAP.put("remarkEn", Sample::getRemarkEn);
        SORT_FIELD_MAP.put("supplier", Sample::getSupplier);
        SORT_FIELD_MAP.put("boothNo", Sample::getBoothNo);
        SORT_FIELD_MAP.put("contactPerson", Sample::getContactPerson);
        SORT_FIELD_MAP.put("contactPhone", Sample::getContactPhone);
        SORT_FIELD_MAP.put("mobile", Sample::getMobile);
        SORT_FIELD_MAP.put("fax", Sample::getFax);
        SORT_FIELD_MAP.put("qq", Sample::getQq);
        SORT_FIELD_MAP.put("registrant", Sample::getRegistrant);
        SORT_FIELD_MAP.put("createTime", Sample::getCreateTime);
        SORT_FIELD_MAP.put("modifier", Sample::getModifier);
        SORT_FIELD_MAP.put("updateTime", Sample::getUpdateTime);
        SORT_FIELD_MAP.put("infringement", Sample::getInfringement);
        SORT_FIELD_MAP.put("batteryInfo", Sample::getBatteryInfo);
    }

    private static final Map<String, String> HEADER_TO_FIELD = new LinkedHashMap<>();
    static {
        HEADER_TO_FIELD.put("厂商编号", "manufacturerCode");
        HEADER_TO_FIELD.put("公司编号", "sampleCode");
        HEADER_TO_FIELD.put("种类编号", "category");
        HEADER_TO_FIELD.put("种类名称", "category");
        HEADER_TO_FIELD.put("样品名称", "sampleName");
        HEADER_TO_FIELD.put("英文名称", "englishName");
        HEADER_TO_FIELD.put("出厂货号", "factoryCode");
        HEADER_TO_FIELD.put("样品单位", "sampleUnit");
        HEADER_TO_FIELD.put("样品英文单位", "sampleUnitEn");
        HEADER_TO_FIELD.put("中文包装", "packagingCn");
        HEADER_TO_FIELD.put("英文包装", "packagingEn");
        HEADER_TO_FIELD.put("出厂价", "factoryPrice");
        HEADER_TO_FIELD.put("价格", "factoryPrice");
        HEADER_TO_FIELD.put("税点价", "taxPrice");
        HEADER_TO_FIELD.put("样品长度", "sampleLength");
        HEADER_TO_FIELD.put("样品 长度", "sampleLength");
        HEADER_TO_FIELD.put("样品宽度", "sampleWidth");
        HEADER_TO_FIELD.put("样品高度", "sampleHeight");
        HEADER_TO_FIELD.put("样品毛重", "sampleGrossWeight");
        HEADER_TO_FIELD.put("样品净重", "sampleNetWeight");
        HEADER_TO_FIELD.put("外箱长度", "cartonLength");
        HEADER_TO_FIELD.put("外箱宽度", "cartonWidth");
        HEADER_TO_FIELD.put("外箱高度", "cartonHeight");
        HEADER_TO_FIELD.put("外箱材积", "cartonMaterialVolume");
        HEADER_TO_FIELD.put("外箱体积", "cartonVolume");
        HEADER_TO_FIELD.put("内盒个数", "innerBoxCount");
        HEADER_TO_FIELD.put("外箱装量", "cartonCapacity");
        HEADER_TO_FIELD.put("装箱单位", "packingUnit");
        HEADER_TO_FIELD.put("外箱毛重", "cartonGrossWeight");
        HEADER_TO_FIELD.put("外箱净重", "cartonNetWeight");
        HEADER_TO_FIELD.put("包装长度", "packageLength");
        HEADER_TO_FIELD.put("包装宽度", "packageWidth");
        HEADER_TO_FIELD.put("包装高度", "packageHeight");
        HEADER_TO_FIELD.put("产品认证", "certification");
        HEADER_TO_FIELD.put("认证总数", "certificationCount");
        HEADER_TO_FIELD.put("颜色", "color");
        HEADER_TO_FIELD.put("英文颜色", "colorEn");
        HEADER_TO_FIELD.put("备注", "remark");
        HEADER_TO_FIELD.put("英文备注", "remarkEn");
        HEADER_TO_FIELD.put("厂商名称", "supplier");
        HEADER_TO_FIELD.put("摊位号", "boothNo");
        HEADER_TO_FIELD.put("联系人", "contactPerson");
        HEADER_TO_FIELD.put("电话", "contactPhone");
        HEADER_TO_FIELD.put("手机", "mobile");
        HEADER_TO_FIELD.put("传真", "fax");
        HEADER_TO_FIELD.put("QQ", "qq");
        HEADER_TO_FIELD.put("登记人", "registrant");
        HEADER_TO_FIELD.put("修改人", "modifier");
        HEADER_TO_FIELD.put("侵权", "infringement");
        HEADER_TO_FIELD.put("电池信息", "batteryInfo");
    }

    private static final Set<String> DECIMAL_FIELDS = new HashSet<>(Arrays.asList(
            "factoryPrice", "taxPrice", "sampleLength", "sampleWidth", "sampleHeight",
            "sampleGrossWeight", "sampleNetWeight", "cartonLength", "cartonWidth", "cartonHeight",
            "cartonMaterialVolume", "cartonVolume", "cartonGrossWeight", "cartonNetWeight",
            "packageLength", "packageWidth", "packageHeight"
    ));

    private static final Set<String> INT_FIELDS = new HashSet<>(Arrays.asList(
            "innerBoxCount", "cartonCapacity", "certificationCount"
    ));

    public PageResult<Sample> list(long current, long size, String keyword, String category, String supplier,
                                   String manufacturerCode, String sortField, String sortOrder) {
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Sample::getSampleCode, keyword)
                    .or()
                    .like(Sample::getSampleName, keyword)
                    .or()
                    .like(Sample::getMaterial, keyword)
                    .or()
                    .like(Sample::getManufacturerCode, keyword));
        }
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            wrapper.eq(Sample::getCategory, category);
        }
        if (StringUtils.hasText(supplier)) {
            wrapper.like(Sample::getSupplier, supplier);
        }
        if (StringUtils.hasText(manufacturerCode)) {
            wrapper.eq(Sample::getManufacturerCode, manufacturerCode);
        }

        boolean asc = !"desc".equalsIgnoreCase(sortOrder);
        if ("hasThumbnail".equals(sortField)) {
            wrapper.last("ORDER BY (SELECT COUNT(1) FROM sample_thumbnail WHERE sample_id = samples.id) " + (asc ? "ASC" : "DESC"));
        } else if (StringUtils.hasText(sortField) && SORT_FIELD_MAP.containsKey(sortField)) {
            wrapper.orderBy(true, asc, SORT_FIELD_MAP.get(sortField));
        } else {
            wrapper.orderByDesc(Sample::getCreateTime);
        }

        wrapper.select(
            Sample::getId, Sample::getCreateTime, Sample::getSampleCode,
            Sample::getManufacturerCode, Sample::getSampleName,
            Sample::getEnglishName, Sample::getCategory, Sample::getFactoryCode,
            Sample::getSampleUnit, Sample::getSampleUnitEn, Sample::getPackagingCn, Sample::getPackagingEn, Sample::getPackageCode,
            Sample::getMaterial, Sample::getColor,
            Sample::getColorEn, Sample::getSize, Sample::getWeight,
            Sample::getOrigin, Sample::getSupplier, Sample::getBoothNo,
            Sample::getContactPerson, Sample::getContactPhone,
            Sample::getMobile, Sample::getFax, Sample::getQq,
            Sample::getFactoryPrice, Sample::getTaxPrice,
            Sample::getSampleLength, Sample::getSampleWidth, Sample::getSampleHeight,
            Sample::getSampleGrossWeight, Sample::getSampleNetWeight,
            Sample::getCartonLength, Sample::getCartonWidth, Sample::getCartonHeight,
            Sample::getCartonMaterialVolume, Sample::getCartonVolume,
            Sample::getInnerBoxCount, Sample::getCartonCapacity, Sample::getPackingUnit,
            Sample::getPackageLength, Sample::getPackageWidth, Sample::getPackageHeight,
            Sample::getCartonGrossWeight, Sample::getCartonNetWeight,
            Sample::getCertification, Sample::getCertificationCount,
            Sample::getDescription, Sample::getRemark, Sample::getRemarkEn,
            Sample::getRegistrant, Sample::getInfringement, Sample::getBatteryInfo
        );

        IPage<Sample> page = sampleMapper.selectPage(new Page<>(current, size), wrapper);
        PageResult<Sample> result = new PageResult<>(page.getRecords(), page.getTotal(), current, size);
        fillThumbnails(result.getRecords());
        return result;
    }

    public PageResult<Sample> advancedSearch(long current, long size, Map<String, String> params, String sortField, String sortOrder) {
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!StringUtils.hasText(value)) continue;
            SFunction<Sample, ?> getter = SORT_FIELD_MAP.get(key);
            if (getter == null) continue;
            wrapper.like(getter, value);
        }

        boolean asc = !"desc".equalsIgnoreCase(sortOrder);
        if ("hasThumbnail".equals(sortField)) {
            wrapper.last("ORDER BY (SELECT COUNT(1) FROM sample_thumbnail WHERE sample_id = samples.id) " + (asc ? "ASC" : "DESC"));
        } else if (StringUtils.hasText(sortField) && SORT_FIELD_MAP.containsKey(sortField)) {
            wrapper.orderBy(true, asc, SORT_FIELD_MAP.get(sortField));
        } else {
            wrapper.orderByDesc(Sample::getCreateTime);
        }

        wrapper.select(
            Sample::getId, Sample::getCreateTime, Sample::getUpdateTime, Sample::getDeleted,
            Sample::getSampleCode, Sample::getManufacturerCode, Sample::getSampleName,
            Sample::getEnglishName, Sample::getCategory, Sample::getFactoryCode,
            Sample::getSampleUnit, Sample::getSampleUnitEn, Sample::getPackagingCn,
            Sample::getPackagingEn, Sample::getMaterial, Sample::getColor, Sample::getColorEn,
            Sample::getSize, Sample::getWeight, Sample::getOrigin, Sample::getSupplier,
            Sample::getBoothNo, Sample::getContactPerson, Sample::getContactPhone,
            Sample::getMobile, Sample::getFax, Sample::getQq,
            Sample::getFactoryPrice, Sample::getTaxPrice,
            Sample::getSampleLength, Sample::getSampleWidth, Sample::getSampleHeight,
            Sample::getSampleGrossWeight, Sample::getSampleNetWeight,
            Sample::getCartonLength, Sample::getCartonWidth, Sample::getCartonHeight,
            Sample::getCartonMaterialVolume, Sample::getCartonVolume,
            Sample::getInnerBoxCount, Sample::getCartonCapacity,
            Sample::getPackingUnit, Sample::getCartonGrossWeight, Sample::getCartonNetWeight,
            Sample::getPackageLength, Sample::getPackageWidth, Sample::getPackageHeight,
            Sample::getCertification, Sample::getCertificationCount,
            Sample::getRemark,
            Sample::getRegistrant, Sample::getModifier, Sample::getStatus,
            Sample::getInfringement, Sample::getBatteryInfo,
            Sample::getCreateBy, Sample::getUpdateBy
        );

        IPage<Sample> page = sampleMapper.selectPage(new Page<>(current, size), wrapper);
        PageResult<Sample> result = new PageResult<>(page.getRecords(), page.getTotal(), current, size);
        fillThumbnails(result.getRecords());
        return result;
    }

    public Sample getById(Long id) {
        Sample sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw new BusinessException(404, "样品不存在");
        }
        return sample;
    }

    @Transactional
    public Sample create(Sample sample) {
        Long userId = UserContext.getUserId();
        sample.setCreateBy(userId);
        sample.setUpdateBy(userId);
        sampleMapper.insert(sample);
        return sample;
    }

    @Transactional
    public void update(Long id, Sample sample) {
        Sample existing = sampleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "样品不存在");
        }
        sample.setId(id);
        sample.setUpdateBy(UserContext.getUserId());
        sampleMapper.updateById(sample);
    }

    @Transactional
    public void delete(Long id) {
        Sample sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw new BusinessException(404, "样品不存在");
        }
        sampleMapper.deleteById(id);
    }

    @Transactional
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            sampleMapper.deleteById(id);
        }
    }

    public java.util.List<Sample> matchByCodes(String type, java.util.List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        if ("factoryCode".equals(type)) {
            wrapper.in(Sample::getFactoryCode, codes);
        } else {
            wrapper.in(Sample::getSampleCode, codes);
        }
        wrapper.select(Sample::getId, Sample::getSampleCode, Sample::getFactoryCode, Sample::getSampleName, Sample::getManufacturerCode);
        return sampleMapper.selectList(wrapper);
    }

    @Transactional
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx 或 .xls 格式文件");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            throw new BusinessException(400, "Excel文件无表头行");
        }

        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            String val = getCellStringValue(cell).trim();
            headers.add(val);
        }

        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;
        int totalDataRows = 0;

        Set<String> existingCodes = new HashSet<>();
        List<Sample> existingSamples = sampleMapper.selectList(
                new LambdaQueryWrapper<Sample>().select(Sample::getSampleCode).isNotNull(Sample::getSampleCode));
        for (Sample s : existingSamples) {
            if (s.getSampleCode() != null) {
                existingCodes.add(s.getSampleCode().trim());
            }
        }

        Set<String> importedCodes = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            boolean isEmptyRow = true;
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && StringUtils.hasText(getCellStringValue(cell).trim())) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) continue;

            totalDataRows++;

            try {
                Sample sample = new Sample();
                StringBuilder rowErrors = new StringBuilder();

                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    if (!StringUtils.hasText(header)) continue;

                    Cell cell = row.getCell(j);
                    String cellValue = getCellStringValue(cell).trim();
                    if (!StringUtils.hasText(cellValue)) continue;

                    String fieldName = HEADER_TO_FIELD.get(header);
                    if (fieldName == null) continue;

                    try {
                        setFieldValue(sample, fieldName, cellValue);
                    } catch (NumberFormatException e) {
                        rowErrors.append(header).append("格式错误; ");
                    } catch (Exception e) {
                        rowErrors.append(header).append("赋值失败; ");
                    }
                }

                if (!StringUtils.hasText(sample.getSampleCode()) && !StringUtils.hasText(sample.getSampleName())) {
                    rowErrors.append("公司编号和样品名称均为空; ");
                }

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    if (existingCodes.contains(code)) {
                        rowErrors.append("公司编号[").append(code).append("]已存在于数据库; ");
                    } else if (importedCodes.contains(code)) {
                        rowErrors.append("公司编号[").append(code).append("]在导入文件中重复; ");
                    }
                }

                if (rowErrors.length() > 0) {
                    Map<String, String> failRow = new LinkedHashMap<>();
                    failRow.put("row", String.valueOf(i + 1));
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        failRow.put(headers.get(j), getCellStringValue(cell).trim());
                    }
                    failRow.put("失败原因", rowErrors.toString());
                    failedRows.add(failRow);
                    continue;
                }

                Long userId = UserContext.getUserId();
                sample.setCreateBy(userId);
                sample.setUpdateBy(userId);

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    Sample softDeleted = sampleMapper.findByCodeIncludeDeleted(code);
                    if (softDeleted != null) {
                        sample.setId(softDeleted.getId());
                        sample.setCreateBy(softDeleted.getCreateBy());
                        sample.setCreateTime(softDeleted.getCreateTime());
                        sample.setUpdateBy(userId);
                        sample.setDeleted(0);
                        sampleMapper.updateById(sample);
                        importedCodes.add(code);
                        existingCodes.add(code);
                        CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                        successCount++;
                        continue;
                    }
                }

                sampleMapper.insert(sample);
                if (StringUtils.hasText(sample.getSampleCode())) {
                    importedCodes.add(sample.getSampleCode().trim());
                }
                successCount++;
            } catch (Exception e) {
                log.warn("导入第{}行失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    failRow.put(headers.get(j), getCellStringValue(cell).trim());
                }
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failedRows.add(failRow);
            }
        }

        workbook.close();

        result.setTotalCount(totalDataRows);
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size());
        result.setFailedRows(failedRows);
        return result;
    }

    public ImportResult batchInsert(List<Sample> samples, boolean updateMode) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;
        int duplicateCount = 0;
        int updatedCount = 0;

        if (samples == null || samples.isEmpty()) {
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

        for (int i = 0; i < samples.size(); i++) {
            Sample sample = samples.get(i);
            try {
                StringBuilder rowErrors = new StringBuilder();
                boolean isDuplicate = false;

                if (!StringUtils.hasText(sample.getSampleCode()) && !StringUtils.hasText(sample.getSampleName())) {
                    rowErrors.append("公司编号和样品名称均为空; ");
                }

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    if (CACHED_EXISTING_CODES.containsKey(code)) {
                        if (updateMode) {
                            truncateFields(sample, i, failedRows);
                            Long userId = UserContext.getUserId();
                            sample.setUpdateBy(userId);
                            LambdaQueryWrapper<Sample> qw = new LambdaQueryWrapper<>();
                            qw.eq(Sample::getSampleCode, code).last("LIMIT 1");
                            Sample existing = sampleMapper.selectOne(qw);
                            if (existing != null) {
                                sample.setId(existing.getId());
                                sample.setCreateBy(existing.getCreateBy());
                                sample.setCreateTime(existing.getCreateTime());
                                sampleMapper.updateById(sample);
                                updatedCount++;
                                continue;
                            }
                            CACHED_EXISTING_CODES.remove(code);
                        } else {
                            rowErrors.append("公司编号[").append(code).append("]已存在于数据库; ");
                            isDuplicate = true;
                        }
                    } else if (importedCodes.contains(code)) {
                        rowErrors.append("公司编号[").append(code).append("]在导入数据中重复; ");
                        isDuplicate = true;
                    }
                }

                if (rowErrors.length() > 0) {
                    Map<String, String> failRow = new LinkedHashMap<>();
                    failRow.put("row", String.valueOf(i + 1));
                    failRow.put("公司编号", sample.getSampleCode() != null ? sample.getSampleCode() : "");
                    failRow.put("样品名称", sample.getSampleName() != null ? sample.getSampleName() : "");
                    failRow.put("失败原因", rowErrors.toString());
                    failRow.put("类型", isDuplicate ? "重复" : "校验失败");
                    failedRows.add(failRow);
                    if (isDuplicate) duplicateCount++;
                    continue;
                }

                truncateFields(sample, i, failedRows);

                Long userId = UserContext.getUserId();
                sample.setCreateBy(userId);
                sample.setUpdateBy(userId);
                sample.setId(null);

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    Sample softDeleted = sampleMapper.findByCodeIncludeDeleted(code);
                    if (softDeleted != null) {
                        sample.setId(softDeleted.getId());
                        sample.setCreateBy(softDeleted.getCreateBy());
                        sample.setCreateTime(softDeleted.getCreateTime());
                        sample.setUpdateBy(userId);
                        sample.setDeleted(0);
                        sampleMapper.updateById(sample);
                        importedCodes.add(code);
                        CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                        successCount++;
                        continue;
                    }
                }

                sampleMapper.insert(sample);

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    importedCodes.add(code);
                    CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("批量导入第{}条失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                failRow.put("公司编号", sample.getSampleCode() != null ? sample.getSampleCode() : "");
                failRow.put("样品名称", sample.getSampleName() != null ? sample.getSampleName() : "");
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failRow.put("类型", "异常");
                failedRows.add(failRow);
            }
        }

        result.setTotalCount(samples.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size() - duplicateCount);
        result.setDuplicateCount(duplicateCount);
        result.setUpdatedCount(updatedCount);
        result.setFailedRows(failedRows);
        return result;
    }

    private synchronized void ensureCodesCacheLoaded() {
        if (codesLoaded) return;
        List<Sample> all = sampleMapper.selectList(
                new LambdaQueryWrapper<Sample>().select(Sample::getSampleCode).isNotNull(Sample::getSampleCode));
        for (Sample s : all) {
            if (s.getSampleCode() != null) {
                CACHED_EXISTING_CODES.put(s.getSampleCode().trim(), Boolean.TRUE);
            }
        }
        List<String> deletedCodes = sampleMapper.findDeletedCodes();
        if (deletedCodes != null) {
            for (String code : deletedCodes) {
                if (code != null) {
                    CACHED_EXISTING_CODES.put(code.trim(), Boolean.TRUE);
                }
            }
        }
        codesLoaded = true;
    }

    private void truncateFields(Sample sample, int rowIndex, List<Map<String, String>> failedRows) {
        int maxLen;
        maxLen = 20; if (sample.getQq() != null && sample.getQq().length() > maxLen) { sample.setQq(sample.getQq().substring(0, maxLen)); }
        maxLen = 20; if (sample.getMobile() != null && sample.getMobile().length() > maxLen) { sample.setMobile(sample.getMobile().substring(0, maxLen)); }
        maxLen = 20; if (sample.getContactPhone() != null && sample.getContactPhone().length() > maxLen) { sample.setContactPhone(sample.getContactPhone().substring(0, maxLen)); }
        maxLen = 20; if (sample.getFax() != null && sample.getFax().length() > maxLen) { sample.setFax(sample.getFax().substring(0, maxLen)); }
        maxLen = 20; if (sample.getSampleUnit() != null && sample.getSampleUnit().length() > maxLen) { sample.setSampleUnit(sample.getSampleUnit().substring(0, maxLen)); }
        maxLen = 20; if (sample.getPackingUnit() != null && sample.getPackingUnit().length() > maxLen) { sample.setPackingUnit(sample.getPackingUnit().substring(0, maxLen)); }
        maxLen = 50; if (sample.getSampleCode() != null && sample.getSampleCode().length() > maxLen) { sample.setSampleCode(sample.getSampleCode().substring(0, maxLen)); }
        maxLen = 50; if (sample.getManufacturerCode() != null && sample.getManufacturerCode().length() > maxLen) { sample.setManufacturerCode(sample.getManufacturerCode().substring(0, maxLen)); }
        maxLen = 50; if (sample.getFactoryCode() != null && sample.getFactoryCode().length() > maxLen) { sample.setFactoryCode(sample.getFactoryCode().substring(0, maxLen)); }
        maxLen = 50; if (sample.getBoothNo() != null && sample.getBoothNo().length() > maxLen) { sample.setBoothNo(sample.getBoothNo().substring(0, maxLen)); }
        maxLen = 50; if (sample.getRegistrant() != null && sample.getRegistrant().length() > maxLen) { sample.setRegistrant(sample.getRegistrant().substring(0, maxLen)); }
        maxLen = 50; if (sample.getModifier() != null && sample.getModifier().length() > maxLen) { sample.setModifier(sample.getModifier().substring(0, maxLen)); }
        maxLen = 50; if (sample.getSampleUnitEn() != null && sample.getSampleUnitEn().length() > maxLen) { sample.setSampleUnitEn(sample.getSampleUnitEn().substring(0, maxLen)); }
        maxLen = 50; if (sample.getContactPerson() != null && sample.getContactPerson().length() > maxLen) { sample.setContactPerson(sample.getContactPerson().substring(0, maxLen)); }
        maxLen = 50; if (sample.getColor() != null && sample.getColor().length() > maxLen) { sample.setColor(sample.getColor().substring(0, maxLen)); }
        maxLen = 50; if (sample.getWeight() != null && sample.getWeight().length() > maxLen) { sample.setWeight(sample.getWeight().substring(0, maxLen)); }
        maxLen = 100; if (sample.getCategory() != null && sample.getCategory().length() > maxLen) { sample.setCategory(sample.getCategory().substring(0, maxLen)); }
        maxLen = 100; if (sample.getMaterial() != null && sample.getMaterial().length() > maxLen) { sample.setMaterial(sample.getMaterial().substring(0, maxLen)); }
        maxLen = 100; if (sample.getSize() != null && sample.getSize().length() > maxLen) { sample.setSize(sample.getSize().substring(0, maxLen)); }
        maxLen = 100; if (sample.getOrigin() != null && sample.getOrigin().length() > maxLen) { sample.setOrigin(sample.getOrigin().substring(0, maxLen)); }
        maxLen = 100; if (sample.getPackagingCn() != null && sample.getPackagingCn().length() > maxLen) { sample.setPackagingCn(sample.getPackagingCn().substring(0, maxLen)); }
        maxLen = 100; if (sample.getPackagingEn() != null && sample.getPackagingEn().length() > maxLen) { sample.setPackagingEn(sample.getPackagingEn().substring(0, maxLen)); }
        maxLen = 50; if (sample.getPackageCode() != null && sample.getPackageCode().length() > maxLen) { sample.setPackageCode(sample.getPackageCode().substring(0, maxLen)); }
        maxLen = 100; if (sample.getColorEn() != null && sample.getColorEn().length() > maxLen) { sample.setColorEn(sample.getColorEn().substring(0, maxLen)); }
        maxLen = 100; if (sample.getInfringement() != null && sample.getInfringement().length() > maxLen) { sample.setInfringement(sample.getInfringement().substring(0, maxLen)); }
        maxLen = 200; if (sample.getSampleName() != null && sample.getSampleName().length() > maxLen) { sample.setSampleName(sample.getSampleName().substring(0, maxLen)); }
        maxLen = 200; if (sample.getEnglishName() != null && sample.getEnglishName().length() > maxLen) { sample.setEnglishName(sample.getEnglishName().substring(0, maxLen)); }
        maxLen = 200; if (sample.getSupplier() != null && sample.getSupplier().length() > maxLen) { sample.setSupplier(sample.getSupplier().substring(0, maxLen)); }
        maxLen = 200; if (sample.getCertification() != null && sample.getCertification().length() > maxLen) { sample.setCertification(sample.getCertification().substring(0, maxLen)); }
        maxLen = 200; if (sample.getBatteryInfo() != null && sample.getBatteryInfo().length() > maxLen) { sample.setBatteryInfo(sample.getBatteryInfo().substring(0, maxLen)); }
    }

    private void setFieldValue(Sample sample, String fieldName, String value) throws Exception {
        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Class<?> paramType;

        if (DECIMAL_FIELDS.contains(fieldName)) {
            paramType = BigDecimal.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, new BigDecimal(value));
        } else if (INT_FIELDS.contains(fieldName)) {
            paramType = Integer.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, Integer.valueOf(value));
        } else {
            paramType = String.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, value);
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private void fillThumbnails(List<Sample> samples) {
        if (samples == null || samples.isEmpty()) return;
        List<Long> ids = new ArrayList<>();
        for (Sample s : samples) {
            if (s.getId() != null) ids.add(s.getId());
        }
        if (ids.isEmpty()) return;
        List<SampleThumbnail> thumbnails = sampleThumbnailMapper.selectBatchIds(ids);
        Map<Long, SampleThumbnail> map = new HashMap<>();
        for (SampleThumbnail t : thumbnails) {
            map.put(t.getSampleId(), t);
        }
        for (Sample s : samples) {
            SampleThumbnail t = map.get(s.getId());
            if (t != null) {
                s.setThumbnail(t.getThumbnail());
                s.setFirstImageId(t.getImageId());
            }
        }
    }

    public PageResult<Sample> listDeleted(int current, int size) {
        IPage<Sample> page = sampleMapper.selectDeleted(new Page<>(current, size));
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public int restoreDeleted(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        String joined = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return sampleMapper.restoreByIds(joined);
    }

}
