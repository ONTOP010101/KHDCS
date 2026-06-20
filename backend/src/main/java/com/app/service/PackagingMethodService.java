package com.app.service;

import com.app.common.PageResult;
import com.app.entity.PackagingMethod;
import com.app.mapper.PackagingMethodMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PackagingMethodService {

    @Autowired
    private PackagingMethodMapper mapper;

    public PageResult<PackagingMethod> list(long current, long size, String keyword) {
        Page<PackagingMethod> page = new Page<>(current, size);
        LambdaQueryWrapper<PackagingMethod> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                .like(PackagingMethod::getCode, keyword)
                .or()
                .like(PackagingMethod::getName, keyword)
                .or()
                .like(PackagingMethod::getNameEn, keyword));
        }
        qw.orderByAsc(PackagingMethod::getCode);

        mapper.selectPage(page, qw);
        List<PackagingMethod> records = page.getRecords();
        records.sort(Comparator.comparingInt(r -> {
            try { return Integer.parseInt(r.getCode()); } catch (Exception e) { return 0; }
        }));
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<PackagingMethod> listAll() {
        LambdaQueryWrapper<PackagingMethod> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(PackagingMethod::getCode);
        List<PackagingMethod> list = mapper.selectList(qw);
        list.sort(Comparator.comparingInt(r -> {
            try { return Integer.parseInt(r.getCode()); } catch (NumberFormatException e) { return 0; }
        }));
        return list;
    }

    public PackagingMethod getById(Long id) {
        return mapper.selectById(id);
    }

    public PackagingMethod create(PackagingMethod packaging) {
        mapper.insert(packaging);
        return packaging;
    }

    public void update(Long id, PackagingMethod packaging) {
        packaging.setId(id);
        mapper.updateById(packaging);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Transactional
    public void deleteBatch(Long[] ids) {
        mapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Transactional
    public Map<String, Object> batchImport(List<PackagingMethod> packagings) {
        int success = 0, fail = 0, duplicate = 0;
        List<Map<String, String>> failedRows = new ArrayList<>();

        for (int i = 0; i < packagings.size(); i++) {
            PackagingMethod pkg = packagings.get(i);
            try {
                if (!StringUtils.hasText(pkg.getCode()) || !StringUtils.hasText(pkg.getName())) {
                    fail++;
                    Map<String, String> fr = new LinkedHashMap<>();
                    fr.put("row", String.valueOf(i + 1));
                    fr.put("失败原因", "编号或名称为空");
                    failedRows.add(fr);
                    continue;
                }
                LambdaQueryWrapper<PackagingMethod> eq = new LambdaQueryWrapper<>();
                eq.eq(PackagingMethod::getCode, pkg.getCode());
                if (mapper.selectCount(eq) > 0) {
                    duplicate++;
                    Map<String, String> fr = new LinkedHashMap<>();
                    fr.put("row", String.valueOf(i + 1));
                    fr.put("编号", pkg.getCode());
                    fr.put("名称", pkg.getName());
                    fr.put("失败原因", "编号已存在");
                    failedRows.add(fr);
                    continue;
                }
                mapper.insert(pkg);
                success++;
            } catch (Exception e) {
                fail++;
                Map<String, String> fr = new LinkedHashMap<>();
                fr.put("row", String.valueOf(i + 1));
                fr.put("失败原因", e.getMessage());
                failedRows.add(fr);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", success);
        result.put("failCount", fail);
        result.put("duplicateCount", duplicate);
        result.put("failedRows", failedRows);
        return result;
    }

    /** 获取所有包装编号集合，用于导入校验 */
    public Set<String> getAllPackageCodes() {
        return mapper.selectList(null).stream()
            .map(PackagingMethod::getCode)
            .collect(Collectors.toSet());
    }
}
