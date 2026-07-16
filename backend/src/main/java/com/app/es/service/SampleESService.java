package com.app.es.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.app.dto.SearchCondition;
import com.app.es.entity.SampleES;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 样品 ES 查询服务
 * 通过 elasticsearch.enabled 配置动态控制是否创建此 Bean
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class SampleESService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Value("${elasticsearch.enabled:true}")
    private boolean esEnabled;

    @Value("${elasticsearch.fallback-enabled:true}")
    private boolean fallbackEnabled;

    @Value("${elasticsearch.timeout:2000}")
    private int timeout;

    @Value("${elasticsearch.index.name:samples}")
    private String indexName;

    private static final Map<String, String> FIELD_TO_ES = Map.<String, String>ofEntries(
            Map.entry("sampleCode", "sampleCode"),
            Map.entry("manufacturerCode", "manufacturerCode"),
            Map.entry("sampleName", "sampleName"),
            Map.entry("englishName", "englishName"),
            Map.entry("category", "category"),
            Map.entry("categoryCode", "categoryCode"),
            Map.entry("factoryCode", "factoryCode"),
            Map.entry("name", "name"),
            Map.entry("boothNo", "boothNo"),
            Map.entry("contact1", "contact1"),
            Map.entry("phone1", "phone1"),
            Map.entry("mobile1", "mobile1"),
            Map.entry("smsNumber", "smsNumber"),
            Map.entry("factoryPrice", "factoryPrice"),
            Map.entry("sampleLength", "sampleLength"),
            Map.entry("sampleWidth", "sampleWidth"),
            Map.entry("sampleHeight", "sampleHeight"),
            Map.entry("packagingCn", "packagingCn"),
            Map.entry("packageCode", "packageCode"),
            Map.entry("certification", "certification"),
            Map.entry("infringement", "infringement"),
            Map.entry("batteryInfo", "batteryInfo"),
            Map.entry("hideFromXzx", "hideFromXzx"),
            Map.entry("cartonCapacity", "cartonCapacity"),
            Map.entry("innerBoxCount", "innerBoxCount"),
            Map.entry("packageLength", "packageLength"),
            Map.entry("packageWidth", "packageWidth"),
            Map.entry("packageHeight", "packageHeight"),
            Map.entry("cartonLength", "cartonLength"),
            Map.entry("cartonWidth", "cartonWidth"),
            Map.entry("cartonHeight", "cartonHeight"),
            Map.entry("registrant", "registrant"),
            Map.entry("modifier", "modifier"),
            Map.entry("createTime", "createTime"),
            Map.entry("updateTime", "updateTime"),
            Map.entry("remark", "remark"),
            Map.entry("image", "firstImageId")
    );

    public SampleESService(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    /**
     * 检查 ES 是否可用
     */
    public boolean isAvailable() {
        if (!esEnabled || elasticsearchTemplate == null) {
            return false;
        }
        try {
            IndexCoordinates index = IndexCoordinates.of(indexName);
            return elasticsearchTemplate.indexOps(index).exists();
        } catch (Exception e) {
            log.warn("ES 不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 综合查询 - ES 版本（优化：NativeQuery + match AND 操作符）
     * @return 样品 ID 列表，ES 不可用时返回 null 触发降级
     */
    public List<Long> search(List<SearchCondition> conditions, String logic,
                              String sortField, String sortOrder,
                              long current, long size) {
        if (!isAvailable()) {
            if (fallbackEnabled) {
                return null;
            }
            throw new RuntimeException("ES 索引不存在且降级功能已禁用");
        }

        long startTime = System.currentTimeMillis();
        try {
            Query esQuery = buildNativeQuery(conditions, logic);

            NativeQuery query = NativeQuery.builder()
                    .withQuery(esQuery)
                    .withPageable(org.springframework.data.domain.PageRequest.of(
                            (int) (current - 1), (int) size))
                    .build();

            // 设置排序
            String esSortField = FIELD_TO_ES.getOrDefault(sortField, "id");
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            query.addSort(asc
                    ? org.springframework.data.domain.Sort.by(esSortField).ascending()
                    : org.springframework.data.domain.Sort.by(esSortField).descending());

            IndexCoordinates index = IndexCoordinates.of(indexName);
            SearchHits<SampleES> searchHits = elasticsearchTemplate.search(query, SampleES.class, index);

            List<Long> ids = new ArrayList<>();
            for (SearchHit<SampleES> hit : searchHits) {
                ids.add(hit.getContent().getId());
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[ES_SEARCH] 命中 {} 条, 耗时 {}ms, 条件数: {}",
                    searchHits.getTotalHits(), elapsed, conditions != null ? conditions.size() : 0);

            return ids;
        } catch (Exception e) {
            log.error("ES 查询失败，降级到 MySQL: {}", e.getMessage(), e);
            if (fallbackEnabled) {
                return null;
            }
            throw new RuntimeException("ES 查询失败", e);
        }
    }

    /**
     * 关键词搜索 - ES 版本（优化：NativeQuery + multi_match + _source 过滤）
     */
    public List<Long> searchByKeyword(String keyword, String sortField, String sortOrder,
                                       long current, long size) {
        if (!isAvailable()) {
            if (fallbackEnabled) {
                return null;
            }
            throw new RuntimeException("ES 索引不存在且降级功能已禁用");
        }

        long startTime = System.currentTimeMillis();
        try {
            String trimmed = keyword.trim();

            // 用 NativeQuery 构建 bool 查询：text 字段走 multi_match(IK分词)，keyword 字段走 wildcard
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        // 必须未删除
                        b.must(m -> m.term(t -> t.field("deleted").value(0)));
                        // text+IK 分词字段合并为一个 multi_match，减少查询开销
                        b.should(s -> s.multiMatch(mm -> mm
                                .query(trimmed)
                                .fields("sampleName", "name")
                                .type(TextQueryType.BestFields)));
                        // keyword 字段做通配符匹配（精确编号字段用 term，模糊用 wildcard）
                        b.should(s -> s.wildcard(w -> w
                                .field("sampleCode").wildcard("*" + trimmed + "*")));
                        b.should(s -> s.wildcard(w -> w
                                .field("factoryCode").wildcard("*" + trimmed + "*")));
                        b.should(s -> s.wildcard(w -> w
                                .field("manufacturerCode").wildcard("*" + trimmed + "*")));
                        // category/packagingCn 在索引中是 keyword，做绝对匹配
                        b.should(s -> s.term(t -> t.field("category").value(trimmed)));
                        b.should(s -> s.term(t -> t.field("packagingCn").value(trimmed)));
                        b.minimumShouldMatch("1");
                        return b;
                    }))
                    .withPageable(org.springframework.data.domain.PageRequest.of(
                            (int) (current - 1), (int) size))
                    .build();

            // 设置排序
            String esSortField = FIELD_TO_ES.getOrDefault(sortField, "id");
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            query.addSort(asc
                    ? org.springframework.data.domain.Sort.by(esSortField).ascending()
                    : org.springframework.data.domain.Sort.by(esSortField).descending());

            IndexCoordinates index = IndexCoordinates.of(indexName);
            SearchHits<SampleES> searchHits = elasticsearchTemplate.search(query, SampleES.class, index);

            List<Long> ids = new ArrayList<>();
            for (SearchHit<SampleES> hit : searchHits) {
                ids.add(hit.getContent().getId());
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[ES_KEYWORD] keyword='{}' 命中 {} 条, 耗时 {}ms",
                    keyword, searchHits.getTotalHits(), elapsed);

            return ids;
        } catch (Exception e) {
            log.error("ES 关键词搜索失败，降级到 MySQL: {}", e.getMessage(), e);
            if (fallbackEnabled) {
                return null;
            }
            throw new RuntimeException("ES 关键词搜索失败", e);
        }
    }

    /**
     * 构建 ES 原生 Query（替代 Criteria）
     */
    private Query buildNativeQuery(List<SearchCondition> conditions, String logic) {
        List<Query> mustClauses = new ArrayList<>();
        // 基础条件：未删除
        mustClauses.add(Query.of(q -> q.term(t -> t.field("deleted").value(0))));

        if (conditions == null || conditions.isEmpty()) {
            return Query.of(q -> q.bool(b -> b.must(mustClauses)));
        }

        boolean useOr = "or".equalsIgnoreCase(logic);
        List<Query> condQueries = new ArrayList<>();

        for (SearchCondition cond : conditions) {
            if (!cond.isValid()) continue;
            if ("keyword".equals(cond.getField())) continue;

            // image 字段：有图片 = firstImageId 不为空
            if ("image".equals(cond.getField())) {
                if ("1".equals(cond.getValue())) {
                    condQueries.add(Query.of(q -> q.exists(e -> e.field("firstImageId"))));
                } else {
                    condQueries.add(Query.of(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("firstImageId"))))));
                }
                continue;
            }
            // video 字段暂不支持
            if ("video".equals(cond.getField())) continue;

            String esField = FIELD_TO_ES.get(cond.getField());
            if (esField == null) continue;

            Query fieldQuery = buildNativeFieldQuery(esField, cond.getOperator(), cond.getValue());
            if (fieldQuery == null) continue;
            condQueries.add(fieldQuery);
        }

        if (!condQueries.isEmpty()) {
            if (useOr) {
                mustClauses.add(Query.of(q -> q.bool(b -> b.should(condQueries).minimumShouldMatch("1"))));
            } else {
                mustClauses.addAll(condQueries);
            }
        }

        return Query.of(q -> q.bool(b -> b.must(mustClauses)));
    }

    /**
     * 根据操作符构建单字段查询（原生 ES Query）
     */
    private Query buildNativeFieldQuery(String esField, String op, String val) {
        switch (op) {
            case "eq":
                return Query.of(q -> q.term(t -> t.field(esField).value(val)));
            case "ne":
                return Query.of(q -> q.bool(b -> b.mustNot(mn -> mn.term(t -> t.field(esField).value(val)))));
            case "like":
                return buildNativeLikeQuery(esField, val);
            case "gt":
                return Query.of(q -> q.range(r -> r.field(esField).gt(JsonData.of(Double.parseDouble(val)))));
            case "ge":
                return Query.of(q -> q.range(r -> r.field(esField).gte(JsonData.of(Double.parseDouble(val)))));
            case "lt":
                return Query.of(q -> q.range(r -> r.field(esField).lt(JsonData.of(Double.parseDouble(val)))));
            case "le":
                return Query.of(q -> q.range(r -> r.field(esField).lte(JsonData.of(Double.parseDouble(val)))));
            default:
                return buildNativeLikeQuery(esField, val);
        }
    }

    /**
     * LIKE 转换为 ES 原生 Query：
     * - Keyword 字段：term 精确匹配或 wildcard 通配
     * - Text 字段：match 分词匹配，使用 AND 操作符（搜"彩妆"必须含"彩妆"，不允许只含"彩"）
     */
    private Query buildNativeLikeQuery(String esField, String val) {
        String trimmed = val.trim();
        String[] words = trimmed.split("\\s+");

        // Keyword 字段：精确匹配
        if (isKeywordField(esField)) {
            if (words.length > 1) {
                List<Query> termQueries = new ArrayList<>();
                for (String w : words) {
                    termQueries.add(Query.of(q -> q.term(t -> t.field(esField).value(w))));
                }
                return Query.of(q -> q.bool(b -> b.must(termQueries)));
            }
            if ("sampleCode".equals(esField)) {
                return Query.of(q -> q.term(t -> t.field(esField).value(trimmed)));
            }
            return Query.of(q -> q.wildcard(w -> w.field(esField).wildcard("*" + trimmed + "*")));
        }

        // Text 字段：match 分词匹配，AND 操作符
        // 搜"彩妆" → IK 分词后所有 token 必须都在文档中出现
        if (words.length > 1) {
            List<Query> matchQueries = new ArrayList<>();
            for (String w : words) {
                matchQueries.add(Query.of(q -> q.match(m -> m.field(esField).query(w).operator(Operator.And))));
            }
            return Query.of(q -> q.bool(b -> b.must(matchQueries)));
        }
        return Query.of(q -> q.match(m -> m.field(esField).query(trimmed).operator(Operator.And)));
    }

    /**
     * 判断是否为 Keyword 类型字段（精确匹配）
     */
    private boolean isKeywordField(String esField) {
        return "sampleCode".equals(esField)
                || "manufacturerCode".equals(esField)
                || "factoryCode".equals(esField)
                || "boothNo".equals(esField)
                || "phone1".equals(esField)
                || "mobile1".equals(esField)
                || "smsNumber".equals(esField)
                || "category".equals(esField)
                || "categoryCode".equals(esField)
                || "packageCode".equals(esField)
                || "infringement".equals(esField)
                || "hideFromXzx".equals(esField)
                || "registrant".equals(esField)
                || "modifier".equals(esField);
    }

    /**
     * 计算总命中数（用于分页）
     */
    public long count(List<SearchCondition> conditions, String logic) {
        if (!isAvailable()) return -1;

        try {
            Query esQuery = buildNativeQuery(conditions, logic);

            NativeQuery query = NativeQuery.builder()
                    .withQuery(esQuery)
                    .withMaxResults(0)
                    .withTrackTotalHits(true)
                    .build();

            IndexCoordinates index = IndexCoordinates.of(indexName);
            SearchHits<SampleES> hits = elasticsearchTemplate.search(query, SampleES.class, index);
            return hits.getTotalHits();
        } catch (Exception e) {
            log.error("ES count 失败: {}", e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 关键词搜索计数（优化：NativeQuery + track_total_hits）
     */
    public long count(String keyword) {
        if (!isAvailable()) return -1;

        long startTime = System.currentTimeMillis();
        try {
            String trimmed = keyword.trim();

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        b.must(m -> m.term(t -> t.field("deleted").value(0)));
                        b.should(s -> s.multiMatch(mm -> mm
                                .query(trimmed)
                                .fields("sampleName", "name")
                                .type(TextQueryType.BestFields)));
                        b.should(s -> s.wildcard(w -> w
                                .field("sampleCode").wildcard("*" + trimmed + "*")));
                        b.should(s -> s.wildcard(w -> w
                                .field("factoryCode").wildcard("*" + trimmed + "*")));
                        b.should(s -> s.wildcard(w -> w
                                .field("manufacturerCode").wildcard("*" + trimmed + "*")));
                        b.should(s -> s.term(t -> t.field("category").value(trimmed)));
                        b.should(s -> s.term(t -> t.field("packagingCn").value(trimmed)));
                        b.minimumShouldMatch("1");
                        return b;
                    }))
                    .withMaxResults(0)
                    .withTrackTotalHits(true)
                    .build();

            IndexCoordinates index = IndexCoordinates.of(indexName);
            SearchHits<SampleES> hits = elasticsearchTemplate.search(query, SampleES.class, index);
            long total = hits.getTotalHits();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[ES_KEYWORD_COUNT] keyword='{}' total={} 耗时 {}ms", keyword, total, elapsed);
            return total;
        } catch (Exception e) {
            log.error("ES keyword count 失败: {}", e.getMessage(), e);
            return -1;
        }
    }
}
