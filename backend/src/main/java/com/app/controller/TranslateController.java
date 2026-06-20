package com.app.controller;

import com.app.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/translate")
public class TranslateController {

    private static final Logger log = LoggerFactory.getLogger(TranslateController.class);

    private static final String APP_ID = "20200507000443115";
    private static final String KEY = "3ymhQm21d1IzMrwLp8iD";
    private static final String API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    private static final int BATCH_SIZE = 20; // 每批最多翻译20条，防止URL过长

    // 100 QPS 限额下用线程池并行发请求
    private static final ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(16, Runtime.getRuntime().availableProcessors() * 2));

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @PostMapping("/batch")
    public Result<List<String>> translateBatch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> texts = (List<String>) body.get("texts");
        String from = (String) body.getOrDefault("from", "zh");
        String to = (String) body.getOrDefault("to", "en");

        if (texts == null || texts.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 去重
        List<String> uniqueTexts = new ArrayList<>();
        Map<String, String> cacheMap = new LinkedHashMap<>();
        for (String t : texts) {
            if (t == null || t.trim().isEmpty()) continue;
            String trimmed = t.trim();
            if (!cacheMap.containsKey(trimmed)) {
                cacheMap.put(trimmed, null);
                uniqueTexts.add(trimmed);
            }
        }

        int totalBatches = (int) Math.ceil((double) uniqueTexts.size() / BATCH_SIZE);
        String[] resultsByBatch = new String[totalBatches * BATCH_SIZE]; // 保持batch顺序的结果数组
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int batch = 0; batch < totalBatches; batch++) {
            final int batchIdx = batch;
            int fromIdx = batch * BATCH_SIZE;
            int toIdx = Math.min(fromIdx + BATCH_SIZE, uniqueTexts.size());
            final List<String> batchTexts = new ArrayList<>(uniqueTexts.subList(fromIdx, toIdx));

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<String> batchResults = doTranslate(batchTexts, from, to);
                if (batchResults == null) {
                    log.warn("第{}/{}批翻译失败，回退为原文", batchIdx + 1, totalBatches);
                    batchResults = batchTexts;
                }
                for (int i = 0; i < batchResults.size(); i++) {
                    resultsByBatch[batchIdx * BATCH_SIZE + i] = batchResults.get(i);
                }
            }, executor);
            futures.add(future);
        }

        // 等待全部完成，最长等待60秒
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("翻译超时（60秒），部分批次可能未完成");
        } catch (Exception e) {
            log.error("翻译并发执行异常", e);
        }

        // 按顺序收集结果
        List<String> uniqueResults = new ArrayList<>();
        for (int i = 0; i < uniqueTexts.size(); i++) {
            String r = resultsByBatch[i];
            if (r != null) {
                uniqueResults.add(r);
            }
        }

        // 失败或无结果的回退为原文
        while (uniqueResults.size() < uniqueTexts.size()) {
            uniqueResults.add(uniqueTexts.get(uniqueResults.size()));
        }

        for (int i = 0; i < uniqueTexts.size(); i++) {
            cacheMap.put(uniqueTexts.get(i), uniqueResults.get(i));
        }

        List<String> finalResults = new ArrayList<>();
        for (String t : texts) {
            String trimmed = t != null ? t.trim() : "";
            String translated = cacheMap.getOrDefault(trimmed, trimmed);
            finalResults.add(translated != null ? translated : trimmed);
        }

        log.info("翻译完成: {}条去重为{}条，分{}批并行发送", texts.size(), uniqueTexts.size(), totalBatches);
        return Result.success(finalResults);
    }

    /** 执行单批翻译，返回null表示失败 */
    private List<String> doTranslate(List<String> texts, String from, String to) {
        try {
            String src = String.join("\n", texts);
            String salt = String.valueOf(System.currentTimeMillis());

            String signStr = APP_ID + src + salt + KEY;
            String sign = md5(signStr);

            String params = "q=" + URLEncoder.encode(src, StandardCharsets.UTF_8) +
                    "&from=" + from +
                    "&to=" + to +
                    "&appid=" + APP_ID +
                    "&salt=" + salt +
                    "&sign=" + sign;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "?" + params))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String respBody = response.body();

            String errorCode = extractBaiduErrorCode(respBody);
            if (errorCode != null) {
                String errorMsg = extractJsonString(respBody, "error_msg");
                log.warn("百度翻译API错误: code={}, msg={}", errorCode, errorMsg);
                return null;
            }

            List<String> results = extractAllDst(respBody);

            if (results.isEmpty()) {
                log.warn("百度翻译返回无dst字段, HTTP状态={}, body前200字符={}",
                        response.statusCode(), respBody.substring(0, Math.min(200, respBody.length())));
                return null;
            }

            if (results.size() != texts.size()) {
                String combined = String.join("\n", results);
                String[] split = combined.split("\n", -1);
                results = Arrays.asList(split);
            }

            // 数字修复：原文有数字但译文丢了或变形，修正译文中的数字
            for (int i = 0; i < texts.size() && i < results.size(); i++) {
                results.set(i, fixNumbers(texts.get(i), results.get(i)));
            }

            return results;
        } catch (Exception e) {
            log.error("翻译请求异常", e);
            return null;
        }
    }

    /** 提取 error_code（数字格式 54001 或字符串格式 "54001"） */
    private String extractBaiduErrorCode(String json) {
        int keyIdx = json.indexOf("\"error_code\"");
        if (keyIdx == -1) return null;
        keyIdx = json.indexOf(':', keyIdx);
        if (keyIdx == -1) return null;
        keyIdx++;
        while (keyIdx < json.length() && json.charAt(keyIdx) == ' ') keyIdx++;
        if (keyIdx < json.length() && json.charAt(keyIdx) == '"') {
            return extractQuoted(json, keyIdx);
        }
        int endIdx = keyIdx;
        while (endIdx < json.length()) {
            char c = json.charAt(endIdx);
            if (c == ',' || c == '}' || c == ' ' || c == '\n' || c == '\r') break;
            endIdx++;
        }
        return json.substring(keyIdx, endIdx).trim();
    }

    private String extractJsonString(String json, String key) {
        int keyIdx = json.indexOf("\"" + key + "\"");
        if (keyIdx == -1) return null;
        keyIdx = json.indexOf(':', keyIdx);
        if (keyIdx == -1) return null;
        keyIdx++;
        while (keyIdx < json.length() && json.charAt(keyIdx) == ' ') keyIdx++;
        if (keyIdx < json.length() && json.charAt(keyIdx) == '"') {
            return extractQuoted(json, keyIdx);
        }
        return null;
    }

    /** 从 "..." 中提取值 */
    private String extractQuoted(String json, int startIdx) {
        startIdx++;
        StringBuilder sb = new StringBuilder();
        for (int i = startIdx; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == 'n') { sb.append('\n'); i++; }
                else if (next == 't') { sb.append('\t'); i++; }
                else if (next == 'r') { sb.append('\r'); i++; }
                else if (next == '"') { sb.append('"'); i++; }
                else if (next == '\\') { sb.append('\\'); i++; }
                else { sb.append(c); }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 提取所有 "dst":"xxx" */
    private List<String> extractAllDst(String json) {
        List<String> results = new ArrayList<>();
        int searchFrom = 0;
        while (true) {
            int dstIdx = json.indexOf("\"dst\":\"", searchFrom);
            if (dstIdx == -1) break;
            dstIdx += 7;
            String val = extractQuoted(json, dstIdx);
            results.add(val);
            searchFrom = dstIdx + val.length() + 2;
        }
        return results;
    }

    /** 修正译文中的数字：原文有数字但译文丢了或变形，在译文中修复 */
    private String fixNumbers(String src, String dst) {
        if (src == null || dst == null || dst.isEmpty()) return dst;
        // 提取原文中所有连续数字
        List<String> srcNums = new ArrayList<>();
        StringBuilder n = new StringBuilder();
        for (char c : src.toCharArray()) {
            if (c >= '0' && c <= '9') {
                n.append(c);
            } else {
                if (n.length() > 0) {
                    srcNums.add(n.toString());
                    n.setLength(0);
                }
            }
        }
        if (n.length() > 0) srcNums.add(n.toString());
        if (srcNums.isEmpty()) return dst;

        // 提取译文中所有连续数字
        List<Integer> dstNumPositions = new ArrayList<>();
        List<String> dstNums = new ArrayList<>();
        StringBuilder dn = new StringBuilder();
        int dnStart = -1;
        for (int i = 0; i < dst.length(); i++) {
            char c = dst.charAt(i);
            if (c >= '0' && c <= '9') {
                if (dn.length() == 0) dnStart = i;
                dn.append(c);
            } else {
                if (dn.length() > 0) {
                    dstNums.add(dn.toString());
                    dstNumPositions.add(dnStart);
                    dn.setLength(0);
                }
            }
        }
        if (dn.length() > 0) {
            dstNums.add(dn.toString());
            dstNumPositions.add(dnStart);
        }

        if (dstNums.isEmpty()) return dst;

        // 找丢失/变形的数字并修复
        String result = dst;
        for (int i = 0; i < srcNums.size() && i < dstNums.size(); i++) {
            String srcNum = srcNums.get(i);
            String dstNum = dstNums.get(i);
            if (!srcNum.equals(dstNum)) {
                // 译文数字和原文不一致，替换
                int pos = dstNumPositions.get(i);
                // 由于替换可能改变字符串长度，需要重新计算偏移
                int actualPos = result.indexOf(dstNum);
                if (actualPos >= 0) {
                    result = result.substring(0, actualPos) + srcNum + result.substring(actualPos + dstNum.length());
                }
            }
        }
        return result;
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }
}
