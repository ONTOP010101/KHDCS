package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.dto.SearchCondition;
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
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String manufacturerCode,
            @RequestParam(required = false) String boothNo,
            @RequestParam(required = false) String boothType,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contact1,
            @RequestParam(required = false) String phone1,
            @RequestParam(required = false) String mobile1,
            @RequestParam(required = false) String fax,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String boothManager,
            @RequestParam(required = false) String mainCard,
            @RequestParam(required = false) String subCard,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String certificate,
            @RequestParam(required = false) String registrant,
            @RequestParam(required = false) String modifier,
            @RequestParam(required = false) String createDateStart,
            @RequestParam(required = false) String createDateEnd,
            @RequestParam(required = false) String updateDateStart,
            @RequestParam(required = false) String updateDateEnd,
            @RequestParam(required = false) String expiryDateStart,
            @RequestParam(required = false) String expiryDateEnd,
            @RequestParam(required = false) String television) {
        return Result.success(manufacturerService.list(current, size, keyword, sortField, sortOrder,
                manufacturerCode, boothNo, boothType, name, contact1, phone1, mobile1,
                fax, email, boothManager, mainCard, subCard, remark, summary, address,
                certificate, registrant, modifier,
                createDateStart, createDateEnd, updateDateStart, updateDateEnd,
                expiryDateStart, expiryDateEnd, television));
    }

    @PostMapping("/search")
    public Result<PageResult<Manufacturer>> search(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestBody List<SearchCondition> conditions) {
        return Result.success(manufacturerService.advancedSearch(current, size, conditions, sortField, sortOrder));
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
