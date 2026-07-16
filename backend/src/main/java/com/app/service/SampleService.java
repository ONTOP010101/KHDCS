package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.dto.SearchCondition;
import com.app.entity.Manufacturer;
import com.app.entity.Sample;
import com.app.entity.SampleThumbnail;
import com.app.es.entity.SampleES;
import com.app.es.repository.SampleESRepository;
import com.app.es.service.SampleESService;
import com.app.mapper.ManufacturerMapper;
import com.app.mapper.SampleMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.app.util.UserContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SampleService {

    private static final Logger log = LoggerFactory.getLogger(SampleService.class);

    private static final ConcurrentHashMap<String, Boolean> CACHED_EXISTING_CODES = new ConcurrentHashMap<>();
    private static volatile boolean codesLoaded = false;
    private static final Pattern SAMPLE_CODE_PATTERN = Pattern.compile("^([A-Za-z]+)(\\d+)$");

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private SampleESService sampleESService;

    @Autowired(required = false)
    private SampleESRepository sampleESRepository;

    private static final Map<String, SFunction<Sample, ?>> SORT_FIELD_MAP = new LinkedHashMap<>();

    // 前端字段名 → 数据库列名
    private static final Map<String, String> FIELD_COL_MAP = new LinkedHashMap<>();
    static {
        FIELD_COL_MAP.put("manufacturerCode", "manufacturer_code");
        FIELD_COL_MAP.put("name", "name");
        FIELD_COL_MAP.put("contact1", "contact1");
        FIELD_COL_MAP.put("phone1", "phone1");
        FIELD_COL_MAP.put("mobile1", "mobile1");
        FIELD_COL_MAP.put("smsNumber", "sms_number");
        FIELD_COL_MAP.put("sampleName", "sample_name");
        FIELD_COL_MAP.put("sampleCode", "sample_code");
        FIELD_COL_MAP.put("factoryCode", "factory_code");
        FIELD_COL_MAP.put("boothNo", "booth_no");
        FIELD_COL_MAP.put("category", "category");
        FIELD_COL_MAP.put("categoryCode", "category_code");
        FIELD_COL_MAP.put("packageCode", "package_code");
        FIELD_COL_MAP.put("packagingCn", "packaging_cn");
        FIELD_COL_MAP.put("certification", "certification");
        FIELD_COL_MAP.put("infringement", "infringement");
        FIELD_COL_MAP.put("batteryInfo", "battery_info");
        FIELD_COL_MAP.put("hideFromXzx", "hide_from_xzx");
        FIELD_COL_MAP.put("factoryPrice", "factory_price");
        FIELD_COL_MAP.put("cartonCapacity", "carton_capacity");
        FIELD_COL_MAP.put("innerBoxCount", "inner_box_count");
        FIELD_COL_MAP.put("sampleLength", "sample_length");
        FIELD_COL_MAP.put("sampleWidth", "sample_width");
        FIELD_COL_MAP.put("sampleHeight", "sample_height");
        FIELD_COL_MAP.put("packageLength", "package_length");
        FIELD_COL_MAP.put("packageWidth", "package_width");
        FIELD_COL_MAP.put("packageHeight", "package_height");
        FIELD_COL_MAP.put("cartonLength", "carton_length");
        FIELD_COL_MAP.put("cartonWidth", "carton_width");
        FIELD_COL_MAP.put("cartonHeight", "carton_height");
        FIELD_COL_MAP.put("createTime", "create_time");
        FIELD_COL_MAP.put("updateTime", "update_time");
        FIELD_COL_MAP.put("registrant", "registrant");
        FIELD_COL_MAP.put("modifier", "modifier");
    }

    static {
        SORT_FIELD_MAP.put("id", Sample::getId);
        SORT_FIELD_MAP.put("manufacturerCode", Sample::getManufacturerCode);
        SORT_FIELD_MAP.put("sampleCode", Sample::getSampleCode);
        SORT_FIELD_MAP.put("category", Sample::getCategory);
        SORT_FIELD_MAP.put("sampleName", Sample::getSampleName);
        SORT_FIELD_MAP.put("englishName", Sample::getEnglishName);
        SORT_FIELD_MAP.put("factoryCode", Sample::getFactoryCode);
        SORT_FIELD_MAP.put("sampleUnit", Sample::getSampleUnit);
        SORT_FIELD_MAP.put("sampleUnitEn", Sample::getSampleUnitEn);
        SORT_FIELD_MAP.put("packagingCn", Sample::getPackagingCn);
        SORT_FIELD_MAP.put("packagingEn", Sample::getPackagingEn);
        SORT_FIELD_MAP.put("factoryPrice", Sample::getFactoryPrice);
        SORT_FIELD_MAP.put("taxPrice", Sample::getTaxPrice);
        SORT_FIELD_MAP.put("sampleLength", Sample::getSampleLength);
        SORT_FIELD_MAP.put("sampleWidth", Sample::getSampleWidth);
        SORT_FIELD_MAP.put("sampleHeight", Sample::getSampleHeight);
        SORT_FIELD_MAP.put("sampleGrossWeight", Sample::getSampleGrossWeight);
        SORT_FIELD_MAP.put("sampleNetWeight", Sample::getSampleNetWeight);
        SORT_FIELD_MAP.put("cartonLength", Sample::getCartonLength);
        SORT_FIELD_MAP.put("cartonWidth", Sample::getCartonWidth);
        SORT_FIELD_MAP.put("cartonHeight", Sample::getCartonHeight);
        SORT_FIELD_MAP.put("cartonMaterialVolume", Sample::getCartonMaterialVolume);
        SORT_FIELD_MAP.put("cartonVolume", Sample::getCartonVolume);
        SORT_FIELD_MAP.put("innerBoxCount", Sample::getInnerBoxCount);
        SORT_FIELD_MAP.put("cartonCapacity", Sample::getCartonCapacity);
        SORT_FIELD_MAP.put("packingUnit", Sample::getPackingUnit);
        SORT_FIELD_MAP.put("cartonGrossWeight", Sample::getCartonGrossWeight);
        SORT_FIELD_MAP.put("cartonNetWeight", Sample::getCartonNetWeight);
        SORT_FIELD_MAP.put("packageLength", Sample::getPackageLength);
        SORT_FIELD_MAP.put("packageWidth", Sample::getPackageWidth);
        SORT_FIELD_MAP.put("packageHeight", Sample::getPackageHeight);
        SORT_FIELD_MAP.put("certification", Sample::getCertification);
        SORT_FIELD_MAP.put("certificationCount", Sample::getCertificationCount);
        SORT_FIELD_MAP.put("color", Sample::getColor);
        SORT_FIELD_MAP.put("colorEn", Sample::getColorEn);
        SORT_FIELD_MAP.put("remark", Sample::getRemark);
        SORT_FIELD_MAP.put("remarkEn", Sample::getRemarkEn);
        SORT_FIELD_MAP.put("name", Sample::getName);
        SORT_FIELD_MAP.put("boothNo", Sample::getBoothNo);
        SORT_FIELD_MAP.put("contact1", Sample::getContact1);
        SORT_FIELD_MAP.put("phone1", Sample::getPhone1);
        SORT_FIELD_MAP.put("mobile1", Sample::getMobile1);
        SORT_FIELD_MAP.put("fax", Sample::getFax);
        SORT_FIELD_MAP.put("qq", Sample::getQq);
        SORT_FIELD_MAP.put("registrant", Sample::getRegistrant);
        SORT_FIELD_MAP.put("createTime", Sample::getCreateTime);
        SORT_FIELD_MAP.put("modifier", Sample::getModifier);
        SORT_FIELD_MAP.put("updateTime", Sample::getUpdateTime);
        SORT_FIELD_MAP.put("infringement", Sample::getInfringement);
        SORT_FIELD_MAP.put("batteryInfo", Sample::getBatteryInfo);
        SORT_FIELD_MAP.put("hideFromXzx", Sample::getHideFromXzx);
        SORT_FIELD_MAP.put("packageCode", Sample::getPackageCode);
        SORT_FIELD_MAP.put("size", Sample::getSize);
        SORT_FIELD_MAP.put("origin", Sample::getOrigin);
    }

    /** 搜索缓存：(hash(条件+分页) -> PageResult)，5分钟过期，最大500条 */
    private final Map<String, CacheEntry<PageResult<Sample>>> searchCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;
    private static final int CACHE_MAX_SIZE = 500;

    private static class CacheEntry<T> {
        final T data;
        final long expireAt;
        CacheEntry(T data, long expireAt) { this.data = data; this.expireAt = expireAt; }
        boolean expired() { return System.currentTimeMillis() > expireAt; }
    }

    private static final Map<String, String> HEADER_TO_FIELD = new LinkedHashMap<>();
    static {
        HEADER_TO_FIELD.put("厂商编号", "manufacturerCode");
        HEADER_TO_FIELD.put("公司编号", "sampleCode");
        HEADER_TO_FIELD.put("种类编号", "categoryCode");
        HEADER_TO_FIELD.put("种类名称", "category");
        HEADER_TO_FIELD.put("样品名称", "sampleName");
        HEADER_TO_FIELD.put("英文名称", "englishName");
        HEADER_TO_FIELD.put("出厂货号", "factoryCode");
        HEADER_TO_FIELD.put("货号", "factoryCode");
        HEADER_TO_FIELD.put("样品单位", "sampleUnit");
        HEADER_TO_FIELD.put("样品英文单位", "sampleUnitEn");
        HEADER_TO_FIELD.put("中文包装", "packagingCn");
        HEADER_TO_FIELD.put("包装", "packagingCn");
        HEADER_TO_FIELD.put("英文包装", "packagingEn");
        HEADER_TO_FIELD.put("包装编号", "packageCode");
        HEADER_TO_FIELD.put("出厂价", "factoryPrice");
        HEADER_TO_FIELD.put("价格", "factoryPrice");
        HEADER_TO_FIELD.put("单价", "factoryPrice");
        HEADER_TO_FIELD.put("税点价", "taxPrice");
        HEADER_TO_FIELD.put("样品长度", "sampleLength");
        HEADER_TO_FIELD.put("样品宽度", "sampleWidth");
        HEADER_TO_FIELD.put("样品高度", "sampleHeight");
        HEADER_TO_FIELD.put("样品毛重", "sampleGrossWeight");
        HEADER_TO_FIELD.put("样品净重", "sampleNetWeight");
        HEADER_TO_FIELD.put("外箱长度", "cartonLength");
        HEADER_TO_FIELD.put("外箱宽度", "cartonWidth");
        HEADER_TO_FIELD.put("外箱高度", "cartonHeight");
        HEADER_TO_FIELD.put("外箱材积", "cartonMaterialVolume");
        HEADER_TO_FIELD.put("外箱体积", "cartonVolume");
        HEADER_TO_FIELD.put("内盒个数", "innerBoxCount");
        HEADER_TO_FIELD.put("外箱装量", "cartonCapacity");
        HEADER_TO_FIELD.put("装箱单位", "packingUnit");
        HEADER_TO_FIELD.put("外箱毛重", "cartonGrossWeight");
        HEADER_TO_FIELD.put("外箱净重", "cartonNetWeight");
        HEADER_TO_FIELD.put("包装长度", "packageLength");
        HEADER_TO_FIELD.put("包装宽度", "packageWidth");
        HEADER_TO_FIELD.put("包装高度", "packageHeight");
        HEADER_TO_FIELD.put("产品认证", "certification");
        HEADER_TO_FIELD.put("认证总数", "certificationCount");
        HEADER_TO_FIELD.put("颜色", "color");
        HEADER_TO_FIELD.put("英文颜色", "colorEn");
        HEADER_TO_FIELD.put("备注", "remark");
        HEADER_TO_FIELD.put("英文备注", "remarkEn");
        HEADER_TO_FIELD.put("厂商名称", "name");
        HEADER_TO_FIELD.put("摊位号", "boothNo");
        HEADER_TO_FIELD.put("联系人", "contact1");
        HEADER_TO_FIELD.put("电话", "phone1");
        HEADER_TO_FIELD.put("手机", "mobile1");
        HEADER_TO_FIELD.put("传真", "fax");
        HEADER_TO_FIELD.put("QQ", "qq");
        HEADER_TO_FIELD.put("登记人", "registrant");
        HEADER_TO_FIELD.put("修改人", "modifier");
        HEADER_TO_FIELD.put("登记日期", "createTime");
        HEADER_TO_FIELD.put("登记时间", "createTime");
        HEADER_TO_FIELD.put("修改日期", "updateTime");
        HEADER_TO_FIELD.put("侵权", "infringement");
        HEADER_TO_FIELD.put("电池信息", "batteryInfo");
        HEADER_TO_FIELD.put("不在小竹熊显示", "hideFromXzx");
        HEADER_TO_FIELD.put("是否不在小竹熊显示", "hideFromXzx");
        HEADER_TO_FIELD.put("品名", "sampleName");
        HEADER_TO_FIELD.put("产品名称", "sampleName");
        // 复合列（在导入循环中单独处理拆分）
        HEADER_TO_FIELD.put("包装规格", "_pkgDimensions");
        HEADER_TO_FIELD.put("包装尺寸", "_pkgDimensions");
        HEADER_TO_FIELD.put("外箱规格", "_cartonDimensions");
        HEADER_TO_FIELD.put("外箱尺寸", "_cartonDimensions");
        HEADER_TO_FIELD.put("规格", "_cartonDimensions");
        HEADER_TO_FIELD.put("箱规", "_cartonDimensions");
        HEADER_TO_FIELD.put("产品规格", "_productDimensions");
        HEADER_TO_FIELD.put("产品尺寸", "_productDimensions");
        HEADER_TO_FIELD.put("尺寸", "_productDimensions");
        HEADER_TO_FIELD.put("毛/净重", "_grossNetWeight");
        HEADER_TO_FIELD.put("毛净重", "_grossNetWeight");
    }

    // 表头匹配：去除空格后查找
    private String resolveHeader(String rawHeader) {
        if (rawHeader == null) return null;
        String cleaned = rawHeader.replaceAll("\\s+", "");
        String field = HEADER_TO_FIELD.get(cleaned);
        if (field != null) return field;
        return HEADER_TO_FIELD.get(rawHeader); // fallback
    }

    private static final Set<String> DECIMAL_FIELDS = new HashSet<>(Arrays.asList(
            "factoryPrice", "taxPrice", "sampleLength", "sampleWidth", "sampleHeight",
            "sampleGrossWeight", "sampleNetWeight", "cartonLength", "cartonWidth", "cartonHeight",
            "cartonMaterialVolume", "cartonVolume", "cartonGrossWeight", "cartonNetWeight",
            "packageLength", "packageWidth", "packageHeight"
    ));

    private static final Set<String> INT_FIELDS = new HashSet<>(Arrays.asList(
            "innerBoxCount", "cartonCapacity", "certificationCount"
    ));

    private static final Set<String> LOCAL_DATETIME_FIELDS = new HashSet<>(Arrays.asList(
            "createTime", "updateTime"
    ));

    public PageResult<Sample> list(long current, long size, String keyword, String category, String name,
                                   String manufacturerCode, String sampleCode, String sortField, String sortOrder) {
        StringBuilder where = new StringBuilder(" WHERE deleted = 0");
        List<Object> whereParams = new ArrayList<>();

        if (StringUtils.hasText(sampleCode)) {
            where.append(" AND sample_code = ?");
            whereParams.add(sampleCode);
        }
        if (StringUtils.hasText(keyword)) {
            log.info("[MYSQL_LIST] keyword='{}'", keyword.trim());
            String trimmed = keyword.trim();
            if (trimmed.length() <= 2) {
                // 短关键词走 LIKE（全文索引最小词长默认 3）
                where.append(" AND (sample_code LIKE ? OR sample_name LIKE ? OR manufacturer_code LIKE ? OR factory_code LIKE ?)");
                String kw = "%" + escapeLike(trimmed) + "%";
                for (int i = 0; i < 4; i++) whereParams.add(kw);
            } else {
                // 关键词搜索用 LIKE（ES 不可用时降级，虽慢但稳定）
                String kw = "%" + escapeLike(trimmed) + "%";
                where.append(" AND (sample_name LIKE ? OR sample_code LIKE ? OR manufacturer_code LIKE ? OR factory_code LIKE ?)");
                for (int i = 0; i < 4; i++) whereParams.add(kw);
            }
        }
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            where.append(" AND category = ?");
            whereParams.add(category);
        }
        if (StringUtils.hasText(name)) {
            where.append(" AND name LIKE ?");
            whereParams.add("%" + escapeLike(name) + "%");
        }
        if (StringUtils.hasText(manufacturerCode)) {
            where.append(" AND manufacturer_code = ?");
            whereParams.add(manufacturerCode);
        }

        // Count
        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM samples" + where, Long.class, whereParams.toArray());

        // Sort
        boolean asc = !"desc".equalsIgnoreCase(sortOrder);
        StringBuilder orderClause = new StringBuilder();
        if ("hasThumbnail".equals(sortField)) {
            orderClause.append(" ORDER BY (SELECT COUNT(1) FROM sample_thumbnail WHERE sample_id = s.id) ")
                       .append(asc ? "ASC" : "DESC");
        } else if ("recent".equals(sortField)) {
            orderClause.append(" ORDER BY GREATEST(COALESCE(update_time,'1970-01-01'), COALESCE(create_time,'1970-01-01')) ")
                       .append(asc ? "ASC" : "DESC");
        } else {
            String dbSortField = FIELD_COL_MAP.getOrDefault(sortField, "create_time");
            orderClause.append(" ORDER BY ").append(dbSortField).append(" ").append(asc ? "ASC" : "DESC");
        }

        // 延迟关联：内层只扫 id，避免 OFFSET 大时扫描整行数据
        String innerSql = "SELECT id FROM samples s" + where + orderClause + " LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(whereParams);
        params.add(size);
        params.add((current - 1) * size);

        String dataSql = "SELECT s.* FROM samples s INNER JOIN (" + innerSql + ") t ON s.id = t.id" + orderClause;

        List<Sample> records = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(Sample.class), params.toArray());
        PageResult<Sample> result = new PageResult<>(records, total != null ? total : 0, current, size);
        fillThumbnails(result.getRecords());
        return result;
    }

    public List<Sample> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Sample> list = sampleMapper.selectBatchIds(ids);
        return list != null ? list : new ArrayList<>();
    }

    public List<Sample> listByIdsWithThumbnails(List<Long> ids) {
        List<Sample> list = listByIds(ids);
        fillThumbnails(list);
        return list;
    }

    /**
     * 根据 ID 列表加载样品，保持 ES 返回的顺序
     */
    private List<Sample> loadByIds(List<Long> ids, String sortField, String sortOrder) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Sample> list = sampleMapper.selectBatchIds(ids);
        if (list == null) list = new ArrayList<>();
        // 按 ids 顺序排列
        Map<Long, Sample> map = new java.util.LinkedHashMap<>();
        for (Sample s : list) {
            map.put(s.getId(), s);
        }
        List<Sample> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Sample s = map.get(id);
            if (s != null) ordered.add(s);
        }
        return ordered;
    }

    public PageResult<Sample> advancedSearch(long current, long size, List<SearchCondition> conditions, String sortField, String sortOrder, String logic) {
        // 1. 构建缓存key
        String cacheKey = buildSearchCacheKey(current, size, conditions, sortField, sortOrder);
        CacheEntry<PageResult<Sample>> cached = searchCache.get(cacheKey);
        if (cached != null && !cached.expired()) {
            return cached.data;
        }

        long startTime = System.currentTimeMillis();

        // 2. 分离 keyword 条件与其他条件
        String keyword = null;
        List<SearchCondition> otherConditions = new ArrayList<>();
        if (conditions != null) {
            for (SearchCondition cond : conditions) {
                if ("keyword".equals(cond.getField()) && cond.isValid()) {
                    keyword = cond.getValue();
                } else {
                    otherConditions.add(cond);
                }
            }
        }

        log.info("[ADV_SEARCH] keyword='{}' otherConditions={}", keyword, otherConditions.size());

        // 3. 关键词搜索：优先使用 ES，降级到 MySQL FULLTEXT
        // 如果包含 video 条件，跳过 ES 关键词搜索
        boolean hasVideoFilter = otherConditions != null && otherConditions.stream()
                .anyMatch(c -> "video".equals(c.getField()));
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 尝试 ES 关键词搜索（video 条件 ES 不支持，跳过）
            List<Long> esIds = null;
            if (sampleESService != null && !hasVideoFilter) {
                try {
                    esIds = sampleESService.searchByKeyword(keyword, sortField, sortOrder, current, size);
                } catch (Exception e) {
                    log.warn("ES 关键词搜索异常，降级到 MySQL: {}", e.getMessage());
                }
            }
            if (esIds != null) {
                long total = sampleESService.count(keyword);
                log.info("[ES_KEYWORD] keyword='{}' total={} took={}ms", keyword, total, System.currentTimeMillis() - startTime);
                List<Sample> records = esIds.isEmpty() ? List.of() : loadByIds(esIds, sortField, sortOrder);
                PageResult<Sample> result = new PageResult<>(records, Math.max(total, 0), current, size);
                fillThumbnails(result.getRecords());
                return result;
            }

            // MySQL 降级：FULLTEXT ngram + LIKE
            List<String> ftTerms = new ArrayList<>();
            List<String> likeTerms = new ArrayList<>();
            for (String t : keyword.trim().split("\\s+")) {
                String tt = t.trim();
                if (tt.isEmpty()) continue;
                boolean hasSpecial = tt.matches(".*[:：\\-/,.\\(\\)\\[\\]\"'@#$%^&*+=~`|\\\\].*");
                if (tt.length() >= 2 && !hasSpecial) {
                    if (!ftTerms.contains(tt)) ftTerms.add(tt);
                } else {
                    if (!likeTerms.contains(tt)) likeTerms.add(tt);
                }
                if (tt.indexOf(':') >= 0) {
                    String fw = tt.replace(':', '：');
                    if (!likeTerms.contains(fw)) likeTerms.add(fw);
                } else if (tt.indexOf('：') >= 0) {
                    String hw = tt.replace('：', ':');
                    if (!likeTerms.contains(hw)) likeTerms.add(hw);
                }
            }
            log.info("[MYSQL_KEYWORD] keyword='{}' ftTerms={} likeTerms={}", keyword, ftTerms, likeTerms);
            IPage<Sample> page = sampleMapper.searchByKeyword(
                    new Page<>(current, size), ftTerms, likeTerms, sortField, sortOrder);
            PageResult<Sample> result = new PageResult<>(page.getRecords(), page.getTotal(), current, size);
            fillThumbnails(result.getRecords());
            if (searchCache.size() >= CACHE_MAX_SIZE) {
                searchCache.entrySet().removeIf(e -> e.getValue().expired());
            }
            if (searchCache.size() < CACHE_MAX_SIZE) {
                searchCache.put(cacheKey, new CacheEntry<>(result, System.currentTimeMillis() + CACHE_TTL_MS));
            }
            return result;
        }

        // 4. 非关键词条件：优先使用 ES，降级到 MySQL JdbcTemplate
        List<Long> esResultIds = null;
        long esTotal = -1;
        boolean esAvailable = sampleESService != null && sampleESService.isAvailable();
        if (hasVideoFilter) esAvailable = false; // video 条件强制走 MySQL
        log.info("[ES_CHECK] sampleESService={} esAvailable={} hasVideoFilter={} otherConditions={}",
                sampleESService != null, esAvailable, hasVideoFilter, otherConditions != null ? otherConditions.size() : 0);
        if (sampleESService != null && otherConditions != null && !otherConditions.isEmpty() && esAvailable) {
            try {
                esResultIds = sampleESService.search(otherConditions, logic, sortField, sortOrder, current, size);
                if (esResultIds != null) {
                    esTotal = sampleESService.count(otherConditions, logic);
                }
            } catch (Exception e) {
                log.warn("ES 综合查询异常，降级到 MySQL: {}", e.getMessage());
            }
        }
        if (esResultIds != null) {
            log.info("[ES_COND] conditions={} total={} took={}ms", otherConditions.size(), esTotal, System.currentTimeMillis() - startTime);
            List<Sample> records = esResultIds.isEmpty() ? List.of() : loadByIds(esResultIds, sortField, sortOrder);
            PageResult<Sample> result = new PageResult<>(records, Math.max(esTotal, 0), current, size);
            result.setElapsed(System.currentTimeMillis() - startTime);
            fillThumbnails(result.getRecords());
            return result;
        }

        // MySQL 降级：JdbcTemplate 动态拼接
        log.info("[MYSQL_COND] otherConditions={}", otherConditions != null ? otherConditions.size() : 0);
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        String joinOp = "or".equalsIgnoreCase(logic) ? " OR " : " AND ";

        if (otherConditions != null) {
            for (SearchCondition cond : otherConditions) {
                if (!cond.isValid()) continue;

                if ("image".equals(cond.getField())) {
                    if (where.length() > 0) where.append(joinOp);
                    where.append("EXISTS (SELECT 1 FROM sample_thumbnail WHERE sample_id = samples.id)");
                    continue;
                }

                if ("video".equals(cond.getField())) {
                    if (where.length() > 0) where.append(joinOp);
                    where.append("EXISTS (SELECT 1 FROM videos WHERE sample_id = samples.id)");
                    continue;
                }

                // 标准字段
                String col = FIELD_COL_MAP.get(cond.getField());
                if (col == null) continue;
                String op = cond.getOperator();
                String val = cond.getValue();

                if (where.length() > 0) where.append(joinOp);
                switch (op) {
                    case "eq":
                        where.append(col).append(" = ?");
                        params.add(val);
                        break;
                    case "ne":
                        where.append(col).append(" <> ?");
                        params.add(val);
                        break;
                    case "like":
                        // 分词匹配：空格分割后每个词都必须在同列中出现
                        String[] words = val.trim().split("\\s+");
                        if (words.length > 1) {
                            where.append("(");
                            for (int i = 0; i < words.length; i++) {
                                String w = words[i].trim();
                                if (w.isEmpty()) continue;
                                if (i > 0) where.append(" AND ");
                                where.append(col).append(" LIKE CONCAT('%',?,'%')");
                                params.add(w);
                                // 冒号变体
                                if (w.indexOf(':') >= 0) {
                                    where.append(" AND ").append(col).append(" LIKE CONCAT('%',?,'%')");
                                    params.add(w.replace(':', '：'));
                                } else if (w.indexOf('：') >= 0) {
                                    where.append(" AND ").append(col).append(" LIKE CONCAT('%',?,'%')");
                                    params.add(w.replace('：', ':'));
                                }
                            }
                            where.append(")");
                        } else if ("sample_code".equals(col)) {
                            // 公司编号：完整搜索走精确匹配，利用唯一索引
                            where.append(col).append(" = ?");
                            params.add(val.trim());
                        } else if ("sample_name".equals(col)) {
                            // 样品名称：走 ngram 全文索引，模糊搜索毫秒级
                            where.append("MATCH(sample_name) AGAINST(? IN BOOLEAN MODE)");
                            params.add("+" + val.trim());
                        } else {
                            where.append(col).append(" LIKE CONCAT('%',?,'%')");
                            params.add(val.trim());
                        }
                        break;
                    case "gt": case "ge": case "lt": case "le":
                        String sqlOp = "gt".equals(op) ? ">" : "ge".equals(op) ? ">=" : "lt".equals(op) ? "<" : "<=";
                        where.append(col).append(" ").append(sqlOp).append(" ?");
                        try { params.add(new BigDecimal(val)); }
                        catch (NumberFormatException ignored) { params.add(val); }
                        break;
                    default:
                        // 默认按 like 处理（分词匹配）
                        String[] defWords = val.trim().split("\\s+");
                        if (defWords.length > 1) {
                            where.append("(");
                            for (int i = 0; i < defWords.length; i++) {
                                String w = defWords[i].trim();
                                if (w.isEmpty()) continue;
                                if (i > 0) where.append(" AND ");
                                where.append(col).append(" LIKE CONCAT('%',?,'%')");
                                params.add(w);
                                if (w.indexOf(':') >= 0) {
                                    where.append(" AND ").append(col).append(" LIKE CONCAT('%',?,'%')");
                                    params.add(w.replace(':', '：'));
                                } else if (w.indexOf('：') >= 0) {
                                    where.append(" AND ").append(col).append(" LIKE CONCAT('%',?,'%')");
                                    params.add(w.replace('：', ':'));
                                }
                            }
                            where.append(")");
                        } else if ("sample_code".equals(col)) {
                            // 公司编号：完整搜索走精确匹配，利用唯一索引
                            where.append(col).append(" = ?");
                            params.add(val.trim());
                        } else if ("sample_name".equals(col)) {
                            // 样品名称：走 ngram 全文索引，模糊搜索毫秒级
                            where.append("MATCH(sample_name) AGAINST(? IN BOOLEAN MODE)");
                            params.add("+" + val.trim());
                        } else {
                            where.append(col).append(" LIKE CONCAT('%',?,'%')");
                            params.add(val.trim());
                        }
                }
            }
        }

        // WHERE 前缀（始终过滤已删除记录）
        String whereClause = " WHERE deleted = 0";
        if (where.length() > 0) {
            whereClause += " AND " + where.toString();
        }
        // 3. COUNT 查询
        Long total = 0L;
        try {
            String countSql = "SELECT COUNT(*) FROM samples" + whereClause;
            total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        } catch (Exception e) {
            log.warn("Count query failed, falling back to limit count", e);
            String countSql2 = "SELECT COUNT(*) FROM (SELECT 1 FROM samples" + whereClause + " LIMIT 10000) t";
            total = jdbcTemplate.queryForObject(countSql2, Long.class, params.toArray());
        }

        // 4. 排序
        String orderClause;
        boolean asc = !"desc".equalsIgnoreCase(sortOrder);
        if ("hasThumbnail".equals(sortField)) {
            orderClause = "ORDER BY (SELECT COUNT(1) FROM sample_thumbnail WHERE sample_id = samples.id) " + (asc ? "ASC" : "DESC");
        } else if ("recent".equals(sortField)) {
            orderClause = "ORDER BY GREATEST(COALESCE(update_time,'1970-01-01'), COALESCE(create_time,'1970-01-01')) " + (asc ? "ASC" : "DESC");
        } else if (sortField != null && !sortField.isEmpty()) {
            String sortCol = FIELD_COL_MAP.get(sortField);
            if (sortCol == null) sortCol = "create_time";
            orderClause = "ORDER BY " + sortCol + " " + (asc ? "ASC" : "DESC");
        } else {
            orderClause = "ORDER BY create_time DESC";
        }

        // 5. 数据查询
        long offset = (current - 1) * size;
        String sql = "SELECT * FROM samples" + whereClause + " " + orderClause + " LIMIT " + offset + ", " + size;
        List<Sample> records = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Sample.class), params.toArray());

        PageResult<Sample> result = new PageResult<>(records, total != null ? total : 0L, current, size);
        result.setElapsed(System.currentTimeMillis() - startTime);
        fillThumbnails(result.getRecords());

        // 6. 缓存
        if (searchCache.size() >= CACHE_MAX_SIZE) {
            searchCache.entrySet().removeIf(e -> e.getValue().expired());
        }
        if (searchCache.size() < CACHE_MAX_SIZE) {
            searchCache.put(cacheKey, new CacheEntry<>(result, System.currentTimeMillis() + CACHE_TTL_MS));
        }

        return result;
    }

    /**
     * 根据搜索条件获取所有匹配的样品ID（不分页），用于工厂选品等批量操作
     */
    public List<Long> getFilteredSampleIds(String keyword, List<SearchCondition> conditions, String logic) {
        // 从 conditions 中提取 keyword（综合查询会把keyword作为条件传递）
        if (keyword == null && conditions != null) {
            for (SearchCondition cond : conditions) {
                if ("keyword".equals(cond.getField()) && cond.isValid()) {
                    keyword = cond.getValue();
                    break;
                }
            }
        }

        // 1. 关键词搜索：优先 ES
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 尝试 ES
            if (sampleESService != null) {
                try {
                    List<Long> esIds = sampleESService.searchByKeyword(keyword, null, null, 1, 10000);
                    if (esIds != null) return esIds;
                } catch (Exception e) {
                    log.warn("ES getFilteredSampleIds keyword 失败: {}", e.getMessage());
                }
            }
            // MySQL 降级
            List<String> ftTerms = new ArrayList<>();
            List<String> likeTerms = new ArrayList<>();
            for (String t : keyword.trim().split("\\s+")) {
                String tt = t.trim();
                if (tt.isEmpty()) continue;
                boolean hasSpecial = tt.matches(".*[:：\\-/,.\\(\\)\\[\\]\"'@#$%^&*+=~`|\\\\].*");
                if (tt.length() >= 2 && !hasSpecial) {
                    if (!ftTerms.contains(tt)) ftTerms.add(tt);
                } else {
                    if (!likeTerms.contains(tt)) likeTerms.add(tt);
                }
                if (tt.indexOf(':') >= 0) {
                    String fw = tt.replace(':', '：');
                    if (!likeTerms.contains(fw)) likeTerms.add(fw);
                } else if (tt.indexOf('：') >= 0) {
                    String hw = tt.replace('：', ':');
                    if (!likeTerms.contains(hw)) likeTerms.add(hw);
                }
            }
            if (!ftTerms.isEmpty() || !likeTerms.isEmpty()) {
                return sampleMapper.searchIdsByKeyword(ftTerms, likeTerms);
            }
            return null;
        }

        // 2. 非关键词条件：优先 ES
        if (conditions != null && !conditions.isEmpty()) {
            if (sampleESService != null) {
                try {
                    List<Long> esIds = sampleESService.search(conditions, logic, null, null, 1, 10000);
                    if (esIds != null) return esIds;
                } catch (Exception e) {
                    log.warn("ES getFilteredSampleIds 失败: {}", e.getMessage());
                }
            }
        }

        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        String joinOp = "or".equalsIgnoreCase(logic) ? " OR " : " AND ";

        if (conditions == null) {
            return null;
        }

        for (SearchCondition cond : conditions) {
            if (!cond.isValid()) continue;

            if ("keyword".equals(cond.getField())) continue; // skip keyword (handled above)

            if ("image".equals(cond.getField())) {
                if (where.length() > 0) where.append(joinOp);
                where.append("EXISTS (SELECT 1 FROM sample_thumbnail WHERE sample_id = samples.id)");
                continue;
            }

            if ("video".equals(cond.getField())) {
                if (where.length() > 0) where.append(joinOp);
                where.append("EXISTS (SELECT 1 FROM videos WHERE sample_id = samples.id)");
                continue;
            }

            String col = FIELD_COL_MAP.get(cond.getField());
            if (col == null) continue;
            String op = cond.getOperator();
            String val = cond.getValue();

            if (where.length() > 0) where.append(joinOp);
            switch (op) {
                case "eq":
                    where.append(col).append(" = ?");
                    params.add(val);
                    break;
                case "ne":
                    where.append(col).append(" <> ?");
                    params.add(val);
                    break;
                case "like":
                    String[] words = val.trim().split("\\s+");
                    if (words.length > 1) {
                        where.append("(");
                        for (int i = 0; i < words.length; i++) {
                            String w = words[i].trim();
                            if (w.isEmpty()) continue;
                            if (i > 0) where.append(" AND ");
                            where.append(col).append(" LIKE CONCAT('%',?,'%')");
                            params.add(w);
                        }
                        where.append(")");
                    } else {
                        where.append(col).append(" LIKE CONCAT('%',?,'%')");
                        params.add(val.trim());
                    }
                    break;
                case "gt": case "ge": case "lt": case "le":
                    String sqlOp2 = "gt".equals(op) ? ">" : "ge".equals(op) ? ">=" : "lt".equals(op) ? "<" : "<=";
                    where.append(col).append(" ").append(sqlOp2).append(" ?");
                    try { params.add(new BigDecimal(val)); }
                    catch (NumberFormatException ignored) { params.add(val); }
                    break;
                default:
                    String[] defWords = val.trim().split("\\s+");
                    if (defWords.length > 1) {
                        where.append("(");
                        for (int i = 0; i < defWords.length; i++) {
                            String w = defWords[i].trim();
                            if (w.isEmpty()) continue;
                            if (i > 0) where.append(" AND ");
                            where.append(col).append(" LIKE CONCAT('%',?,'%')");
                            params.add(w);
                        }
                        where.append(")");
                    } else {
                        where.append(col).append(" LIKE CONCAT('%',?,'%')");
                        params.add(val.trim());
                    }
            }
        }

        if (where.length() == 0) return null; // no valid conditions

        String whereClause = " WHERE deleted = 0 AND " + where.toString();
        String sql = "SELECT id FROM samples" + whereClause;
        return jdbcTemplate.queryForList(sql, Long.class, params.toArray());
    }

    /** 构建搜索缓存key */
    private String buildSearchCacheKey(long current, long size, List<SearchCondition> conditions, String sortField, String sortOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append(current).append('|').append(size).append('|').append(sortField).append('|').append(sortOrder);
        if (conditions != null) {
            for (SearchCondition c : conditions) {
                if (c.isValid()) {
                    sb.append('|').append(c.getField()).append('=').append(c.getOperator()).append('=').append(c.getValue());
                }
            }
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    /** 转义 LIKE 特殊字符，防止 SQL 注入和意外通配 */
    private String escapeLike(String s) {
        if (s == null) return "";
        return s.replace("'", "''").replace("%", "\\%").replace("_", "\\_");
    }

    public Sample getById(Long id) {
        Sample sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw new BusinessException(404, "样品不存在");
        }
        return sample;
    }

    @Transactional
    public Sample create(Sample sample) {
        Long userId = UserContext.getUserId();
        sample.setCreateBy(userId);
        sample.setUpdateBy(userId);
        if (sample.getRegistrant() == null) {
            sample.setRegistrant(UserContext.getRealName());
        }
        sampleMapper.insert(sample);
        syncToES(sample);
        return sample;
    }

    @Transactional
    public void update(Long id, Sample sample) {
        Sample existing = sampleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "样品不存在");
        }
        sample.setId(id);
        sample.setUpdateBy(UserContext.getUserId());
        sample.setUpdateTime(null);
        sampleMapper.updateById(sample);
        // 重新加载完整数据同步到 ES
        Sample updated = sampleMapper.selectById(id);
        if (updated != null) syncToES(updated);
    }

    @Transactional
    public void delete(Long id) {
        Sample sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw new BusinessException(404, "样品不存在");
        }
        sampleMapper.deleteById(id);
        deleteFromES(id);
    }

    @Transactional
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            sampleMapper.deleteById(id);
            deleteFromES(id);
        }
    }

    // ========== ES 数据同步 ==========

    /**
     * 同步样品到 ES
     */
    private void syncToES(Sample sample) {
        if (sample == null || sampleESRepository == null) return;
        try {
            SampleES es = SampleES.builder()
                    .id(sample.getId())
                    .sampleCode(sample.getSampleCode())
                    .manufacturerCode(sample.getManufacturerCode())
                    .sampleName(sample.getSampleName())
                    .englishName(sample.getEnglishName())
                    .category(sample.getCategory())
                    .categoryCode(sample.getCategoryCode())
                    .factoryCode(sample.getFactoryCode())
                    .name(sample.getName())
                    .boothNo(sample.getBoothNo())
                    .contact1(sample.getContact1())
                    .phone1(sample.getPhone1())
                    .mobile1(sample.getMobile1())
                    .smsNumber(sample.getSmsNumber())
                    .factoryPrice(sample.getFactoryPrice())
                    .sampleLength(sample.getSampleLength())
                    .sampleWidth(sample.getSampleWidth())
                    .sampleHeight(sample.getSampleHeight())
                    .packagingCn(sample.getPackagingCn())
                    .packageCode(sample.getPackageCode())
                    .certification(sample.getCertification())
                    .infringement(sample.getInfringement())
                    .batteryInfo(sample.getBatteryInfo())
                    .hideFromXzx(sample.getHideFromXzx())
                    .cartonCapacity(sample.getCartonCapacity())
                    .innerBoxCount(sample.getInnerBoxCount())
                    .packageLength(sample.getPackageLength())
                    .packageWidth(sample.getPackageWidth())
                    .packageHeight(sample.getPackageHeight())
                    .cartonLength(sample.getCartonLength())
                    .cartonWidth(sample.getCartonWidth())
                    .cartonHeight(sample.getCartonHeight())
                    .registrant(sample.getRegistrant())
                    .modifier(sample.getModifier())
                    .createTime(sample.getCreateTime())
                    .updateTime(sample.getUpdateTime())
                    .remark(sample.getRemark())
                    .deleted(sample.getDeleted())
                    .firstImageId(sample.getFirstImageId())
                    .build();
            sampleESRepository.save(es);
            log.debug("ES 同步成功: id={}", sample.getId());
        } catch (Exception e) {
            log.warn("ES 同步失败 id={}: {}", sample.getId(), e.getMessage());
        }
    }

    /**
     * 从 ES 删除样品
     */
    private void deleteFromES(Long id) {
        if (id == null || sampleESRepository == null) return;
        try {
            sampleESRepository.deleteById(id);
            log.debug("ES 删除成功: id={}", id);
        } catch (Exception e) {
            log.warn("ES 删除失败 id={}: {}", id, e.getMessage());
        }
    }

    /**
     * 全量同步所有样品到 ES
     */
    public Map<String, Object> syncAllToES() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (sampleESRepository == null || sampleESService == null) {
            result.put("success", false);
            result.put("message", "ES 未启用，无法同步");
            return result;
        }

        if (!sampleESService.isAvailable()) {
            result.put("success", false);
            result.put("message", "ES 不可用，请检查连接和索引");
            return result;
        }

        long startTime = System.currentTimeMillis();
        int total = 0;
        int batchSize = 500;
        long lastId = 0;
        int errors = 0;

        try {
            while (true) {
                List<Sample> batch = sampleMapper.selectList(
                    new LambdaQueryWrapper<Sample>()
                        .gt(Sample::getId, lastId)
                        .last("LIMIT " + batchSize)
                        .orderByAsc(Sample::getId)
                );

                if (batch == null || batch.isEmpty()) break;

                // 批量加载缩略图获取 firstImageId
                List<Long> batchIds = batch.stream().map(Sample::getId).collect(Collectors.toList());
                Map<Long, Long> imageIdMap = new HashMap<>();
                if (!batchIds.isEmpty()) {
                    List<SampleThumbnail> thumbs = sampleThumbnailMapper.selectBatchIds(batchIds);
                    for (SampleThumbnail t : thumbs) {
                        imageIdMap.put(t.getSampleId(), t.getImageId());
                    }
                }

                List<SampleES> esBatch = new ArrayList<>(batch.size());
                for (Sample s : batch) {
                    s.setFirstImageId(imageIdMap.get(s.getId()));
                    esBatch.add(SampleES.builder()
                            .id(s.getId())
                            .sampleCode(s.getSampleCode())
                            .manufacturerCode(s.getManufacturerCode())
                            .sampleName(s.getSampleName())
                            .englishName(s.getEnglishName())
                            .category(s.getCategory())
                            .categoryCode(s.getCategoryCode())
                            .factoryCode(s.getFactoryCode())
                            .name(s.getName())
                            .boothNo(s.getBoothNo())
                            .contact1(s.getContact1())
                            .phone1(s.getPhone1())
                            .mobile1(s.getMobile1())
                            .smsNumber(s.getSmsNumber())
                            .factoryPrice(s.getFactoryPrice())
                            .sampleLength(s.getSampleLength())
                            .sampleWidth(s.getSampleWidth())
                            .sampleHeight(s.getSampleHeight())
                            .packagingCn(s.getPackagingCn())
                            .packageCode(s.getPackageCode())
                            .certification(s.getCertification())
                            .infringement(s.getInfringement())
                            .batteryInfo(s.getBatteryInfo())
                            .hideFromXzx(s.getHideFromXzx())
                            .cartonCapacity(s.getCartonCapacity())
                            .innerBoxCount(s.getInnerBoxCount())
                            .packageLength(s.getPackageLength())
                            .packageWidth(s.getPackageWidth())
                            .packageHeight(s.getPackageHeight())
                            .cartonLength(s.getCartonLength())
                            .cartonWidth(s.getCartonWidth())
                            .cartonHeight(s.getCartonHeight())
                            .registrant(s.getRegistrant())
                            .modifier(s.getModifier())
                            .createTime(s.getCreateTime())
                            .updateTime(s.getUpdateTime())
                            .remark(s.getRemark())
                            .deleted(s.getDeleted())
                            .firstImageId(s.getFirstImageId())
                            .build());
                    lastId = s.getId();
                }

                try {
                    sampleESRepository.saveAll(esBatch);
                    total += esBatch.size();
                    log.info("ES 全量同步进度: {} 条", total);
                } catch (Exception e) {
                    log.error("ES 批量同步失败: lastId={}, error={}", lastId, e.getMessage());
                    errors += esBatch.size();
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            result.put("success", true);
            result.put("total", total);
            result.put("errors", errors);
            result.put("elapsedMs", elapsed);
            result.put("message", String.format("同步完成: %d 条成功, %d 条失败, 耗时 %.1f 秒",
                    total, errors, elapsed / 1000.0));
            log.info("ES 全量同步完成: {} 条成功, {} 条失败, 耗时 {}ms", total, errors, elapsed);
        } catch (Exception e) {
            log.error("ES 全量同步异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "同步异常: " + e.getMessage());
            result.put("total", total);
        }

        return result;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        value = value.trim();
        // 尝试多种常见格式
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd HH:mm",
                "yyyy-M-d H:mm:ss",
                "yyyy-M-d H:mm",
                "yyyy/M/d H:mm:ss",
                "yyyy/M/d H:mm",
                "yyyy-MM-dd",
                "yyyy/MM/dd"
        };
        for (String pattern : patterns) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
                if (pattern.contains("HH")) {
                    return LocalDateTime.parse(value, fmt);
                } else {
                    return LocalDateTime.parse(value + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            } catch (DateTimeParseException ignored) {}
        }
        // 最后尝试 ISO 格式
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {}
        return null;
    }

    public java.util.List<Sample> matchByCodes(String type, java.util.List<String> codes, String manufacturerCode) {
        if (codes == null || codes.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        if ("factoryCode".equals(type)) {
            wrapper.in(Sample::getFactoryCode, codes);
        } else {
            wrapper.in(Sample::getSampleCode, codes);
        }
        if (manufacturerCode != null && !manufacturerCode.isEmpty()) {
            wrapper.eq(Sample::getManufacturerCode, manufacturerCode);
        }
        java.util.List<Sample> result = sampleMapper.selectList(wrapper);
        fillThumbnails(result);
        return result;
    }

    @Transactional
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx 或 .xls 格式文件");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // 自动检测表头行：扫描前5行，匹配别名最多的作为表头
        Row headerRow = null;
        int bestRow = 0;
        int bestMatch = 0;
        List<String> bestHeaders = new ArrayList<>();
        for (int r = 0; r < Math.min(5, sheet.getLastRowNum() + 1); r++) {
            Row candidate = sheet.getRow(r);
            if (candidate == null) continue;
            int match = 0;
            List<String> tentative = new ArrayList<>();
            for (int i = 0; i < candidate.getLastCellNum(); i++) {
                Cell cell = candidate.getCell(i);
                String val = getCellStringValue(cell).trim();
                tentative.add(val);
                if (StringUtils.hasText(val) && resolveHeader(val) != null) match++;
            }
            if (match > bestMatch) {
                bestMatch = match;
                bestRow = r;
                bestHeaders = tentative;
                headerRow = candidate;
            }
        }
        if (headerRow == null || bestMatch == 0) {
            workbook.close();
            throw new BusinessException(400, "Excel文件中未找到可识别的表头行");
        }
        List<String> headers = bestHeaders;

        // 数据起始行 = 表头行 + 1，直接跳过前面的标题行
        int dataStartRow = bestRow + 1;

        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;
        int totalDataRows = 0;

        Set<String> existingCodes = new HashSet<>();
        List<Sample> existingSamples = sampleMapper.selectList(
                new LambdaQueryWrapper<Sample>().select(Sample::getSampleCode).isNotNull(Sample::getSampleCode));
        for (Sample s : existingSamples) {
            if (s.getSampleCode() != null) {
                existingCodes.add(s.getSampleCode().trim());
            }
        }

        Set<String> importedCodes = new HashSet<>();

        for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            boolean isEmptyRow = true;
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                if (cell != null && StringUtils.hasText(getCellStringValue(cell).trim())) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) continue;

            totalDataRows++;

            try {
                Sample sample = new Sample();
                StringBuilder rowErrors = new StringBuilder();
                Map<String, String> compositeValues = new HashMap<>();

                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    if (!StringUtils.hasText(header)) continue;

                    Cell cell = row.getCell(j);
                    String cellValue = getCellStringValue(cell).trim();
                    if (!StringUtils.hasText(cellValue)) continue;

                    String fieldName = resolveHeader(header);
                    if (fieldName == null) continue;

                    // 复合列暂存到 map，后续统一拆分
                    if (fieldName.startsWith("_")) {
                        compositeValues.put(fieldName, cellValue);
                        continue;
                    }

                    try {
                        setFieldValue(sample, fieldName, cellValue);
                    } catch (NumberFormatException e) {
                        rowErrors.append(header).append("格式错误; ");
                    } catch (Exception e) {
                        rowErrors.append(header).append("赋值失败; ");
                    }
                }

                // 复合列拆分
                applySplits(sample, compositeValues);

                // 必须提供厂商编号或公司编号
                if (!StringUtils.hasText(sample.getManufacturerCode()) && !StringUtils.hasText(sample.getSampleCode())) {
                    rowErrors.append("厂商编号和公司编号至少需要一个; ");
                }

                // 自动生成公司编号：有厂商编号、无公司编号、有样品名称时 → 新增，自动生成编号
                if (!StringUtils.hasText(sample.getSampleCode()) && StringUtils.hasText(sample.getManufacturerCode()) && StringUtils.hasText(sample.getSampleName())) {
                    Set<String> allExisting = new HashSet<>(existingCodes);
                    allExisting.addAll(importedCodes);
                    String generatedCode = generateNextSampleCode(allExisting);
                    sample.setSampleCode(generatedCode);
                }

                if (!StringUtils.hasText(sample.getSampleCode()) && !StringUtils.hasText(sample.getSampleName())) {
                    rowErrors.append("公司编号和样品名称均为空; ");
                } else if (!StringUtils.hasText(sample.getSampleName())) {
                    rowErrors.append("样品名称为空; ");
                }

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    if (existingCodes.contains(code)) {
                        rowErrors.append("公司编号[").append(code).append("]已存在于数据库; ");
                    } else if (importedCodes.contains(code)) {
                        rowErrors.append("公司编号[").append(code).append("]在导入文件中重复; ");
                    }
                }

                if (rowErrors.length() > 0) {
                    Map<String, String> failRow = new LinkedHashMap<>();
                    failRow.put("row", String.valueOf(i + 1));
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        failRow.put(headers.get(j), getCellStringValue(cell).trim());
                    }
                    failRow.put("失败原因", rowErrors.toString());
                    failedRows.add(failRow);
                    continue;
                }

                // 将成功导入的编号加入已导入集合，防止同一批内重复
                if (StringUtils.hasText(sample.getSampleCode())) {
                    importedCodes.add(sample.getSampleCode().trim());
                    existingCodes.add(sample.getSampleCode().trim());
                }

                // 根据厂商编码自动回填摊位号等信息
                fillFromManufacturer(sample, null);

                // 如果外箱长宽高有值但体积/材积为空，自动计算
                calculateVolumeIfAbsent(sample);

                Long userId = UserContext.getUserId();
                sample.setCreateBy(userId);
                sample.setUpdateBy(userId);

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    Sample softDeleted = sampleMapper.findByCodeIncludeDeleted(code);
                    if (softDeleted != null) {
                        sample.setId(softDeleted.getId());
                        if (sample.getCreateTime() == null) sample.setCreateTime(softDeleted.getCreateTime());
                        if (sample.getCreateBy() == null) sample.setCreateBy(softDeleted.getCreateBy());
                        sample.setUpdateBy(userId);
                        sample.setDeleted(0);
                        sampleMapper.updateById(sample);
                        CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                        successCount++;
                        continue;
                    }
                }

                if (sample.getRegistrant() == null) {
                    sample.setRegistrant(UserContext.getRealName());
                }
                sampleMapper.insert(sample);
                successCount++;
            } catch (Exception e) {
                log.warn("导入第{}行失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    failRow.put(headers.get(j), getCellStringValue(cell).trim());
                }
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failedRows.add(failRow);
            }
        }

        workbook.close();

        result.setTotalCount(totalDataRows);
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size());
        result.setFailedRows(failedRows);
        return result;
    }

    public ImportResult batchInsert(List<Sample> samples, boolean updateMode) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;
        int duplicateCount = 0;
        int updatedCount = 0;

        if (samples == null || samples.isEmpty()) {
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
        Map<String, Manufacturer> manufacturerCache = new HashMap<>();

        for (int i = 0; i < samples.size(); i++) {
            Sample sample = samples.get(i);
            try {
                StringBuilder rowErrors = new StringBuilder();
                boolean isDuplicate = false;

                // 必须提供厂商编号或公司编号
                if (!StringUtils.hasText(sample.getManufacturerCode()) && !StringUtils.hasText(sample.getSampleCode())) {
                    rowErrors.append("厂商编号和公司编号至少需要一个; ");
                }

                // 自动生成公司编号：有厂商编号、无公司编号、有样品名称时 → 新增，自动生成编号
                if (!StringUtils.hasText(sample.getSampleCode()) && StringUtils.hasText(sample.getManufacturerCode()) && StringUtils.hasText(sample.getSampleName())) {
                    Set<String> allExisting = new HashSet<>(CACHED_EXISTING_CODES.keySet());
                    allExisting.addAll(importedCodes);
                    String generatedCode = generateNextSampleCode(allExisting);
                    sample.setSampleCode(generatedCode);
                }

                if (!StringUtils.hasText(sample.getSampleCode()) && !StringUtils.hasText(sample.getSampleName())) {
                    rowErrors.append("公司编号和样品名称均为空; ");
                }

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    if (CACHED_EXISTING_CODES.containsKey(code)) {
                        if (updateMode) {
                            // 根据厂商编码自动回填摊位号等信息
                            fillFromManufacturer(sample, manufacturerCache);
                            calculateVolumeIfAbsent(sample);
                            truncateFields(sample, i, failedRows);
                            Long userId = UserContext.getUserId();
                            sample.setUpdateBy(userId);
                            LambdaQueryWrapper<Sample> qw = new LambdaQueryWrapper<>();
                            qw.eq(Sample::getSampleCode, code).last("LIMIT 1");
                            Sample existing = sampleMapper.selectOne(qw);
                            if (existing != null) {
                                sample.setId(existing.getId());
                                if (sample.getCreateTime() == null) sample.setCreateTime(existing.getCreateTime());
                                if (sample.getCreateBy() == null) sample.setCreateBy(existing.getCreateBy());
                                sampleMapper.updateById(sample);
                                updatedCount++;
                                continue;
                            }
                            CACHED_EXISTING_CODES.remove(code);
                        } else {
                            rowErrors.append("公司编号[").append(code).append("]已存在于数据库; ");
                            isDuplicate = true;
                        }
                    } else if (importedCodes.contains(code)) {
                        rowErrors.append("公司编号[").append(code).append("]在导入数据中重复; ");
                        isDuplicate = true;
                    }
                }

                if (rowErrors.length() > 0) {
                    Map<String, String> failRow = new LinkedHashMap<>();
                    failRow.put("row", String.valueOf(i + 1));
                    failRow.put("公司编号", sample.getSampleCode() != null ? sample.getSampleCode() : "");
                    failRow.put("样品名称", sample.getSampleName() != null ? sample.getSampleName() : "");
                    failRow.put("失败原因", rowErrors.toString());
                    failRow.put("类型", isDuplicate ? "重复" : "校验失败");
                    failedRows.add(failRow);
                    if (isDuplicate) duplicateCount++;
                    continue;
                }

                // 将成功导入的编号加入已导入集合，防止同一批内重复
                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    importedCodes.add(code);
                    CACHED_EXISTING_CODES.put(code, Boolean.TRUE);
                }

                // 根据厂商编码自动回填摊位号等信息
                fillFromManufacturer(sample, manufacturerCache);
                calculateVolumeIfAbsent(sample);

                truncateFields(sample, i, failedRows);

                Long userId = UserContext.getUserId();
                sample.setCreateBy(userId);
                sample.setUpdateBy(userId);
                sample.setId(null);

                if (StringUtils.hasText(sample.getSampleCode())) {
                    String code = sample.getSampleCode().trim();
                    Sample softDeleted = sampleMapper.findByCodeIncludeDeleted(code);
                    if (softDeleted != null) {
                        sample.setId(softDeleted.getId());
                        sample.setCreateBy(softDeleted.getCreateBy());
                        sample.setCreateTime(softDeleted.getCreateTime());
                        sample.setUpdateBy(userId);
                        sample.setDeleted(0);
                        sampleMapper.updateById(sample);
                        successCount++;
                        continue;
                    }
                }

                if (sample.getRegistrant() == null) {
                    sample.setRegistrant(UserContext.getRealName());
                }
                sampleMapper.insert(sample);

                successCount++;
            } catch (Exception e) {
                log.warn("批量导入第{}条失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                failRow.put("公司编号", sample.getSampleCode() != null ? sample.getSampleCode() : "");
                failRow.put("样品名称", sample.getSampleName() != null ? sample.getSampleName() : "");
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failRow.put("类型", "异常");
                failedRows.add(failRow);
            }
        }

        result.setTotalCount(samples.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size() - duplicateCount);
        result.setDuplicateCount(duplicateCount);
        result.setUpdatedCount(updatedCount);
        result.setFailedRows(failedRows);
        return result;
    }

    private synchronized void ensureCodesCacheLoaded() {
        if (codesLoaded) return;
        List<Sample> all = sampleMapper.selectList(
                new LambdaQueryWrapper<Sample>().select(Sample::getSampleCode).isNotNull(Sample::getSampleCode));
        for (Sample s : all) {
            if (s.getSampleCode() != null) {
                CACHED_EXISTING_CODES.put(s.getSampleCode().trim(), Boolean.TRUE);
            }
        }
        List<String> deletedCodes = sampleMapper.findDeletedCodes();
        if (deletedCodes != null) {
            for (String code : deletedCodes) {
                if (code != null) {
                    CACHED_EXISTING_CODES.put(code.trim(), Boolean.TRUE);
                }
            }
        }
        codesLoaded = true;
    }

    /**
     * 自动生成下一个公司编号：基于已有编号中字母+数字模式的数字部分最大值+1。
     * 例如存在 YX01,YX02 → 返回 YX03；YX99 → 返回 YX100。
     * 若数据库无任何编号，默认返回 YX01。
     */
    private String generateNextSampleCode(Set<String> existingCodes) {
        String prefix = "YX";
        int maxNum = 0;
        int padding = 2;

        for (String code : existingCodes) {
            if (code == null) continue;
            Matcher m = SAMPLE_CODE_PATTERN.matcher(code.trim());
            if (m.matches()) {
                String pfx = m.group(1).toUpperCase();
                int num = Integer.parseInt(m.group(2));
                int digits = m.group(2).length();
                if (num > maxNum) {
                    maxNum = num;
                    prefix = pfx;
                    padding = digits;
                }
            }
        }

        int next = maxNum + 1;
        String formatted = String.format("%0" + padding + "d", next);
        if (formatted.length() > padding) {
            formatted = String.format("%0" + formatted.length() + "d", next);
        }
        return prefix + formatted;
    }

    private void truncateFields(Sample sample, int rowIndex, List<Map<String, String>> failedRows) {
        int maxLen;
        maxLen = 20; if (sample.getQq() != null && sample.getQq().length() > maxLen) { sample.setQq(sample.getQq().substring(0, maxLen)); }
        maxLen = 20; if (sample.getMobile1() != null && sample.getMobile1().length() > maxLen) { sample.setMobile1(sample.getMobile1().substring(0, maxLen)); }
        maxLen = 20; if (sample.getPhone1() != null && sample.getPhone1().length() > maxLen) { sample.setPhone1(sample.getPhone1().substring(0, maxLen)); }
        maxLen = 20; if (sample.getFax() != null && sample.getFax().length() > maxLen) { sample.setFax(sample.getFax().substring(0, maxLen)); }
        maxLen = 20; if (sample.getSampleUnit() != null && sample.getSampleUnit().length() > maxLen) { sample.setSampleUnit(sample.getSampleUnit().substring(0, maxLen)); }
        maxLen = 20; if (sample.getPackingUnit() != null && sample.getPackingUnit().length() > maxLen) { sample.setPackingUnit(sample.getPackingUnit().substring(0, maxLen)); }
        maxLen = 50; if (sample.getSampleCode() != null && sample.getSampleCode().length() > maxLen) { sample.setSampleCode(sample.getSampleCode().substring(0, maxLen)); }
        maxLen = 50; if (sample.getManufacturerCode() != null && sample.getManufacturerCode().length() > maxLen) { sample.setManufacturerCode(sample.getManufacturerCode().substring(0, maxLen)); }
        maxLen = 50; if (sample.getFactoryCode() != null && sample.getFactoryCode().length() > maxLen) { sample.setFactoryCode(sample.getFactoryCode().substring(0, maxLen)); }
        maxLen = 50; if (sample.getBoothNo() != null && sample.getBoothNo().length() > maxLen) { sample.setBoothNo(sample.getBoothNo().substring(0, maxLen)); }
        maxLen = 50; if (sample.getRegistrant() != null && sample.getRegistrant().length() > maxLen) { sample.setRegistrant(sample.getRegistrant().substring(0, maxLen)); }
        maxLen = 50; if (sample.getModifier() != null && sample.getModifier().length() > maxLen) { sample.setModifier(sample.getModifier().substring(0, maxLen)); }
        maxLen = 50; if (sample.getSampleUnitEn() != null && sample.getSampleUnitEn().length() > maxLen) { sample.setSampleUnitEn(sample.getSampleUnitEn().substring(0, maxLen)); }
        maxLen = 50; if (sample.getContact1() != null && sample.getContact1().length() > maxLen) { sample.setContact1(sample.getContact1().substring(0, maxLen)); }
        maxLen = 50; if (sample.getColor() != null && sample.getColor().length() > maxLen) { sample.setColor(sample.getColor().substring(0, maxLen)); }
        maxLen = 100; if (sample.getCategory() != null && sample.getCategory().length() > maxLen) { sample.setCategory(sample.getCategory().substring(0, maxLen)); }
        maxLen = 100; if (sample.getSize() != null && sample.getSize().length() > maxLen) { sample.setSize(sample.getSize().substring(0, maxLen)); }
        maxLen = 100; if (sample.getOrigin() != null && sample.getOrigin().length() > maxLen) { sample.setOrigin(sample.getOrigin().substring(0, maxLen)); }
        maxLen = 100; if (sample.getPackagingCn() != null && sample.getPackagingCn().length() > maxLen) { sample.setPackagingCn(sample.getPackagingCn().substring(0, maxLen)); }
        maxLen = 100; if (sample.getPackagingEn() != null && sample.getPackagingEn().length() > maxLen) { sample.setPackagingEn(sample.getPackagingEn().substring(0, maxLen)); }
        maxLen = 50; if (sample.getPackageCode() != null && sample.getPackageCode().length() > maxLen) { sample.setPackageCode(sample.getPackageCode().substring(0, maxLen)); }
        maxLen = 100; if (sample.getColorEn() != null && sample.getColorEn().length() > maxLen) { sample.setColorEn(sample.getColorEn().substring(0, maxLen)); }
        maxLen = 100; if (sample.getInfringement() != null && sample.getInfringement().length() > maxLen) { sample.setInfringement(sample.getInfringement().substring(0, maxLen)); }
        maxLen = 200; if (sample.getSampleName() != null && sample.getSampleName().length() > maxLen) { sample.setSampleName(sample.getSampleName().substring(0, maxLen)); }
        maxLen = 200; if (sample.getEnglishName() != null && sample.getEnglishName().length() > maxLen) { sample.setEnglishName(sample.getEnglishName().substring(0, maxLen)); }
        maxLen = 200; if (sample.getName() != null && sample.getName().length() > maxLen) { sample.setName(sample.getName().substring(0, maxLen)); }
        maxLen = 200; if (sample.getCertification() != null && sample.getCertification().length() > maxLen) { sample.setCertification(sample.getCertification().substring(0, maxLen)); }
        maxLen = 200; if (sample.getBatteryInfo() != null && sample.getBatteryInfo().length() > maxLen) { sample.setBatteryInfo(sample.getBatteryInfo().substring(0, maxLen)); }
    }

    private void setFieldValue(Sample sample, String fieldName, String value) throws Exception {
        if (fieldName.startsWith("_")) {
            // 复合列暂存不直接赋值，后续通过 applySplits 处理
            return;
        }
        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Class<?> paramType;

        if (DECIMAL_FIELDS.contains(fieldName)) {
            paramType = BigDecimal.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, new BigDecimal(value));
        } else if (INT_FIELDS.contains(fieldName)) {
            paramType = Integer.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, Integer.valueOf(value));
        } else if (LOCAL_DATETIME_FIELDS.contains(fieldName)) {
            paramType = LocalDateTime.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, parseDateTime(value));
        } else {
            paramType = String.class;
            Method setter = Sample.class.getMethod(setterName, paramType);
            setter.invoke(sample, value);
        }
    }

    // === 复合列拆分方法 ===

    // 尺寸拆分: 13.5*13.5*13.5 | 13.5x13.5x13.5CM → [长, 宽, 高]
    private BigDecimal[] splitDimensions(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim().replaceAll("(?i)cm$", "");
        String[] parts = cleaned.split("[*xX]");
        if (parts.length < 3) return null;
        try {
            BigDecimal l = new BigDecimal(parts[0].trim());
            BigDecimal w = new BigDecimal(parts[1].trim());
            BigDecimal h = new BigDecimal(parts[2].trim());
            return new BigDecimal[]{l, w, h};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 毛净重拆分: 26/24 → 毛重26 净重24 (大值=毛重)
    private BigDecimal[] splitGrossNet(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.trim().split("/");
        if (parts.length < 2) return null;
        try {
            BigDecimal a = new BigDecimal(parts[0].trim());
            BigDecimal b = new BigDecimal(parts[1].trim());
            return new BigDecimal[]{a.max(b), a.min(b)}; // [gross, net]
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applySplits(Sample sample, Map<String, String> compositeValues) {
        // 包装规格 → 包装长宽高
        String pkgDim = compositeValues.get("_pkgDimensions");
        if (pkgDim != null) {
            BigDecimal[] dims = splitDimensions(pkgDim);
            if (dims != null) {
                sample.setPackageLength(dims[0]);
                sample.setPackageWidth(dims[1]);
                sample.setPackageHeight(dims[2]);
                if (!StringUtils.hasText(sample.getPackagingCn())) {
                    sample.setPackagingCn(pkgDim.trim());
                }
            }
        }
        // 外箱规格 → 外箱长宽高
        String cartonDim = compositeValues.get("_cartonDimensions");
        if (cartonDim != null) {
            BigDecimal[] dims = splitDimensions(cartonDim);
            if (dims != null) {
                sample.setCartonLength(dims[0]);
                sample.setCartonWidth(dims[1]);
                sample.setCartonHeight(dims[2]);
            }
        }
        // 产品规格 → 产品长宽高
        String prodDim = compositeValues.get("_productDimensions");
        if (prodDim != null) {
            BigDecimal[] dims = splitDimensions(prodDim);
            if (dims != null) {
                sample.setSampleLength(dims[0]);
                sample.setSampleWidth(dims[1]);
                sample.setSampleHeight(dims[2]);
            }
        }
        // 毛/净重 → 外箱毛重/净重 + 样品毛重/净重
        String gn = compositeValues.get("_grossNetWeight");
        if (gn != null) {
            BigDecimal[] gnv = splitGrossNet(gn);
            if (gnv != null) {
                sample.setCartonGrossWeight(gnv[0]);
                sample.setCartonNetWeight(gnv[1]);
                sample.setSampleGrossWeight(gnv[0]);
                sample.setSampleNetWeight(gnv[1]);
            }
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 根据厂商编码从厂商表回填摊位号、厂商名称、联系人等信息（仅填充样品中为空的字段）
     * @param sample 样品对象
     * @param manufacturerCache 厂商缓存，批量导入时复用，单条导入传 null 即可
     */
    private void fillFromManufacturer(Sample sample, Map<String, Manufacturer> manufacturerCache) {
        String mfrCode = sample.getManufacturerCode();
        if (!StringUtils.hasText(mfrCode)) return;

        Manufacturer mfr;
        if (manufacturerCache != null) {
            mfr = manufacturerCache.computeIfAbsent(mfrCode, code -> {
                LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();
                qw.eq(Manufacturer::getManufacturerCode, code).last("LIMIT 1");
                return manufacturerMapper.selectOne(qw);
            });
        } else {
            LambdaQueryWrapper<Manufacturer> qw = new LambdaQueryWrapper<>();
            qw.eq(Manufacturer::getManufacturerCode, mfrCode).last("LIMIT 1");
            mfr = manufacturerMapper.selectOne(qw);
        }

        if (mfr == null) return;

        if (!StringUtils.hasText(sample.getBoothNo()) && StringUtils.hasText(mfr.getBoothNo())) {
            sample.setBoothNo(mfr.getBoothNo());
        }
        if (!StringUtils.hasText(sample.getName()) && StringUtils.hasText(mfr.getName())) {
            sample.setName(mfr.getName());
        }
        if (!StringUtils.hasText(sample.getContact1()) && StringUtils.hasText(mfr.getContact1())) {
            sample.setContact1(mfr.getContact1());
        }
        if (!StringUtils.hasText(sample.getPhone1()) && StringUtils.hasText(mfr.getPhone1())) {
            sample.setPhone1(mfr.getPhone1());
        }
        if (!StringUtils.hasText(sample.getMobile1()) && StringUtils.hasText(mfr.getMobile1())) {
            sample.setMobile1(mfr.getMobile1());
        }
        if (!StringUtils.hasText(sample.getQq()) && StringUtils.hasText(mfr.getQq())) {
            sample.setQq(mfr.getQq());
        }
    }

    /**
     * 如果外箱长宽高有值但体积/材积为空，自动计算填充
     */
    private void calculateVolumeIfAbsent(Sample sample) {
        BigDecimal length = sample.getCartonLength();
        BigDecimal width = sample.getCartonWidth();
        BigDecimal height = sample.getCartonHeight();
        if (length == null || width == null || height == null) return;

        // 体积 CBM（立方米）：(长cm/100) * (宽cm/100) * (高cm/100) = 长*宽*高 / 1,000,000
        if (sample.getCartonVolume() == null) {
            BigDecimal volume = length.multiply(width).multiply(height)
                    .divide(new BigDecimal("1000000"), 2, RoundingMode.HALF_UP);
            sample.setCartonVolume(volume);
        }

        // 材积 CUFT（立方英尺）：长(cm) * 宽(cm) * 高(cm) / 28316.8
        if (sample.getCartonMaterialVolume() == null) {
            BigDecimal materialVolume = length.multiply(width).multiply(height)
                    .divide(new BigDecimal("28316.8"), 2, RoundingMode.HALF_UP);
            sample.setCartonMaterialVolume(materialVolume);
        }
    }

    private void fillThumbnails(List<Sample> samples) {
        if (samples == null || samples.isEmpty()) return;
        List<Long> ids = new ArrayList<>();
        for (Sample s : samples) {
            if (s.getId() != null) ids.add(s.getId());
        }
        if (ids.isEmpty()) return;
        List<SampleThumbnail> thumbnails = sampleThumbnailMapper.selectBatchIds(ids);
        Map<Long, SampleThumbnail> map = new HashMap<>();
        for (SampleThumbnail t : thumbnails) {
            map.put(t.getSampleId(), t);
        }
        for (Sample s : samples) {
            SampleThumbnail t = map.get(s.getId());
            if (t != null) {
                s.setThumbnail(t.getThumbnail());
                s.setFirstImageId(t.getImageId());
                s.setFirstImageHash(t.getHash());
            }
        }
    }

    public PageResult<Sample> listDeleted(int current, int size) {
        IPage<Sample> page = sampleMapper.selectDeleted(new Page<>(current, size));
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public int restoreDeleted(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return sampleMapper.restoreByIds(ids);
    }

    public List<Map<String, Object>> vendorConfirmReportData(List<Long> sampleIds) {
        String sql = "SELECT m.id AS company_id, m.manufacturer_code AS vendor_code, s.manufacturer_code AS manufacturerCode, m.name AS vendor_name, "
                + "m.booth_no AS boothNo, m.contact1 AS contact, m.mobile1 AS mobile, "
                + "m.phone1 AS phone, m.qq AS qq, m.address AS address, "
                + "m.booth_type AS boothType, m.floor_area AS floorZone, "
                + "m.booth_area AS boothArea, m.certificate AS certNo, "
                + "m.last_expiry AS lastExpiry, m.expiry_date AS expiryDate, "
                + "m.registrant AS registrant, m.create_time AS createTime, "
                + "s.id AS sample_id, s.sample_code AS sampleCode, s.sample_name AS sampleName, "
                + "s.english_name AS englishName, s.factory_code AS factoryCode, "
                + "s.sample_unit AS sampleUnit, s.packaging_cn AS packagingCn, "
                + "s.packaging_en AS packagingEn, s.package_code AS packageCode, "
                + "s.factory_price AS factoryPrice, s.tax_price AS taxPrice, "
                + "CONCAT(s.sample_length,'x',s.sample_width,'x',s.sample_height) AS productSpec, "
                + "s.sample_length AS sampleLength, s.sample_width AS sampleWidth, s.sample_height AS sampleHeight, "
                + "s.sample_gross_weight AS sampleGrossWeight, s.sample_net_weight AS sampleNetWeight, "
                + "s.carton_length AS cartonLength, s.carton_width AS cartonWidth, "
                + "s.carton_height AS cartonHeight, "
                + "CONCAT(s.carton_length,'x',s.carton_width,'x',s.carton_height) AS cartonSpec, "
                + "s.carton_volume AS cartonVolume, s.carton_material_volume AS cartonMaterialVolume, "
                + "s.carton_gross_weight AS cartonGrossWeight, s.carton_net_weight AS cartonNetWeight, "
                + "s.inner_box_count AS innerBoxCount, s.carton_capacity AS cartonCapacity, "
                + "s.packing_unit AS packingUnit, "
                + "s.package_length AS packageLength, s.package_width AS packageWidth, s.package_height AS packageHeight, "
                + "CONCAT(s.package_length,'x',s.package_width,'x',s.package_height) AS packageSpec, "
                + "s.certification AS certification, s.category AS category, s.category_code AS categoryCode, "
                + "s.color AS color, s.size AS size, "
                + "s.battery_info AS batteryInfo, "
                + "s.hide_from_xzx AS hideFromXzx, "
                + "s.infringement AS infringement, "
                + "s.remark AS remarkCn, s.remark_en AS remarkEn, "
                + "CONCAT('/thumbnails/', st.thumbnail) AS imagePath, "
                + "st.thumbnail AS thumbnail, "
                + "NOW() AS printTime "
                + "FROM manufacturers m "
                + "LEFT JOIN samples s ON m.manufacturer_code = s.manufacturer_code "
                + "LEFT JOIN sample_thumbnail st ON st.sample_id = s.id "
                + "WHERE m.deleted = 0 AND s.deleted = 0 ";

        if (sampleIds != null && !sampleIds.isEmpty()) {
            String placeholders = sampleIds.stream().map(id -> "?").collect(Collectors.joining(","));
            sql += "AND s.id IN (" + placeholders + ") ";
        } else {
            // 空列表 = 没有匹配数据，加一个永远不成立的条件
            sql += "AND 1=0 ";
        }
        sql += "ORDER BY m.create_time DESC, s.create_time DESC";

        if (sampleIds != null && !sampleIds.isEmpty()) {
            Object[] params = sampleIds.toArray();
            return jdbcTemplate.queryForList(sql, params);
        }
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取当前活跃操作员名称（打印报表用）
     */
    public String getActiveOperatorName() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COALESCE(real_name, '') FROM users WHERE status = 1 LIMIT 1",
                String.class
            );
        } catch (Exception e) {
            return "";
        }
    }

}
