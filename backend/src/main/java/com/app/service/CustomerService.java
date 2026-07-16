package com.app.service;

import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.entity.Customer;
import com.app.mapper.CustomerMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private static final ConcurrentHashMap<String, Boolean> CACHED_EXISTING_CODES = new ConcurrentHashMap<>();
    private static volatile boolean codesLoaded = false;

    @Autowired
    private CustomerMapper customerMapper;

    @Value("${app.upload.image-path}")
    private String imagePath;

    private static final Map<String, SFunction<Customer, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();
    static {
        SORT_FIELD_MAP.put("customerCode", Customer::getCustomerCode);
        SORT_FIELD_MAP.put("customerName", Customer::getCustomerName);
        SORT_FIELD_MAP.put("registerDate", Customer::getRegisterDate);
        SORT_FIELD_MAP.put("modifyDate", Customer::getModifyDate);
        SORT_FIELD_MAP.put("createTime", Customer::getCreateTime);
    }

    public PageResult<Customer> list(long current, long size, String keyword, String sortField, String sortOrder) {
        Page<Customer> page = new Page<>(current, size);
        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                .like(Customer::getCustomerName, keyword)
                .or()
                .like(Customer::getCustomerCode, keyword)
                .or()
                .like(Customer::getSmsNumber, keyword)
                .or()
                .like(Customer::getMobile1, keyword)
                .or()
                .like(Customer::getContactPerson1, keyword));
        }

        applySort(qw, sortField, sortOrder);
        customerMapper.selectPage(page, qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    private void applySort(LambdaQueryWrapper<Customer> qw, String sortField, String sortOrder) {
        if (StringUtils.hasText(sortField)) {
            SFunction<Customer, ?> sortFunc = SORT_FIELD_MAP.get(sortField);
            if (sortFunc != null) {
                boolean asc = !"desc".equalsIgnoreCase(sortOrder);
                if (asc) {
                    qw.orderByAsc(sortFunc);
                } else {
                    qw.orderByDesc(sortFunc);
                }
            }
        }
    }

    public Customer getById(Long id) {
        return customerMapper.selectById(id);
    }

    public Customer create(Customer customer) {
        Long userId = UserContext.getUserId();
        customer.setCreateBy(userId);
        customer.setUpdateBy(userId);
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if (!StringUtils.hasText(customer.getRegisterDate())) {
            customer.setRegisterDate(now);
        }
        if (!StringUtils.hasText(customer.getModifyDate())) {
            customer.setModifyDate(now);
        }
        // 自动生成客户编号: 5位数字递增, 从00000开始
        if (!StringUtils.hasText(customer.getCustomerCode())) {
            customer.setCustomerCode(generateCustomerCode());
        }
        // 自动设置登记人为当前用户姓名
        if (!StringUtils.hasText(customer.getRegistrant())) {
            String realName = UserContext.getRealName();
            if (StringUtils.hasText(realName)) {
                customer.setRegistrant(realName);
            }
        }
        customerMapper.insert(customer);
        if (StringUtils.hasText(customer.getCustomerCode())) {
            CACHED_EXISTING_CODES.put(customer.getCustomerCode().trim(), Boolean.TRUE);
        }
        return customer;
    }

    private String generateCustomerCode() {
        // 查询数据库中最大的5位数字编号
        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();
        qw.select(Customer::getCustomerCode)
          .orderByDesc(Customer::getCustomerCode)
          .last("LIMIT 1");
        Customer latest = customerMapper.selectOne(qw);
        int nextNum = 0;
        if (latest != null && StringUtils.hasText(latest.getCustomerCode())) {
            String code = latest.getCustomerCode().trim();
            try {
                // 尝试解析为整数并递增
                int currentMax = Integer.parseInt(code);
                nextNum = currentMax + 1;
            } catch (NumberFormatException e) {
                // 如果已有编号不是纯数字, 从0开始
            }
        }
        return String.format("%05d", nextNum);
    }

    public void update(Long id, Customer customer) {
        customer.setId(id);
        customer.setUpdateBy(UserContext.getUserId());
        customer.setModifier(UserContext.getUsername());
        customer.setModifyDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        customerMapper.updateById(customer);
    }

    public void delete(Long id) {
        customerMapper.deleteById(id);
    }

    public void deleteBatch(Long[] ids) {
        customerMapper.deleteBatchIds(Arrays.asList(ids));
    }

    public String uploadCertificate(Long id, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "customer_cert_" + id + "_" + System.currentTimeMillis() + ext;
        try {
            Path dir = Paths.get(imagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path filePath = dir.resolve(filename);
            file.transferTo(filePath.toFile());

            // 使用 LambdaUpdateWrapper 只更新指定字段，避免 updateById 的全字段覆盖问题
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            LambdaUpdateWrapper<Customer> uw = new LambdaUpdateWrapper<>();
            uw.set(Customer::getCertificate, filename)
              .set(Customer::getModifyDate, now)
              .set(Customer::getUpdateBy, UserContext.getUserId())
              .eq(Customer::getId, id);
            customerMapper.update(null, uw);

            return filename;
        } catch (Exception e) {
            throw new RuntimeException("上传营业执照失败: " + e.getMessage(), e);
        }
    }

    public void deleteCertificate(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null || !StringUtils.hasText(customer.getCertificate())) {
            return;
        }
        // 删除磁盘文件
        try {
            Path filePath = Paths.get(imagePath, customer.getCertificate());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("删除营业执照文件失败: {}", e.getMessage());
        }
        // 清空数据库字段
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        LambdaUpdateWrapper<Customer> uw = new LambdaUpdateWrapper<>();
        uw.set(Customer::getCertificate, "")
          .set(Customer::getModifyDate, now)
          .set(Customer::getUpdateBy, UserContext.getUserId())
          .eq(Customer::getId, id);
        customerMapper.update(null, uw);
    }

    @Transactional
    public ImportResult batchInsert(List<Customer> customers, boolean updateMode) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;
        int duplicateCount = 0;
        int updatedCount = 0;

        if (customers == null || customers.isEmpty()) {
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setDuplicateCount(0);
            result.setUpdatedCount(0);
            result.setFailedRows(failedRows);
            return result;
        }

        ensureCodesCacheLoaded();
        Set<String> importedCodes = new HashSet<>();

        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            try {
                StringBuilder rowErrors = new StringBuilder();
                boolean isDuplicate = false;

                if (!StringUtils.hasText(c.getCustomerCode()) && !StringUtils.hasText(c.getCustomerName())) {
                    rowErrors.append("客户编号和客户名称均为空; ");
                }

                if (StringUtils.hasText(c.getCustomerCode())) {
                    String code = c.getCustomerCode().trim();
                    if (CACHED_EXISTING_CODES.containsKey(code)) {
                        if (updateMode) {
                            truncateFields(c);
                            Long userId = UserContext.getUserId();
                            c.setUpdateBy(userId);
                            LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();
                            qw.eq(Customer::getCustomerCode, code).last("LIMIT 1");
                            Customer existing = customerMapper.selectOne(qw);
                            if (existing != null) {
                                c.setId(existing.getId());
                                c.setCreateBy(existing.getCreateBy());
                                c.setCreateTime(existing.getCreateTime());
                                customerMapper.updateById(c);
                                updatedCount++;
                                continue;
                            }
                        } else {
                            rowErrors.append("客户编号[").append(code).append("]已存在于数据库; ");
                            isDuplicate = true;
                        }
                    } else if (importedCodes.contains(code)) {
                        rowErrors.append("客户编号[").append(code).append("]在导入数据中重复; ");
                        isDuplicate = true;
                    }
                }

                if (rowErrors.length() > 0) {
                    Map<String, String> failRow = new LinkedHashMap<>();
                    failRow.put("row", String.valueOf(i + 1));
                    failRow.put("客户编号", c.getCustomerCode() != null ? c.getCustomerCode() : "");
                    failRow.put("客户名称", c.getCustomerName() != null ? c.getCustomerName() : "");
                    failRow.put("失败原因", rowErrors.toString());
                    failRow.put("类型", isDuplicate ? "重复" : "校验失败");
                    failedRows.add(failRow);
                    if (isDuplicate) duplicateCount++;
                    continue;
                }

                truncateFields(c);
                Long userId = UserContext.getUserId();
                c.setCreateBy(userId);
                c.setUpdateBy(userId);
                c.setId(null);
                customerMapper.insert(c);

                if (StringUtils.hasText(c.getCustomerCode())) {
                    String code = c.getCustomerCode().trim();
                    importedCodes.add(code);
                    CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("批量导入客户第{}条失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                failRow.put("客户编号", c.getCustomerCode() != null ? c.getCustomerCode() : "");
                failRow.put("客户名称", c.getCustomerName() != null ? c.getCustomerName() : "");
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failRow.put("类型", "异常");
                failedRows.add(failRow);
            }
        }

        result.setTotalCount(customers.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size() - duplicateCount);
        result.setDuplicateCount(duplicateCount);
        result.setUpdatedCount(updatedCount);
        result.setFailedRows(failedRows);
        return result;
    }

    private void ensureCodesCacheLoaded() {
        if (codesLoaded) return;
        synchronized (CustomerService.class) {
            if (codesLoaded) return;
            List<Customer> all = customerMapper.selectList(null);
            for (Customer c : all) {
                if (StringUtils.hasText(c.getCustomerCode())) {
                    CACHED_EXISTING_CODES.put(c.getCustomerCode().trim(), Boolean.TRUE);
                }
            }
            codesLoaded = true;
        }
    }

    private void truncateFields(Customer c) {
        int maxLen;
        maxLen = 50; if (c.getCustomerCode() != null && c.getCustomerCode().length() > maxLen) { c.setCustomerCode(c.getCustomerCode().substring(0, maxLen)); }
        maxLen = 100; if (c.getCustomerName() != null && c.getCustomerName().length() > maxLen) { c.setCustomerName(c.getCustomerName().substring(0, maxLen)); }
        maxLen = 50; if (c.getCountry() != null && c.getCountry().length() > maxLen) { c.setCountry(c.getCountry().substring(0, maxLen)); }
        maxLen = 200; if (c.getAddress() != null && c.getAddress().length() > maxLen) { c.setAddress(c.getAddress().substring(0, maxLen)); }
        maxLen = 50; if (c.getContactPerson1() != null && c.getContactPerson1().length() > maxLen) { c.setContactPerson1(c.getContactPerson1().substring(0, maxLen)); }
        maxLen = 20; if (c.getMobile1() != null && c.getMobile1().length() > maxLen) { c.setMobile1(c.getMobile1().substring(0, maxLen)); }
        maxLen = 20; if (c.getPhone1() != null && c.getPhone1().length() > maxLen) { c.setPhone1(c.getPhone1().substring(0, maxLen)); }
        maxLen = 100; if (c.getEmail() != null && c.getEmail().length() > maxLen) { c.setEmail(c.getEmail().substring(0, maxLen)); }
        maxLen = 20; if (c.getQq() != null && c.getQq().length() > maxLen) { c.setQq(c.getQq().substring(0, maxLen)); }
        maxLen = 50; if (c.getModifier() != null && c.getModifier().length() > maxLen) { c.setModifier(c.getModifier().substring(0, maxLen)); }
        maxLen = 50; if (c.getContactPerson2() != null && c.getContactPerson2().length() > maxLen) { c.setContactPerson2(c.getContactPerson2().substring(0, maxLen)); }
        maxLen = 20; if (c.getMobile2() != null && c.getMobile2().length() > maxLen) { c.setMobile2(c.getMobile2().substring(0, maxLen)); }
        maxLen = 20; if (c.getPhone2() != null && c.getPhone2().length() > maxLen) { c.setPhone2(c.getPhone2().substring(0, maxLen)); }
        maxLen = 200; if (c.getRemark1() != null && c.getRemark1().length() > maxLen) { c.setRemark1(c.getRemark1().substring(0, maxLen)); }
        maxLen = 50; if (c.getRegistrant() != null && c.getRegistrant().length() > maxLen) { c.setRegistrant(c.getRegistrant().substring(0, maxLen)); }
        maxLen = 50; if (c.getContactPerson3() != null && c.getContactPerson3().length() > maxLen) { c.setContactPerson3(c.getContactPerson3().substring(0, maxLen)); }
        maxLen = 20; if (c.getMobile3() != null && c.getMobile3().length() > maxLen) { c.setMobile3(c.getMobile3().substring(0, maxLen)); }
        maxLen = 20; if (c.getPhone3() != null && c.getPhone3().length() > maxLen) { c.setPhone3(c.getPhone3().substring(0, maxLen)); }
        maxLen = 200; if (c.getRemark2() != null && c.getRemark2().length() > maxLen) { c.setRemark2(c.getRemark2().substring(0, maxLen)); }
        maxLen = 50; if (c.getRegion() != null && c.getRegion().length() > maxLen) { c.setRegion(c.getRegion().substring(0, maxLen)); }
        maxLen = 50; if (c.getSmsNumber() != null && c.getSmsNumber().length() > maxLen) { c.setSmsNumber(c.getSmsNumber().substring(0, maxLen)); }
        maxLen = 100; if (c.getCertificate() != null && c.getCertificate().length() > maxLen) { c.setCertificate(c.getCertificate().substring(0, maxLen)); }
    }
}
