package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.dto.InventorySummary;
import com.app.entity.Inventory;
import com.app.service.InventoryService;
import com.app.service.OutboundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OutboundService outboundService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<PageResult<Inventory>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String codeName,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(inventoryService.list(current, size, keyword, codeName, sortField, sortOrder));
    }

    /**
     * 总库存汇总 - 按公司编号去重，计算在展数量
     */
    @GetMapping("/summary")
    public Result<PageResult<InventorySummary>> summary(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String boothNo,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String manufacturerName,
            @RequestParam(required = false) String floor) {
        return Result.success(inventoryService.summary(current, size, keyword, sortField, sortOrder, boothNo, mobile, manufacturerName, floor));
    }

    /**
     * 按公司编号查询明细记录（入库 + 出库）
     */
    @GetMapping("/detail/{companyCode}")
    public Result<Map<String, Object>> detail(@PathVariable String companyCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("inbound", inventoryService.listByCompanyCode(companyCode));
        result.put("outbound", outboundService.listByCompanyCode(companyCode));
        return Result.success(result);
    }

    /**
     * 获取单条
     */
    @GetMapping("/{id}")
    public Result<Inventory> getById(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Inventory> create(@RequestBody Inventory inventory) {
        return Result.success("添加成功", inventoryService.create(inventory));
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Inventory inventory) {
        inventoryService.update(id, inventory);
        return Result.ok("修改成功");
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量提交 - 将勾选的记录标记为已提交，进入总库存
     */
    @PutMapping("/submit")
    public Result<Integer> batchSubmit(@RequestBody List<Long> ids) {
        int count = inventoryService.batchSubmit(ids);
        return Result.success("已提交 " + count + " 条", count);
    }

    /**
     * 导入 Excel
     */
    @PostMapping("/import")
    public Result<ImportResult> batchImport(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = inventoryService.batchImport(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入失败: {}", e.getMessage(), e);
            return Result.error(500, "导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    /**
     * 导出 Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String ids,
            @RequestParam(required = false) String fields) {
        byte[] data = inventoryService.exportToExcel(ids, fields);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=inventory_export.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    // ==================== 明细操作 ====================

    /**
     * 按代号查询所有库存明细
     */
    @GetMapping("/{codeName}/items")
    public Result<List<Inventory>> getItems(@PathVariable String codeName) {
        return Result.success(inventoryService.listByCodeName(codeName));
    }

    /**
     * 添加库存明细项（关联到代号组）
     */
    @PostMapping("/{codeName}/items")
    public Result<Inventory> addItem(@PathVariable String codeName,
                                     @RequestBody Map<String, String> body) {
        String companyCode = body.get("companyCode");
        String creator = body.get("creator");
        String floor = body.get("floor");
        boolean submitted = "true".equals(body.get("submitted"));
        return Result.success("添加成功", inventoryService.addItem(codeName, companyCode, creator, floor, submitted));
    }

    /**
     * 删除单条明细
     */
    @DeleteMapping("/{codeName}/items/{id}")
    public Result<Void> removeItem(@PathVariable Long id) {
        inventoryService.removeItem(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除明细
     */
    @DeleteMapping("/{codeName}/items")
    public Result<Void> removeItems(@RequestBody List<Long> ids) {
        inventoryService.removeItems(ids);
        return Result.ok("删除成功");
    }

    /**
     * 批量添加明细（一次请求）
     */
    @PostMapping("/{codeName}/items/batch")
    public Result<Map<String, Object>> batchAddItems(@PathVariable String codeName,
                                                     @RequestBody Map<String, Object> body) {
        String creator = (String) body.get("creator");
        String floor = (String) body.get("floor");
        boolean submitted = "true".equals(String.valueOf(body.getOrDefault("submitted", "false")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        int success = 0, fail = 0;
        for (Map<String, Object> item : items) {
            String companyCode = (String) item.get("companyCode");
            Integer quantity = (Integer) item.get("quantity");
            int qty = quantity != null ? Math.max(1, Math.min(999, quantity)) : 1;
            try {
                for (int i = 0; i < qty; i++) {
                    inventoryService.addItem(codeName, companyCode, creator, floor, submitted);
                    success++;
                }
            } catch (Exception e) {
                fail += qty;
            }
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return Result.success(result);
    }

    @PostMapping("/repair-images")
    public Result<Map<String, Object>> repairImages() {
        int fixed = inventoryService.repairImageUrls();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("fixed", fixed);
        return Result.success(result);
    }
}
