package com.app.controller;

import com.app.entity.Manufacturer;
import com.app.entity.ManufacturerWeworkBinding;
import com.app.mapper.ManufacturerMapper;
import com.app.mapper.ManufacturerWeworkBindingMapper;
import com.app.service.WeworkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wework")
public class WeworkCallbackController {

    private static final Logger log = LoggerFactory.getLogger(WeworkCallbackController.class);

    @Autowired
    private WeworkService weworkService;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private ManufacturerWeworkBindingMapper bindingMapper;

    /**
     * URL 验证（GET）：企业微信配置回调地址时会先验证
     */
    @GetMapping("/callback")
    public String verifyUrl(@RequestParam("msg_signature") String msgSignature,
                            @RequestParam("timestamp") String timestamp,
                            @RequestParam("nonce") String nonce,
                            @RequestParam("echostr") String echostr) {
        log.info("企业微信 URL 验证: timestamp={}, nonce={}", timestamp, nonce);
        String result = weworkService.verifyUrl(msgSignature, timestamp, nonce, echostr);
        if (result != null) {
            return result;
        }
        return "error";
    }

    /**
     * 消息回调（POST）：接收用户回复等事件
     */
    @PostMapping("/callback")
    public String handleCallback(@RequestParam("msg_signature") String msgSignature,
                                 @RequestParam("timestamp") String timestamp,
                                 @RequestParam("nonce") String nonce,
                                 @RequestBody String xmlBody) {
        log.info("===== 收到企业微信回调 ===== timestamp={}, nonce={}, xmlBody={}", timestamp, nonce, xmlBody);
        return weworkService.handleCallback(msgSignature, timestamp, nonce, xmlBody);
    }

    /**
     * 清除 Redis 缓存的 access_token，强制重新获取（权限变更后使用）
     */
    @PostMapping("/refresh-token")
    public Map<String, Object> refreshToken() {
        String token = weworkService.refreshAccessToken();
        if (token != null) {
            log.info("access_token 已刷新");
            return Map.of("code", 0, "message", "token 已刷新", "token_preview", token.substring(0, 8) + "...");
        }
        return Map.of("code", -1, "message", "token 刷新失败");
    }

    /**
     * 测试发送消息
     */
    @PostMapping("/send")
    public String sendTest(@RequestParam String externalUserId,
                           @RequestParam String content) {
        String result = weworkService.sendTextMessageWithResult(externalUserId, content);
        return result != null ? result : "发送失败";
    }

    /**
     * 生成厂商专属「联系我」二维码
     */
    @PostMapping("/qr-code")
    public Map<String, Object> createQrCode(@RequestBody Map<String, List<String>> body) {
        List<String> userIds = body.getOrDefault("userIds", Collections.emptyList());
        if (userIds.isEmpty()) {
            userIds = List.of("DuWenJia");
        }
        try {
            Map<String, String> result = weworkService.createManufacturerQrCode(userIds);
            if (result != null) {
                return Map.of("code", 0, "data", result);
            }
            return Map.of("code", -1, "message", "创建二维码失败");
        } catch (RuntimeException e) {
            return Map.of("code", -1, "message", e.getMessage());
        }
    }

    /**
     * 厂商绑定（两步流程）
     * 步骤1：POST { uid, phone } → 发送短信验证码
     * 步骤2：POST { uid, phone, code } → 验证码确认绑定
     */
    @PostMapping("/bind")
    public Map<String, Object> bind(@RequestBody Map<String, String> body) {
        String uid = body.get("uid");
        String phone = body.get("phone");
        String code = body.get("code");

        if (uid == null || uid.isEmpty()) {
            return Map.of("code", -1, "message", "缺少用户标识");
        }

        // 步骤2：验证码确认
        if (code != null && !code.isEmpty()) {
            Map<String, Object> result = weworkService.verifySmsBindCode(uid, code);
            return result;
        }

        // 步骤1：发送验证码
        if (phone == null || phone.isEmpty()) {
            return Map.of("code", -1, "message", "请填写手机号");
        }
        Map<String, Object> result = weworkService.sendSmsBindCode(uid, phone);
        return result;
    }

