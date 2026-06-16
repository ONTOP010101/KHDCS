package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.dto.PasswordRequest;
import com.app.dto.UserCreateRequest;
import com.app.dto.UserUpdateRequest;
import com.app.entity.Role;
import com.app.entity.User;
import com.app.entity.UserRole;
import com.app.mapper.RoleMapper;
import com.app.mapper.UserMapper;
import com.app.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public PageResult<Map<String, Object>> list(long current, long size, String keyword, String department, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
        if (StringUtils.hasText(department) && !"all".equals(department)) {
            wrapper.eq(User::getDepartment, department);
        }
        if ("enabled".equals(status)) {
            wrapper.eq(User::getStatus, 1);
        } else if ("disabled".equals(status)) {
            wrapper.eq(User::getStatus, 0);
        }
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> page = userMapper.selectPage(new Page<>(current, size), wrapper);

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(List.of(), page.getTotal(), current, size);
        }

        // 批量查询所有用户的角色，避免N+1问题
        List<Long> userIds = page.getRecords().stream().map(User::getId).collect(Collectors.toList());

        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.in(UserRole::getUserId, userIds);
        List<UserRole> allUserRoles = userRoleMapper.selectList(urWrapper);

        // 批量查询所有相关角色
        List<Long> roleIds = allUserRoles.stream()
                .map(UserRole::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, Role> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            roleMapper.selectBatchIds(roleIds).forEach(r -> roleMap.put(r.getId(), r));
        }

        // 按userId分组
        Map<Long, UserRole> urByUser = allUserRoles.stream()
                .collect(Collectors.toMap(UserRole::getUserId, ur -> ur, (a, b) -> a));

        List<Map<String, Object>> records = new ArrayList<>();
        for (User user : page.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", user.getId());
            record.put("username", user.getUsername());
            record.put("realName", user.getRealName());
            record.put("phone", user.getPhone());
            record.put("email", user.getEmail());
            record.put("department", user.getDepartment());
            record.put("status", user.getStatus());
            record.put("lastLoginTime", user.getLastLoginTime());
            record.put("createTime", user.getCreateTime());

            UserRole userRole = urByUser.get(user.getId());
            if (userRole != null) {
                Role role = roleMap.get(userRole.getRoleId());
                record.put("roleId", userRole.getRoleId());
                record.put("role", role != null ? role.getName() : null);
            } else {
                record.put("roleId", null);
                record.put("role", null);
            }

            records.add(record);
        }

        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public Map<String, Object> getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        Map<String, Object> record = new HashMap<>();
        record.put("id", user.getId());
        record.put("username", user.getUsername());
        record.put("realName", user.getRealName());
        record.put("phone", user.getPhone());
        record.put("email", user.getEmail());
        record.put("department", user.getDepartment());
        record.put("status", user.getStatus());
        record.put("lastLoginTime", user.getLastLoginTime());
        record.put("createTime", user.getCreateTime());

        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRole::getUserId, user.getId());
        UserRole userRole = userRoleMapper.selectOne(urWrapper);
        if (userRole != null) {
            Role role = roleMapper.selectById(userRole.getRoleId());
            record.put("roleId", userRole.getRoleId());
            record.put("role", role != null ? role.getName() : null);
        }

        return record;
    }

    @Transactional
    public Map<String, Object> create(UserCreateRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setStatus(request.getStatus());
        userMapper.insert(user);

        if (request.getRoleId() != null && request.getRoleId() > 0) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(request.getRoleId());
            userRoleMapper.insert(userRole);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    @Transactional
    public void update(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        userMapper.updateById(user);
    }

    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(400, "不能删除超级管理员");
        }
        userMapper.deleteById(id);
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        userRoleMapper.delete(wrapper);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user == null) continue;
            if ("admin".equals(user.getUsername())) continue;
            userMapper.deleteById(id);
            LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRole::getUserId, id);
            userRoleMapper.delete(wrapper);
        }
    }

    public void resetPassword(Long id, PasswordRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.updateById(user);
    }

    @Transactional
    public void assignRole(Long userId, Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        UserRole existing = userRoleMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setRoleId(roleId);
            userRoleMapper.updateById(existing);
        } else {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
