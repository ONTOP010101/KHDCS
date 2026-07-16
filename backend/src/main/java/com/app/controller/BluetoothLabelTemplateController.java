package com.app.controller;

import com.app.common.Result;
import com.app.entity.BluetoothLabelTemplate;
import com.app.service.BluetoothLabelTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bluetooth-label-templates")
public class BluetoothLabelTemplateController {

    @Autowired
    private BluetoothLabelTemplateService service;

    @GetMapping
    public Result<List<BluetoothLabelTemplate>> list() {
        return Result.success(service.list());
    }

    @GetMapping("/{id}")
    public Result<BluetoothLabelTemplate> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<BluetoothLabelTemplate> create(@RequestBody BluetoothLabelTemplate template) {
        return Result.success("创建成功", service.create(template));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BluetoothLabelTemplate template) {
        service.update(id, template);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok("删除成功");
    }
}
