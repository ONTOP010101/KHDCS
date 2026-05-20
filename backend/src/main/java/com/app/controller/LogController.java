package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/login")
    public Result<PageResult<Map<String, Object>>> listLoginLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        return Result.success(logService.listLoginLogs(current, size, keyword, userId));
    }

    @GetMapping("/operation")
    public Result<PageResult<Map<String, Object>>> listOperationLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId) {
        return Result.success(logService.listOperationLogs(current, size, keyword, module, userId));
    }
}
