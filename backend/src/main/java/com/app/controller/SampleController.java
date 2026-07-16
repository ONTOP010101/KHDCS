package com.app.controller;

import com.app.cache.VendorConfirmCache;
import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.entity.Sample;
import com.app.service.SampleService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/samples")
public class SampleController {

    private static final Logger log = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private SampleService sampleService;

    @Autowired
    private VendorConfirmCache vendorConfirmCache;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailDirPath;

    @GetMapping
    public Result<PageResult<Sample>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String manufacturerCode,
            @RequestParam(required = false) String sampleCode,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(sampleService.list(current, size, keyword, category, name, manufacturerCode, sampleCode, sortField, sortOrder));
    }

    @PostMapping("/search")
    public Result<PageResult<Sample>> advancedSearch(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "50") long size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestBody java.util.Map<String, Object> body) {
        // 从 body 中提取 conditions
        @SuppressWarnings("unchecked")
        var rawConditions = (java.util.List<java.util.Map<String, Object>>) body.get("conditions");
        java.util.List<com.app.dto.SearchCondition> conditions = new java.util.ArrayList<>();
        if (rawConditions != null) {
            for (var raw : rawConditions) {
                var sc = new com.app.dto.SearchCondition();
                sc.setField((String) raw.get("field"));
                sc.setOperator((String) raw.get("operator"));
                sc.setValue(raw.get("value") != null ? String.valueOf(raw.get("value")) : null);
                conditions.add(sc);
            }
        }
        String logic = (String) body.get("logic");
        return Result.success(sampleService.advancedSearch(current, size,
            conditions, sortField, sortOrder, logic));
    }

    @GetMapping("/{id}")
    public Result<Sample> getById(@PathVariable Long id) {
        return Result.success(sampleService.getById(id));
    }

    @PostMapping
    public Result<Sample> create(@RequestBody Sample sample) {
        return Result.success("创建成功", sampleService.create(sample));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Sample sample) {
        sampleService.update(id, sample);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sampleService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody Long[] ids) {
        sampleService.deleteBatch(ids);
        return Result.ok("批量删除成功");
    }

    @PostMapping("/match-by-codes")
    public Result<java.util.List<Sample>> matchByCodes(@RequestBody java.util.Map<String, Object> body) {
        String type = (String) body.get("type");
        String manufacturerCode = (String) body.get("manufacturerCode");
        @SuppressWarnings("unchecked")
        java.util.List<String> codes = (java.util.List<String>) body.get("codes");
        return Result.success(sampleService.matchByCodes(type, codes, manufacturerCode));
    }

    @PostMapping("/import")
    public Result<ImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = sampleService.importFromExcel(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入Excel失败: {}", e.getMessage(), e);
            return Result.error(500, "导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/batch-import")
    public Result<ImportResult> batchImport(@RequestBody List<Sample> samples,
                                            @RequestParam(defaultValue = "false") boolean updateMode) {
        try {
            ImportResult result = sampleService.batchInsert(samples, updateMode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=template.csv");
        String[] headers = {"公司编号","出厂货号","厂商编号","种类名称","样品名称","英文名称",
            "出厂价","报出价","包装规格","包装规格(英)","包装单位","内盒数","装箱量",
            "外箱长","外箱宽","外箱高","外箱毛重","外箱净重",
            "产品长","产品宽","产品高","产品毛重","产品净重",
            "体积","材积","摊位号","厂商名称","联系人","联系电话","手机","传真","QQ",
            "颜色","颜色(英)","尺寸","原产地",
            "样品单位","样品单位(英)","认证","认证数量","电池信息",
            "侵权信息","备注","备注(英)"};
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            w.write("\uFEFF");
            w.println(String.join(",", headers));
        }
    }

    @GetMapping("/deleted")
    public Result<PageResult<Sample>> listDeleted(@RequestParam(defaultValue = "1") int current,
                                                   @RequestParam(defaultValue = "20") int size) {
        return Result.success(sampleService.listDeleted(current, size));
    }

    @PostMapping("/restore")
    public Result<Integer> restoreDeleted(@RequestBody List<Long> ids) {
        int count = sampleService.restoreDeleted(ids);
        return Result.success("已恢复 " + count + " 条记录", count);
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=samples.csv");
        PageResult<Sample> result = sampleService.list(1L, 100000L, keyword, category, name, null, null, null, null);
        List<Sample> list = result.getRecords();
        String[] headers = {"ID","公司编号","出厂货号","厂商编号","种类名称","样品名称","英文名称",
            "出厂价","报出价","包装规格","包装规格(英)","包装单位","内盒数","装箱量",
            "外箱长","外箱宽","外箱高","外箱毛重","外箱净重",
            "产品长","产品宽","产品高","产品毛重","产品净重",
            "体积","材积","摊位号","厂商名称","联系人","联系电话","手机","传真","QQ",
            "颜色","颜色(英)","尺寸","原产地",
            "样品单位","样品单位(英)","认证","认证数量","电池信息",
            "侵权信息","备注","备注(英)"};
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            w.write("\uFEFF");
            w.println(String.join(",", headers));
            for (Sample s : list) {
                w.println(toCsvLine(s));
            }
        }
    }

    @PostMapping("/export")
    public void exportExcelPost(HttpServletResponse response,
            @RequestBody Map<String, Object> body) throws Exception {
        @SuppressWarnings("unchecked")
        List<String> fields = (List<String>) body.get("fields");
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        if (fields == null || fields.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"fields required\"}");
            return;
        }
        List<Sample> list;
        if (idsRaw != null && !idsRaw.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (Integer id : idsRaw) ids.add(id.longValue());
            list = sampleService.listByIds(ids);
        } else {
            PageResult<Sample> result = sampleService.list(1L, 100000L, null, null, null, null, null, null, null);
            list = result.getRecords();
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "样品资料" + java.time.LocalDate.now().toString().replace("-", "") + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(filename, "UTF-8"));
        Map<String, String> labelMap = buildLabelMap();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("samples");
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(labelMap.getOrDefault(fields.get(i), fields.get(i)));
                cell.setCellStyle(headerStyle);
            }
            for (int r = 0; r < list.size(); r++) {
                Row row = sheet.createRow(r + 1);
                Sample s = list.get(r);
                for (int c = 0; c < fields.size(); c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(getFieldValue(s, fields.get(c)));
                    cell.setCellStyle(dataStyle);
                }
            }
            for (int i = 0; i < fields.size(); i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width > 40 * 256) sheet.setColumnWidth(i, 40 * 256);
                if (width < 10 * 256) sheet.setColumnWidth(i, 10 * 256);
            }
            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/vendor-confirm")
    public void exportVendorConfirm(HttpServletResponse response,
            @RequestBody Map<String, Object> body) throws Exception {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        @SuppressWarnings("unchecked")
        List<String> fields = (List<String>) body.get("fields");
        if (idsRaw == null || idsRaw.isEmpty() || fields == null || fields.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"ids and fields required\"}");
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> headerMap = (Map<String, Object>) body.getOrDefault("header", new java.util.HashMap<>());
        String companyName = headerMap.get("companyName") != null ? headerMap.get("companyName").toString() : "";
        String address = headerMap.get("address") != null ? headerMap.get("address").toString() : "";
        String phone = headerMap.get("phone") != null ? headerMap.get("phone").toString() : "";
        String title = headerMap.get("title") != null ? headerMap.get("title").toString() : "厂商确认表";
        String logoBase64 = headerMap.get("logoBase64") != null ? headerMap.get("logoBase64").toString() : "";
        List<Long> ids = new ArrayList<>();
        for (Integer id : idsRaw) ids.add(id.longValue());
        List<Sample> list = sampleService.listByIds(ids);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "厂商确认表" + java.time.LocalDate.now().toString().replace("-", "") + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(filename, "UTF-8"));
        Map<String, String> labelMap = buildLabelMap();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("厂商确认表");
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 18);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setAlignment(HorizontalAlignment.RIGHT);
            Font subtitleFont = workbook.createFont();
            subtitleFont.setFontHeightInPoints((short) 10);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            Font headerCellFont = workbook.createFont();
            headerCellFont.setBold(true);
            headerCellFont.setFontHeightInPoints((short) 11);
            headerCellStyle.setFont(headerCellFont);
            CellStyle dataCenterStyle = workbook.createCellStyle();
            dataCenterStyle.setAlignment(HorizontalAlignment.CENTER);
            dataCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataCenterStyle.setBorderBottom(BorderStyle.THIN);
            dataCenterStyle.setBorderTop(BorderStyle.THIN);
            dataCenterStyle.setBorderLeft(BorderStyle.THIN);
            dataCenterStyle.setBorderRight(BorderStyle.THIN);
            CellStyle companyStyle = workbook.createCellStyle();
            companyStyle.setAlignment(HorizontalAlignment.LEFT);
            companyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font companyFont = workbook.createFont();
            companyFont.setFontHeightInPoints((short) 14);
            companyFont.setBold(true);
            companyStyle.setFont(companyFont);
            CellStyle infoStyle = workbook.createCellStyle();
            infoStyle.setAlignment(HorizontalAlignment.LEFT);
            infoStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font infoFont = workbook.createFont();
            infoFont.setFontHeightInPoints((short) 9);
            infoFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            infoStyle.setFont(infoFont);
            int rowIdx = 0;
            Row row0 = sheet.createRow(rowIdx++);
            row0.setHeightInPoints(50);
            int logoColWidth = 0;
            if (logoBase64 != null && !logoBase64.isEmpty()) {
                byte[] logoBytes = java.util.Base64.getMimeDecoder().decode(logoBase64);
                int picIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0); anchor.setRow1(0);
                anchor.setCol2(2); anchor.setRow2(1);
                drawing.createPicture(anchor, picIdx);
                logoColWidth = 3;
            }
            Cell nameCell = row0.createCell(Math.max(logoColWidth, 0));
            nameCell.setCellValue(companyName);
            nameCell.setCellStyle(companyStyle);
            if (!address.isEmpty()) {
                Cell addrCell = row0.createCell(Math.max(logoColWidth, 0) + 1);
                addrCell.setCellValue(address);
                addrCell.setCellStyle(infoStyle);
            }
            if (!phone.isEmpty()) {
                Cell phoneCell = row0.createCell(Math.max(logoColWidth, 0) + 2);
                phoneCell.setCellValue(phone);
                phoneCell.setCellStyle(infoStyle);
            }
            if (logoColWidth > 0 && !companyName.isEmpty()) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, logoColWidth, logoColWidth));
            }
            rowIdx++;
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, fields.size() + 1));
            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.setHeightInPoints(18);
            Cell dateCell = dateRow.createCell(fields.size() + 1);
            dateCell.setCellValue(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            dateCell.setCellStyle(subtitleStyle);
            rowIdx++;
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            Cell seqHeaderCell = headerRow.createCell(0);
            seqHeaderCell.setCellValue("序号");
            seqHeaderCell.setCellStyle(headerCellStyle);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(labelMap.getOrDefault(fields.get(i), fields.get(i)));
                cell.setCellStyle(headerCellStyle);
            }
            Cell imgHeaderCell = headerRow.createCell(fields.size() + 1);
            imgHeaderCell.setCellValue("图片");
            imgHeaderCell.setCellStyle(headerCellStyle);
            int imgCol = fields.size() + 1;
            int dataStartRow = rowIdx;
            for (int r = 0; r < list.size(); r++) {
                Sample s = list.get(r);
                Row dataRow = sheet.createRow(rowIdx++);
                dataRow.setHeightInPoints(45);
                Cell seqCell = dataRow.createCell(0);
                seqCell.setCellValue(r + 1);
                seqCell.setCellStyle(dataCenterStyle);
                for (int c = 0; c < fields.size(); c++) {
                    Cell cell = dataRow.createCell(c + 1);
                    cell.setCellValue(getFieldValue(s, fields.get(c)));
                    cell.setCellStyle(dataCenterStyle);
                }
                String thumbFile = s.getThumbnail();
                if (thumbFile != null && !thumbFile.isEmpty()) {
                    java.io.File imgFile = new java.io.File(thumbnailDirPath, thumbFile);
                    if (imgFile.exists()) {
                        try {
                            byte[] imgBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
                            String ext = thumbFile.toLowerCase();
                            int picType = ext.endsWith(".png") ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
                            int picIdx = workbook.addPicture(imgBytes, picType);
                            Drawing<?> drawing = sheet.createDrawingPatriarch();
                            CreationHelper helper2 = workbook.getCreationHelper();
                            ClientAnchor anchor = helper2.createClientAnchor();
                            anchor.setCol1(imgCol); anchor.setRow1(r + dataStartRow);
                            anchor.setCol2(imgCol + 1); anchor.setRow2(r + dataStartRow + 1);
                            anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                            drawing.createPicture(anchor, picIdx);
                        } catch (Exception ignored) {}
                    }
                }
            }
            sheet.setColumnWidth(0, 6 * 256);
            for (int i = 0; i < fields.size(); i++) {
                sheet.setColumnWidth(i + 1, 14 * 256);
            }
            sheet.setColumnWidth(imgCol, 16 * 256);
            workbook.write(response.getOutputStream());
        }
    }

    private Map<String, String> buildLabelMap() {
        Map<String, String> m = new java.util.LinkedHashMap<>();
        m.put("sampleCode", "公司编号");
        m.put("factoryCode", "出厂货号");
        m.put("manufacturerCode", "厂商编号");
        m.put("category", "种类名称");
        m.put("sampleName", "样品名称");
        m.put("englishName", "英文名称");
        m.put("factoryPrice", "出厂价");
        m.put("taxPrice", "报出价");
        m.put("packagingCn", "包装规格");
        m.put("packagingEn", "包装规格(英)");
        m.put("packingUnit", "包装单位");
        m.put("innerBoxCount", "内盒数");
        m.put("cartonCapacity", "装箱量");
        m.put("cartonLength", "外箱长");
        m.put("cartonWidth", "外箱宽");
        m.put("cartonHeight", "外箱高");
        m.put("cartonGrossWeight", "外箱毛重");
        m.put("cartonNetWeight", "外箱净重");
        m.put("sampleLength", "产品长");
        m.put("sampleWidth", "产品宽");
        m.put("sampleHeight", "产品高");
        m.put("sampleGrossWeight", "产品毛重");
        m.put("sampleNetWeight", "产品净重");
        m.put("cartonVolume", "体积");
        m.put("cartonMaterialVolume", "材积");
        m.put("boothNo", "摊位号");
        m.put("name", "厂商名称");
        m.put("contact1", "联系人");
        m.put("phone1", "联系电话");
        m.put("mobile1", "手机");
        m.put("fax", "传真");
        m.put("qq", "QQ");
        m.put("color", "颜色");
        m.put("colorEn", "颜色(英)");
        m.put("size", "尺寸");
        m.put("origin", "原产地");
        m.put("sampleUnit", "样品单位");
        m.put("sampleUnitEn", "样品单位(英)");
        m.put("certification", "认证");
        m.put("certificationCount", "认证数量");
        m.put("batteryInfo", "电池信息");
        m.put("hideFromXzx", "不在小竹熊显示");
        m.put("infringement", "侵权信息");
        m.put("remark", "中文备注");
        m.put("remarkEn", "备注(英)");
        m.put("registrant", "登记人");
        m.put("modifier", "修改人");
        m.put("createTime", "登记时间");
        m.put("updateTime", "修改时间");
        return m;
    }

    private String getFieldValue(Sample s, String key) {
        if (key == null) return "";
        switch (key) {
            case "sampleCode": return n(s.getSampleCode());
            case "factoryCode": return n(s.getFactoryCode());
            case "manufacturerCode": return n(s.getManufacturerCode());
            case "category": return n(s.getCategory());
            case "sampleName": return n(s.getSampleName());
            case "englishName": return n(s.getEnglishName());
            case "factoryPrice": return n(s.getFactoryPrice());
            case "taxPrice": return n(s.getTaxPrice());
            case "packagingCn": return n(s.getPackagingCn());
            case "packagingEn": return n(s.getPackagingEn());
            case "packingUnit": return n(s.getPackingUnit());
            case "innerBoxCount": return n(s.getInnerBoxCount());
            case "cartonCapacity": return n(s.getCartonCapacity());
            case "cartonLength": return n(s.getCartonLength());
            case "cartonWidth": return n(s.getCartonWidth());
            case "cartonHeight": return n(s.getCartonHeight());
            case "cartonGrossWeight": return n(s.getCartonGrossWeight());
            case "cartonNetWeight": return n(s.getCartonNetWeight());
            case "sampleLength": return n(s.getSampleLength());
            case "sampleWidth": return n(s.getSampleWidth());
            case "sampleHeight": return n(s.getSampleHeight());
            case "sampleGrossWeight": return n(s.getSampleGrossWeight());
            case "sampleNetWeight": return n(s.getSampleNetWeight());
            case "cartonVolume": return n(s.getCartonVolume());
            case "cartonMaterialVolume": return n(s.getCartonMaterialVolume());
            case "boothNo": return n(s.getBoothNo());
            case "name": return n(s.getName());
            case "contact1": return n(s.getContact1());
            case "phone1": return n(s.getPhone1());
            case "mobile1": return n(s.getMobile1());
            case "fax": return n(s.getFax());
            case "qq": return n(s.getQq());
            case "color": return n(s.getColor());
            case "colorEn": return n(s.getColorEn());
            case "size": return n(s.getSize());
            case "origin": return n(s.getOrigin());
            case "sampleUnit": return n(s.getSampleUnit());
            case "sampleUnitEn": return n(s.getSampleUnitEn());
            case "certification": return n(s.getCertification());
            case "certificationCount": return n(s.getCertificationCount());
            case "batteryInfo": return n(s.getBatteryInfo());
            case "hideFromXzx": return n(s.getHideFromXzx());
            case "infringement": return n(s.getInfringement());
            case "remark": return n(s.getRemark());
            case "remarkEn": return n(s.getRemarkEn());
            case "registrant": return n(s.getRegistrant());
            case "modifier": return n(s.getModifier());
            case "createTime": return s.getCreateTime() != null ? s.getCreateTime().toString().replace("T", " ") : "";
            case "updateTime": return s.getUpdateTime() != null ? s.getUpdateTime().toString().replace("T", " ") : "";
            default: return "";
        }
    }

    private String n(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String csvEscape(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    @GetMapping("/vendor-confirm-report")
    public Map<String, Object> vendorConfirmReportData(@RequestParam(defaultValue = "") String key) {
        log.info("[厂商确认表] 收到请求 key='{}'", key);
        long t0 = System.currentTimeMillis();
        List<Map<String, Object>> data;
        if (key != null && !key.isBlank()) {
            List<Long> sampleIds = vendorConfirmCache.get(key);
            if (sampleIds == null || sampleIds.isEmpty()) {
                data = java.util.Collections.emptyList();
                System.out.println("[厂商确认表] 无效key（样本ID为空），返回空数据");
            } else {
                long t1 = System.currentTimeMillis();
                data = sampleService.vendorConfirmReportData(sampleIds);
                long t2 = System.currentTimeMillis();
                System.out.println("[厂商确认表] cache=" + (t1-t0) + "ms sql=" + (t2-t1) + "ms count=" + data.size());
            }
        } else {
            // 无有效key时返回空数据，避免全表查询
            data = java.util.Collections.emptyList();
            System.out.println("[厂商确认表] 无有效key，返回空数据");
        }
        // 注入 operator_name（操作员）和 total_pages（总页数）
        String operatorName = sampleService.getActiveOperatorName();
        int pageSize = 500;
        int totalPages = data.isEmpty() ? 0 : (int) Math.ceil((double) data.size() / pageSize);
        for (Map<String, Object> row : data) {
            row.put("operator_name", operatorName);
            row.put("total_pages", totalPages);
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("data", data);
        result.put("total", data.size());
        return result;
    }

    @PostMapping("/vendor-confirm-session")
    public Map<String, String> createVendorConfirmSession(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> idsRaw = (List<Number>) body.get("sampleIds");
        List<Long> sampleIds = idsRaw != null ? idsRaw.stream().map(Number::longValue).collect(Collectors.toList()) : List.of();
        String key = vendorConfirmCache.put(sampleIds);
        return Map.of("key", key);
    }

    private String toCsvLine(Sample s) {
        Object[] vals = {s.getId(), s.getSampleCode(), s.getFactoryCode(), s.getManufacturerCode(),
            s.getCategory(), s.getSampleName(), s.getEnglishName(),
            s.getFactoryPrice(), s.getTaxPrice(), s.getPackagingCn(), s.getPackagingEn(), s.getPackingUnit(),
            s.getInnerBoxCount(), s.getCartonCapacity(),
            s.getCartonLength(), s.getCartonWidth(), s.getCartonHeight(),
            s.getCartonGrossWeight(), s.getCartonNetWeight(),
            s.getSampleLength(), s.getSampleWidth(), s.getSampleHeight(),
            s.getSampleGrossWeight(), s.getSampleNetWeight(),
            s.getCartonVolume(), s.getCartonMaterialVolume(),
            s.getBoothNo(), s.getName(), s.getContact1(), s.getPhone1(), s.getMobile1(), s.getFax(), s.getQq(),
            s.getColor(), s.getColorEn(), s.getSize(), s.getOrigin(),
            s.getSampleUnit(), s.getSampleUnitEn(), s.getCertification(), s.getCertificationCount(),
            s.getBatteryInfo(), s.getInfringement(), s.getRemark(), s.getRemarkEn()};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(csvEscape(vals[i] == null ? "" : String.valueOf(vals[i])));
        }
        return sb.toString();
    }

    // ========== ES 同步 ==========

    @PostMapping("/sync-to-es")
    public Result<java.util.Map<String, Object>> syncToES() {
        return Result.success(sampleService.syncAllToES());
    }
}