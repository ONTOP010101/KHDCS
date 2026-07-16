package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.OutboundCode;
import com.app.service.OutboundCodeService;
import com.app.service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/outbound-codes")
public class OutboundCodeController {

    @Autowired
    private OutboundCodeService outboundCodeService;

    @Autowired
    private OutboundService outboundService;

    @GetMapping
    public Result<PageResult<OutboundCode>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(outboundCodeService.list(current, size, keyword, sortField, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<OutboundCode> getById(@PathVariable Long id) {
        return Result.success(outboundCodeService.getById(id));
    }

    @PostMapping
    public Result<OutboundCode> create(@RequestBody OutboundCode outboundCode) {
        return Result.success("添加成功", outboundCodeService.create(outboundCode));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody OutboundCode outboundCode) {
        outboundCodeService.update(id, outboundCode);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        outboundCodeService.delete(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量提交 - 将勾选代号组下所有出库条目标记为已提交
     */
    @PutMapping("/submit")
    public Result<Integer> batchSubmit(@RequestBody List<String> codeNames) {
        int count = outboundService.batchSubmitByCodeNames(codeNames);
        return Result.success("已提交 " + count + " 条", count);
    }
}
