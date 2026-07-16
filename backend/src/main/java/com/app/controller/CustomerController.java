package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.entity.Customer;
import com.app.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Value("${app.upload.image-path}")
    private String imagePath;

    @GetMapping
    public Result<PageResult<Customer>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "500") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(customerService.list(current, size, keyword, sortField, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer customer) {
        return Result.success("创建成功", customerService.create(customer));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Customer customer) {
        customerService.update(id, customer);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody Long[] ids) {
        customerService.deleteBatch(ids);
        return Result.ok("批量删除成功");
    }

    @PostMapping("/batch-import")
    public Result<ImportResult> batchImport(@RequestBody List<Customer> customers,
                                            @RequestParam(defaultValue = "false") boolean updateMode) {
        try {
            ImportResult result = customerService.batchInsert(customers, updateMode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入客户失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/{id}/certificate/image")
    public ResponseEntity<byte[]> getCertificateImage(@PathVariable Long id) {
        try {
            Customer customer = customerService.getById(id);
            if (customer == null || !StringUtils.hasText(customer.getCertificate())) {
                return ResponseEntity.notFound().build();
            }
            Path filePath = Paths.get(imagePath, customer.getCertificate());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "image/png";
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(bytes);
        } catch (Exception e) {
            log.error("获取营业执照图片失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/certificate")
    public Result<Map<String, String>> uploadCertificate(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) {
        try {
            Customer customer = customerService.getById(id);
            if (customer == null) {
                return Result.error(404, "客户不存在");
            }
            String result = customerService.uploadCertificate(id, file);
            Map<String, String> data = new java.util.HashMap<>();
            data.put("filePath", result);
            return Result.success(data);
        } catch (Exception e) {
            log.error("上传营业执照失败: {}", e.getMessage(), e);
            return Result.error(500, "上传失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/{id}/certificate")
    public Result<Void> deleteCertificate(@PathVariable Long id) {
        try {
            customerService.deleteCertificate(id);
            return Result.ok("营业执照已删除");
        } catch (Exception e) {
            log.error("删除营业执照失败: {}", e.getMessage(), e);
            return Result.error(500, "删除失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
}
