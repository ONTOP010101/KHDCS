package com.app.service;

import com.app.common.BusinessException;
import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.entity.User;
import com.app.entity.UserRole;
import com.app.entity.Role;
import com.app.entity.RolePermission;
import com.app.entity.Permission;
import com.app.util.JwtUtil;
import com.app.mapper.UserMapper;
import com.app.mapper.UserRoleMapper;
import com.app.mapper.RoleMapper;
import com.app.mapper.RolePermissionMapper;
import com.app.mapper.PermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginRequest.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(400, "账号已被禁用");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        Long roleId = null;
        String roleName = null;
        List<String> permissions = new ArrayList<>();

        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRole::getUserId, user.getId());
        List<UserRole> userRoles = userRoleMapper.selectList(urWrapper);

        if (!userRoles.isEmpty()) {
            Long firstRoleId = userRoles.get(0).getRoleId();
            roleId = firstRoleId;

            Role role = roleMapper.selectById(firstRoleId);
            if (role != null) {
                roleName = role.getName();
            }

            LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
            rpWrapper.eq(RolePermission::getRoleId, firstRoleId);
            List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rpWrapper);

            if (!rolePermissions.isEmpty()) {
                List<Long> permIds = rolePermissions.stream()
                        .map(RolePermission::getPermissionId)
                        .collect(Collectors.toList());
                LambdaQueryWrapper<Permission> permWrapper = new LambdaQueryWrapper<>();
                permWrapper.in(Permission::getId, permIds);
                permissions = permissionMapper.selectList(permWrapper).stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toList());
            }
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleId);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        response.setRoleId(roleId);
        response.setRoleName(roleName);
        response.setPermissions(permissions);

        return response;
    }
}
