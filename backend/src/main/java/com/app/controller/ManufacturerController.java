package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.entity.Manufacturer;
import com.app.service.ManufacturerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manufacturers")
public class ManufacturerController {

    private static final Logger log = LoggerFactory.getLogger(ManufacturerController.class);

    @Autowired
    private ManufacturerService manufacturerService;

    @GetMapping
    public Result<PageResult<Manufacturer>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(manufacturerService.list(current, size, keyword, sortField, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<Manufacturer> getById(@PathVariable Long id) {
        return Result.success(manufacturerService.getById(id));
    }

    @PostMapping
    public Result<Manufacturer> create(@RequestBody Manufacturer manufacturer) {
        return Result.success("创建成功", manufacturerService.create(manufacturer));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Manufacturer manufacturer) {
        manufacturerService.update(id, manufacturer);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        manufacturerService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody Long[] ids) {
        manufacturerService.deleteBatch(ids);
        return Result.ok("批量删除成功");
    }

    @PostMapping("/batch-import")
    public Result<ImportResult> batchImport(@RequestBody List<Manufacturer> manufacturers,
                                            @RequestParam(defaultValue = "false") boolean updateMode) {
        try {
            ImportResult result = manufacturerService.batchInsert(manufacturers, updateMode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入厂商失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/{id}/certificate")
    public Result<Map<String, String>> uploadCertificate(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) {
        return manufacturerService.uploadCertificate(id, file);
    }
}
