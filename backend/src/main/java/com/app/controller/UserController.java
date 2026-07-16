package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.PasswordRequest;
import com.app.dto.RoleAssignRequest;
import com.app.dto.UserCreateRequest;
import com.app.dto.UserUpdateRequest;
import com.app.entity.UserPreference;
import com.app.mapper.UserPreferenceMapper;
import com.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        PageResult<Map<String, Object>> pageResult = userService.list(current, size, keyword, department, status);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success("创建成功", userService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.ok("更新成功");
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.ok("批量删除成功");
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        userService.resetPassword(id, request);
        return Result.ok("密码重置成功");
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRole(@PathVariable Long id, @RequestBody RoleAssignRequest request) {
        userService.assignRole(id, request.getRoleId());
        return Result.ok("角色分配成功");
    }

    /** 获取当前用户的页面偏好 */
    @GetMapping("/preferences/{pageKey}")
    public Result<Map<String, Object>> getPreference(HttpServletRequest request, @PathVariable String pageKey) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.success(null);
        UserPreference pref = userPreferenceMapper.findByUserAndPage(userId, pageKey);
        return Result.success(pref != null ? Map.of("preferenceJson", pref.getPreferenceJson()) : null);
    }

    /** 保存当前用户的页面偏好 */
    @PostMapping("/preferences/{pageKey}")
    public Result<Void> savePreference(HttpServletRequest request, @PathVariable String pageKey, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        String json = body.get("preferenceJson");
        UserPreference existing = userPreferenceMapper.findByUserAndPage(userId, pageKey);
        if (existing != null) {
            existing.setPreferenceJson(json);
            existing.setUpdatedAt(LocalDateTime.now());
            userPreferenceMapper.updateById(existing);
        } else {
            UserPreference pref = new UserPreference();
            pref.setUserId(userId);
            pref.setPageKey(pageKey);
            pref.setPreferenceJson(json);
            pref.setUpdatedAt(LocalDateTime.now());
            userPreferenceMapper.insert(pref);
        }
        return Result.ok("保存成功");
    }
}