    /**
     * 查询厂商绑定列表
     */
    @GetMapping("/bindings")
    public Map<String, Object> getBindings(@RequestParam Long manufacturerId) {
        List<Map<String, Object>> list = weworkService.getBindingDetailList(manufacturerId);
        String warning = weworkService.checkBindingOverLimit(manufacturerId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("count", list.size());
        if (warning != null) {
            data.put("warning", warning);
        }
        return Map.of("code", 0, "data", data);
    }

    /**
     * 解绑（删除绑定记录）
     */
    @DeleteMapping("/bindings/{bindingId}")
    public Map<String, Object> unbind(@PathVariable Long bindingId,
                                      @RequestParam Long manufacturerId) {
        boolean ok = weworkService.unbind(manufacturerId, bindingId);
        return ok ? Map.of("code", 0, "message", "已解绑")
                  : Map.of("code", -1, "message", "解绑失败，绑定记录不存在");
    }

    /**
     * 检查绑定超限状态
     */
    @GetMapping("/bindings/check-overlimit")
    public Map<String, Object> checkOverLimit(@RequestParam Long manufacturerId) {
        String warning = weworkService.checkBindingOverLimit(manufacturerId);
        return Map.of("code", 0, "overLimit", warning != null, "warning", warning != null ? warning : "");
    }

    /**
     * 企微绑定查询（概览）：分页列出厂商的手机号及关联状态，支持按手机号搜索
     */
    @GetMapping("/bindings/overview")
    public Map<String, Object> getBindingOverview(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<Manufacturer> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Manufacturer> mqw = new LambdaQueryWrapper<>();
        if (phone != null && !phone.trim().isEmpty()) {
            String kw = phone.trim();
            mqw.and(w -> w.eq(Manufacturer::getPhone1, kw)
                    .or().eq(Manufacturer::getMobile1, kw)
                    .or().eq(Manufacturer::getPhone2, kw)
                    .or().eq(Manufacturer::getMobile2, kw)
                    .or().eq(Manufacturer::getPhone3, kw)
                    .or().eq(Manufacturer::getMobile3, kw)
                    .or().eq(Manufacturer::getSmsNumber, kw)
                    .or().eq(Manufacturer::getVisitorMobile, kw));
        }
        mqw.orderByAsc(Manufacturer::getName);
        Page<Manufacturer> result = manufacturerMapper.selectPage(pageObj, mqw);

        // 查询所有绑定记录，按 manufacturerId 分组
        List<ManufacturerWeworkBinding> allBindings = bindingMapper.selectList(null);
        Map<Long, Set<String>> boundPhonesByMfr = new LinkedHashMap<>();
        for (ManufacturerWeworkBinding b : allBindings) {
            if (b.getPhone() != null && !b.getPhone().isEmpty()) {
                boundPhonesByMfr.computeIfAbsent(b.getManufacturerId(), k -> new LinkedHashSet<>()).add(b.getPhone());
            }
        }

        // 组装返回数据
        List<Map<String, Object>> list = new ArrayList<>();
        for (Manufacturer m : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("name", m.getName() != null ? m.getName() : "");
            Set<String> boundSet = boundPhonesByMfr.getOrDefault(m.getId(), Collections.emptySet());

            item.put("mobile1", buildPhoneInfo(m.getMobile1(), boundSet));
            item.put("mobile2", buildPhoneInfo(m.getMobile2(), boundSet));
            item.put("mobile3", buildPhoneInfo(m.getMobile3(), boundSet));
            item.put("visitorMobile", buildPhoneInfo(m.getVisitorMobile(), boundSet));
            item.put("smsNumber", buildPhoneInfo(m.getSmsNumber(), boundSet));

            long boundCount = 0;
            // 去重：同一号码出现在多个字段只算1个
            Set<String> uniquePhones = new LinkedHashSet<>();
            addPhoneIfNotEmpty(uniquePhones, m.getMobile1());
            addPhoneIfNotEmpty(uniquePhones, m.getMobile2());
            addPhoneIfNotEmpty(uniquePhones, m.getMobile3());
            addPhoneIfNotEmpty(uniquePhones, m.getVisitorMobile());
            addPhoneIfNotEmpty(uniquePhones, m.getSmsNumber());
            for (String p : uniquePhones) {
                if (boundSet.contains(p)) boundCount++;
            }
            item.put("boundCount", boundCount);

            list.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("pageSize", pageSize);
        return Map.of("code", 0, "data", data);
    }

    private void addPhoneIfNotEmpty(Set<String> set, String phone) {
        if (phone != null && !phone.isEmpty()) {
            set.add(phone);
        }
    }

    private Map<String, Object> buildPhoneInfo(String phoneValue, Set<String> boundSet) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("value", phoneValue != null && !phoneValue.isEmpty() ? phoneValue : "");
        boolean bound = phoneValue != null && !phoneValue.isEmpty() && boundSet.contains(phoneValue);
        info.put("bound", bound);
        return info;
    }
}
