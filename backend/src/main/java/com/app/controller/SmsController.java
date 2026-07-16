package com.app.controller;

import com.app.common.Result;
import com.app.entity.Manufacturer;
import com.app.entity.SendRecord;
import com.app.entity.SmsTemplateEntity;
import com.app.mapper.ClientSampleItemMapper;
import com.app.mapper.ManufacturerMapper;
import com.app.service.SmsService;
import com.app.service.WeworkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private WeworkService weworkService;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private ClientSampleItemMapper clientSampleItemMapper;

    // ==================== 模板管理 ====================

    @GetMapping("/templates")
    public Result<List<SmsTemplateEntity>> listTemplates(@RequestParam(required = false) String type) {
        return Result.success(smsService.listTemplates(type));
    }

    @PostMapping("/templates")
    public Result<SmsTemplateEntity> createTemplate(@RequestBody SmsTemplateEntity template) {
        return Result.success("创建成功", smsService.createTemplate(template));
    }

    @PutMapping("/templates/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody SmsTemplateEntity template) {
        template.setId(id);
        smsService.updateTemplate(template);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        smsService.deleteTemplate(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/templates/{id}/sync-status")
    public Result<Void> syncTemplateStatus(@PathVariable Long id) {
        smsService.syncTemplateStatus(id);
        return Result.ok("同步成功");
    }

    // ==================== 短信发送 ====================

    /**
     * 模板短信发送
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendSms(@RequestBody SendSmsRequest req) {
        try {
            String taskId = smsService.sendSms(
                    req.getSmsType(),
                    req.getLianluTemplateId(),
                    req.getPhones(),
                    req.getVariables(),
                    req.getCodeName(),
                    req.getTag()
            );
            // 保存发送记录
            List<String> varNames = extractVarNames(req.getTemplateContent());
            int varCount = varNames.size();
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < req.getPhones().size(); i++) {
                SendRecord record = new SendRecord();
                record.setCodeName(req.getCodeName());
                record.setClientName(req.getClientName());
                record.setType("sms");
                record.setLianluTemplateId(req.getLianluTemplateId());
                record.setTemplateName(req.getTemplateName());
                record.setManufacturerCode(i < req.getManufacturerCodes().size() ? req.getManufacturerCodes().get(i) : null);
                record.setManufacturerName(i < req.getManufacturerNames().size() ? req.getManufacturerNames().get(i) : null);
                record.setBoothNo(i < req.getBoothNos().size() ? req.getBoothNos().get(i) : null);
                record.setPhone(req.getPhones().get(i));
                // 替换模板变量为实际值
                Map<String, String> vars = new LinkedHashMap<>();
                for (int j = 0; j < varCount && i * varCount + j < req.getVariables().size(); j++) {
                    vars.put(varNames.get(j), req.getVariables().get(i * varCount + j));
                }
                vars.put("日期", LocalDate.now().toString());
                record.setContent(fillTemplateContent(req.getTemplateContent(), vars));
                record.setSendTime(now);
                record.setSendStatus("pending");
                record.setTaskId(taskId);
                record.setTag(req.getTag());
                smsService.saveRecords(List.of(record));
            }
            return Result.success(Map.of("taskId", taskId));
        } catch (Exception e) {
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 个性短信发送
     */
    @PostMapping("/send-personal")
    public Result<?> sendPersonalSms(@RequestBody SendPersonalRequest req) {
        try {
            SmsService.SendResult result = smsService.sendPersonalSms(
                    req.getSmsType(),
                    req.getLianluTemplateId(),
                    req.getContextList(),
                    req.getTag()
            );
            if (!result.isSuccess()) {
                return Result.error(result.getError());
            }
            // 保存发送记录
            List<String> varNames = extractVarNames(req.getTemplateContent());
            int varCount = varNames.size();
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < req.getContextList().size(); i++) {
                List<String> ctx = req.getContextList().get(i);
                SendRecord record = new SendRecord();
                record.setCodeName(req.getCodeName());
                record.setClientName(req.getClientName());
                record.setType("sms");
                record.setLianluTemplateId(req.getLianluTemplateId());
                record.setTemplateName(req.getTemplateName());
                record.setPhone(String.valueOf(ctx.get(0)));
                // 替换模板变量为实际值：ctx[0]=手机号, ctx[1..]=变量值
                Map<String, String> vars = new LinkedHashMap<>();
                for (int j = 0; j < varCount && j + 1 < ctx.size(); j++) {
                    vars.put(varNames.get(j), ctx.get(j + 1));
                }
                vars.put("日期", LocalDate.now().toString());
                record.setContent(fillTemplateContent(req.getTemplateContent(), vars));
                // 优先用请求体直接传的厂商字段
                setIfNotBlank(record::setManufacturerName, getOrDefault(req.getManufacturerNames(), i));
                setIfNotBlank(record::setManufacturerCode, getOrDefault(req.getManufacturerCodes(), i));
                setIfNotBlank(record::setBoothNo, getOrDefault(req.getBoothNos(), i));
                // 兜底：从模板变量中回填（兼容旧版请求）
                fillRecordFieldsFromVars(record, varNames, ctx);
                record.setSendTime(now);
                record.setSendStatus("pending");
                record.setTaskId(result.getTaskId());
                record.setTag(req.getTag());
                smsService.saveRecords(List.of(record));
            }
            return Result.success(Map.of("taskId", result.getTaskId()));
        } catch (Exception e) {
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    // ==================== 企业微信发送 ====================

    @PostMapping("/send-wework")
    public Result<?> sendWework(@RequestBody SendWeworkRequest req) {
        if (req.getManufacturers() == null || req.getManufacturers().isEmpty()) {
            return Result.error("没有可发送的厂商");
        }

        String todayStr = java.time.LocalDate.now().toString();
        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;
        int failCount = 0;
        List<String> failedNames = new ArrayList<>();

        for (SendWeworkRequest.ManufacturerItem item : req.getManufacturers()) {
            // 查找厂商
            Manufacturer m = manufacturerMapper.selectOne(
                    new LambdaQueryWrapper<Manufacturer>()
                            .eq(Manufacturer::getManufacturerCode, item.getManufacturerCode()));

            if (m == null) {
                failCount++;
                failedNames.add(item.getManufacturerName());
                saveWeworkRecord(req, item, "fail", now);
                continue;
            }

            List<String> externalUserIds = weworkService.getBoundExternalUserIds(m.getId());
            if (externalUserIds.isEmpty()) {
                failCount++;
                failedNames.add(item.getManufacturerName());
                saveWeworkRecord(req, item, "fail", now);
                continue;
            }

            // 填充模板变量
            WeworkService.SendResult result = weworkService.sendTemplateMessage(
                    externalUserIds,
                    req.getTemplateContent(),
                    item.getManufacturerName(),
                    item.getFactoryCode(),
                    item.getBoothNo(),
                    todayStr
            );

            // 保存发送记录
            String status = result.isSuccess() ? "success" : "fail";
            saveWeworkRecord(req, item, status, now);

            if (result.isSuccess()) {
                successCount++;
            } else {
                failCount++;
                failedNames.add(item.getManufacturerName());
            }
        }

        return Result.success(Map.of(
                "successCount", successCount,
                "failCount", failCount,
                "failedNames", failedNames,
                "total", req.getManufacturers().size()
        ));
    }

    private void saveWeworkRecord(SendWeworkRequest req, SendWeworkRequest.ManufacturerItem item,
                                  String status, LocalDateTime now) {
        SendRecord record = new SendRecord();
        record.setCodeName(req.getCodeName());
        record.setClientName(req.getClientName());
        record.setType("wework");
        record.setTemplateName(req.getTemplateName());
        record.setManufacturerCode(item.getManufacturerCode());
        record.setManufacturerName(item.getManufacturerName());
        record.setPhone(item.getBoothNo()); // boothNo 借用到内容字段
        // 替换模板变量为实际值
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("厂商名称", item.getManufacturerName());
        vars.put("出厂货号", item.getFactoryCode());
        vars.put("摊位号", item.getBoothNo());
        vars.put("洽谈室号", item.getBoothNo());
        vars.put("日期", LocalDate.now().toString());
        record.setContent(fillTemplateContent(req.getTemplateContent(), vars));
        record.setSendTime(now);
        record.setSendStatus(status);
        record.setTag(req.getTag());
        smsService.saveRecords(List.of(record));
    }

    // ==================== 发送记录 ====================

    @GetMapping("/records")
    public Result<Map<String, Object>> queryRecords(
            @RequestParam(required = false) String codeName,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "500") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sendStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<SendRecord> page = smsService.queryRecords(codeName, pageNo, pageSize, type, sendStatus, search, startDate, endDate);
        return Result.success(Map.of(
                "records", page.getRecords(),
                "total", page.getTotal(),
                "pageNo", pageNo,
                "pageSize", pageSize
        ));
    }

    /**
     * 手动同步发送状态——从联麓拉取报告更新 pending 记录
     */
    @PostMapping("/sync-status")
    public Result<Map<String, Object>> syncStatus(@RequestParam String codeName) {
        int updated = smsService.syncPendingRecords(codeName);
        return Result.success(Map.of("updated", updated));
    }

    // ==================== 辅助方法 ====================

    /**
     * 将模板内容中的 {%变量名%} 替换为实际值
     */
    private String fillTemplateContent(String templateContent, Map<String, String> vars) {
        if (templateContent == null) return null;
        String result = templateContent;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("{%" + entry.getKey() + "%}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    /**
     * 从模板内容提取变量名列表（按出现顺序）
     */
    private List<String> extractVarNames(String templateContent) {
        List<String> names = new ArrayList<>();
        if (templateContent == null) return names;
        Matcher m = Pattern.compile("\\{%([^%]+)%\\}").matcher(templateContent);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    /**
     * 根据变量名从 ctx 中提取值填充 SendRecord 的厂商字段
     */
    private void fillRecordFieldsFromVars(SendRecord record, List<String> varNames, List<String> ctx) {
        for (int j = 0; j < varNames.size() && j + 1 < ctx.size(); j++) {
            String name = varNames.get(j);
            String value = ctx.get(j + 1);
            if (value == null || value.isEmpty() || "-".equals(value)) continue;
            switch (name) {
                case "厂商名称":
                    record.setManufacturerName(value);
                    break;
                case "出厂货号":
                    record.setManufacturerCode(value);
                    break;
                case "摊位号":
                case "洽谈室号":
                    record.setBoothNo(value);
                    break;
            }
        }
    }

    /**
     * 仅当字符串非空非 "-" 时调用 setter（避免覆盖已有值）
     */
    private void setIfNotBlank(Consumer<String> setter, String value) {
        if (value != null && !value.isEmpty() && !"-".equals(value)) {
            setter.accept(value);
        }
    }

    /**
     * 从列表中安全取值，越界返回 null
     */
    private String getOrDefault(List<String> list, int index) {
        if (list != null && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    // ==================== 联麓回调 ====================

    /**
     * 联麓短信回执推送回调
     * 在联麓平台配置：产品页面 > 通用管理 > 产品配置 > 推送配置 > 发送状态推送地址
     */
    @PostMapping(value = "/callback/lianlu", produces = "application/json;charset=utf-8")
    public Map<String, String> lianluCallback(@RequestBody JsonNode body) {
        String taskId = body.path("taskId").asText();
        String phone = body.path("phone").asText();
        String respCode = body.path("respCode").asText();
        String codeDesc = body.path("codeDesc").asText();
        String status = body.path("status").asText();
        long resptime = body.path("resptime").asLong();

        if (taskId.isEmpty()) {
            return Map.of("llcode", "0");
        }

        smsService.updateRecordByTaskId(taskId, phone, respCode, codeDesc, status, resptime);
        return Map.of("llcode", "0");
    }

    // ==================== 辅助查询 ====================

    @GetMapping("/balance")
    public Result<JsonNode> queryBalance(@RequestParam(defaultValue = "通知") String smsType) {
        JsonNode result = smsService.queryBalance(smsType);
        if (result != null && "00".equals(result.path("status").asText())) {
            return Result.success(result);
        }
        return Result.error("查询余额失败");
    }

    @GetMapping("/signs")
    public Result<JsonNode> querySigns(@RequestParam(defaultValue = "通知") String smsType) {
        JsonNode result = smsService.querySigns(smsType);
        if (result != null && "00".equals(result.path("status").asText())) {
            return Result.success(result);
        }
        return Result.error("查询签名失败");
    }

    @GetMapping("/manufacturer-count")
    public Result<Map<String, Object>> getManufacturerCount(@RequestParam String codeName) {
        int count = clientSampleItemMapper.countDistinctManufacturers(codeName);
        return Result.success(Map.of("codeName", codeName, "total", count));
    }

    // ==================== 请求 DTO ====================

    public static class SendSmsRequest {
        private String codeName;
        private String clientName;
        private String smsType;
        private Integer lianluTemplateId;
        private String templateName;
        private String templateContent;
        private List<String> phones;
        private List<String> manufacturerCodes;
        private List<String> manufacturerNames;
        private List<String> boothNos;
        private List<String> variables;
        private String tag;

        public String getCodeName() { return codeName; }
        public void setCodeName(String codeName) { this.codeName = codeName; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getSmsType() { return smsType; }
        public void setSmsType(String smsType) { this.smsType = smsType; }
        public Integer getLianluTemplateId() { return lianluTemplateId; }
        public void setLianluTemplateId(Integer lianluTemplateId) { this.lianluTemplateId = lianluTemplateId; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getTemplateContent() { return templateContent; }
        public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }
        public List<String> getPhones() { return phones; }
        public void setPhones(List<String> phones) { this.phones = phones; }
        public List<String> getManufacturerCodes() { return manufacturerCodes; }
        public void setManufacturerCodes(List<String> manufacturerCodes) { this.manufacturerCodes = manufacturerCodes; }
        public List<String> getManufacturerNames() { return manufacturerNames; }
        public void setManufacturerNames(List<String> manufacturerNames) { this.manufacturerNames = manufacturerNames; }
        public List<String> getBoothNos() { return boothNos; }
        public void setBoothNos(List<String> boothNos) { this.boothNos = boothNos; }
        public List<String> getVariables() { return variables; }
        public void setVariables(List<String> variables) { this.variables = variables; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
    }

    public static class SendPersonalRequest {
        private String codeName;
        private String clientName;
        private String smsType;
        private Integer lianluTemplateId;
        private String templateName;
        private String templateContent;
        private List<List<String>> contextList;
        private List<String> manufacturerNames;
        private List<String> manufacturerCodes;
        private List<String> boothNos;
        private String tag;

        public String getCodeName() { return codeName; }
        public void setCodeName(String codeName) { this.codeName = codeName; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getSmsType() { return smsType; }
        public void setSmsType(String smsType) { this.smsType = smsType; }
        public Integer getLianluTemplateId() { return lianluTemplateId; }
        public void setLianluTemplateId(Integer lianluTemplateId) { this.lianluTemplateId = lianluTemplateId; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getTemplateContent() { return templateContent; }
        public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }
        public List<List<String>> getContextList() { return contextList; }
        public void setContextList(List<List<String>> contextList) { this.contextList = contextList; }
        public List<String> getManufacturerNames() { return manufacturerNames; }
        public void setManufacturerNames(List<String> manufacturerNames) { this.manufacturerNames = manufacturerNames; }
        public List<String> getManufacturerCodes() { return manufacturerCodes; }
        public void setManufacturerCodes(List<String> manufacturerCodes) { this.manufacturerCodes = manufacturerCodes; }
        public List<String> getBoothNos() { return boothNos; }
        public void setBoothNos(List<String> boothNos) { this.boothNos = boothNos; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
    }

    public static class SendWeworkRequest {
        private String codeName;
        private String clientName;
        private String templateName;
        private String templateContent;
        private List<ManufacturerItem> manufacturers;
        private String tag;

        public String getCodeName() { return codeName; }
        public void setCodeName(String codeName) { this.codeName = codeName; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getTemplateContent() { return templateContent; }
        public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }
        public List<ManufacturerItem> getManufacturers() { return manufacturers; }
        public void setManufacturers(List<ManufacturerItem> manufacturers) { this.manufacturers = manufacturers; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }

        public static class ManufacturerItem {
            private String manufacturerCode;
            private String manufacturerName;
            private String boothNo;
            private String factoryCode;

            public String getManufacturerCode() { return manufacturerCode; }
            public void setManufacturerCode(String manufacturerCode) { this.manufacturerCode = manufacturerCode; }
            public String getManufacturerName() { return manufacturerName; }
            public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
            public String getBoothNo() { return boothNo; }
            public void setBoothNo(String boothNo) { this.boothNo = boothNo; }
            public String getFactoryCode() { return factoryCode; }
            public void setFactoryCode(String factoryCode) { this.factoryCode = factoryCode; }
        }
    }
}
