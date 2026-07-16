package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.entity.InventoryGroup;
import com.app.service.InventoryGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/inventory-groups")
public class InventoryGroupController {

    private static final Logger log = LoggerFactory.getLogger(InventoryGroupController.class);

    @Autowired
    private InventoryGroupService inventoryGroupService;

    @GetMapping
    public Result<PageResult<InventoryGroup>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(inventoryGroupService.list(current, size, keyword, sortField, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<InventoryGroup> getById(@PathVariable Long id) {
        return Result.success(inventoryGroupService.getById(id));
    }

    @GetMapping("/next-code")
    public Result<Map<String, String>> nextCode() {
        return Result.success(inventoryGroupService.nextCode());
    }

    @PostMapping
    public Result<InventoryGroup> create(@RequestBody InventoryGroup group) {
        return Result.success("添加成功", inventoryGroupService.create(group));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryGroup group) {
        inventoryGroupService.update(id, group);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryGroupService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/import")
    public Result<ImportResult> batchImport(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = inventoryGroupService.batchImport(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入失败: {}", e.getMessage(), e);
            return Result.error(500, "导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String ids,
            @RequestParam(required = false) String fields) {
        byte[] data = inventoryGroupService.exportToExcel(ids, fields);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_group_export.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
