package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.ProductCategory;
import com.app.service.ProductCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/product-categories")
public class ProductCategoryController {

    private static final Logger log = LoggerFactory.getLogger(ProductCategoryController.class);

    @Autowired
    private ProductCategoryService service;

    @GetMapping
    public Result<PageResult<ProductCategory>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String parentCode) {
        return Result.success(service.list(current, size, keyword, level, parentCode));
    }

    @GetMapping("/all")
    public Result<List<ProductCategory>> listAll() {
        return Result.success(service.listAll());
    }

    @GetMapping("/codes")
    public Result<Set<String>> getAllCodes() {
        return Result.success(service.getAllCategoryCodes());
    }

    @GetMapping("/keywords-map")
    public Result<List<ProductCategory>> getKeywordsMap() {
        return Result.success(service.getKeywordsMap());
    }

    @GetMapping("/{id}")
    public Result<ProductCategory> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<ProductCategory> create(@RequestBody ProductCategory category) {
        return Result.success("创建成功", service.create(category));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductCategory category) {
        service.update(id, category);
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
    public Result<Map<String, Object>> batchImport(@RequestBody List<ProductCategory> categories) {
        try {
            Map<String, Object> result = service.batchImport(categories);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入种类失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入失败: " + e.getMessage());
        }
    }
}
