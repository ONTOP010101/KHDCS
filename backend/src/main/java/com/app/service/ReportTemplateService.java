package com.app.service;

import com.app.common.PageResult;
import com.app.entity.ReportTemplate;
import com.app.mapper.ReportTemplateMapper;
import com.app.mapper.UserMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportTemplateService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private UserMapper userMapper;

    /** 分页查询模板列表 */
    public PageResult<ReportTemplate> list(long current, long size, String keyword, String type) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ReportTemplate::getTitle, keyword);
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(ReportTemplate::getType, type);
        }
        wrapper.orderByDesc(ReportTemplate::getUpdateTime);

        Page<ReportTemplate> page = reportTemplateMapper.selectPage(
                new Page<>(current, size), wrapper);

        fillUpdateByName(page.getRecords());

        PageResult<ReportTemplate> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    /** 获取所有模板（不分页，供选择器使用） */
    public List<ReportTemplate> listAll(String type) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) {
            wrapper.eq(ReportTemplate::getType, type);
        }
        wrapper.orderByDesc(ReportTemplate::getUpdateTime);
        List<ReportTemplate> list = reportTemplateMapper.selectList(wrapper);
        fillUpdateByName(list);
        return list;
    }

    /** 填充 updateByName */
    private void fillUpdateByName(List<ReportTemplate> templates) {
        if (templates == null || templates.isEmpty()) return;
        var userIds = templates.stream()
                .map(ReportTemplate::getUpdateBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        var users = userMapper.selectBatchIds(userIds);
        Map<Long, String> nameMap = users.stream()
                .collect(Collectors.toMap(
                        u -> u.getId(),
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername()
                ));
        for (var tpl : templates) {
            if (tpl.getUpdateBy() != null) {
                tpl.setUpdateByName(nameMap.getOrDefault(tpl.getUpdateBy(), String.valueOf(tpl.getUpdateBy())));
            }
        }
    }

    /** 根据ID获取模板 */
    public ReportTemplate getById(Long id) {
        return reportTemplateMapper.selectById(id);
    }

    /** 创建模板 */
    public ReportTemplate create(ReportTemplate template) {
        template.setCreateBy(UserContext.getUserId());
        template.setUpdateBy(UserContext.getUserId());
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        reportTemplateMapper.insert(template);
        return template;
    }

    /** 更新模板 */
    public void update(Long id, ReportTemplate template) {
        template.setId(id);
        template.setUpdateBy(UserContext.getUserId());
        template.setUpdateTime(LocalDateTime.now());
        reportTemplateMapper.updateById(template);
    }

    /** 删除模板（逻辑删除） */
    public void delete(Long id) {
        reportTemplateMapper.deleteById(id);
    }
}
