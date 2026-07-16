package com.app.service;

import com.app.config.WeworkConfig;
import com.app.entity.Manufacturer;
import com.app.entity.ManufacturerWeworkBinding;
import com.app.mapper.ManufacturerMapper;
import com.app.mapper.ManufacturerWeworkBindingMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WeworkService {

    private static final Logger log = LoggerFactory.getLogger(WeworkService.class);
    private static final String ACCESS_TOKEN_KEY = "wework:access_token";
    private static final String CONTACT_ACCESS_TOKEN_KEY = "wework:contact_access_token";
    private static final String SMS_BIND_PREFIX = "wework:sms_bind:";

    @Autowired
    private WeworkConfig config;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private ManufacturerWeworkBindingMapper bindingMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ==================== Access Token ====================

    public String getAccessToken() {
        // 先从 Redis 取
        Object cached = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (cached != null) {
            return cached.toString();
        }
        return fetchAndCacheToken();
    }

    /**
     * 获取客户联系专用 access_token（用于 externalcontact 接口）
     */
    public String getContactAccessToken() {
        Object cached = redisTemplate.opsForValue().get(CONTACT_ACCESS_TOKEN_KEY);
        if (cached != null) {
            return cached.toString();
        }
        return fetchAndCacheContactToken();
    }

    /**
     * 清除缓存的 token 并强制重新获取（权限变更后使用）
     */
    public String refreshAccessToken() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
        redisTemplate.delete(CONTACT_ACCESS_TOKEN_KEY);
        log.info("已清除缓存的 access_token，重新获取...");
        return fetchAndCacheToken();
    }

    private String fetchAndCacheContactToken() {
        String secret = config.getCustomerContactSecret();
        if (secret == null || secret.isEmpty()) {
            log.warn("未配置 customer-contact-secret，回退使用应用 secret");
            return fetchAndCacheToken();
        }
        return fetchTokenWithSecret(secret, CONTACT_ACCESS_TOKEN_KEY);
    }

    private String fetchAndCacheToken() {
        return fetchTokenWithSecret(config.getSecret(), ACCESS_TOKEN_KEY);
    }

    private String fetchTokenWithSecret(String secret, String cacheKey) {
        try {
            String url = config.getApiBase() + "/cgi-bin/gettoken?corpid=" + config.getCorpid()
                    + "&corpsecret=" + secret;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("access_token") && node.get("errcode").asInt() == 0) {
                String token = node.get("access_token").asText();
                int expiresIn = node.get("expires_in").asInt();
                redisTemplate.opsForValue().set(cacheKey, token, expiresIn - 300, TimeUnit.SECONDS);
                log.info("access_token 已刷新并缓存, cacheKey={}, 有效期 {} 秒", cacheKey, expiresIn);
                return token;
            }
            log.error("获取企业微信 access_token 失败: {}", response.body());
        } catch (Exception e) {
            log.error("获取企业微信 access_token 异常", e);
        }
        return null;
    }

    // ==================== URL 验证 ====================

    public String verifyUrl(String msgSignature, String timestamp, String nonce, String echostr) {
        try {
            String sortStr = sortParams(config.getToken(), timestamp, nonce, echostr);
            String sha1 = sha1(sortStr);
            if (!sha1.equals(msgSignature)) {
                log.error("企业微信 URL 验证失败: 签名不匹配");
                return null;
            }
            // 解密 echostr
            return decrypt(echostr);
        } catch (Exception e) {
            log.error("企业微信 URL 验证异常", e);
            return null;
        }
    }

    // ==================== 消息回调处理 ====================

    public String handleCallback(String msgSignature, String timestamp, String nonce, String xmlBody) {
        try {
            // 1. 解析 XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            org.w3c.dom.Document doc = factory.newDocumentBuilder()
                    .parse(new org.xml.sax.InputSource(new StringReader(xmlBody)));

            String encrypt = getXmlValue(doc, "Encrypt");

            // 2. 验证签名
            String sortStr = sortParams(config.getToken(), timestamp, nonce, encrypt);
            String sha1 = sha1(sortStr);
            if (!sha1.equals(msgSignature)) {
                log.error("企业微信回调签名验证失败");
                return "fail";
            }

            // 3. 解密
            String plainXml = decrypt(encrypt);
            log.info("企业微信回调解密后: {}", plainXml);

            // 4. 解析明文 XML
            org.w3c.dom.Document plainDoc = factory.newDocumentBuilder()
                    .parse(new org.xml.sax.InputSource(new StringReader(plainXml)));

            String msgType = getXmlValue(plainDoc, "MsgType");
            String fromUser = getXmlValue(plainDoc, "FromUserName");
            String toUser = getXmlValue(plainDoc, "ToUserName");
            String agentId = getXmlValue(plainDoc, "AgentID");

            if ("text".equals(msgType)) {
                String content = getXmlValue(plainDoc, "Content");
                String replyXml = handleTextReply(content, fromUser, toUser, agentId);
                return encryptReply(replyXml, timestamp, nonce);
            } else if ("event".equals(msgType)) {
                String event = getXmlValue(plainDoc, "Event");
                if ("enter_agent".equals(event)) {
                    // 用户进入应用，发送引导消息
                    return buildTextReply(fromUser, toUser,
                            "欢迎！请直接发送手机号（如 13812345678），系统将发送短信验证码完成关联。");
                } else if ("change_external_contact".equals(event)) {
                    // 外部联系人变更事件（加好友）
                    String changeType = getXmlValue(plainDoc, "ChangeType");
                    log.info("外部联系人事件: changeType={}, plainXml={}", changeType, plainXml);
                    if ("add_external_contact".equals(changeType)) {
                        String externalUserId = getXmlValue(plainDoc, "ExternalUserID");
                        String state = getXmlValue(plainDoc, "State");
                        String welcomeCode = getXmlValue(plainDoc, "WelcomeCode");
                        log.info("添加外部联系人: externalUserId={}, state={}, welcomeCode={}", externalUserId, state, welcomeCode);
                        if (externalUserId != null && !externalUserId.isEmpty()) {
                            // 尝试欢迎语 API（仅首次未对话时有效，41051 表示已聊天，可忽略）
                            if (welcomeCode != null) {
                                sendWelcomeByCode(welcomeCode, externalUserId);
                            }
                            // sendTextMessage API 暂时无权限(48002)，改用后台欢迎语 + pending bind 方案
                            // 后台欢迎语设置引导链接: https://wework.ontopxyx.com/bind.html
                            log.info("[绑定链接] externalUserId={} → 链接: https://wework.ontopxyx.com/bind.html?uid={}", externalUserId, externalUserId);
                        }
                    }
                }
            }

            return "success";
        } catch (Exception e) {
            log.error("处理企业微信回调异常", e);
            return "fail";
        }
    }

    /**
     * 处理用户回复的文本消息
     * 流程：发送手机号 → 短信验证码 → 回复验证码完成绑定
     * 兼容旧流程：厂商名称 手机号（直接绑定）
     */
    private String handleTextReply(String content, String externalUserId, String corpId, String agentId) {
        content = content.trim();

        // 1. 检查是否是验证码（4位数字）
        if (content.matches("[0-9]{4}")) {
            // 检查短信验证码绑定
            String smsKey = SMS_BIND_PREFIX + content;
            Object smsObj = redisTemplate.opsForValue().get(smsKey);
            if (smsObj != null) {
                // 格式: externalUserId::phone::mfrId1,mfrId2,mfrId3
                String[] parts = smsObj.toString().split("::", 3);
                String storedUserId = parts[0];
                String boundPhone = parts.length > 1 ? parts[1] : "";
                if (!storedUserId.equals(externalUserId)) {
                    return buildTextReply(externalUserId, corpId, "验证码不匹配，请重新发送手机号获取验证码。");
                }
                String[] mfrIds = parts.length > 2 ? parts[2].split(",") : new String[0];
                redisTemplate.delete(smsKey);

                StringBuilder result = new StringBuilder();
                int successCount = 0;
                for (String mfrIdStr : mfrIds) {
                    Long mfrId = Long.valueOf(mfrIdStr);
                    Manufacturer m = manufacturerMapper.selectById(mfrId);
                    if (m == null) continue;
                    String checkResult = checkCanBind(m, externalUserId);
                    if (checkResult != null) {
                        result.append(m.getName()).append("：").append(checkResult).append("\n");
                    } else {
                        doBind(m, externalUserId, boundPhone);
                        successCount++;
                        result.append(m.getName()).append("（").append(m.getManufacturerCode()).append("）关联成功！\n");
                        log.info("短信验证码关联成功: name={}, code={}, externalUserId={}", m.getName(), m.getManufacturerCode(), externalUserId);
                    }
                }
                if (successCount == 0 && result.length() == 0) {
                    result.append("未找到匹配的厂商，请重新发送手机号获取验证码。");
                }
                return buildTextReply(externalUserId, corpId, result.toString().trim());
            }
        }

        // 2. 纯手机号 → 触发短信验证码流程
        if (content.matches("1[3-9]\\d{9}")) {
            return handleSmsBindRequest(externalUserId, content, corpId);
        }

        // 其他格式 → 引导用户发送手机号
        return buildTextReply(externalUserId, corpId, "请直接发送手机号（如 13812345678），系统会发送短信验证码完成绑定。");
    }

    /**
     * 短信验证码绑定：根据手机号匹配厂商、发送短信验证码
     * SMS_BIND_PREFIX:{code} → externalUserId::mfrId1,mfrId2
     */
    private String handleSmsBindRequest(String externalUserId, String phone, String corpId) {
        // 查找所有匹配该手机号的厂商
        LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();
        qw.eq(Manufacturer::getSmsNumber, phone)
          .or().eq(Manufacturer::getMobile1, phone)
          .or().eq(Manufacturer::getMobile2, phone)
          .or().eq(Manufacturer::getMobile3, phone);
        List<Manufacturer> manufacturers = manufacturerMapper.selectList(qw);

        if (manufacturers.isEmpty()) {
            return buildTextReply(externalUserId, corpId,
                    "未找到手机号 " + phone + " 关联的厂商，请核实后重试。");
        }

        // 过滤：已绑定该微信的跳过，但报告
        List<Manufacturer> toBind = new ArrayList<>();
        StringBuilder alreadyMsg = new StringBuilder();
        for (Manufacturer m : manufacturers) {
            List<String> existing = getBoundExternalUserIds(m.getId());
            if (existing.contains(externalUserId)) {
                alreadyMsg.append(m.getName()).append("（已关联）、");
            } else {
                toBind.add(m);
            }
        }

        if (toBind.isEmpty()) {
            String msg = alreadyMsg.length() > 0
                    ? "该手机号关联的厂商均已绑定：" + alreadyMsg.substring(0, alreadyMsg.length() - 1)
                    : "未找到可绑定的厂商。";
            return buildTextReply(externalUserId, corpId, msg);
        }

        List<Manufacturer> validBind = toBind;

        // 生成4位验证码
        String chars = "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        String verifyCode = code.toString();

        // 存入 Redis（格式: externalUserId::phone::mfrId1,mfrId2）
        String mfrIds = validBind.stream().map(m -> String.valueOf(m.getId())).collect(Collectors.joining(","));
        String redisValue = externalUserId + "::" + phone + "::" + mfrIds;
        redisTemplate.opsForValue().set(SMS_BIND_PREFIX + verifyCode, redisValue, 5, TimeUnit.MINUTES);

        // 发送短信验证码
        try {
            smsService.sendSms("通知", 70295149, List.of(phone), List.of(verifyCode), "企微绑定验证码", null);
            log.info("短信验证码已发送: phone={}, code={}, manufacturers={}", phone, verifyCode, validBind.size());
        } catch (Exception e) {
            redisTemplate.delete(SMS_BIND_PREFIX + verifyCode);
            log.error("短信验证码发送失败: phone={}, error={}", phone, e.getMessage());
            return buildTextReply(externalUserId, corpId, "验证码发送失败，请稍后重试。");
        }

        // 构造回复
        List<String> names = validBind.stream().map(Manufacturer::getName).collect(Collectors.toList());
        String nameList = String.join("、", names);
        StringBuilder reply = new StringBuilder();
        reply.append("验证码已发送到 ").append(phone).append("，5分钟内有效。\n");
        reply.append("匹配厂商：").append(nameList);
        if (alreadyMsg.length() > 0) {
            reply.append("\n（").append(alreadyMsg.substring(0, alreadyMsg.length() - 1)).append("，已绑定无需重复操作）");
        }
        reply.append("\n请回复验证码完成绑定。");

        return buildTextReply(externalUserId, corpId, reply.toString());
    }

    /**
     * WEB 绑定步骤1：根据手机号发送短信验证码（返回 JSON 结构）
     */
    public Map<String, Object> sendSmsBindCode(String externalUserId, String phone) {
        Map<String, Object> result = new LinkedHashMap<>();
        LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();
        qw.eq(Manufacturer::getSmsNumber, phone)
          .or().eq(Manufacturer::getMobile1, phone)
          .or().eq(Manufacturer::getMobile2, phone)
          .or().eq(Manufacturer::getMobile3, phone);
        List<Manufacturer> manufacturers = manufacturerMapper.selectList(qw);

        if (manufacturers.isEmpty()) {
            result.put("code", -1);
            result.put("message", "未找到手机号 " + phone + " 关联的厂商，请核实后重试。");
            return result;
        }

        List<Manufacturer> toBind = new ArrayList<>();
        List<String> alreadyNames = new ArrayList<>();
        for (Manufacturer m : manufacturers) {
            List<String> existing = getBoundExternalUserIds(m.getId());
            if (existing.contains(externalUserId)) {
                alreadyNames.add(m.getName());
            } else {
                toBind.add(m);
            }
        }

        if (toBind.isEmpty()) {
            result.put("code", -1);
            result.put("message", "该手机号关联的厂商均已绑定：" + String.join("、", alreadyNames));
            return result;
        }

        // 生成验证码
        String chars = "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        String verifyCode = code.toString();

        String mfrIds = toBind.stream().map(m -> String.valueOf(m.getId())).collect(Collectors.joining(","));
        redisTemplate.opsForValue().set(SMS_BIND_PREFIX + verifyCode, externalUserId + "::" + phone + "::" + mfrIds, 5, TimeUnit.MINUTES);

        try {
            smsService.sendSms("通知", 70295149, List.of(phone), List.of(verifyCode), "企微绑定验证码", null);
            log.info("WEB短信验证码已发送: phone={}, code={}, manufacturers={}", phone, verifyCode, toBind.size());
        } catch (Exception e) {
            redisTemplate.delete(SMS_BIND_PREFIX + verifyCode);
            log.error("WEB短信验证码发送失败: phone={}, error={}", phone, e.getMessage());
            result.put("code", -1);
            result.put("message", "验证码发送失败，请稍后重试。");
            return result;
        }

        List<String> names = toBind.stream().map(Manufacturer::getName).collect(Collectors.toList());
        result.put("code", 0);
        result.put("message", "验证码已发送到 " + phone + "，5分钟内有效。");
        result.put("manufacturers", names);
        result.put("count", toBind.size());
        if (!alreadyNames.isEmpty()) {
            result.put("alreadyBound", String.join("、", alreadyNames));
        }
        return result;
    }

    /**
     * WEB 绑定步骤2：验证短信验证码并完成绑定
     */
    public Map<String, Object> verifySmsBindCode(String externalUserId, String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        String smsKey = SMS_BIND_PREFIX + code.toUpperCase();
        Object smsObj = redisTemplate.opsForValue().get(smsKey);
        if (smsObj == null) {
            result.put("code", -1);
            result.put("message", "验证码无效或已过期，请重新获取。");
            return result;
        }

        String[] parts = smsObj.toString().split("::", 3);
        String storedUserId = parts[0];
        String boundPhone = parts.length > 1 ? parts[1] : "";
        if (!storedUserId.equals(externalUserId)) {
            result.put("code", -1);
            result.put("message", "验证码与当前用户不匹配。");
            return result;
        }

        String[] mfrIds = parts.length > 2 ? parts[2].split(",") : new String[0];
        redisTemplate.delete(smsKey);

        List<String> bound = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String mfrIdStr : mfrIds) {
            Long mfrId = Long.valueOf(mfrIdStr);
            Manufacturer m = manufacturerMapper.selectById(mfrId);
            if (m == null) continue;
            String checkResult = checkCanBind(m, externalUserId);
            if (checkResult != null) {
                failed.add(m.getName() + "：" + checkResult);
            } else {
                doBind(m, externalUserId, boundPhone);
                bound.add(m.getName() + "（" + m.getManufacturerCode() + "）");
                log.info("WEB短信验证码关联成功: name={}, code={}, externalUserId={}", m.getName(), m.getManufacturerCode(), externalUserId);
            }
        }

        if (bound.isEmpty()) {
            result.put("code", -1);
            result.put("message", "绑定失败：" + String.join("；", failed));
        } else {
            result.put("code", 0);
            result.put("message", "关联成功！" + String.join("、", bound));
            result.put("bound", bound);
        }
        if (!failed.isEmpty()) {
            result.put("failed", failed);
        }
        return result;
    }

    // ==================== 多微信绑定辅助方法 ====================

    /**
     * 获取厂商的手机号总数（上限）
     */
    private int getPhoneCount(Manufacturer m) {
        Set<String> unique = new LinkedHashSet<>();
        addIfNotEmpty(m.getMobile1(), unique);
        addIfNotEmpty(m.getMobile2(), unique);
        addIfNotEmpty(m.getMobile3(), unique);
        addIfNotEmpty(m.getSmsNumber(), unique);
        return unique.size();
    }

    /**
     * 获取厂商已绑定的所有微信ID列表（去重）
     */
    public List<String> getBoundExternalUserIds(Long manufacturerId) {
        Set<String> ids = new LinkedHashSet<>();
        // 旧字段
        Manufacturer m = manufacturerMapper.selectById(manufacturerId);
        if (m != null && m.getWeworkExternalUserid() != null && !m.getWeworkExternalUserid().isEmpty()) {
            ids.add(m.getWeworkExternalUserid());
        }
        // 关联表
        List<ManufacturerWeworkBinding> bindings = bindingMapper.selectList(
                new LambdaQueryWrapper<ManufacturerWeworkBinding>()
                        .eq(ManufacturerWeworkBinding::getManufacturerId, manufacturerId));
        for (ManufacturerWeworkBinding b : bindings) {
            if (b.getWeworkExternalUserid() != null && !b.getWeworkExternalUserid().isEmpty()) {
                ids.add(b.getWeworkExternalUserid());
            }
        }
        return new ArrayList<>(ids);
    }

    /**
     * 检查是否可以绑定，返回 null 表示可以，否则返回错误消息
     */
    private String checkCanBind(Manufacturer m, String externalUserId) {
        // 检查是否已绑定同一个微信
        List<String> existing = getBoundExternalUserIds(m.getId());
        if (existing.contains(externalUserId)) {
            return "该微信已关联过（" + m.getName() + "），无需重复操作。";
        }
        // 检查是否达到手机号上限
        int phoneCount = getPhoneCount(m);
        if (phoneCount == 0) {
            return "该厂商未录入手机号，无法绑定微信。";
        }
        if (existing.size() >= phoneCount) {
            return "该厂商（" + m.getName() + "）绑定数已达上限（" + phoneCount + "个手机号），请先解绑后再添加。";
        }
        return null;
    }

    /**
     * 执行绑定：写入 bindings 表 + 更新 manufacturer.weworkExternalUserid
     */
    private void doBind(Manufacturer m, String externalUserId, String phone) {
        // 写入关联表
        ManufacturerWeworkBinding binding = new ManufacturerWeworkBinding();
        binding.setManufacturerId(m.getId());
        binding.setWeworkExternalUserid(externalUserId);
        binding.setPhone(phone);
        bindingMapper.insert(binding);

        // 更新 manufacturer 主字段（保持兼容）
        m.setWeworkExternalUserid(externalUserId);
        manufacturerMapper.updateById(m);

        log.info("厂商关联成功: name={}, code={}, externalUserId={}, phone={}",
                m.getName(), m.getManufacturerCode(), externalUserId, phone);
    }

    /**
     * 获取厂商绑定详情列表（含 bindingId、phone）
     */
    public List<Map<String, Object>> getBindingDetailList(Long manufacturerId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 关联表中的
        List<ManufacturerWeworkBinding> bindings = bindingMapper.selectList(
                new LambdaQueryWrapper<ManufacturerWeworkBinding>()
                        .eq(ManufacturerWeworkBinding::getManufacturerId, manufacturerId));
        for (ManufacturerWeworkBinding b : bindings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("bindingId", b.getId());
            item.put("weworkExternalUserid", b.getWeworkExternalUserid());
            item.put("phone", b.getPhone() != null ? b.getPhone() : "");
            item.put("createTime", b.getCreateTime() != null ? b.getCreateTime().toString() : "");
            result.add(item);
        }

        // 旧字段中的（如果没有在关联表中）
        Manufacturer m = manufacturerMapper.selectById(manufacturerId);
        if (m != null && m.getWeworkExternalUserid() != null && !m.getWeworkExternalUserid().isEmpty()) {
            boolean alreadyInList = bindings.stream()
                    .anyMatch(b -> m.getWeworkExternalUserid().equals(b.getWeworkExternalUserid()));
            if (!alreadyInList) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("bindingId", 0); // 0 表示旧字段
                item.put("weworkExternalUserid", m.getWeworkExternalUserid());
                item.put("phone", "");
                item.put("createTime", "");
                result.add(item);
            }
        }

        return result;
    }

    /**
     * 删除一个绑定
     * @param bindingId 绑定记录ID，0 表示清空旧字段
     */
    public boolean unbind(Long manufacturerId, Long bindingId) {
        if (bindingId == 0) {
            // 清空旧字段
            Manufacturer m = manufacturerMapper.selectById(manufacturerId);
            if (m != null && m.getWeworkExternalUserid() != null) {
                log.info("解绑(旧字段): manufacturerId={}, externalUserId={}", manufacturerId, m.getWeworkExternalUserid());
                m.setWeworkExternalUserid(null);
                manufacturerMapper.updateById(m);
                return true;
            }
            return false;
        }
        ManufacturerWeworkBinding binding = bindingMapper.selectById(bindingId);
        if (binding == null || !binding.getManufacturerId().equals(manufacturerId)) {
            return false;
        }
        log.info("解绑: manufacturerId={}, bindingId={}, externalUserId={}",
                manufacturerId, bindingId, binding.getWeworkExternalUserid());
        bindingMapper.deleteById(bindingId);
        return true;
    }

    /**
     * 检查绑定数是否超过手机号上限，返回警告消息（null=正常）
     */
    public String checkBindingOverLimit(Long manufacturerId) {
        Manufacturer m = manufacturerMapper.selectById(manufacturerId);
        if (m == null) return null;
        int phoneCount = getPhoneCount(m);
        List<String> boundIds = getBoundExternalUserIds(manufacturerId);
        if (boundIds.size() > phoneCount) {
            return "厂商「" + m.getName() + "」已绑定 " + boundIds.size()
                    + " 个微信，但仅有 " + phoneCount + " 个手机号，超出部分无法新增绑定，建议清理多余绑定。";
        }
        return null;
    }

    // ==================== 厂商资料变更同步 ====================

    /**
     * 删除厂商时，级联清理所有 wework 绑定记录
     */
    public void deleteAllBindingsForManufacturer(Long manufacturerId) {
        // 清除旧字段
        Manufacturer m = manufacturerMapper.selectById(manufacturerId);
        if (m != null && m.getWeworkExternalUserid() != null && !m.getWeworkExternalUserid().isEmpty()) {
            log.info("删除厂商时清空旧字段: manufacturerId={}, externalUserId={}", manufacturerId, m.getWeworkExternalUserid());
            m.setWeworkExternalUserid(null);
            manufacturerMapper.updateById(m);
        }
        // 删除关联表记录
        LambdaQueryWrapper<ManufacturerWeworkBinding> qw = new LambdaQueryWrapper<>();
        qw.eq(ManufacturerWeworkBinding::getManufacturerId, manufacturerId);
        List<ManufacturerWeworkBinding> bindings = bindingMapper.selectList(qw);
        if (!bindings.isEmpty()) {
            for (ManufacturerWeworkBinding b : bindings) {
                bindingMapper.deleteById(b.getId());
                log.info("删除厂商时解绑: manufacturerId={}, bindingId={}, externalUserId={}",
                        manufacturerId, b.getId(), b.getWeworkExternalUserid());
            }
        }
    }

    /**
     * 厂商资料更新后，同步处理 wework 绑定
     * 1. 如果某些手机号字段被清空，解绑对应 phone 的微信
     * 2. 如果手机号总数减少导致超限，自动解绑多余的绑定（按创建时间从新到旧）
     */
    public List<String> syncBindingAfterUpdate(Long manufacturerId, Manufacturer before, Manufacturer after) {
        List<String> warnings = new ArrayList<>();

        // 收集更新前后的手机号集合
        Set<String> phonesBefore = new LinkedHashSet<>();
        Set<String> phonesAfter = new LinkedHashSet<>();
        collectPhones(before, phonesBefore);
        collectPhones(after, phonesAfter);

        // 找出被删除的手机号
        Set<String> removedPhones = new LinkedHashSet<>(phonesBefore);
        removedPhones.removeAll(phonesAfter);

        // 如果某手机号被删除，且存在对应 phone 的绑定，则解绑
        if (!removedPhones.isEmpty()) {
            LambdaQueryWrapper<ManufacturerWeworkBinding> qw = new LambdaQueryWrapper<>();
            qw.eq(ManufacturerWeworkBinding::getManufacturerId, manufacturerId);
            List<ManufacturerWeworkBinding> bindings = bindingMapper.selectList(qw);

            for (ManufacturerWeworkBinding b : bindings) {
                if (b.getPhone() != null && removedPhones.contains(b.getPhone())) {
                    bindingMapper.deleteById(b.getId());
                    log.info("厂商手机号被删除，自动解绑: manufacturerId={}, bindingId={}, externalUserId={}, phone={}",
                            manufacturerId, b.getId(), b.getWeworkExternalUserid(), b.getPhone());
                    warnings.add("手机号 " + b.getPhone() + " 已删除，对应微信 " + b.getWeworkExternalUserid() + " 自动解绑。");
                }
            }
        }

        // 检查超限：如果绑定数 > 新手机号数，自动解绑多余的
        int newPhoneCount = getPhoneCount(after);
        List<String> boundIds = getBoundExternalUserIds(manufacturerId);
        if (boundIds.size() > newPhoneCount) {
            // 获取绑定列表（按创建时间排序，新在前）
            LambdaQueryWrapper<ManufacturerWeworkBinding> qw = new LambdaQueryWrapper<>();
            qw.eq(ManufacturerWeworkBinding::getManufacturerId, manufacturerId)
              .orderByDesc(ManufacturerWeworkBinding::getCreateTime);
            List<ManufacturerWeworkBinding> bindings = bindingMapper.selectList(qw);

            int needRemove = boundIds.size() - newPhoneCount;
            int removed = 0;
            for (ManufacturerWeworkBinding b : bindings) {
                if (removed >= needRemove) break;
                bindingMapper.deleteById(b.getId());
                log.info("厂商手机号减少导致超限，自动解绑: manufacturerId={}, bindingId={}, externalUserId={}",
                        manufacturerId, b.getId(), b.getWeworkExternalUserid());
                warnings.add("手机号总数减少至 " + newPhoneCount + "，超出部分微信 " + b.getWeworkExternalUserid() + " 自动解绑。");
                removed++;
            }
        }

        return warnings;
    }

    /**
     * 收集厂商所有非空手机号字段
     */
    private void collectPhones(Manufacturer m, Set<String> phones) {
        addIfNotEmpty(m.getMobile1(), phones);
        addIfNotEmpty(m.getMobile2(), phones);
        addIfNotEmpty(m.getMobile3(), phones);
        addIfNotEmpty(m.getSmsNumber(), phones);
    }

    private void addIfNotEmpty(String value, Set<String> set) {
        if (value != null && !value.trim().isEmpty()) {
            set.add(value.trim());
        }
    }

    // ==================== 发送外部联系人消息 ====================

    /**
     * 发送加好友欢迎语（使用 send_welcome_msg API）
     */
    public void sendWelcomeByCode(String welcomeCode, String externalUserId) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("welcome_code", welcomeCode);
            Map<String, String> text = new LinkedHashMap<>();
            text.put("content", "欢迎！请点击链接完成厂商关联：\n"
                    + "https://wework.ontopxyx.com/bind.html?uid=" + externalUserId);
            body.put("text", text);

            String json = objectMapper.writeValueAsString(body);
            String urlPath = "/cgi-bin/externalcontact/send_welcome_msg";

            callWithTokenRefresh(urlPath, json, CONTACT_ACCESS_TOKEN_KEY, (node) -> {
                if (node.get("errcode").asInt() == 0) {
                    log.info("发送欢迎语成功: welcomeCode={}, externalUserId={}", welcomeCode, externalUserId);
                } else {
                    log.error("发送欢迎语失败: {}", node.toString());
                }
            });
        } catch (Exception e) {
            log.error("发送欢迎语异常", e);
        }
    }

    /**
     * 调用企微 API，遇到 48002/42001 自动清缓存重试一次
     */
    private void callWithTokenRefresh(String urlPath, String jsonBody, String cacheKey,
                                       java.util.function.Consumer<JsonNode> handler) throws Exception {
        String token = getTokenByCacheKey(cacheKey);
        if (token == null) return;

        String url = config.getApiBase() + urlPath + "?access_token=" + token;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode node = objectMapper.readTree(response.body());
        int errcode = node.get("errcode").asInt();

        // 48002=权限不足(token没权限), 42001=token过期 —— 清缓存重试一次
        if ((errcode == 48002 || errcode == 42001) && cacheKey != null) {
            log.warn("API 返回 {}，清除缓存 token 并重试...", errcode);
            redisTemplate.delete(cacheKey);
            token = getTokenByCacheKey(cacheKey);
            if (token == null) return;
            url = config.getApiBase() + urlPath + "?access_token=" + token;
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            node = objectMapper.readTree(response.body());
        }

        handler.accept(node);
    }

    private String getTokenByCacheKey(String cacheKey) {
        if (CONTACT_ACCESS_TOKEN_KEY.equals(cacheKey)) {
            return getContactAccessToken();
        }
        return getAccessToken();
    }

    /**
     * 发送加好友欢迎语（使用 send_welcome_msg API）
     */
    public void sendWelcomeMessage(String externalUserId) {
        String content = "欢迎！请回复「厂商名称 手机号」完成关联绑定，例如：ABC玩具厂 13812345678";
        sendTextMessage(externalUserId, content);
    }

    /**
     * 创建「联系我」二维码（厂商专属，state=manufacturer_bind）
     * @param userIds 使用成员的 userid 列表
     * @return { config_id, qr_code }
     */
    public Map<String, String> createManufacturerQrCode(List<String> userIds) {
        try {
            String token = getAccessToken();
            if (token == null) return null;

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("type", 1);           // 单人
            body.put("scene", 2);          // 二维码
            body.put("user", userIds);
            body.put("state", "manufacturer_bind");
            body.put("remark", "工厂专用");
            body.put("skip_verify", true); // 自动通过好友

            String json = objectMapper.writeValueAsString(body);
            String url = config.getApiBase() + "/cgi-bin/externalcontact/add_contact_way?access_token=" + token;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());

            int errcode = node.get("errcode").asInt();
            if (errcode == 0) {
                Map<String, String> result = new LinkedHashMap<>();
                result.put("config_id", node.get("config_id").asText());
                result.put("qr_code", node.get("qr_code").asText());
                log.info("创建厂商专属二维码成功: config_id={}", result.get("config_id"));
                return result;
            }
            String errmsg = node.has("errmsg") ? node.get("errmsg").asText() : "未知错误";
            String msg = "创建二维码失败[" + errcode + "]: " + errmsg;
            log.error(msg + " request body: {}", json);
            throw new RuntimeException(msg);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建二维码异常", e);
            throw new RuntimeException("创建二维码异常: " + e.getMessage());
        }
    }

    public boolean sendTextMessage(String externalUserId, String content) {
        String result = sendTextMessageWithResult(externalUserId, content);
        return result != null && result.contains("\"errcode\":0");
    }

    /**
     * 发送文本消息并返回 API 原始响应（用于调试）
     */
    public String sendTextMessageWithResult(String externalUserId, String content) {
        try {
            String token = getAccessToken();
            if (token == null) return "access_token 获取失败";

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("chat_type", "single");
            body.put("sender", "DuWenJia");
            body.put("external_userid", java.util.Collections.singletonList(externalUserId));
            body.put("allow_select", false);
            body.put("msgtype", "text");
            Map<String, String> text = new LinkedHashMap<>();
            text.put("content", content);
            body.put("text", text);

            String json = objectMapper.writeValueAsString(body);
            String url = config.getApiBase() + "/cgi-bin/externalcontact/add_msg_template?access_token=" + token;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("sendTextMessage 响应: {}", response.body());
            return response.body();
        } catch (Exception e) {
            log.error("企业微信消息发送异常", e);
            return "异常: " + e.getMessage();
        }
    }

    /**
     * 发送模板消息（替换变量后发送给所有绑定的微信ID）
     */
    public SendResult sendTemplateMessage(List<String> externalUserIds, String templateContent,
                                          String manufacturerName, String factoryCode,
                                          String boothNo, String dateStr) {
        String content = templateContent
                .replace("{%厂商名称%}", manufacturerName != null ? manufacturerName : "-")
                .replace("{%出厂货号%}", factoryCode != null ? factoryCode : "-")
                .replace("{%摊位号%}", boothNo != null ? boothNo : "-")
                .replace("{%洽谈室号%}", boothNo != null ? boothNo : "-")
                .replace("{%日期%}", dateStr != null ? dateStr : "");

        SendResult result = new SendResult();
        boolean allSuccess = true;
        for (String uid : externalUserIds) {
            if (!sendTextMessage(uid, content)) {
                allSuccess = false;
            }
        }
        result.setSuccess(allSuccess);
        result.setContent(content);
        return result;
    }

    // ==================== 加密工具方法 ====================

    /**
     * 加密回复并包装成企业微信要求的 XML 格式
     */
    private String encryptReply(String replyXml, String timestamp, String nonce) throws Exception {
        String encrypted = encrypt(replyXml);
        String sortStr = sortParams(config.getToken(), timestamp, nonce, encrypted);
        String signature = sha1(sortStr);
        return String.format(
                "<xml>" +
                "<Encrypt><![CDATA[%s]]></Encrypt>" +
                "<MsgSignature><![CDATA[%s]]></MsgSignature>" +
                "<TimeStamp>%s</TimeStamp>" +
                "<Nonce><![CDATA[%s]]></Nonce>" +
                "</xml>",
                encrypted, signature, timestamp, nonce);
    }

    /**
     * 加密明文（decrypt 的逆操作）
     */
    private String encrypt(String plainText) throws Exception {
        byte[] aesKey = Base64.getDecoder().decode(config.getEncodingAesKey() + "=");
        String corpId = config.getCorpid();
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);

        // 组装: 16字节随机 + 4字节网络序msg_len + msg + corpid
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);

        byte[] corpIdBytes = corpId.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[16 + 4 + plainBytes.length + corpIdBytes.length];
        System.arraycopy(randomBytes, 0, data, 0, 16);
        // 网络序写入 msg_len
        data[16] = (byte) ((plainBytes.length >> 24) & 0xFF);
        data[17] = (byte) ((plainBytes.length >> 16) & 0xFF);
        data[18] = (byte) ((plainBytes.length >> 8) & 0xFF);
        data[19] = (byte) (plainBytes.length & 0xFF);
        System.arraycopy(plainBytes, 0, data, 20, plainBytes.length);
        System.arraycopy(corpIdBytes, 0, data, 20 + plainBytes.length, corpIdBytes.length);

        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encryptText) throws Exception {
        byte[] aesKey = Base64.getDecoder().decode(config.getEncodingAesKey() + "=");
        byte[] encrypted = Base64.getDecoder().decode(encryptText);

        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        // IV = AES key 的前16字节
        IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);

        // PKCS5Padding 会自动去除填充，无需手动处理
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] decrypted = cipher.doFinal(encrypted);

        // doFinal 已去除填充，直接解析: 16字节随机 + 4字节网络序msg_len + msg + corpid
        int msgLen = ((decrypted[16] & 0xFF) << 24)
                | ((decrypted[17] & 0xFF) << 16)
                | ((decrypted[18] & 0xFF) << 8)
                | (decrypted[19] & 0xFF);

        return new String(decrypted, 20, msgLen, StandardCharsets.UTF_8);
    }

    private String sha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String sortParams(String... params) {
        Arrays.sort(params);
        return String.join("", params);
    }

    private String getXmlValue(org.w3c.dom.Document doc, String tagName) {
        var nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    /**
     * 构建文本消息回复 XML
     */
    private String buildTextReply(String toUser, String fromUser, String content) {
        return String.format(
                "<xml>" +
                "<ToUserName><![CDATA[%s]]></ToUserName>" +
                "<FromUserName><![CDATA[%s]]></FromUserName>" +
                "<CreateTime>%d</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[%s]]></Content>" +
                "</xml>",
                toUser, fromUser, System.currentTimeMillis() / 1000, content);
    }

    // ==================== 内部类 ====================

    public static class SendResult {
        private boolean success;
        private String content;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
