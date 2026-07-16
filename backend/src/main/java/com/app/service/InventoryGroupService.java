package com.app.service;

import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.entity.InventoryGroup;
import com.app.entity.Inventory;
import com.app.mapper.InventoryGroupMapper;
import com.app.mapper.InventoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InventoryGroupService {

    private static final Logger log = LoggerFactory.getLogger(InventoryGroupService.class);
    private static final Pattern CODE_PATTERN = Pattern.compile("^(\\d{6})-(\\d+)$");

    @Autowired
    private InventoryGroupMapper inventoryGroupMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    private static final Map<String, String> FIELD_COL_MAP = new LinkedHashMap<>();
    static {
        FIELD_COL_MAP.put("codeName", "code_name");
        FIELD_COL_MAP.put("companyCode", "company_code");
        FIELD_COL_MAP.put("factoryNo", "factory_no");
        FIELD_COL_MAP.put("sampleName", "sample_name");
        FIELD_COL_MAP.put("chinesePackage", "chinese_package");
        FIELD_COL_MAP.put("boothNumber", "booth_number");
        FIELD_COL_MAP.put("manufacturerName", "manufacturer_name");
        FIELD_COL_MAP.put("mobile", "mobile");
        FIELD_COL_MAP.put("telephone", "telephone");
        FIELD_COL_MAP.put("manufacturerCode", "manufacturer_code");
        FIELD_COL_MAP.put("floor", "floor");
        FIELD_COL_MAP.put("remark", "remark");
    }

    public PageResult<InventoryGroup> list(long current, long size, String keyword, String sortField, String sortOrder) {
        Page<InventoryGroup> page = new Page<>(current, size);
        LambdaQueryWrapper<InventoryGroup> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                    .like(InventoryGroup::getCodeName, keyword)
                    .or()
                    .like(InventoryGroup::getCompanyCode, keyword)
                    .or()
                    .like(InventoryGroup::getSampleName, keyword)
                    .or()
                    .like(InventoryGroup::getManufacturerName, keyword));
        }

        if (StringUtils.hasText(sortField)) {
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "codeName": qw.orderBy(true, asc, InventoryGroup::getCodeName); break;
                case "companyCode": qw.orderBy(true, asc, InventoryGroup::getCompanyCode); break;
                case "sampleName": qw.orderBy(true, asc, InventoryGroup::getSampleName); break;
                default: qw.orderByDesc(InventoryGroup::getCreateTime); break;
            }
        } else {
            qw.orderByDesc(InventoryGroup::getCreateTime);
        }

        inventoryGroupMapper.selectPage(page, qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public InventoryGroup getById(Long id) {
        return inventoryGroupMapper.selectById(id);
    }

    public InventoryGroup getByCodeName(String codeName) {
        LambdaQueryWrapper<InventoryGroup> qw = new LambdaQueryWrapper<>();
        qw.eq(InventoryGroup::getCodeName, codeName);
        return inventoryGroupMapper.selectOne(qw);
    }

    @Transactional
    public InventoryGroup create(InventoryGroup group) {
        String codeName = group.getCodeName();
        if (!StringUtils.hasText(codeName)) {
            codeName = generateNextCodeName();
        } else {
            codeName = codeName.trim();
            LambdaQueryWrapper<InventoryGroup> qw = new LambdaQueryWrapper<>();
            qw.eq(InventoryGroup::getCodeName, codeName);
            if (inventoryGroupMapper.selectCount(qw) > 0) {
                throw new RuntimeException("代号[" + codeName + "]已被使用");
            }
        }
        group.setCodeName(codeName);
        inventoryGroupMapper.insert(group);
        return group;
    }

    public void update(Long id, InventoryGroup group) {
        InventoryGroup existing = inventoryGroupMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("记录不存在");
        }
        group.setCodeName(existing.getCodeName());
        group.setId(id);
        inventoryGroupMapper.updateById(group);
    }

    public void delete(Long id) {
        InventoryGroup group = inventoryGroupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("记录不存在");
        }
        // 检查该代号下是否还有库存明细
        List<Inventory> items = inventoryMapper.selectByCodeName(group.getCodeName());
        if (items != null && !items.isEmpty()) {
            throw new RuntimeException("该代号下还有 " + items.size() + " 条库存明细，请先到详情页清空后再删除代号。");
        }
        inventoryGroupMapper.deleteById(id);
    }

    public Map<String, String> nextCode() {
        String codeName = generateNextCodeName();
        Map<String, String> result = new HashMap<>();
        result.put("codeName", codeName);
        return result;
    }

    private String generateNextCodeName() {
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = datePart + "-";

        LambdaQueryWrapper<InventoryGroup> qw = new LambdaQueryWrapper<>();
        qw.likeRight(InventoryGroup::getCodeName, prefix);
        qw.orderByDesc(InventoryGroup::getCodeName);
        qw.last("LIMIT 1");
        InventoryGroup last = inventoryGroupMapper.selectOne(qw);

        int nextNum = 100000;
        if (last != null && last.getCodeName() != null) {
            Matcher m = CODE_PATTERN.matcher(last.getCodeName().trim());
            if (m.find()) {
                nextNum = Integer.parseInt(m.group(2)) + 1;
            }
        }
        return datePart + "-" + nextNum;
    }

    @Transactional
    public ImportResult batchImport(MultipartFile file) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int totalCount = 0;
        int successCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.setTotalCount(0);
                return result;
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.setTotalCount(0);
                return result;
            }
            Map<String, Integer> colMap = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    colMap.put(cell.getStringCellValue().trim(), i);
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                totalCount++;
                try {
                    InventoryGroup group = new InventoryGroup();
                    group.setCodeName(getCellString(row, colMap.get("本次代号")));
                    group.setCompanyCode(getCellString(row, colMap.get("公司编号")));
                    group.setFactoryNo(getCellString(row, colMap.get("出厂货号")));
                    group.setSampleName(getCellString(row, colMap.get("样品名称")));
                    group.setChinesePackage(getCellString(row, colMap.get("中文包装")));
                    group.setBoothNumber(getCellString(row, colMap.get("摊位号")));
                    group.setManufacturerName(getCellString(row, colMap.get("厂商名称")));
                    group.setMobile(getCellString(row, colMap.get("手机")));
                    group.setTelephone(getCellString(row, colMap.get("电话")));
                    group.setManufacturerCode(getCellString(row, colMap.get("厂商编号")));
                    group.setFloor(getCellString(row, colMap.get("楼层")));
                    group.setRemark(getCellString(row, colMap.get("备注")));
                    group.setImage(getCellString(row, colMap.get("图片")));
                    create(group);
                    successCount++;
                } catch (Exception e) {
                    Map<String, String> fail = new LinkedHashMap<>();
                    fail.put("row", String.valueOf(i));
                    fail.put("失败原因", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    failedRows.add(fail);
                }
            }
        } catch (Exception e) {
            log.error("导入失败: {}", e.getMessage(), e);
        }

        result.setTotalCount(totalCount);
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size());
        result.setFailedRows(failedRows);
        return result;
    }

    private String getCellString(Row row, Integer colIndex) {
        if (colIndex == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    public byte[] exportToExcel(String ids, String fields) {
        try {
            List<InventoryGroup> list;
            if (StringUtils.hasText(ids)) {
                List<Long> idList = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
                list = inventoryGroupMapper.selectBatchIds(idList);
            } else {
                LambdaQueryWrapper<InventoryGroup> qw = new LambdaQueryWrapper<>();
                qw.orderByDesc(InventoryGroup::getCreateTime);
                list = inventoryGroupMapper.selectList(qw);
            }

            List<String> fieldList;
            if (StringUtils.hasText(fields)) {
                fieldList = Arrays.asList(fields.split(","));
            } else {
                fieldList = new ArrayList<>(FIELD_COL_MAP.keySet());
            }

            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("入库管理代号组");
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < fieldList.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellStyle(headerStyle);
                    cell.setCellValue(fieldList.get(i));
                }

                for (int i = 0; i < list.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    InventoryGroup item = list.get(i);
                    for (int j = 0; j < fieldList.size(); j++) {
                        String field = fieldList.get(j);
                        String colName = FIELD_COL_MAP.get(field);
                        String value = getFieldValue(item, colName);
                        row.createCell(j).setCellValue(value);
                    }
                }

                for (int i = 0; i < fieldList.size(); i++) {
                    sheet.autoSizeColumn(i);
                }
                workbook.write(bos);
                return bos.toByteArray();
            }
        } catch (Exception e) {
            log.error("导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    private String getFieldValue(InventoryGroup item, String colName) {
        if (colName == null) return "";
        return switch (colName) {
            case "code_name" -> nvl(item.getCodeName());
            case "company_code" -> nvl(item.getCompanyCode());
            case "factory_no" -> nvl(item.getFactoryNo());
            case "sample_name" -> nvl(item.getSampleName());
            case "chinese_package" -> nvl(item.getChinesePackage());
            case "booth_number" -> nvl(item.getBoothNumber());
            case "manufacturer_name" -> nvl(item.getManufacturerName());
            case "mobile" -> nvl(item.getMobile());
            case "telephone" -> nvl(item.getTelephone());
            case "manufacturer_code" -> nvl(item.getManufacturerCode());
            case "floor" -> nvl(item.getFloor());
            case "remark" -> nvl(item.getRemark());
            default -> "";
        };
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
