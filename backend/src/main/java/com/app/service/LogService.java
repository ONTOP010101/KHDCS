package com.app.service;

import com.app.common.PageResult;
import com.app.entity.LoginLog;
import com.app.entity.OperationLog;
import com.app.mapper.LoginLogMapper;
import com.app.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    public PageResult<Map<String, Object>> listLoginLogs(long current, long size, String keyword, Long userId) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(LoginLog::getUserId, userId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(LoginLog::getUsername, keyword);
        }
        wrapper.orderByDesc(LoginLog::getCreateTime);

        IPage<LoginLog> page = loginLogMapper.selectPage(new Page<>(current, size), wrapper);

        List<Map<String, Object>> records = page.getRecords().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("username", log.getUsername());
            map.put("ip", log.getIp());
            map.put("location", log.getLocation());
            map.put("browser", log.getBrowser());
            map.put("os", log.getOs());
            map.put("status", log.getStatus());
            map.put("message", log.getMessage());
            map.put("createTime", log.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public PageResult<Map<String, Object>> listOperationLogs(long current, long size, String keyword, String module, Long userId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(OperationLog::getUsername, keyword).or().like(OperationLog::getAction, keyword);
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> page = operationLogMapper.selectPage(new Page<>(current, size), wrapper);

        List<Map<String, Object>> records = page.getRecords().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("username", log.getUsername());
            map.put("module", log.getModule());
            map.put("action", log.getAction());
            map.put("method", log.getMethod());
            map.put("params", log.getParams());
            map.put("ip", log.getIp());
            map.put("duration", log.getDuration());
            map.put("status", log.getStatus());
            map.put("message", log.getMessage());
            map.put("createTime", log.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), current, size);
    }
}
