package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.Permission;
import com.app.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(roleService.list(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String code = (String) request.get("code");
        String description = (String) request.get("description");
        Integer status = request.get("status") != null ? ((Number) request.get("status")).intValue() : 1;
        return Result.success("创建成功", roleService.create(name, code, description, status));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        Integer status = request.get("status") != null ? ((Number) request.get("status")).intValue() : null;
        roleService.update(id, name, description, status);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        roleService.batchDelete(ids);
        return Result.ok("批量删除成功");
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(id);
        return Result.success(permissionIds);
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> updatePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.updatePermissions(id, permissionIds);
        return Result.ok("权限配置成功");
    }

    @GetMapping("/permissions")
    public Result<List<Permission>> getAllPermissions() {
        return Result.success(roleService.getAllPermissions());
    }
}
