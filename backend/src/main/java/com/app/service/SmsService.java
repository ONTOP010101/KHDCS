package com.app.service;

import com.app.config.SmsConfig;
import com.app.entity.SmsTemplateEntity;
import com.app.entity.SendRecord;
import com.app.mapper.SmsTemplateMapper;
import com.app.mapper.SendRecordMapper;
import com.app.util.SmsSignUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private SmsTemplateMapper templateMapper;

    @Autowired
    private SendRecordMapper sendRecordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ==================== 模板管理 ====================

    /**
     * 查询模板列表
     */
    public List<SmsTemplateEntity> listTemplates(String type) {
        LambdaQueryWrapper<SmsTemplateEntity> qw = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            qw.eq(SmsTemplateEntity::getType, type);
        }
        qw.orderByDesc(SmsTemplateEntity::getCreateTime);
        return templateMapper.selectList(qw);
    }

    /**
     * 创建模板 (本地 + 联麓远程)
     */
    public SmsTemplateEntity createTemplate(SmsTemplateEntity template) {
        if (!"sms".equals(template.getType())) {
            // 微信/企业微信模板仅本地存储
            template.setStatus(1); // 无需审核
            templateMapper.insert(template);
            return template;
        }
        // 短信模板：同步到联麓
        if (template.getSignId() == null || template.getSignId() <= 0) {
            throw new RuntimeException("短信模板缺少有效签名ID，请先选择签名");
        }
        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(template.getSmsType());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        params.put("SignId", template.getSignId());
        params.put("TemplateName", template.getTemplateName());
        params.put("content", template.getContent());
        params.put("ApplyPurpose", template.getApplyPurpose() != null ? template.getApplyPurpose() : "B06-其他业务管理服务类");
        params.put("VariableType", template.getVariableType() != null ? template.getVariableType() : "number_letter");
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        log.info("联麓创建模板 params: {}", params);
        JsonNode resp = callLianlu("/sms/product/template/create", params);
        if (resp != null && "00".equals(resp.path("status").asText())) {
            template.setLianluTemplateId(resp.path("TemplateId").asInt());
            template.setStatus(0); // 待审核
        } else {
            String msg = resp != null ? resp.path("message").asText() : "创建模板失败";
            log.warn("联麓创建模板失败: {}", msg);
            // 仍保存本地
            template.setStatus(2); // 标记为异常
        }
        templateMapper.insert(template);
        return template;
    }

    /**
     * 更新模板 (本地 + 联麓远程)
     */
    public void updateTemplate(SmsTemplateEntity template) {
        SmsTemplateEntity existing = templateMapper.selectById(template.getId());
        if (existing == null) return;

        if ("sms".equals(existing.getType()) && existing.getLianluTemplateId() != null) {
            SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(existing.getSmsType());
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("MchId", smsConfig.getMchId());
            params.put("AppId", app.getAppId());
            params.put("Version", smsConfig.getVersion());
            params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("SignType", smsConfig.getSignType());
            params.put("TemplateId", String.valueOf(existing.getLianluTemplateId()));
            if (template.getTemplateName() != null) {
                params.put("TemplateName", template.getTemplateName());
            }
            if (template.getContent() != null) {
                params.put("content", template.getContent());
            }
            if (template.getSignId() != null) {
                params.put("SignId", String.valueOf(template.getSignId()));
            }
            if (template.getApplyPurpose() != null && !template.getApplyPurpose().isEmpty()) {
                params.put("ApplyPurpose", template.getApplyPurpose());
            }
            if (template.getVariableType() != null && !template.getVariableType().isEmpty()) {
                params.put("VariableType", template.getVariableType());
            }
            params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

            log.info("联麓更新模板 params: {}", params);
            JsonNode resp = callLianlu("/sms/product/template/update", params);
            if (resp == null || !"00".equals(resp.path("status").asText())) {
                String msg = resp != null ? resp.path("message").asText() : "更新模板失败";
                throw new RuntimeException("联麓更新模板失败: " + msg);
            }
            // 重新拉取联麓实际状态（更新后联麓会重置为待审核）
            syncTemplateStatus(template.getId());
        }

        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
    }

    /**
     * 删除模板
     */
    public void deleteTemplate(Long id) {
        SmsTemplateEntity template = templateMapper.selectById(id);
        if (template == null) return;

        if ("sms".equals(template.getType()) && template.getLianluTemplateId() != null) {
            SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(template.getSmsType());
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("MchId", smsConfig.getMchId());
            params.put("AppId", app.getAppId());
            params.put("Version", smsConfig.getVersion());
            params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("TemplateId", String.valueOf(template.getLianluTemplateId()));
            params.put("SignType", smsConfig.getSignType());
            params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

            callLianlu("/sms/product/template/delete", params);
        }
        templateMapper.deleteById(id);
    }

    /**
     * 从联麓同步模板状态
     */
    public void syncTemplateStatus(Long localId) {
        SmsTemplateEntity template = templateMapper.selectById(localId);
        if (template == null || template.getLianluTemplateId() == null) return;

        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(template.getSmsType());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        params.put("TemplateId", template.getLianluTemplateId());
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        JsonNode resp = callLianlu("/sms/product/template/getById", params);
        if (resp != null && "00".equals(resp.path("status").asText())) {
            JsonNode data = resp.path("data");
            // 联麓 API 状态: 1=审核通过, 2=待审核, 3=审核驳回
            // 本地状态: 0=待审核, 1=审核通过, 2=审核驳回
            int lianluStatus = data.path("status").asInt();
            int localStatus;
            switch (lianluStatus) {
                case 1: localStatus = 1; break; // 审核通过
                case 3: localStatus = 2; break; // 审核驳回
                default: localStatus = 0; break; // 待审核 (联麓=2 或其他)
            }
            template.setStatus(localStatus);
            String refuseReason = data.path("refuseReason").asText();
            if (refuseReason != null && !refuseReason.isEmpty()) {
                template.setRefuseReason(refuseReason);
            }
            templateMapper.updateById(template);
        }
    }

    // ==================== 短信发送 ====================

    /**
     * 模板短信发送
     */
    public String sendSms(String smsType, Integer lianluTemplateId, List<String> phones, List<String> variables, String codeName, String tag) {
        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(smsType);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("Type", "3"); // 模板发送
        params.put("TemplateId", String.valueOf(lianluTemplateId));
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        // PhoneNumberSet 不参与签名
        params.put("PhoneNumberSet", phones);
        params.put("TemplateParamSet", variables != null ? variables : Collections.emptyList());
        if (tag != null && !tag.isEmpty()) {
            params.put("Tag", tag);
        }
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        JsonNode resp = callLianlu("/sms/trade/template/send", params);
        if (resp != null && "00".equals(resp.path("status").asText())) {
            return resp.path("taskId").asText();
        }
        String msg = resp != null ? resp.path("message").asText() : "发送失败";
        throw new RuntimeException("短信发送失败: " + msg);
    }

    /**
     * 个性短信发送 (不同手机不同内容)
     * ContextParamSet: [["手机号","变量1","变量2"], ...]
     */
    public SendResult sendPersonalSms(String smsType, Integer lianluTemplateId, List<List<String>> contextList, String tag) {
        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(smsType);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("Type", "2"); // 个性发送
        params.put("TemplateId", String.valueOf(lianluTemplateId));
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        if (tag != null && !tag.isEmpty()) {
            params.put("Tag", tag);
        }
        params.put("ContextParamSet", contextList);
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        JsonNode resp = callLianlu("/sms/trade/personal/send", params);
        SendResult result = new SendResult();
        if (resp != null && "00".equals(resp.path("status").asText())) {
            result.setTaskId(resp.path("taskId").asText());
            result.setCount(resp.path("count").asInt());
        } else {
            result.setTaskId(null);
            String msg = resp != null ? resp.path("message").asText() : "发送失败";
            result.setError(msg);
        }
        return result;
    }

    // ==================== 发送记录 ====================

    /**
     * 查询发送记录
     */
    public Page<SendRecord> queryRecords(String codeName, Integer pageNo, Integer pageSize, String type, String sendStatus, String search, String startDate, String endDate) {
        LambdaQueryWrapper<SendRecord> qw = new LambdaQueryWrapper<>();
        if (codeName != null && !codeName.isEmpty()) {
            qw.eq(SendRecord::getCodeName, codeName);
        }
        if (type != null && !type.isEmpty()) {
            qw.eq(SendRecord::getType, type);
        }
        if (sendStatus != null && !sendStatus.isEmpty()) {
            qw.eq(SendRecord::getSendStatus, sendStatus);
        }
        if (search != null && !search.isEmpty()) {
            qw.and(w -> w.like(SendRecord::getTemplateName, search)
                    .or().like(SendRecord::getManufacturerName, search)
                    .or().like(SendRecord::getPhone, search));
        }
        if (startDate != null && !startDate.isEmpty()) {
            qw.ge(SendRecord::getSendTime, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            qw.le(SendRecord::getSendTime, endDate);
        }
        qw.orderByDesc(SendRecord::getCreateTime);
        return sendRecordMapper.selectPage(new Page<>(pageNo, pageSize), qw);
    }

    /**
     * 批量保存发送记录
     */
    public void saveRecords(List<SendRecord> records) {
        for (SendRecord r : records) {
            sendRecordMapper.insert(r);
        }
    }

    /**
     * 根据 taskId + phone 更新回执状态
     */
    @Transactional
    public void updateRecordByTaskId(String taskId, String phone, String respCode,
                                      String codeDesc, String status, long resptime) {
        log.info("收到联麓回执推送: taskId={}, phone={}, respCode={}, codeDesc={}, resptime={}",
                taskId, phone, respCode, codeDesc, resptime);

        LambdaQueryWrapper<SendRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(SendRecord::getTaskId, taskId);
        if (phone != null && !phone.isEmpty()) {
            qw.eq(SendRecord::getPhone, phone);
        }
        List<SendRecord> records = sendRecordMapper.selectList(qw);

        if (records.isEmpty()) {
            log.warn("联麓回执未匹配到发送记录: taskId={}, phone={}", taskId, phone);
            return;
        }

        log.info("联麓回执匹配到 {} 条记录，开始更新状态", records.size());
        for (SendRecord r : records) {
            if ("DELIVRD".equals(respCode)) {
                r.setSendStatus("success");
            } else {
                r.setSendStatus("fail");
            }
            r.setRespCode(respCode);
            r.setCodeDesc(codeDesc);
            if (resptime > 0) {
                r.setRespTime(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(resptime),
                        java.time.ZoneId.systemDefault()));
            }
            sendRecordMapper.updateById(r);
            log.info("发送记录状态已更新: id={}, phone={}, status={}, respCode={}",
                    r.getId(), r.getPhone(), r.getSendStatus(), r.getRespCode());
        }
    }

    /**
     * 主动拉取联麓发送报告，更新 pending 记录
     * @param taskId 联麓任务ID
     * @param smsType 短信类型（通知/营销）
     * @return 更新的记录数
     */
    public int pullReport(String taskId, String smsType) {
        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(smsType);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        params.put("TaskId", taskId);
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        log.info("拉取联麓报告: taskId={}, smsType={}", taskId, smsType);
        JsonNode resp = callLianlu("/sms/trade/report", params);
        if (resp == null || !"00".equals(resp.path("status").asText())) {
            log.warn("拉取联麓报告失败: taskId={}, resp={}", taskId, resp);
            return 0;
        }

        JsonNode data = resp.path("data");
        if (!data.isArray()) return 0;

        int updated = 0;
        for (JsonNode item : data) {
            String phone = item.path("phone").asText();
            String respCode = item.path("respCode").asText();
            String codeDesc = item.path("codeDesc").asText();
            String reportStatus = item.path("status").asText();
            String respTime = item.path("respTime").asText();

            // 只处理有最终状态的
            if (reportStatus == null || reportStatus.isEmpty() || "0".equals(reportStatus)) continue;

            // 查找匹配记录
            LambdaQueryWrapper<SendRecord> qw = new LambdaQueryWrapper<>();
            qw.eq(SendRecord::getTaskId, taskId);
            if (phone != null && !phone.isEmpty()) {
                qw.eq(SendRecord::getPhone, phone);
            }
            List<SendRecord> records = sendRecordMapper.selectList(qw);
            for (SendRecord r : records) {
                if ("DELIVRD".equals(respCode)) {
                    r.setSendStatus("success");
                } else {
                    r.setSendStatus("fail");
                }
                r.setRespCode(respCode);
                r.setCodeDesc(codeDesc);
                if (respTime != null && !respTime.isEmpty()) {
                    try {
                        r.setRespTime(LocalDateTime.parse(respTime,
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } catch (Exception ignored) {}
                }
                sendRecordMapper.updateById(r);
                updated++;
            }
        }
        log.info("拉取联麓报告完成: taskId={}, 更新 {} 条记录", taskId, updated);
        return updated;
    }

    /**
     * 同步指定代号下所有 pending 状态的发送记录
     * 收集所有不同的 taskId，逐个拉取报告
     */
    public int syncPendingRecords(String codeName) {
        LambdaQueryWrapper<SendRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(SendRecord::getCodeName, codeName);
        qw.eq(SendRecord::getSendStatus, "pending");
        qw.isNotNull(SendRecord::getTaskId);
        qw.ne(SendRecord::getTaskId, "");
        // 只取最近24小时的，避免扫太多旧数据
        qw.ge(SendRecord::getCreateTime, LocalDateTime.now().minusHours(24));
        List<SendRecord> records = sendRecordMapper.selectList(qw);

        // 收集去重的 taskId
        Set<String> taskIds = records.stream()
                .map(SendRecord::getTaskId)
                .collect(java.util.stream.Collectors.toSet());

        log.info("同步 pending 记录: codeName={}, taskId 数={}", codeName, taskIds.size());

        int total = 0;
        for (String taskId : taskIds) {
            // 先试通知，再试营销（无法从记录直接获取 smsType）
            int updated = pullReport(taskId, "通知");
            if (updated == 0) {
                updated = pullReport(taskId, "营销");
            }
            total += updated;
        }
        return total;
    }

    // ==================== 余额查询 ====================

    public JsonNode queryBalance(String smsType) {
        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(smsType);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        return callLianlu("/sms/product/balance", params);
    }

    // ==================== 签名查询 ====================

    public JsonNode querySigns(String smsType) {
        SmsConfig.AppKeyConfig app = smsConfig.getAppConfig(smsType);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MchId", smsConfig.getMchId());
        params.put("AppId", app.getAppId());
        params.put("Version", smsConfig.getVersion());
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("SignType", smsConfig.getSignType());
        params.put("Signature", SmsSignUtil.sign(params, app.getAppKey()));

        return callLianlu("/sms/product/sign/get", params);
    }

    // ==================== 底层 API 调用 ====================

    private JsonNode callLianlu(String path, Map<String, Object> params) {
        try {
            // 序列化参数（排除签名排除的 key）
            Map<String, Object> bodyParams = new LinkedHashMap<>(params);
            bodyParams.remove("Signature");
            bodyParams.remove("PhoneNumberSet");
            bodyParams.remove("SessionContext");
            bodyParams.remove("SessionContextSet");
            bodyParams.remove("ContextParamSet");
            bodyParams.remove("TemplateParamSet");
            bodyParams.remove("PhoneList");
            bodyParams.remove("phoneSet");

            // 构建完整请求体
            Map<String, Object> requestBody = new LinkedHashMap<>(bodyParams);
            requestBody.put("Signature", params.get("Signature"));
            if (params.containsKey("PhoneNumberSet")) {
                requestBody.put("PhoneNumberSet", params.get("PhoneNumberSet"));
            }
            if (params.containsKey("TemplateParamSet")) {
                requestBody.put("TemplateParamSet", params.get("TemplateParamSet"));
            }
            if (params.containsKey("ContextParamSet")) {
                requestBody.put("ContextParamSet", params.get("ContextParamSet"));
            }

            String json = objectMapper.writeValueAsString(requestBody);
            log.debug("联麓 API 请求 [{}]: {}", path, json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(smsConfig.getApiBase() + path))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("联麓 API 响应 [{}]: {}", path, response.body());

            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            log.error("联麓 API 调用失败 [{}]: {}", path, e.getMessage());
            return null;
        }
    }

    // ==================== 内部类 ====================

    public static class SendResult {
        private String taskId;
        private String error;
        private int count;

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public boolean isSuccess() { return taskId != null; }
    }
}
