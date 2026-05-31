package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.entity.Sample;
import com.app.service.SampleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/samples")
public class SampleController {

    private static final Logger log = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private SampleService sampleService;

    @GetMapping
    public Result<PageResult<Sample>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String manufacturerCode,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(sampleService.list(current, size, keyword, category, supplier, manufacturerCode, sortField, sortOrder));
    }

    @PostMapping("/search")
    public Result<PageResult<Sample>> advancedSearch(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestBody Map<String, String> params) {
        return Result.success(sampleService.advancedSearch(current, size, params, sortField, sortOrder));
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
        return Result.ok("\u6279\u91cf\u5220\u9664\u6210\u529f");
    }

    @PostMapping("/match-by-codes")
    public Result<java.util.List<Sample>> matchByCodes(@RequestBody java.util.Map<String, Object> body) {
        String type = (String) body.get("type");
        @SuppressWarnings("unchecked")
        java.util.List<String> codes = (java.util.List<String>) body.get("codes");
        return Result.success(sampleService.matchByCodes(type, codes));
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
            "材料","颜色","颜色(英)","尺寸","重量","原产地",
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
            @RequestParam(required = false) String supplier) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=samples.csv");
        PageResult<Sample> result = sampleService.list(1, 100000, keyword, category, supplier, null, null, null);
        List<Sample> list = result.getRecords();
        String[] headers = {"ID","公司编号","出厂货号","厂商编号","种类名称","样品名称","英文名称",
            "出厂价","报出价","包装规格","包装规格(英)","包装单位","内盒数","装箱量",
            "外箱长","外箱宽","外箱高","外箱毛重","外箱净重",
            "产品长","产品宽","产品高","产品毛重","产品净重",
            "体积","材积","摊位号","厂商名称","联系人","联系电话","手机","传真","QQ",
            "材料","颜色","颜色(英)","尺寸","重量","原产地",
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
            s.getBoothNo(), s.getSupplier(), s.getContactPerson(), s.getContactPhone(),
            s.getMobile(), s.getFax(), s.getQq(),
            s.getMaterial(), s.getColor(), s.getColorEn(), s.getSize(), s.getWeight(), s.getOrigin(),
            s.getSampleUnit(), s.getSampleUnitEn(), s.getCertification(), s.getCertificationCount(),
            s.getBatteryInfo(), s.getInfringement(), s.getRemark(), s.getRemarkEn()};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(",");
            String v = vals[i] == null ? "" : String.valueOf(vals[i]);
            if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
                sb.append("\"").append(v.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(v);
            }
        }
        return sb.toString();
    }
}
