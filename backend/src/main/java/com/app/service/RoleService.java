package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.entity.Permission;
import com.app.entity.Role;
import com.app.entity.RolePermission;
import com.app.mapper.PermissionMapper;
import com.app.mapper.RoleMapper;
import com.app.mapper.RolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    public PageResult<Map<String, Object>> list(long current, long size, String keyword) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Role::getName, keyword).or().like(Role::getCode, keyword);
        }
        wrapper.orderByDesc(Role::getCreateTime);

        IPage<Role> page = roleMapper.selectPage(new Page<>(current, size), wrapper);

        List<Map<String, Object>> records = page.getRecords().stream().map(role -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", role.getId());
            record.put("name", role.getName());
            record.put("code", role.getCode());
            record.put("description", role.getDescription());
            record.put("status", role.getStatus());
            record.put("createTime", role.getCreateTime());

            LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
            rpWrapper.eq(RolePermission::getRoleId, role.getId());
            List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rpWrapper);
            List<String> permissions = rolePermissions.stream()
                    .map(rp -> {
                        Permission p = permissionMapper.selectById(rp.getPermissionId());
                        return p != null ? p.getCode() : null;
                    })
                    .filter(p -> p != null)
                    .collect(Collectors.toList());
            record.put("permissions", permissions);
            record.put("permissionIds", rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList()));

            return record;
        }).collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), current, size);
    }

    @Cacheable(value = "roles", key = "#id")
    public Map<String, Object> getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        Map<String, Object> record = new HashMap<>();
        record.put("id", role.getId());
        record.put("name", role.getName());
        record.put("code", role.getCode());
        record.put("description", role.getDescription());
        record.put("status", role.getStatus());
        record.put("createTime", role.getCreateTime());

        LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(RolePermission::getRoleId, id);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rpWrapper);
        record.put("permissionIds", rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList()));

        return record;
    }

    @Transactional
    public Map<String, Object> create(String name, String code, String description, Integer status) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, code);
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }

        Role role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setDescription(description);
        role.setStatus(status != null ? status : 1);
        roleMapper.insert(role);

        Map<String, Object> result = new HashMap<>();
        result.put("id", role.getId());
        result.put("name", role.getName());
        return result;
    }

    @Transactional
    public void update(Long id, String name, String description, Integer status) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        if (name != null) role.setName(name);
        if (description != null) role.setDescription(description);
        if (status != null) role.setStatus(status);
        roleMapper.updateById(role);
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public void delete(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        if ("admin".equals(role.getCode())) {
            throw new BusinessException(400, "不能删除超级管理员角色");
        }
        roleMapper.deleteById(id);
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        rolePermissionMapper.delete(wrapper);
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public void updatePermissions(Long id, List<Long> permissionIds) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        rolePermissionMapper.delete(wrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permId);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Cacheable("all_permissions")
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectList(null);
    }
}
