package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.Outbound;
import com.app.service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/outbound")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    @GetMapping
    public Result<PageResult<Outbound>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String codeName,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(outboundService.list(current, size, keyword, codeName, sortField, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<Outbound> getById(@PathVariable Long id) {
        return Result.success(outboundService.getById(id));
    }

    @PostMapping
    public Result<Outbound> create(@RequestBody Outbound outbound) {
        return Result.success("添加成功", outboundService.create(outbound));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Outbound outbound) {
        outboundService.update(id, outbound);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        outboundService.delete(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量提交 - 将勾选的记录标记为已提交，进入总库存
     */
    @PutMapping("/submit")
    public Result<Integer> batchSubmit(@RequestBody List<Long> ids) {
        int count = outboundService.batchSubmit(ids);
        return Result.success("已提交 " + count + " 条", count);
    }

    // ==================== 明细操作 ====================

    @GetMapping("/{codeName}/items")
    public Result<List<Outbound>> getItems(@PathVariable String codeName) {
        return Result.success(outboundService.listByCodeName(codeName));
    }

    @PostMapping("/{codeName}/items")
    public Result<Outbound> addItem(@PathVariable String codeName,
                                    @RequestBody Map<String, String> body) {
        String companyCode = body.get("companyCode");
        String creator = body.get("creator");
        String floor = body.get("floor");
        boolean submitted = "true".equals(body.get("submitted"));
        return Result.success("添加成功", outboundService.addItem(codeName, companyCode, creator, floor, submitted));
    }

    @DeleteMapping("/{codeName}/items/{id}")
    public Result<Void> removeItem(@PathVariable Long id) {
        outboundService.removeItem(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping("/{codeName}/items")
    public Result<Void> removeItems(@RequestBody List<Long> ids) {
        outboundService.removeItems(ids);
        return Result.ok("删除成功");
    }

    @PostMapping("/{codeName}/items/batch")
    public Result<Map<String, Object>> batchAddItems(@PathVariable String codeName,
                                                     @RequestBody Map<String, Object> body) {
        String creator = (String) body.get("creator");
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
                    outboundService.addItem(codeName, companyCode, creator, null, submitted);
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
}
