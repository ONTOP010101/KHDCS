package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.PackagingMethod;
import com.app.service.PackagingMethodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/packaging-methods")
public class PackagingMethodController {

    private static final Logger log = LoggerFactory.getLogger(PackagingMethodController.class);

    @Autowired
    private PackagingMethodService service;

    @GetMapping
    public Result<PageResult<PackagingMethod>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(service.list(current, size, keyword));
    }

    @GetMapping("/all")
    public Result<List<PackagingMethod>> listAll() {
        return Result.success(service.listAll());
    }

    @GetMapping("/codes")
    public Result<Set<String>> getAllCodes() {
        return Result.success(service.getAllPackageCodes());
    }

    @GetMapping("/{id}")
    public Result<PackagingMethod> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<PackagingMethod> create(@RequestBody PackagingMethod packaging) {
        return Result.success("创建成功", service.create(packaging));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PackagingMethod packaging) {
        service.update(id, packaging);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody Long[] ids) {
        service.deleteBatch(ids);
        return Result.ok("批量删除成功");
    }

    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImport(@RequestBody List<PackagingMethod> packagings) {
        try {
            Map<String, Object> result = service.batchImport(packagings);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入包装方式失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入失败: " + e.getMessage());
        }
    }
}
