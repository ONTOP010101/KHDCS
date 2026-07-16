package com.app.controller;

import com.app.common.Result;
import com.app.entity.ReportTemplate;
import com.app.service.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report-templates")
public class ReportTemplateController {

    @Autowired
    private ReportTemplateService reportTemplateService;

    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return Result.success(reportTemplateService.list(current, size, keyword, type));
    }

    @GetMapping("/all")
    public Result<List<ReportTemplate>> listAll(@RequestParam(required = false) String type) {
        return Result.success(reportTemplateService.listAll(type));
    }

    @GetMapping("/{id}")
    public Result<ReportTemplate> getById(@PathVariable Long id) {
        return Result.success(reportTemplateService.getById(id));
    }

    @PostMapping
    public Result<ReportTemplate> create(@RequestBody ReportTemplate template) {
        return Result.success(reportTemplateService.create(template));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ReportTemplate template) {
        reportTemplateService.update(id, template);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reportTemplateService.delete(id);
        return Result.ok("删除成功");
    }
}
