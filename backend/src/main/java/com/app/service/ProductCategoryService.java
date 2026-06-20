package com.app.service;

import com.app.common.PageResult;
import com.app.entity.ProductCategory;
import com.app.mapper.ProductCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryMapper mapper;

    public PageResult<ProductCategory> list(long current, long size, String keyword, Integer level, String parentCode) {
        Page<ProductCategory> page = new Page<>(current, size);
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                .like(ProductCategory::getCode, keyword)
                .or()
                .like(ProductCategory::getName, keyword));
        }
        if (level != null) {
            qw.eq(ProductCategory::getLevel, level);
        }
        if (StringUtils.hasText(parentCode)) {
            qw.eq(ProductCategory::getParentCode, parentCode);
        }
        qw.orderByAsc(ProductCategory::getCode);

        mapper.selectPage(page, qw);
        List<ProductCategory> records = page.getRecords();
        records.sort(Comparator.comparingInt(r -> {
            try { return Integer.parseInt(r.getCode()); } catch (Exception e) { return 0; }
        }));
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<ProductCategory> listAll() {
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(ProductCategory::getCode);
        List<ProductCategory> list = mapper.selectList(qw);
        list.sort(Comparator.comparingInt(r -> {
            try { return Integer.parseInt(r.getCode()); } catch (Exception e) { return 0; }
        }));
        return list;
    }

    public ProductCategory getById(Long id) {
        return mapper.selectById(id);
    }

    public ProductCategory create(ProductCategory category) {
        mapper.insert(category);
        return category;
    }

    public void update(Long id, ProductCategory category) {
        category.setId(id);
        mapper.updateById(category);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Transactional
    public void deleteBatch(Long[] ids) {
        mapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Transactional
    public Map<String, Object> batchImport(List<ProductCategory> categories) {
        int success = 0, fail = 0, duplicate = 0;
        List<Map<String, String>> failedRows = new ArrayList<>();

        for (int i = 0; i < categories.size(); i++) {
            ProductCategory cat = categories.get(i);
            try {
                if (!StringUtils.hasText(cat.getCode()) || !StringUtils.hasText(cat.getName())) {
                    fail++;
                    Map<String, String> fr = new LinkedHashMap<>();
                    fr.put("row", String.valueOf(i + 1));
                    fr.put("失败原因", "编号或名称为空");
                    failedRows.add(fr);
                    continue;
                }
                // 检查编码是否已存在
                LambdaQueryWrapper<ProductCategory> eq = new LambdaQueryWrapper<>();
                eq.eq(ProductCategory::getCode, cat.getCode());
                if (mapper.selectCount(eq) > 0) {
                    duplicate++;
                    Map<String, String> fr = new LinkedHashMap<>();
                    fr.put("row", String.valueOf(i + 1));
                    fr.put("编号", cat.getCode());
                    fr.put("名称", cat.getName());
                    fr.put("失败原因", "编号已存在");
                    failedRows.add(fr);
                    continue;
                }
                mapper.insert(cat);
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

    /** 获取所有种类编号集合，用于导入校验 */
    public Set<String> getAllCategoryCodes() {
        return mapper.selectList(null).stream()
            .map(ProductCategory::getCode)
            .collect(Collectors.toSet());
    }

    /** 获取所有有关键词的种类，用于导入时自动匹配 */
    public List<ProductCategory> getKeywordsMap() {
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<>();
        qw.isNotNull(ProductCategory::getKeywords);
        qw.ne(ProductCategory::getKeywords, "");
        return mapper.selectList(qw);
    }
}
