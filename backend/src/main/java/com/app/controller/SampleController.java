package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.entity.Sample;
import com.app.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/samples")
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @GetMapping
    public Result<PageResult<Sample>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(sampleService.list(current, size, keyword, category, supplier, sortField, sortOrder));
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
        return Result.ok("批量删除成功");
    }

    @PostMapping("/import")
    public Result<ImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = sampleService.importFromExcel(file);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
