package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.dto.InventorySummary;
import com.app.entity.Inventory;
import com.app.entity.InventoryCode;
import com.app.entity.Sample;
import com.app.entity.SampleThumbnail;
import com.app.mapper.InventoryCodeMapper;
import com.app.mapper.InventoryMapper;
import com.app.mapper.SampleMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
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

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    @Autowired
    private InventoryCodeMapper inventoryCodeMapper;

    private static final Map<String, SFunction<Inventory, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("inventoryCode", Inventory::getInventoryCode);
        SORT_FIELD_MAP.put("codeName", Inventory::getCodeName);
        SORT_FIELD_MAP.put("createDate", Inventory::getCreateDate);
        SORT_FIELD_MAP.put("creator", Inventory::getCreator);
        SORT_FIELD_MAP.put("floor", Inventory::getFloor);
    }

    private static final Map<String, String> FIELD_COL_MAP = new LinkedHashMap<>();
    static {
        FIELD_COL_MAP.put("inventoryCode", "inventory_code");
        FIELD_COL_MAP.put("codeName", "code_name");
        FIELD_COL_MAP.put("createDate", "create_date");
        FIELD_COL_MAP.put("creator", "creator");
        FIELD_COL_MAP.put("floor", "floor");
        FIELD_COL_MAP.put("remark", "remark");
        FIELD_COL_MAP.put("createTime", "create_time");
        FIELD_COL_MAP.put("updateTime", "update_time");
    }

    public PageResult<Inventory> list(long current, long size, String keyword, String codeName, String sortField, String sortOrder) {
        LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(codeName)) {
            qw.eq(Inventory::getCodeName, codeName);
        } else if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                    .like(Inventory::getInventoryCode, keyword)
                    .or().like(Inventory::getCodeName, keyword));
        }
        applySort(qw, sortField, sortOrder);

        Page<Inventory> page = new Page<>(current, size);
        Page<Inventory> result = inventoryMapper.selectPage(page, qw);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void applySort(LambdaQueryWrapper<Inventory> qw, String sortField, String sortOrder) {
        if (!StringUtils.hasText(sortField)) {
            qw.orderByDesc(Inventory::getCreateTime);
            return;
        }
        SFunction<Inventory, ?> field = SORT_FIELD_MAP.get(sortField);
        if (field == null) {
            qw.orderByDesc(Inventory::getCreateTime);
            return;
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            qw.orderByAsc(field);
        } else {
            qw.orderByDesc(field);
        }
    }

    public Inventory getById(Long id) {
        return inventoryMapper.selectById(id);
    }

    public Inventory create(Inventory inventory) {
        inventoryMapper.insert(inventory);
        return inventory;
    }

    public void update(Long id, Inventory inventory) {
        inventory.setId(id);
        inventoryMapper.updateById(inventory);
    }

    public void delete(Long id) {
        inventoryMapper.deleteById(id);
    }

    /**
     * 批量提交 - 标记 submitted=1
     */
    public int batchSubmit(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        int count = 0;
        for (Long id : ids) {
            Inventory item = inventoryMapper.selectById(id);
            if (item != null && (item.getSubmitted() == null || item.getSubmitted() == 0)) {
                item.setSubmitted(1);
                inventoryMapper.updateById(item);
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
            List<Inventory> items = inventoryMapper.selectByCodeName(codeName);
            for (Inventory item : items) {
                if (item.getSubmitted() == null || item.getSubmitted() == 0) {
                    item.setSubmitted(1);
                    inventoryMapper.updateById(item);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 按公司编号查询所有入库明细记录
     */
    public List<Inventory> listByCompanyCode(String companyCode) {
        LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<>();
        qw.eq(Inventory::getCompanyCode, companyCode)
          .eq(Inventory::getDeleted, 0)
          .eq(Inventory::getSubmitted, 1)
          .orderByAsc(Inventory::getStockInTime);
        return inventoryMapper.selectList(qw);
    }

    // ==================== 明细操作 ====================

    /**
     * 按代号查询所有库存明细
     */
    public List<Inventory> listByCodeName(String codeName) {
        return inventoryMapper.selectByCodeName(codeName);
    }

    /**
     * 添加库存明细项（关联到代号组），自动从 samples 表回填样品资料
     */
    public Inventory addItem(String codeName, String companyCode, String creator, String floor, boolean submitted) {
        if (!StringUtils.hasText(codeName)) {
            throw new BusinessException(400, "代号不能为空");
        }
        if (!StringUtils.hasText(companyCode)) {
            throw new BusinessException(400, "公司编号不能为空");
        }

        // 校验公司编号必须存在于 samples 表中
        Sample sample = sampleMapper.findBySampleCode(companyCode);
        if (sample == null) {
            throw new BusinessException(400, "输入的公司编号不存在");
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

        Inventory item = new Inventory();
        item.setCodeName(codeName);
        item.setInventoryCode(today + "-" + codeName);
        item.setCompanyCode(companyCode);
        item.setCreateDate(now);
        item.setStockInTime(now);
        item.setCreator(StringUtils.hasText(creator) ? creator : "");
        // 楼层：优先用传入的，否则从代号表取默认值
        if (StringUtils.hasText(floor)) {
            item.setFloor(floor);
        } else {
            InventoryCode code = inventoryCodeMapper.selectOne(
                new LambdaQueryWrapper<InventoryCode>().eq(InventoryCode::getCodeName, codeName)
            );
            log.info("addItem 查询代号楼层: codeName={}, code={}, floor={}", codeName, code != null ? "found" : "null", code != null ? code.getFloor() : "N/A");
            item.setFloor(code != null && StringUtils.hasText(code.getFloor()) ? code.getFloor() : "");
        }

        // 从 samples 表回填样品数据
        item.setImage("");
        item.setFactoryNo(nvl(sample.getFactoryCode()));
        item.setSampleName(nvl(sample.getSampleName()));
        item.setChinesePackage(nvl(sample.getPackagingCn()));
        item.setBoothNumber(nvl(sample.getBoothNo()));
        item.setManufacturerName(nvl(sample.getName()));
        item.setMobile(nvl(sample.getMobile1()));
        item.setTelephone(nvl(sample.getPhone1()));
        item.setManufacturerCode(nvl(sample.getManufacturerCode()));

        // 提交状态由调用方控制：web 端直接提交，app 端保存后由 web 端统一提交
        item.setSubmitted(submitted ? 1 : 0);

        // 查缩略图
        SampleThumbnail thumbnail = sampleThumbnailMapper.selectById(sample.getId());
        if (thumbnail != null && thumbnail.getImageId() != null) {
            // 优先用 webp 缩略图路径
            if (thumbnail.getThumbnail() != null && !thumbnail.getThumbnail().isEmpty()) {
                item.setImage("/thumbnails/" + thumbnail.getThumbnail());
            } else {
                item.setImage("/images/thumbnail/" + thumbnail.getImageId());
            }
            item.setImageId(thumbnail.getImageId());
        }

        inventoryMapper.insert(item);
        return item;
    }

    /**
     * 删除单条明细
     */
    public void removeItem(Long id) {
        inventoryMapper.deleteById(id);
    }

    /**
     * 批量删除明细
     */
    public void removeItems(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            inventoryMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 删除指定代号下的所有库存明细（用于撤销提交）
     */
    public int removeByCodeName(String codeName) {
        LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<>();
        qw.eq(Inventory::getCodeName, codeName);
        return inventoryMapper.delete(qw);
    }

    @Transactional
    public ImportResult batchImport(MultipartFile file) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.setFailCount(0);
                result.setTotalCount(0);
                result.setFailedRows(failedRows);
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
                    String header = cell.getStringCellValue().trim();
                    colMap.put(header, i);
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                totalCount++;
                try {
                    Inventory inventory = new Inventory();
                    inventory.setInventoryCode(getCellString(row, colMap.get("库存编号")));
                    inventory.setCodeName(getCellString(row, colMap.get("本次代号")));
                    inventory.setCreateDate(getCellString(row, colMap.get("创建日期")));
                    inventory.setCreator(getCellString(row, colMap.get("创建人")));
                    inventory.setFloor(getCellString(row, colMap.get("楼层")));
                    inventory.setRemark(getCellString(row, colMap.get("备注")));
                    inventoryMapper.insert(inventory);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> fail = new LinkedHashMap<>();
                    fail.put("row", String.valueOf(i));
                    fail.put("失败原因", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    failedRows.add(fail);
                    log.error("导入第{}行失败: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("导入文件处理失败: {}", e.getMessage(), e);
            Map<String, String> fail = new LinkedHashMap<>();
            fail.put("失败原因", "文件解析失败: " + e.getMessage());
            failedRows.add(fail);
            failCount = totalCount + 1;
        }

        result.setTotalCount(totalCount);
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
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
            List<Inventory> list;
            if (StringUtils.hasText(ids)) {
                List<Long> idList = Arrays.stream(ids.split(","))
                        .map(Long::parseLong)
                        .toList();
                list = inventoryMapper.selectBatchIds(idList);
            } else {
                LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<>();
                qw.orderByDesc(Inventory::getCreateTime);
                list = inventoryMapper.selectList(qw);
            }

            List<String> fieldList;
            if (StringUtils.hasText(fields)) {
                fieldList = Arrays.asList(fields.split(","));
            } else {
                fieldList = new ArrayList<>(FIELD_COL_MAP.keySet());
            }

            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("入库管理");
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
                    Inventory item = list.get(i);
                    for (int j = 0; j < fieldList.size(); j++) {
                        String field = fieldList.get(j);
                        String colName = FIELD_COL_MAP.get(field);
                        String value = "";
                        if ("inventory_code".equals(colName)) value = nvl(item.getInventoryCode());
                        else if ("code_name".equals(colName)) value = nvl(item.getCodeName());
                        else if ("create_date".equals(colName)) value = nvl(item.getCreateDate());
                        else if ("creator".equals(colName)) value = nvl(item.getCreator());
                        else if ("floor".equals(colName)) value = nvl(item.getFloor());
                        else if ("remark".equals(colName)) value = nvl(item.getRemark());
                        else if ("create_time".equals(colName)) {
                            value = item.getCreateTime() != null ?
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getCreateTime()) : "";
                        }
                        else if ("update_time".equals(colName)) {
                            value = item.getUpdateTime() != null ?
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getUpdateTime()) : "";
                        }
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
            log.error("导出Excel失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    public int repairImageUrls() {
        // 把旧记录的 localhost:8080 绝对路径全改成相对路径
        List<Inventory> list = inventoryMapper.selectList(
            new LambdaQueryWrapper<Inventory>()
                .like(Inventory::getImage, "http://localhost:8080")
        );
        int fixed = 0;
        for (Inventory item : list) {
            String img = item.getImage().replace("http://localhost:8080", "");
            item.setImage(img);
            inventoryMapper.updateById(item);
            fixed++;
        }
        return fixed;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * 按公司编号去重汇总库存（总库存视图）
     * 在展数量 = 入库次数 - 出库次数
     */
    public PageResult<InventorySummary> summary(long current, long size, String keyword, String sortField, String sortOrder,
                                                String boothNo, String mobile, String manufacturerName, String floor) {
        Page<InventorySummary> page = new Page<>(current, size);
        Page<InventorySummary> result = inventoryMapper.selectSummaryPage(page, keyword, sortField, sortOrder, boothNo, mobile, manufacturerName, floor);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
}
