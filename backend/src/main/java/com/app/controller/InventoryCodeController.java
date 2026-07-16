package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.InventoryCode;
import com.app.service.InventoryCodeService;
import com.app.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory-codes")
public class InventoryCodeController {

    @Autowired
    private InventoryCodeService inventoryCodeService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<InventoryCode>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(inventoryCodeService.list(current, size, keyword, sortField, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<InventoryCode> getById(@PathVariable Long id) {
        return Result.success(inventoryCodeService.getById(id));
    }

    @PostMapping
    public Result<InventoryCode> create(@RequestBody InventoryCode inventoryCode) {
        return Result.success("添加成功", inventoryCodeService.create(inventoryCode));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryCode inventoryCode) {
        inventoryCodeService.update(id, inventoryCode);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryCodeService.delete(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量提交 - 将勾选代号组下所有条目标记为已提交
     */
    @PutMapping("/submit")
    public Result<Integer> batchSubmit(@RequestBody List<String> codeNames) {
        int count = inventoryService.batchSubmitByCodeNames(codeNames);
        return Result.success("已提交 " + count + " 条", count);
    }
}
