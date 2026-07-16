package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.dto.ImportResult;
import com.app.dto.SearchCondition;
import com.app.entity.*;
import com.app.mapper.ClientSampleMapper;
import com.app.mapper.ClientSampleItemMapper;
import com.app.mapper.CustomerMapper;
import com.app.service.ClientSampleService;
import com.app.service.CustomerService;
import com.app.service.ExportTaskService;
import com.app.service.ReportTemplateService;
import com.app.service.SampleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import com.app.mapper.ImageMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.app.entity.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/client-samples")
public class ClientSampleController {

    private static final Logger log = LoggerFactory.getLogger(ClientSampleController.class);

    @Autowired
    private ClientSampleService clientSampleService;

    @Autowired
    private ReportTemplateService reportTemplateService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ClientSampleMapper clientSampleMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private SampleService sampleService;

    @Value("${app.upload.image-path}")
    private String imageStoragePath;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailStoragePath;

    // Dedicated thread pool for parallel image processing in exports
    private static final ExecutorService IMAGE_PROCESSOR = Executors.newFixedThreadPool(32, r -> {
        Thread t = new Thread(r, "img-proc"); t.setDaemon(true); return t;
    });

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    @Autowired
    private ClientSampleItemMapper clientSampleItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExportTaskService exportTaskService;

    @Autowired
    private com.app.service.ImageService imageService;

    @GetMapping
    public Result<PageResult<ClientSample>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        return Result.success(clientSampleService.list(current, size, keyword, sortField, sortOrder, dateFrom, dateTo));
    }

    @GetMapping("/{id}")
    public Result<ClientSample> getById(@PathVariable Long id) {
        return Result.success(clientSampleService.getById(id));
    }

    @GetMapping("/next-code")
    public Result<Map<String, String>> nextCode() {
        return Result.success(clientSampleService.nextCode());
    }

    @PostMapping
    public Result<ClientSample> create(@RequestBody ClientSample sample) {
        return Result.success("添加成功", clientSampleService.create(sample));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ClientSample sample) {
        clientSampleService.update(id, sample);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        clientSampleService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/import")
    public Result<ImportResult> batchImport(@RequestBody List<ClientSample> samples,
                                            @RequestParam(defaultValue = "false") boolean updateMode) {
        try {
            ImportResult result = clientSampleService.batchInsert(samples, updateMode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入客户择样失败: {}", e.getMessage(), e);
            return Result.error(500, "批量导入失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    // ==================== 标签模板关联 ====================

    @GetMapping("/{codeName}/label-templates")
    public Result<String> getLabelTemplates(@PathVariable String codeName) {
        return Result.success(clientSampleService.getLabelTemplateIds(codeName));
    }

    @PutMapping("/{codeName}/label-templates")
    public Result<Void> saveLabelTemplates(@PathVariable String codeName, @RequestBody Map<String, String> body) {
        clientSampleService.saveLabelTemplateIds(codeName, body.get("templateIds"));
        return Result.ok("保存成功");
    }

    // ==================== 择样明细 ====================

    @GetMapping("/items/sample")
    public Result<List<Sample>> getSampleItems(@RequestParam(defaultValue = "5") int size) {
        return Result.success(clientSampleService.getSampleItems(size));
    }

    @GetMapping("/{codeName}/items")
    public Result<List<Sample>> getItems(@PathVariable String codeName) {
        return Result.success(clientSampleService.getItemsByCodeName(codeName));
    }

    @PostMapping("/{codeName}/items")
    public Result<Integer> addItems(@PathVariable String codeName,
                                     @RequestBody List<Long> sampleIds,
                                     @RequestParam(required = false) String boxCount,
                                     @RequestParam(required = false) String otherRemark,
                                     @RequestParam(defaultValue = "false") boolean force) {
        int count = clientSampleService.addItems(codeName, sampleIds, boxCount, otherRemark, force);
        return Result.success("成功添加 " + count + " 条记录", count);
    }

    /** 获取厂商总数（无过滤） */
    @GetMapping("/factory-count")
    public Result<Integer> getFactoryCount() {
        int count = clientSampleService.getFactoryCount(null, null, null);
        return Result.success(count);
    }

    /** 获取厂商总数（带过滤条件） */
    @PostMapping("/factory-count")
    public Result<Integer> getFactoryCountFiltered(@RequestBody Map<String, Object> params) {
        System.out.println("[factory-count POST] params=" + params);
        String keyword = (String) params.get("keyword");
        List<SearchCondition> conditions = null;
        if (params.get("conditions") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> raw = (List<Map<String, Object>>) params.get("conditions");
            conditions = raw.stream().map(m -> {
                SearchCondition c = new SearchCondition();
                c.setField((String) m.get("field"));
                c.setOperator((String) m.get("operator"));
                c.setValue(m.get("value") != null ? m.get("value").toString() : null);
                return c;
            }).collect(Collectors.toList());
        }
        String logic = (String) params.get("logic");
        int count = clientSampleService.getFactoryCount(keyword, conditions, logic);
        return Result.success(count);
    }

    /** 按厂商选品预览（真实统计，非简单乘法） */
    @PostMapping("/factory-count/preview")
    public Result<Map<String, Object>> previewFactorySelection(@RequestBody Map<String, Object> params) {
        String keyword = (String) params.get("keyword");
        List<SearchCondition> conditions = null;
        if (params.get("conditions") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> raw = (List<Map<String, Object>>) params.get("conditions");
            conditions = raw.stream().map(m -> {
                SearchCondition c = new SearchCondition();
                c.setField((String) m.get("field"));
                c.setOperator((String) m.get("operator"));
                c.setValue(m.get("value") != null ? m.get("value").toString() : null);
                return c;
            }).collect(Collectors.toList());
        }
        String logic = (String) params.get("logic");
        int factoryCount = ((Number) params.getOrDefault("factoryCount", 0)).intValue();
        int productsPerFactory = ((Number) params.getOrDefault("productsPerFactory", 1)).intValue();
        Map<String, Object> result = clientSampleService.previewFactorySelection(
                keyword, conditions, logic, factoryCount, productsPerFactory);
        return Result.success(result);
    }

    /** 按厂商自动选品 */
    @SuppressWarnings("unchecked")
    @PostMapping("/{codeName}/select-by-factory")
    public Result<Integer> selectByFactory(@PathVariable String codeName,
                                            @RequestBody Map<String, Object> params) {
        int factoryCount = ((Number) params.getOrDefault("factoryCount", 0)).intValue();
        int productsPerFactory = ((Number) params.getOrDefault("productsPerFactory", 1)).intValue();
        String keyword = (String) params.get("keyword");
        boolean force = Boolean.TRUE.equals(params.get("force"));
        List<SearchCondition> conditions = null;
        if (params.get("conditions") instanceof List) {
            List<Map<String, Object>> raw = (List<Map<String, Object>>) params.get("conditions");
            conditions = raw.stream().map(m -> {
                SearchCondition c = new SearchCondition();
                c.setField((String) m.get("field"));
                c.setOperator((String) m.get("operator"));
                c.setValue(m.get("value") != null ? m.get("value").toString() : null);
                return c;
            }).collect(Collectors.toList());
        }
        String logic = (String) params.get("logic");
        int count = clientSampleService.selectByFactory(codeName, factoryCount, productsPerFactory, keyword, conditions, logic, force);
        return Result.success("成功添加 " + count + " 条记录", count);
    }

    @DeleteMapping("/{codeName}/items/{itemId}")
    public Result<Void> removeItem(@PathVariable Long itemId) {
        clientSampleService.removeItem(itemId);
        return Result.ok("删除成功");
    }

    @DeleteMapping("/{codeName}/items")
    public Result<Void> removeItems(@RequestBody List<Long> itemIds) {
        clientSampleService.removeItems(itemIds);
        return Result.ok("删除成功");
    }

    /**
     * 恢复已删除的择样明细（前端本地缓存删除记录后重新写入）
     */
    @PostMapping("/{codeName}/items/batch-restore")
    public Result<Integer> batchRestoreItems(@PathVariable String codeName, @RequestBody List<Sample> items) {
        int count = clientSampleService.restoreItems(codeName, items);
        return Result.success("成功恢复 " + count + " 条记录", count);
    }

    /**
     * 查询已删除的明细记录
     */
    @GetMapping("/{codeName}/items/deleted")
    public Result<List<Map<String, Object>>> getDeletedItems(@PathVariable String codeName) {
        return Result.success(clientSampleService.getDeletedItems(codeName));
    }

    /**
     * 更新样品快照（修改资料时调用，仅影响当前代号）
     */
    @PutMapping("/{codeName}/items/{itemId}")
    public Result<Void> updateItemSnapshot(@PathVariable Long itemId, @RequestBody Sample updatedSample) {
        clientSampleService.updateItemSnapshot(itemId, updatedSample);
        return Result.ok("更新成功");
    }

    /**
     * 更新明细标记（展厅已补、借样等）
     */
    @PutMapping("/{codeName}/items/{itemId}/flag")
    public Result<Void> updateItemFlag(@PathVariable Long itemId,
                                        @RequestParam String field,
                                        @RequestParam Integer value) {
        clientSampleService.updateItemFlag(itemId, field, value);
        return Result.ok("更新成功");
    }

    /**
     * 批量更新明细标记
     */
    @PutMapping("/{codeName}/items/flags")
    public Result<Void> updateItemFlags(@RequestBody List<Map<String, Object>> flags) {
        clientSampleService.updateItemFlags(flags);
        return Result.ok("批量更新成功");
    }

    // ==================== 报价设置 ====================

    @PostMapping("/{codeName}/calculate-price")
    public Result<List<Sample>> calculatePrices(@PathVariable String codeName, @RequestBody List<Long> sampleIds) {
        return Result.success(clientSampleService.calculatePrices(codeName, sampleIds));
    }

    @GetMapping("/{codeName}/price-setting")
    public Result<ClientSamplePriceSetting> getPriceSetting(@PathVariable String codeName,
                                                            @RequestParam(defaultValue = "1") String type) {
        ClientSamplePriceSetting setting = clientSampleService.getPriceSetting(codeName, type);
        return Result.success(setting);
    }

    @PostMapping("/{codeName}/price-setting")
    public Result<ClientSamplePriceSetting> savePriceSetting(@PathVariable String codeName,
                                                              @RequestParam(defaultValue = "1") String type,
                                                              @RequestBody Map<String, Object> body) {
        ClientSamplePriceSetting setting = objectMapper.convertValue(body.get("setting"), ClientSamplePriceSetting.class);
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("sampleIds");
        List<Long> sampleIds = null;
        if (rawIds != null && !rawIds.isEmpty()) {
            sampleIds = rawIds.stream().map(Integer::longValue).collect(Collectors.toList());
        }
        ClientSamplePriceSetting saved = clientSampleService.savePriceSetting(codeName, type, setting, sampleIds);
        return Result.success("保存成功", saved);
    }

    @PostMapping("/{codeName}/price-export")
    public void priceExport(@PathVariable String codeName,
                            @RequestBody Map<String, Object> body,
                            HttpServletResponse response) throws IOException {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = null;
        Integer templateId = null;
        try {
        idsRaw = (List<Integer>) body.get("ids");
        try {
            templateId = body.get("templateId") != null
                    ? ((Number) body.get("templateId")).intValue() : null;
        } catch (Exception e) {
            response.sendError(400, "templateId must be a number");
            return;
        }

        if (idsRaw == null || idsRaw.isEmpty() || templateId == null) {
            response.sendError(400, "ids and templateId required");
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (Integer id : idsRaw) ids.add(id.longValue());

        // 1. Load template
        ReportTemplate template = reportTemplateService.getById(templateId.longValue());
        if (template == null) {
            response.sendError(404, "template not found");
            return;
        }

        // 2. Load client_sample_items (择样详情数据源) — 只查选中样品
        List<ClientSampleItem> allItems = clientSampleItemMapper.selectItemsByCodeNameAndSampleIds(codeName, ids);
        Map<Long, ClientSampleItem> itemBySampleMap = new LinkedHashMap<>();
        if (allItems != null) {
            for (ClientSampleItem item : allItems) {
                if (item.getSampleId() != null) {
                    itemBySampleMap.put(item.getSampleId(), item);
                }
            }
        }
        // Live samples only for null-fill (factoryPrice/taxPrice) and image fallback
        Map<Long, Sample> liveSampleMap = new HashMap<>();
        List<Sample> liveSamples = sampleService.listByIdsWithThumbnails(ids);
        if (liveSamples != null) {
            for (Sample s : liveSamples) liveSampleMap.put(s.getId(), s);
        }
        // Price1 setting for calculatedPrice fallback
        ClientSamplePriceSetting price1Setting = clientSampleService.getPriceSetting(codeName, "1");

        // 3. Load customer data
        Map<String, Object> customerData = null;
        ClientSample clientSample = clientSampleMapper.selectOne(
                new LambdaQueryWrapper<ClientSample>().eq(ClientSample::getCodeName, codeName));
        if (clientSample != null && clientSample.getClientCode() != null) {
            Customer customer = customerMapper.selectOne(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerCode, clientSample.getClientCode()));
            if (customer != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = objectMapper.convertValue(customer, Map.class);
                customerData = m;
            }
        }

        // 4. Parse template JSON
        String templateJson = template.getTemplateData();
        if (templateJson == null || templateJson.isEmpty()) {
            response.sendError(400, "template data is empty");
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> td;
        try {
            td = objectMapper.readValue(templateJson, Map.class);
        } catch (Exception e) {
            log.error("price-export: failed to parse template JSON: {}", e.getMessage());
            response.sendError(400, "template JSON parse error: " + e.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cellDataRaw = null;
        try { cellDataRaw = (Map<String, Map<String, Object>>) td.get("cellData"); } catch (Exception ignore) {}
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mergedCellsList = null;
        try { mergedCellsList = (List<Map<String, Object>>) td.get("mergedCells"); } catch (Exception ignore) {}
        @SuppressWarnings("unchecked")
        Map<String, Object> colWidthsRaw = null;
        try { colWidthsRaw = (Map<String, Object>) td.get("colWidths"); } catch (Exception ignore) {}
        @SuppressWarnings("unchecked")
        Map<String, Object> rowHeightsRaw = null;
        try { rowHeightsRaw = (Map<String, Object>) td.get("rowHeights"); } catch (Exception ignore) {}
        @SuppressWarnings("unchecked")
        Map<String, Object> config = null;
        try { config = (Map<String, Object>) td.get("config"); } catch (Exception ignore) {}
        int defaultColWidth = 120, defaultRowHeight = 48;
        if (colWidthsRaw == null) colWidthsRaw = new LinkedHashMap<>();
        if (rowHeightsRaw == null) rowHeightsRaw = new LinkedHashMap<>();

        // 5. Parse cells & compute bands (identical to frontend clustering algorithm)
        List<CellDef> allCells = new ArrayList<>();
        if (cellDataRaw != null) {
            for (Map.Entry<String, Map<String, Object>> e : cellDataRaw.entrySet()) {
                Matcher m = Pattern.compile("R(\\d+)C(\\d+)").matcher(e.getKey());
                if (!m.find()) continue;
                CellDef cd = new CellDef();
                cd.r = Integer.parseInt(m.group(1));
                cd.c = Integer.parseInt(m.group(2));
                Map<String, Object> vv = e.getValue();
                cd.v = vv.get("v") != null ? String.valueOf(vv.get("v")) : "";
                @SuppressWarnings("unchecked")
                Map<String, Object> fmt = (Map<String, Object>) vv.get("fmt");
                cd.fmt = fmt;
                allCells.add(cd);
            }
        }
        if (allCells.isEmpty()) {
            response.sendError(400, "empty template");
            return;
        }

        BandResult bands = computeBands(allCells);
        int maxC = allCells.stream().mapToInt(c -> c.c).max().orElse(1);

        // cellsByKey for O(1) lookup (replaces O(n) find() from frontend)
        Map<String, CellDef> cellsByKey = new HashMap<>();
        for (CellDef cd : allCells) cellsByKey.put(cd.r + "_" + cd.c, cd);

        // Determine row groups
        Set<Integer> headerRowSet = new TreeSet<>();
        for (int r = 1; r < bands.loopBaseR; r++) headerRowSet.add(r);

        Set<Integer> trailingRowSet = new TreeSet<>();
        for (CellDef cd : allCells) {
            if (cd.r > bands.loopMaxR) trailingRowSet.add(cd.r);
        }
        List<Integer> trailingList = new ArrayList<>(trailingRowSet);
        Collections.sort(trailingList);

        // 6. Build enriched row data (samples + computed fields + thumbnail info)
        List<Map<String, Object>> enrichedRows = new ArrayList<>();
        Map<Long, String> sampleImagePathCache = new HashMap<>(); // sampleId -> full image filePath
        Map<Long, String> sampleImageHashCache = new HashMap<>(); // sampleId -> image hash

        // Build enriched rows from snapshot data (client_sample_items), ordered by ids
        for (Long id : ids) {
            ClientSampleItem item = itemBySampleMap.get(id);
            Sample snapSample = null;
            // Parse snapshot JSON as primary data source
            if (item != null && item.getSnapshotData() != null && !item.getSnapshotData().isEmpty()) {
                try {
                    snapSample = objectMapper.readValue(item.getSnapshotData(), Sample.class);
                } catch (Exception e) {
                    log.warn("price-export: failed to parse snapshot for sampleId={}: {}", id, e.getMessage());
                }
            }
            if (snapSample == null) snapSample = liveSampleMap.get(id);
            if (snapSample == null) continue;
            snapSample.setId(id);

            // Null-fill factoryPrice/taxPrice from live sample
            Sample live = liveSampleMap.get(id);
            if (live != null) {
                if (snapSample.getFactoryPrice() == null) snapSample.setFactoryPrice(live.getFactoryPrice());
                if (snapSample.getTaxPrice() == null) snapSample.setTaxPrice(live.getTaxPrice());
                if (snapSample.getName() == null || snapSample.getName().isEmpty()) snapSample.setName(live.getName());
                if (snapSample.getPhone1() == null || snapSample.getPhone1().isEmpty()) snapSample.setPhone1(live.getPhone1());
                if (snapSample.getMobile1() == null || snapSample.getMobile1().isEmpty()) snapSample.setMobile1(live.getMobile1());
            }

            // Calculated prices (报价1/价格2) from client_sample_items
            if (item != null) {
                if (item.getCalculatedPrice() != null) {
                    snapSample.setCalculatedPrice(item.getCalculatedPrice());
                } else if (price1Setting != null) {
                    snapSample.setCalculatedPrice(java.math.BigDecimal.ZERO);
                } else {
                    snapSample.setCalculatedPrice(snapSample.getFactoryPrice());
                }
                // 同步到 taxPrice：模板中 ${taxPrice} = 价格1，应展示报价计算结果
                if (price1Setting != null) {
                    snapSample.setTaxPrice(snapSample.getCalculatedPrice());
                }
                snapSample.setTaxPrice2(item.getCalculatedPrice2());
                snapSample.setBorrowedSample(item.getBorrowedSample());
                snapSample.setShowroomReplenished(item.getShowroomReplenished());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> row = objectMapper.convertValue(snapSample, Map.class);
            enrichRow(row);

            // Image: snapshot first, fall back to live sample thumbnail
            String hash = snapSample.getFirstImageHash();
            if ((hash == null || hash.isEmpty()) && live != null) {
                hash = live.getFirstImageHash();
                if (hash != null && !hash.isEmpty()) {
                    row.put("firstImageHash", hash);
                    row.put("firstImageId", live.getFirstImageId());
                    row.put("thumbnail", live.getThumbnail());
                }
            }
            if (hash != null && !hash.isEmpty()) {
                sampleImageHashCache.put(id, hash);
            }

            enrichedRows.add(row);
        }

        // Batch-load image file paths for embedding
        Set<String> imageHashes = new HashSet<>(sampleImageHashCache.values());
        imageHashes.remove(null);
        if (!imageHashes.isEmpty()) {
            List<Image> imageEntities = imageMapper.selectList(
                    new LambdaQueryWrapper<Image>().in(Image::getHash, imageHashes));
            if (imageEntities != null) {
                Map<String, String> hashToPath = new HashMap<>();
                for (Image img : imageEntities) hashToPath.put(img.getHash(), img.getFilePath());
                for (Map.Entry<Long, String> e : sampleImageHashCache.entrySet()) {
                    String fp = hashToPath.get(e.getValue());
                    if (fp != null) sampleImagePathCache.put(e.getKey(), fp);
                }
            }
        }

        // Footer context
        Map<String, Object> footerData = new LinkedHashMap<>();
        if (!enrichedRows.isEmpty()) {
            footerData.putAll(new LinkedHashMap<>(enrichedRows.get(0)));
        }
        footerData.put("currentPage", 1);
        footerData.put("page", 1);
        footerData.put("total_pages", 1);
        footerData.put("printTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        if (config != null) footerData.put("title", config.getOrDefault("title", ""));

        // 7. Create workbook (SXSSFWorkbook for streaming to avoid OOM on large datasets)
        // Row window: keep at most 500 rows in memory, rest flushed to temp files
        int estimatedRows = headerRowSet.size() + enrichedRows.size() * (bands.loopMaxR - bands.loopBaseR + 1) + trailingList.size() + 10;
        int rowWindowSize = Math.min(estimatedRows, 500);
        SXSSFWorkbook wb = new SXSSFWorkbook(rowWindowSize);
        wb.setCompressTempFiles(true);
        Map<String, XSSFCellStyle> styleCache = new HashMap<>();

        // 默认字体：对齐前端 config.fontSize 和 config.fontFamily
        int defaultFontSize = 12;
        if (config != null && config.get("fontSize") instanceof Number)
            defaultFontSize = ((Number) config.get("fontSize")).intValue();
        String defaultFontFamily = config != null ? (String) config.get("fontFamily") : null;
        if (defaultFontFamily == null) defaultFontFamily = "SimSun, serif";

        Sheet sheet = wb.createSheet(config != null && config.get("title") != null
                ? String.valueOf(config.get("title")) : "报价表");
        // 关闭默认网格线，只显示模板定义的边框（对齐前端 showGridLines=false）
        sheet.setDisplayGridlines(false);

        // Column widths
        for (int c = 1; c <= maxC; c++) {
            int w = defaultColWidth;
            Object wVal = colWidthsRaw.get(String.valueOf(c));
            if (wVal instanceof Number) w = ((Number) wVal).intValue();
            sheet.setColumnWidth(c - 1, Math.round(w / 7f) * 256);
        }

        // Color parser
        java.util.function.Function<String, XSSFColor> parseColor = hex -> {
            if (hex == null || hex.isEmpty()) return new XSSFColor(new byte[]{0, 0, 0}, null);
            try {
                String h = hex.replace("#", "");
                int r = Integer.parseInt(h.substring(0, 2), 16);
                int g = Integer.parseInt(h.substring(2, 4), 16);
                int b = Integer.parseInt(h.substring(4, 6), 16);
                return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b}, null);
            } catch (Exception ignored) {
                return new XSSFColor(new byte[]{0, 0, 0}, null);
            }
        };

        Map<String, Object> finalCustomerData = customerData;
        Map<String, Object> finalConfig = config;

        // === Phase A: Render header rows ===
        Map<String, Object> hdrData = enrichedRows.isEmpty() ? new LinkedHashMap<>() : enrichedRows.get(0);

        for (int r : headerRowSet) {
            Row row = sheet.createRow(r - 1);
            int h = defaultRowHeight;
            Object hVal = rowHeightsRaw.get(String.valueOf(r));
            if (hVal instanceof Number) h = ((Number) hVal).intValue();
            row.setHeightInPoints(h);

            for (int c = 1; c <= maxC; c++) {
                CellDef cd = cellsByKey.get(r + "_" + c);
                if (cd == null) continue; // 对齐前端：无模板数据的列不创建单元格
                Cell cell = row.createCell(c - 1);
                String val = resolvePlaceholder(cd.v, hdrData, finalCustomerData, finalConfig);
                cell.setCellValue(val);
                applyCellStyle(cell, cd.fmt, parseColor, styleCache, defaultFontSize, defaultFontFamily);
            }
        }

        // === Header merged cells (必须在 Phase A 结束后立即执行，避免 SXSSF 刷出行内存) ===
        if (mergedCellsList != null) {
            for (Map<String, Object> mc : mergedCellsList) {
                int sR = ((Number) mc.get("sR")).intValue();
                if (sR >= bands.loopBaseR) continue;
                int eR = ((Number) mc.get("eR")).intValue();
                if (eR >= bands.loopBaseR) continue;
                int sC = ((Number) mc.get("sC")).intValue();
                int eC = ((Number) mc.get("eC")).intValue();
                ensureMergeCellsExist(sheet, sR, eR, sC, eC);
                try {
                    sheet.addMergedRegion(new CellRangeAddress(sR - 1, eR - 1, sC - 1, eC - 1));
                } catch (Exception ignored) {}
            }
        }

        // === Phase B: Embed logo ===
        CellDef logoCell = allCells.stream()
                .filter(cd -> cd.v != null && cd.v.contains("${logo}")).findFirst().orElse(null);
        if (logoCell != null && config != null && config.get("logoImage") != null) {
            try {
                String b64 = config.get("logoImage").toString()
                        .replaceAll("^data:image/\\w+;base64,", "");
                byte[] imgBytes = Base64.getDecoder().decode(b64);
                ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes);
                BufferedImage bi = ImageIO.read(bis);
                if (bi != null) {
                    ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                    ImageIO.write(bi, "png", bos2);
                    int picIdx = wb.addPicture(bos2.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    XSSFClientAnchor anchor = (XSSFClientAnchor) wb.getCreationHelper().createClientAnchor();

                    // 从模板合并单元格中查找 logo 所在的合并区域尺寸
                    int logoSpanC = 1, logoSpanR = 1; // 默认 1x1，无合并
                    if (mergedCellsList != null) {
                        for (Map<String, Object> mc : mergedCellsList) {
                            int msR = ((Number) mc.get("sR")).intValue();
                            int meR = ((Number) mc.get("eR")).intValue();
                            int msC = ((Number) mc.get("sC")).intValue();
                            int meC = ((Number) mc.get("eC")).intValue();
                            if (logoCell.r >= msR && logoCell.r <= meR
                                    && logoCell.c >= msC && logoCell.c <= meC) {
                                logoSpanC = meC - msC + 1;
                                logoSpanR = meR - msR + 1;
                                break;
                            }
                        }
                    }
                    // 与产品图片一致：锚定到合并单元格区域，图片填满单元格
                    anchor.setCol1(logoCell.c - 1);
                    anchor.setRow1(logoCell.r - 1);
                    anchor.setCol2(logoCell.c - 1 + logoSpanC);
                    anchor.setRow2(logoCell.r - 1 + logoSpanR);
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    drawing.createPicture(anchor, picIdx);
                }
            } catch (Exception e) {
                log.warn("price-export: failed to embed logo: {}", e.getMessage());
            }
        }

        // === Phase C: Render data loop rows ===
        int totalLoopRows = 0;
        // Collect image tasks: {r, c, sampleId}
        List<Map<String, Integer>> imageTasks = new ArrayList<>();

        if (!enrichedRows.isEmpty() && !bands.loopRows.isEmpty()) {
            int blockRows = bands.loopMaxR - bands.loopBaseR + 1;
            int headerMaxR = headerRowSet.isEmpty() ? 0 : Collections.max(headerRowSet);

            for (int di = 0; di < enrichedRows.size(); di++) {
                Map<String, Object> rowData = enrichedRows.get(di);
                int blockStart = headerMaxR + di * blockRows + 1;

                for (int r0 = 0; r0 < blockRows; r0++) {
                    int absR = blockStart + r0;
                    int tmplR = bands.loopBaseR + r0;

                    Row row = sheet.createRow(absR - 1);
                    int h = defaultRowHeight;
                    Object hVal = rowHeightsRaw.get(String.valueOf(tmplR));
                    if (hVal instanceof Number) h = ((Number) hVal).intValue();
                    row.setHeightInPoints(h);

                    for (int c = 1; c <= maxC; c++) {
                        CellDef cd = cellsByKey.get(tmplR + "_" + c);
                        if (cd == null) continue; // 对齐前端：无模板数据的列不创建单元格
                        Cell cell = row.createCell(c - 1);

                        if (containsImagePlaceholder(cd.v)) {
                            // Collect for later embedding
                            Map<String, Integer> task = new HashMap<>();
                            task.put("r", absR);
                            task.put("c", c);
                            task.put("di", di);
                            imageTasks.add(task);
                            cell.setCellValue("");
                        } else {
                            String val = resolvePlaceholder(cd.v, rowData, finalCustomerData, finalConfig);
                            cell.setCellValue(val);
                        }
                        applyCellStyle(cell, cd.fmt, parseColor, styleCache, defaultFontSize, defaultFontFamily);
                    }
                }

                // Block-level merged cells
                if (mergedCellsList != null) {
                    for (Map<String, Object> mc : mergedCellsList) {
                        int sR = ((Number) mc.get("sR")).intValue();
                        if (sR < bands.loopBaseR) continue;
                        int eR = ((Number) mc.get("eR")).intValue();
                        // 确保合并区域完全在当前数据块内，不越界
                        if (eR > bands.loopMaxR) continue;
                        int sC = ((Number) mc.get("sC")).intValue();
                        int eC = ((Number) mc.get("eC")).intValue();
                        int absSR = blockStart + sR - bands.loopBaseR;
                        int absER = blockStart + eR - bands.loopBaseR;
                        ensureMergeCellsExist(sheet, absSR, absER, sC, eC);
                        try {
                            sheet.addMergedRegion(new CellRangeAddress(absSR - 1, absER - 1, sC - 1, eC - 1));
                        } catch (Exception ignored) {}
                    }
                }
            }
            totalLoopRows = enrichedRows.size() * blockRows;
        }

        // === Phase D: Render footer rows ===
        if (!trailingList.isEmpty()) {
            int headerMaxR = headerRowSet.isEmpty() ? 0 : Collections.max(headerRowSet);
            int footerStart = headerMaxR + totalLoopRows + 1;
            int trMinR = trailingList.get(0);

            for (int tr : trailingList) {
                int absR = footerStart + tr - trMinR;
                Row row = sheet.createRow(absR - 1);
                int h = defaultRowHeight;
                Object hVal = rowHeightsRaw.get(String.valueOf(tr));
                if (hVal instanceof Number) h = ((Number) hVal).intValue();
                row.setHeightInPoints(h);

                for (int c = 1; c <= maxC; c++) {
                    CellDef cd = cellsByKey.get(tr + "_" + c);
                    if (cd == null) continue;
                    Cell cell = row.createCell(c - 1);
                    if (containsImagePlaceholder(cd.v)) {
                        cell.setCellValue("");
                    } else {
                        String val = resolvePlaceholder(cd.v, footerData, finalCustomerData, finalConfig);
                        cell.setCellValue(val);
                    }
                    applyCellStyle(cell, cd.fmt, parseColor, styleCache, defaultFontSize, defaultFontFamily);
                }
            }

            // Footer merged cells
            if (mergedCellsList != null) {
                int trMaxR = trailingList.get(trailingList.size() - 1);
                for (Map<String, Object> mc : mergedCellsList) {
                    int sR = ((Number) mc.get("sR")).intValue();
                    if (sR < trMinR) continue;
                    int eR = ((Number) mc.get("eR")).intValue();
                    // 确保合并区域完全在页脚区域内
                    if (eR > trMaxR) continue;
                    int sC = ((Number) mc.get("sC")).intValue();
                    int eC = ((Number) mc.get("eC")).intValue();
                    int absSR = footerStart + sR - trMinR;
                    int absER = footerStart + eR - trMinR;
                    ensureMergeCellsExist(sheet, absSR, absER, sC, eC);
                    try {
                        sheet.addMergedRegion(new CellRangeAddress(absSR - 1, absER - 1, sC - 1, eC - 1));
                    } catch (Exception ignored) {}
                }
            }
        }

        // === Phase E: Pre-process images in parallel (read + resize), then embed sequentially ===
        if (!imageTasks.isEmpty()) {
            long t0 = System.currentTimeMillis();
            int cpuCores = Runtime.getRuntime().availableProcessors();
            // Collect unique sample IDs that need image processing
            Set<Long> needProcessIds = new LinkedHashSet<>();
            for (Map<String, Integer> task : imageTasks) {
                int di = task.get("di");
                if (di < enrichedRows.size()) {
                    Object sIdObj = enrichedRows.get(di).get("id");
                    long sId = sIdObj instanceof Number ? ((Number) sIdObj).longValue() : -1;
                    if (sId >= 0) needProcessIds.add(sId);
                }
            }

            // Parallel: read + resize all unique images (dedicated pool, up to 24 threads)
            Map<Long, byte[]> resizedImageCache = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Long sId : needProcessIds) {
                String filePath = sampleImagePathCache.get(sId);
                if (filePath == null) continue;
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        Path fullPath = Paths.get(imageStoragePath, filePath);
                        if (Files.exists(fullPath)) {
                            byte[] resized = readAndResizeImage(fullPath, 1200);
                            if (resized != null && resized.length > 0) {
                                resizedImageCache.put(sId, resized);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("price-export: failed to pre-process image for sampleId={}: {}", sId, e.toString());
                    }
                }, IMAGE_PROCESSOR));
            }

            if (!futures.isEmpty()) {
                // Wait all parallel tasks with timeout (2 min max)
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .get(120, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("price-export: image pre-processing interrupted: {}", e.getMessage());
                }
            }
            long t1 = System.currentTimeMillis();
            int poolSize = IMAGE_PROCESSOR instanceof java.util.concurrent.ThreadPoolExecutor
                    ? ((java.util.concurrent.ThreadPoolExecutor) IMAGE_PROCESSOR).getMaximumPoolSize()
                    : cpuCores;
            log.info("price-export: parallel image pre-processing took {}ms for {} images (pool {} threads)",
                    t1 - t0, needProcessIds.size(), poolSize);

            // Sequential: embed with POI (not thread-safe)
            for (Map<String, Integer> task : imageTasks) {
                int absR = task.get("r");
                int c = task.get("c");
                int di = task.get("di");
                if (di >= enrichedRows.size()) continue;
                Map<String, Object> rowData = enrichedRows.get(di);
                Object sIdObj = rowData.get("id");
                long sId = sIdObj instanceof Number ? ((Number) sIdObj).longValue() : -1;
                if (sId < 0) continue;

                try {
                    byte[] imgBytes = resizedImageCache.get(sId);
                    if (imgBytes == null) continue;

                    int picIdx = wb.addPicture(imgBytes, Workbook.PICTURE_TYPE_JPEG);

                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    XSSFClientAnchor anchor = (XSSFClientAnchor) wb.getCreationHelper().createClientAnchor();
                    anchor.setCol1(c - 1);
                    anchor.setRow1(absR - 1);
                    anchor.setCol2(c);
                    anchor.setRow2(absR);
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    drawing.createPicture(anchor, picIdx);

                } catch (Exception e) {
                    log.warn("price-export: failed to embed image R{}C{}: {}", absR, c, e.getMessage());
                }
            }

            long t2 = System.currentTimeMillis();
            log.info("price-export: image embedding total {}ms (pre-process {}ms + embed {}ms)",
                    t2 - t0, t1 - t0, t2 - t1);
        }

        // === Phase F: Stream workbook directly (bytes flow early, avoids Undertow idle timeout) ===
        log.info("price-export: writing workbook with {} data rows, {} images", enrichedRows.size(), imageTasks.size());
        long tw0 = System.currentTimeMillis();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = "报价明细.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20") + "\"");
        // Flush headers early so nginx sees the response has started
        OutputStream os = new BufferedOutputStream(response.getOutputStream(), 65536);
        wb.write(os);
        os.flush();
        long tw1 = System.currentTimeMillis();
        log.info("price-export: wb.write+stream took {}ms ({} rows, {} images)",
                tw1 - tw0, enrichedRows.size(), imageTasks.size());
        wb.dispose();
        wb.close();
        } catch (Throwable t) {
            // Client disconnect: log as warning, don't try to send error response
            String msg = t.getMessage() != null ? t.getMessage() : "";
            String clsName = t.getClass().getSimpleName();
            if (clsName.contains("AsyncRequestNotUsable") || msg.contains("broken pipe")
                    || msg.contains("Connection reset") || msg.contains("aborted")) {
                log.warn("price-export: client disconnected during processing, codeName={}, ids={}, templateId={}",
                        codeName, idsRaw, templateId);
            } else {
                log.error("price-export error: codeName={}, ids={}, templateId={}", codeName, idsRaw, templateId, t);
                String errMsg = t.getClass().getSimpleName() + ": " + msg;
                log.error("price-export 详细错误: {}", errMsg);
                try {
                    if (!response.isCommitted()) {
                        response.reset();
                        response.sendError(500, "生成报价失败: " + errMsg);
                    }
                } catch (Exception ignored) {
                    log.warn("price-export: failed to send error response (response already closed)");
                }
            }
        }
    }

    // ========== 异步导出报价（避免 nginx 超时） ==========

    /**
     * 启动异步报价导出，立即返回 taskId。
     */
    @PostMapping("/{codeName}/price-export-async")
    public Result<Map<String, Object>> priceExportAsync(@PathVariable String codeName,
                                                         @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        Integer templateId;
        try {
            templateId = body.get("templateId") != null
                    ? ((Number) body.get("templateId")).intValue() : null;
        } catch (Exception e) {
            return Result.error(400, "templateId must be a number");
        }
        if (idsRaw == null || idsRaw.isEmpty() || templateId == null) {
            return Result.error(400, "ids and templateId required");
        }
        List<Long> ids = new ArrayList<>();
        for (Integer id : idsRaw) ids.add(id.longValue());

        // Validate template exists
        ReportTemplate template = reportTemplateService.getById(templateId.longValue());
        if (template == null) {
            return Result.error(404, "template not found");
        }

        String taskId = exportTaskService.submit(codeName, new ExportTaskService.ExportRunner() {
            @Override
            public Path run(java.util.function.Consumer<Integer> onProgress,
                           java.util.function.Consumer<String> onMessage) throws Exception {
                return buildPriceExcelToTempFile(codeName, ids, templateId, onProgress, onMessage);
            }

            @Override
            public String getFileName() { return "报价明细.xlsx"; }

            @Override
            public String getDescription() { return ids.size() + " samples"; }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        return Result.success("导出任务已启动", result);
    }

    /**
     * 查询异步导出任务状态。
     */
    @GetMapping("/price-export-async/{taskId}/status")
    public Result<ExportTaskService.ExportTask> priceExportAsyncStatus(@PathVariable String taskId) {
        ExportTaskService.ExportTask task = exportTaskService.getStatus(taskId);
        if (task == null) {
            return Result.error(404, "任务不存在或已过期");
        }
        return Result.success(task);
    }

    /**
     * 下载异步生成的报价 Excel 文件。
     */
    @GetMapping("/price-export-async/{taskId}/download")
    public void priceExportAsyncDownload(@PathVariable String taskId,
                                          HttpServletResponse response) throws IOException {
        Path filePath = exportTaskService.getDownloadFile(taskId);
        if (filePath == null || !Files.exists(filePath)) {
            response.sendError(404, "文件不存在或已过期，请重新导出");
            return;
        }
        String fileName = exportTaskService.getFileName(taskId);
        if (fileName == null) fileName = "报价明细.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20") + "\"");
        try (OutputStream os = new BufferedOutputStream(response.getOutputStream(), 65536)) {
            Files.copy(filePath, os);
            os.flush();
        }
    }

    // ========== 异步导出图片 ZIP（避免浏览器内存爆炸）==========

    /**
     * 启动异步图片 ZIP 导出，立即返回 taskId。
     */
    @PostMapping("/{codeName}/image-export-async")
    public Result<Map<String, Object>> imageExportAsync(@PathVariable String codeName,
                                                         @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        String namingMode = body.get("namingMode") != null ? body.get("namingMode").toString() : "sampleCode";
        String folderName = body.get("folderName") != null ? body.get("folderName").toString() : "";

        if (idsRaw == null || idsRaw.isEmpty()) {
            return Result.error(400, "ids required");
        }
        List<Long> ids = new ArrayList<>();
        for (Integer id : idsRaw) ids.add(id.longValue());

        String taskId = exportTaskService.submit(codeName, new ExportTaskService.ExportRunner() {
            @Override
            public Path run(java.util.function.Consumer<Integer> onProgress,
                           java.util.function.Consumer<String> onMessage) throws Exception {
                return buildImageZipToTempFile(codeName, ids, namingMode, folderName, onProgress, onMessage);
            }

            @Override
            public String getFileName() {
                String name = folderName != null && !folderName.isEmpty() ? folderName : "择样图片_" + codeName;
                return name + ".zip";
            }

            @Override
            public String getDescription() { return ids.size() + " samples, " + ids.size() + " images"; }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        return Result.success("导出任务已启动", result);
    }

    /**
     * 查询图片导出任务状态（复用 ExportTaskService）
     */
    @GetMapping("/image-export-async/{taskId}/status")
    public Result<ExportTaskService.ExportTask> imageExportAsyncStatus(@PathVariable String taskId) {
        ExportTaskService.ExportTask task = exportTaskService.getStatus(taskId);
        if (task == null) {
            return Result.error(404, "任务不存在或已过期");
        }
        return Result.success(task);
    }

    /**
     * 下载异步生成的图片 ZIP 文件。
     */
    @GetMapping("/image-export-async/{taskId}/download")
    public void imageExportAsyncDownload(@PathVariable String taskId,
                                          HttpServletResponse response) throws IOException {
        Path filePath = exportTaskService.getDownloadFile(taskId);
        if (filePath == null || !Files.exists(filePath)) {
            response.sendError(404, "文件不存在或已过期，请重新导出");
            return;
        }
        String fileName = exportTaskService.getFileName(taskId);
        if (fileName == null) fileName = "择样图片.zip";
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20") + "\"");
        try (OutputStream os = new BufferedOutputStream(response.getOutputStream(), 65536)) {
            Files.copy(filePath, os);
            os.flush();
        }
    }

    /**
     * 构建图片 ZIP 到临时文件（后端本地磁盘读图，避免浏览器内存爆炸）。
     */
    private Path buildImageZipToTempFile(String codeName, List<Long> ids, String namingMode,
                                          String folderName,
                                          java.util.function.Consumer<Integer> onProgress,
                                          java.util.function.Consumer<String> onMessage) throws Exception {
        onProgress.accept(5);
        onMessage.accept("正在查询图片信息...");

        // 1. 获取图片信息（filePath, fileName, hash）
        Map<Long, List<Map<String, Object>>> imageInfoMap = imageService.getAllImagesBySampleIds(ids);

        // 2. 获取样品编码（sampleCode / factoryCode）
        Map<Long, Sample> sampleMap = new HashMap<>();
        List<Sample> samples = sampleService.listByIdsWithThumbnails(ids);
        if (samples != null) {
            for (Sample s : samples) sampleMap.put(s.getId(), s);
        }

        // 3. 统计总文件数
        int totalFiles = 0;
        for (Long id : ids) {
            List<Map<String, Object>> imgs = imageInfoMap.getOrDefault(id, Collections.emptyList());
            totalFiles += imgs.size();
        }
        if (totalFiles == 0) throw new IllegalArgumentException("所选样品没有图片");

        onProgress.accept(10);
        onMessage.accept("正在打包 " + totalFiles + " 张图片...");

        // 4. 创建 ZIP
        Path tempFile = Files.createTempFile("image-export-", ".zip");
        int processed = 0;
        java.util.Set<String> usedEntries = new java.util.HashSet<>();

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(tempFile), 65536))) {
            zos.setLevel(java.util.zip.Deflater.BEST_COMPRESSION);

            for (Long id : ids) {
                Sample sample = sampleMap.get(id);
                String namingKey;
                if ("factoryCode".equals(namingMode)) {
                    String fc = sample != null ? sample.getFactoryCode() : null;
                    namingKey = sanitizeFileName(fc != null && !fc.isEmpty() ? fc
                            : (sample != null ? sample.getSampleCode() : String.valueOf(id)));
                } else {
                    namingKey = sanitizeFileName(sample != null && sample.getSampleCode() != null
                            ? sample.getSampleCode() : String.valueOf(id));
                }

                List<Map<String, Object>> imgs = imageInfoMap.getOrDefault(id, Collections.emptyList());
                for (int i = 0; i < imgs.size(); i++) {
                    Map<String, Object> img = imgs.get(i);
                    String filePath = (String) img.get("filePath");
                    String fileName = (String) img.get("fileName");
                    if (filePath == null) continue;

                    Path fullPath = Paths.get(imageStoragePath, filePath);
                    if (!Files.exists(fullPath)) continue;

                    // 文件名：单图用 namingKey，多图加 _1, _2
                    String ext = "";
                    if (fileName != null && fileName.contains(".")) {
                        ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
                    }
                    String entryName;
                    if (imgs.size() == 1) {
                        entryName = namingKey + ext;
                    } else {
                        entryName = namingKey + "_" + (i + 1) + ext;
                    }
                    entryName = makeUniqueEntry(usedEntries, entryName);

                    java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryName);
                    try {
                        zos.putNextEntry(entry);
                        Files.copy(fullPath, zos);
                        zos.closeEntry();
                    } catch (Exception e) {
                        // 单张图片损坏/读取失败不中断整个 ZIP
                        log.warn("image-export: skip broken file {}: {}", fileName, e.toString());
                        continue;
                    }

                    processed++;
                    if (totalFiles > 1 && processed % Math.max(1, totalFiles / 20) == 0) {
                        onProgress.accept(10 + (int)((double) processed / totalFiles * 85));
                        onMessage.accept("正在打包...(" + processed + "/" + totalFiles + ")");
                    }
                }
            }
        }

        onProgress.accept(95);
        onMessage.accept("正在完成打包...");

        onProgress.accept(100);
        onMessage.accept("导出完成");

        log.info("build-zip: export done, {} files, {} samples → {}", processed, ids.size(), tempFile);
        return tempFile;
    }

    /** 清理文件名中的非法字符 */
    private String sanitizeFileName(String s) {
        if (s == null || s.isEmpty()) return "unnamed";
        return s.replaceAll("[\\\\/:*?\"<>|]", "-").trim();
    }

    /** 防重名：已存在则加 _1, _2... */
    private String makeUniqueEntry(java.util.Set<String> used, String name) {
        if (used.add(name)) return name;
        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0) { base = name.substring(0, dot); ext = name.substring(dot); }
        int i = 1;
        while (!used.add(base + "_" + i + ext)) { i++; }
        return base + "_" + i + ext;
    }

    /**
     * 构建报价 Excel 到临时文件（异步版本）。
     * 与 priceExport 逻辑相同，但写入临时文件并上报真实进度。
     */
    private Path buildPriceExcelToTempFile(String codeName, List<Long> ids, Integer templateId,
                                            java.util.function.Consumer<Integer> onProgress,
                                            java.util.function.Consumer<String> onMessage) throws Exception {
        onProgress.accept(5);
        onMessage.accept("正在加载数据...");

        // 1. Load template
        ReportTemplate template = reportTemplateService.getById(templateId.longValue());
        if (template == null) throw new IllegalArgumentException("template not found");

        // 2. Load client_sample_items
        List<ClientSampleItem> allItems = clientSampleItemMapper.selectItemsByCodeNameAndSampleIds(codeName, ids);
        Map<Long, ClientSampleItem> itemBySampleMap = new LinkedHashMap<>();
        if (allItems != null) {
            for (ClientSampleItem item : allItems) {
                if (item.getSampleId() != null) itemBySampleMap.put(item.getSampleId(), item);
            }
        }
        Map<Long, Sample> liveSampleMap = new HashMap<>();
        List<Sample> liveSamples = sampleService.listByIdsWithThumbnails(ids);
        if (liveSamples != null) {
            for (Sample s : liveSamples) liveSampleMap.put(s.getId(), s);
        }
        ClientSamplePriceSetting price1Setting = clientSampleService.getPriceSetting(codeName, "1");

        // 3. Load customer data
        Map<String, Object> customerData = null;
        ClientSample clientSample = clientSampleMapper.selectOne(
                new LambdaQueryWrapper<ClientSample>().eq(ClientSample::getCodeName, codeName));
        if (clientSample != null && clientSample.getClientCode() != null) {
            Customer customer = customerMapper.selectOne(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerCode, clientSample.getClientCode()));
            if (customer != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = objectMapper.convertValue(customer, Map.class);
                customerData = m;
            }
        }

        onProgress.accept(10);
        onMessage.accept("正在解析模板...");

        // 4. Parse template JSON
        String templateJson = template.getTemplateData();
        if (templateJson == null || templateJson.isEmpty())
            throw new IllegalArgumentException("template data is empty");
        @SuppressWarnings("unchecked")
        Map<String, Object> td;
        try {
            td = objectMapper.readValue(templateJson, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("template JSON parse error: " + e.getMessage());
        }
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cellDataRaw =
                (Map<String, Map<String, Object>>) td.get("cellData");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mergedCellsList =
                (List<Map<String, Object>>) td.get("mergedCells");
        @SuppressWarnings("unchecked")
        Map<String, Object> colWidthsRaw = (Map<String, Object>) td.get("colWidths");
        @SuppressWarnings("unchecked")
        Map<String, Object> rowHeightsRaw = (Map<String, Object>) td.get("rowHeights");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) td.get("config");
        if (colWidthsRaw == null) colWidthsRaw = new LinkedHashMap<>();
        if (rowHeightsRaw == null) rowHeightsRaw = new LinkedHashMap<>();

        // 5. Parse cells & compute bands
        List<CellDef> allCells = new ArrayList<>();
        if (cellDataRaw != null) {
            for (Map.Entry<String, Map<String, Object>> e : cellDataRaw.entrySet()) {
                Matcher m = Pattern.compile("R(\\d+)C(\\d+)").matcher(e.getKey());
                if (!m.find()) continue;
                CellDef cd = new CellDef();
                cd.r = Integer.parseInt(m.group(1));
                cd.c = Integer.parseInt(m.group(2));
                Map<String, Object> vv = e.getValue();
                cd.v = vv.get("v") != null ? String.valueOf(vv.get("v")) : "";
                @SuppressWarnings("unchecked")
                Map<String, Object> fmt = (Map<String, Object>) vv.get("fmt");
                cd.fmt = fmt;
                allCells.add(cd);
            }
        }
        if (allCells.isEmpty()) throw new IllegalArgumentException("empty template");
        BandResult bands = computeBands(allCells);
        int maxC = allCells.stream().mapToInt(c -> c.c).max().orElse(1);
        Map<String, CellDef> cellsByKey = new HashMap<>();
        for (CellDef cd : allCells) cellsByKey.put(cd.r + "_" + cd.c, cd);

        Set<Integer> headerRowSet = new TreeSet<>();
        for (int r = 1; r < bands.loopBaseR; r++) headerRowSet.add(r);
        Set<Integer> trailingRowSet = new TreeSet<>();
        for (CellDef cd : allCells) {
            if (cd.r > bands.loopMaxR) trailingRowSet.add(cd.r);
        }
        List<Integer> trailingList = new ArrayList<>(trailingRowSet);
        Collections.sort(trailingList);

        onProgress.accept(15);
        onMessage.accept("正在构建数据行...");

        // 6. Build enriched row data
        List<Map<String, Object>> enrichedRows = new ArrayList<>();
        Map<Long, String> sampleImagePathCache = new HashMap<>();
        Map<Long, String> sampleImageHashCache = new HashMap<>();
        for (Long id : ids) {
            ClientSampleItem item = itemBySampleMap.get(id);
            Sample snapSample = null;
            if (item != null && item.getSnapshotData() != null && !item.getSnapshotData().isEmpty()) {
                try { snapSample = objectMapper.readValue(item.getSnapshotData(), Sample.class); }
                catch (Exception ex) { log.warn("build-price: failed parse snapshot sampleId={}", id); }
            }
            if (snapSample == null) snapSample = liveSampleMap.get(id);
            if (snapSample == null) continue;
            snapSample.setId(id);
            Sample live = liveSampleMap.get(id);
            if (live != null) {
                if (snapSample.getFactoryPrice() == null) snapSample.setFactoryPrice(live.getFactoryPrice());
                if (snapSample.getTaxPrice() == null) snapSample.setTaxPrice(live.getTaxPrice());
                if (snapSample.getName() == null || snapSample.getName().isEmpty()) snapSample.setName(live.getName());
                if (snapSample.getPhone1() == null || snapSample.getPhone1().isEmpty()) snapSample.setPhone1(live.getPhone1());
                if (snapSample.getMobile1() == null || snapSample.getMobile1().isEmpty()) snapSample.setMobile1(live.getMobile1());
            }
            if (item != null) {
                if (item.getCalculatedPrice() != null) {
                    snapSample.setCalculatedPrice(item.getCalculatedPrice());
                } else if (price1Setting != null) {
                    snapSample.setCalculatedPrice(java.math.BigDecimal.ZERO);
                } else {
                    snapSample.setCalculatedPrice(snapSample.getFactoryPrice());
                }
                if (price1Setting != null) snapSample.setTaxPrice(snapSample.getCalculatedPrice());
                snapSample.setTaxPrice2(item.getCalculatedPrice2());
                snapSample.setBorrowedSample(item.getBorrowedSample());
                snapSample.setShowroomReplenished(item.getShowroomReplenished());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> row = objectMapper.convertValue(snapSample, Map.class);
            enrichRow(row);
            String hash = snapSample.getFirstImageHash();
            if ((hash == null || hash.isEmpty()) && live != null) {
                hash = live.getFirstImageHash();
                if (hash != null && !hash.isEmpty()) {
                    row.put("firstImageHash", hash);
                    row.put("firstImageId", live.getFirstImageId());
                    row.put("thumbnail", live.getThumbnail());
                }
            }
            if (hash != null && !hash.isEmpty()) sampleImageHashCache.put(id, hash);
            enrichedRows.add(row);
        }

        // Batch-load image file paths
        Set<String> imageHashes = new HashSet<>(sampleImageHashCache.values());
        imageHashes.remove(null);
        if (!imageHashes.isEmpty()) {
            List<Image> imageEntities = imageMapper.selectList(
                    new LambdaQueryWrapper<Image>().in(Image::getHash, imageHashes));
            if (imageEntities != null) {
                Map<String, String> hashToPath = new HashMap<>();
                for (Image img : imageEntities) hashToPath.put(img.getHash(), img.getFilePath());
                for (Map.Entry<Long, String> e : sampleImageHashCache.entrySet()) {
                    String fp = hashToPath.get(e.getValue());
                    if (fp != null) sampleImagePathCache.put(e.getKey(), fp);
                }
            }
        }

        // Footer context
        Map<String, Object> footerData = new LinkedHashMap<>();
        if (!enrichedRows.isEmpty()) {
            footerData.putAll(new LinkedHashMap<>(enrichedRows.get(0)));
        }
        footerData.put("currentPage", 1);
        footerData.put("page", 1);
        footerData.put("total_pages", 1);
        footerData.put("printTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        if (config != null) footerData.put("title", config.getOrDefault("title", ""));

        // 7. Create workbook
        int estimatedRows = headerRowSet.size() + enrichedRows.size() * (bands.loopMaxR - bands.loopBaseR + 1) + trailingList.size() + 10;
        int rowWindowSize = Math.min(estimatedRows, 500);
        SXSSFWorkbook wb = new SXSSFWorkbook(rowWindowSize);
        wb.setCompressTempFiles(true);
        Map<String, XSSFCellStyle> styleCache = new HashMap<>();

        int defaultFontSize = 12;
        if (config != null && config.get("fontSize") instanceof Number)
            defaultFontSize = ((Number) config.get("fontSize")).intValue();
        String defaultFontFamily = config != null ? (String) config.get("fontFamily") : null;
        if (defaultFontFamily == null) defaultFontFamily = "SimSun, serif";

        Sheet sheet = wb.createSheet(config != null && config.get("title") != null
                ? String.valueOf(config.get("title")) : "报价表");
        sheet.setDisplayGridlines(false);

        for (int c = 1; c <= maxC; c++) {
            int w = 120;
            Object wVal = colWidthsRaw.get(String.valueOf(c));
            if (wVal instanceof Number) w = ((Number) wVal).intValue();
            sheet.setColumnWidth(c - 1, Math.round(w / 7f) * 256);
        }

        java.util.function.Function<String, XSSFColor> parseColor = hex -> {
            if (hex == null || hex.isEmpty()) return new XSSFColor(new byte[]{0, 0, 0}, null);
            try {
                String h = hex.replace("#", "");
                return new XSSFColor(new byte[]{
                        (byte) Integer.parseInt(h.substring(0, 2), 16),
                        (byte) Integer.parseInt(h.substring(2, 4), 16),
                        (byte) Integer.parseInt(h.substring(4, 6), 16)}, null);
            } catch (Exception ignored) {
                return new XSSFColor(new byte[]{0, 0, 0}, null);
            }
        };

        Map<String, Object> finalCustomerData = customerData;
        Map<String, Object> finalConfig = config;

        // === Phase A: Header rows ===
        onProgress.accept(20);
        onMessage.accept("正在渲染表头...");

        Map<String, Object> hdrData = enrichedRows.isEmpty() ? new LinkedHashMap<>() : enrichedRows.get(0);
        for (int r : headerRowSet) {
            Row row = sheet.createRow(r - 1);
            int h = 48;
            Object hVal = rowHeightsRaw.get(String.valueOf(r));
            if (hVal instanceof Number) h = ((Number) hVal).intValue();
            row.setHeightInPoints(h);
            for (int c = 1; c <= maxC; c++) {
                CellDef cd = cellsByKey.get(r + "_" + c);
                if (cd == null) continue;
                Cell cell = row.createCell(c - 1);
                cell.setCellValue(resolvePlaceholder(cd.v, hdrData, finalCustomerData, finalConfig));
                applyCellStyle(cell, cd.fmt, parseColor, styleCache, defaultFontSize, defaultFontFamily);
            }
        }
        if (mergedCellsList != null) {
            for (Map<String, Object> mc : mergedCellsList) {
                int sR = ((Number) mc.get("sR")).intValue();
                if (sR >= bands.loopBaseR) continue;
                int eR = ((Number) mc.get("eR")).intValue();
                if (eR >= bands.loopBaseR) continue;
                int sC = ((Number) mc.get("sC")).intValue();
                int eC = ((Number) mc.get("eC")).intValue();
                ensureMergeCellsExist(sheet, sR, eR, sC, eC);
                try { sheet.addMergedRegion(new CellRangeAddress(sR - 1, eR - 1, sC - 1, eC - 1)); }
                catch (Exception ignored) {}
            }
        }

        // === Phase B: Logo ===
        CellDef logoCell = allCells.stream()
                .filter(cd -> cd.v != null && cd.v.contains("${logo}")).findFirst().orElse(null);
        if (logoCell != null && config != null && config.get("logoImage") != null) {
            try {
                String b64 = config.get("logoImage").toString()
                        .replaceAll("^data:image/\\w+;base64,", "");
                byte[] imgBytes = Base64.getDecoder().decode(b64);
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imgBytes));
                if (bi != null) {
                    ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                    ImageIO.write(bi, "png", bos2);
                    int picIdx = wb.addPicture(bos2.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    XSSFClientAnchor anchor = (XSSFClientAnchor) wb.getCreationHelper().createClientAnchor();
                    int logoSpanC = 1, logoSpanR = 1;
                    if (mergedCellsList != null) {
                        for (Map<String, Object> mc : mergedCellsList) {
                            int msR = ((Number) mc.get("sR")).intValue();
                            int meR = ((Number) mc.get("eR")).intValue();
                            int msC = ((Number) mc.get("sC")).intValue();
                            int meC = ((Number) mc.get("eC")).intValue();
                            if (logoCell.r >= msR && logoCell.r <= meR && logoCell.c >= msC && logoCell.c <= meC) {
                                logoSpanC = meC - msC + 1;
                                logoSpanR = meR - msR + 1;
                                break;
                            }
                        }
                    }
                    anchor.setCol1(logoCell.c - 1);
                    anchor.setRow1(logoCell.r - 1);
                    anchor.setCol2(logoCell.c - 1 + logoSpanC);
                    anchor.setRow2(logoCell.r - 1 + logoSpanR);
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    drawing.createPicture(anchor, picIdx);
                }
            } catch (Exception e) {
                log.warn("build-price: failed embed logo: {}", e.getMessage());
            }
        }

        // === Phase C: Data rows ===
        onProgress.accept(25);
        onMessage.accept("正在生成数据行...");

        List<Map<String, Integer>> imageTasks = new ArrayList<>();
        int totalLoopRows = 0;
        if (!enrichedRows.isEmpty() && !bands.loopRows.isEmpty()) {
            int blockRows = bands.loopMaxR - bands.loopBaseR + 1;
            int headerMaxR = headerRowSet.isEmpty() ? 0 : Collections.max(headerRowSet);
            int totalBlocks = enrichedRows.size();
            for (int di = 0; di < totalBlocks; di++) {
                Map<String, Object> rowData = enrichedRows.get(di);
                int blockStart = headerMaxR + di * blockRows + 1;
                for (int r0 = 0; r0 < blockRows; r0++) {
                    int absR = blockStart + r0;
                    int tmplR = bands.loopBaseR + r0;
                    Row row = sheet.createRow(absR - 1);
                    int h = 48;
                    Object hVal = rowHeightsRaw.get(String.valueOf(tmplR));
                    if (hVal instanceof Number) h = ((Number) hVal).intValue();
                    row.setHeightInPoints(h);
                    for (int c = 1; c <= maxC; c++) {
                        CellDef cd = cellsByKey.get(tmplR + "_" + c);
                        if (cd == null) continue;
                        Cell cell = row.createCell(c - 1);
                        if (containsImagePlaceholder(cd.v)) {
                            Map<String, Integer> task = new HashMap<>();
                            task.put("r", absR); task.put("c", c); task.put("di", di);
                            imageTasks.add(task);
                            cell.setCellValue("");
                        } else {
                            cell.setCellValue(resolvePlaceholder(cd.v, rowData, finalCustomerData, finalConfig));
                        }
                        applyCellStyle(cell, cd.fmt, parseColor, styleCache, defaultFontSize, defaultFontFamily);
                    }
                }
                // Block merged cells
                if (mergedCellsList != null) {
                    for (Map<String, Object> mc : mergedCellsList) {
                        int sR = ((Number) mc.get("sR")).intValue();
                        if (sR < bands.loopBaseR) continue;
                        int eR = ((Number) mc.get("eR")).intValue();
                        if (eR > bands.loopMaxR) continue;
                        int sC = ((Number) mc.get("sC")).intValue();
                        int eC = ((Number) mc.get("eC")).intValue();
                        int absSR = blockStart + sR - bands.loopBaseR;
                        int absER = blockStart + eR - bands.loopBaseR;
                        ensureMergeCellsExist(sheet, absSR, absER, sC, eC);
                        try { sheet.addMergedRegion(new CellRangeAddress(absSR - 1, absER - 1, sC - 1, eC - 1)); }
                        catch (Exception ignored) {}
                    }
                }
                // 数据行进度：25% → 50%（占总进度 25%）
                if (totalBlocks > 1 && di % Math.max(1, totalBlocks / 10) == 0) {
                    onProgress.accept(25 + (int)((double) di / totalBlocks * 25));
                }
            }
            totalLoopRows = enrichedRows.size() * blockRows;
        }

        // === Phase D: Footer rows ===
        onProgress.accept(50);
        onMessage.accept("正在渲染页脚...");

        if (!trailingList.isEmpty()) {
            int headerMaxR = headerRowSet.isEmpty() ? 0 : Collections.max(headerRowSet);
            int footerStart = headerMaxR + totalLoopRows + 1;
            int trMinR = trailingList.get(0);
            for (int tr : trailingList) {
                int absR = footerStart + tr - trMinR;
                Row row = sheet.createRow(absR - 1);
                int h = 48;
                Object hVal = rowHeightsRaw.get(String.valueOf(tr));
                if (hVal instanceof Number) h = ((Number) hVal).intValue();
                row.setHeightInPoints(h);
                for (int c = 1; c <= maxC; c++) {
                    CellDef cd = cellsByKey.get(tr + "_" + c);
                    if (cd == null) continue;
                    Cell cell = row.createCell(c - 1);
                    if (containsImagePlaceholder(cd.v)) {
                        cell.setCellValue("");
                    } else {
                        cell.setCellValue(resolvePlaceholder(cd.v, footerData, finalCustomerData, finalConfig));
                    }
                    applyCellStyle(cell, cd.fmt, parseColor, styleCache, defaultFontSize, defaultFontFamily);
                }
            }
            if (mergedCellsList != null) {
                int trMaxR = trailingList.get(trailingList.size() - 1);
                for (Map<String, Object> mc : mergedCellsList) {
                    int sR = ((Number) mc.get("sR")).intValue();
                    if (sR < trMinR) continue;
                    int eR = ((Number) mc.get("eR")).intValue();
                    if (eR > trMaxR) continue;
                    int sC = ((Number) mc.get("sC")).intValue();
                    int eC = ((Number) mc.get("eC")).intValue();
                    int absSR = footerStart + sR - trMinR;
                    int absER = footerStart + eR - trMinR;
                    ensureMergeCellsExist(sheet, absSR, absER, sC, eC);
                    try { sheet.addMergedRegion(new CellRangeAddress(absSR - 1, absER - 1, sC - 1, eC - 1)); }
                    catch (Exception ignored) {}
                }
            }
        }

        // === Phase E: Image pre-processing + embedding (batch to avoid OOM) ===
        onProgress.accept(52);
        int totalImages = imageTasks.size();
        if (!imageTasks.isEmpty()) {
            onMessage.accept("正在处理图片...(" + totalImages + " 张)");

            // 分批处理：每批最多 500 张图，避免全量缓存撑爆内存
            int batchSize = 500;
            int totalBatches = (totalImages + batchSize - 1) / batchSize;
            int embeddedTotal = 0;

            for (int bi = 0; bi < totalBatches; bi++) {
                int batchStart = bi * batchSize;
                int batchEnd = Math.min(batchStart + batchSize, totalImages);
                List<Map<String, Integer>> batch = imageTasks.subList(batchStart, batchEnd);

                // Collect unique sample IDs in this batch
                Set<Long> batchNeedIds = new LinkedHashSet<>();
                for (Map<String, Integer> task : batch) {
                    int di = task.get("di");
                    if (di < enrichedRows.size()) {
                        Object sIdObj = enrichedRows.get(di).get("id");
                        long sId = sIdObj instanceof Number ? ((Number) sIdObj).longValue() : -1;
                        if (sId >= 0) batchNeedIds.add(sId);
                    }
                }

                // Parallel pre-processing for this batch only
                Map<Long, byte[]> batchCache = new ConcurrentHashMap<>();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                int batchUnique = batchNeedIds.size();

                for (Long sId : batchNeedIds) {
                    String filePath = sampleImagePathCache.get(sId);
                    if (filePath == null) continue;
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            Path fullPath = Paths.get(imageStoragePath, filePath);
                            if (Files.exists(fullPath)) {
                                byte[] resized = readAndResizeImage(fullPath, 1200);
                                if (resized != null && resized.length > 0) {
                                    batchCache.put(sId, resized);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("build-price: failed pre-process image sampleId={}: {}", sId, e.toString());
                        }
                    }, IMAGE_PROCESSOR));
                }

                if (!futures.isEmpty()) {
                    try {
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .get(300, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.warn("build-price: batch {} pre-process interrupted: {}", bi, e.getMessage());
                    }
                }

                // Sequential embedding for this batch
                for (Map<String, Integer> task : batch) {
                    int absR = task.get("r"), c = task.get("c"), di = task.get("di");
                    if (di >= enrichedRows.size()) continue;
                    Map<String, Object> rowData = enrichedRows.get(di);
                    Object sIdObj = rowData.get("id");
                    long sId = sIdObj instanceof Number ? ((Number) sIdObj).longValue() : -1;
                    if (sId < 0) continue;
                    try {
                        byte[] imgBytes = batchCache.get(sId);
                        if (imgBytes == null) continue;
                        int picIdx = wb.addPicture(imgBytes, Workbook.PICTURE_TYPE_JPEG);
                        Drawing<?> drawing = sheet.createDrawingPatriarch();
                        XSSFClientAnchor anchor = (XSSFClientAnchor) wb.getCreationHelper().createClientAnchor();
                        anchor.setCol1(c - 1); anchor.setRow1(absR - 1);
                        anchor.setCol2(c); anchor.setRow2(absR);
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                        drawing.createPicture(anchor, picIdx);
                    } catch (Exception e) {
                        log.warn("build-price: failed embed image R{}C{}: {}", absR, c, e.getMessage());
                    }
                    embeddedTotal++;
                }

                // 分批进度：52% → 95%
                int batchProgress = 52 + (int)((double) (bi + 1) / totalBatches * 43);
                onProgress.accept(batchProgress);
                onMessage.accept("正在处理图片...(" + embeddedTotal + "/" + totalImages + ")");
            }
        }

        // === Phase F: Write to temp file ===
        onProgress.accept(90);
        onMessage.accept("正在压缩打包...");

        onProgress.accept(95);
        onMessage.accept("正在写入文件（" + totalImages + " 张图）...");

        Path tempFile = Files.createTempFile("price-export-", ".xlsx");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(tempFile), 65536)) {
            wb.write(os);
            os.flush();
        }
        wb.dispose();
        wb.close();

        onProgress.accept(100);
        onMessage.accept("导出完成");

        log.info("build-price: export done, {} rows, {} images → {}",
                enrichedRows.size(), totalImages, tempFile);
        return tempFile;
    }

    private String resolvePlaceholder(String val, Map<String, Object> rowData,
                                      Map<String, Object> customerData, Map<String, Object> config) {
        if (val == null) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\$\\{(\\w+)\\}").matcher(val);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String field = m.group(1);
            if ("logo".equals(field)) {
                m.appendReplacement(sb, "");
                continue;
            }
            String replacement = resolveFieldValue(field, rowData, customerData, config);
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String resolveFieldValue(String field, Map<String, Object> rowData,
                                      Map<String, Object> customerData, Map<String, Object> config) {
        // Config-level
        if ("title".equals(field)) return config != null
                ? String.valueOf(config.getOrDefault("title", "")) : "";
        // Image fields — skip text, handled separately
        if (isImageField(field)) return "";
        // Computed fields (matching frontend enrichRow)
        if ("cartonSpec".equals(field)) return fmtThreeDim(rowData, "cartonLength", "cartonWidth", "cartonHeight");
        if ("productSpec".equals(field)) return fmtThreeDim(rowData, "sampleLength", "sampleWidth", "sampleHeight");
        if ("packageSpec".equals(field)) return fmtThreeDim(rowData, "packageLength", "packageWidth", "packageHeight");
        if ("innerCartonSpec".equals(field)) return fmtThreeDim(rowData, "innerCartonLength", "innerCartonWidth", "innerCartonHeight");
        if ("fullSpec".equals(field)) {
            String spec = fmtThreeDim(rowData, "cartonLength", "cartonWidth", "cartonHeight");
            String coding = rowData != null ? String.valueOf(rowData.getOrDefault("codingSpec", "")) : "";
            return spec + (coding.isEmpty() ? "" : " " + coding);
        }
        if ("vendorContact".equals(field)) {
            Object name = rowData != null ? rowData.get("name") : null;
            Object phone = rowData != null ? rowData.get("phone1") : null;
            Object mobile = rowData != null ? rowData.get("mobile1") : null;
            StringBuilder sb = new StringBuilder();
            if (name != null && !name.toString().isEmpty()) sb.append(name);
            if (phone != null && !phone.toString().isEmpty()) sb.append(sb.length() > 0 ? "  " : "").append(phone);
            if (mobile != null && !mobile.toString().isEmpty()) sb.append(sb.length() > 0 ? "  " : "").append(mobile);
            return Matcher.quoteReplacement(sb.toString());
        }
        // 别名映射：模板字段名 → Sample 实体字段名（前端 resolveVal 也有同样的回退逻辑）
        if ("remarkCn".equals(field)) {
            return rowData != null && rowData.get("remark") != null
                    ? Matcher.quoteReplacement(toPlainStr(rowData.get("remark"))) : "";
        }
        // Direct field lookup（BigDecimal 用 toPlainString 避免科学计数法）
        if (rowData != null && rowData.get(field) != null)
            return Matcher.quoteReplacement(toPlainStr(rowData.get(field)));
        if (customerData != null && customerData.get(field) != null)
            return Matcher.quoteReplacement(toPlainStr(customerData.get(field)));
        return "";
    }

    /** BigDecimal 转字符串：去尾部零 + toPlainString 避免科学计数法（638.00 → "638"） */
    private String toPlainStr(Object v) {
        if (v instanceof java.math.BigDecimal)
            return ((java.math.BigDecimal) v).stripTrailingZeros().toPlainString();
        return String.valueOf(v);
    }

    private String fmtThreeDim(Map<String, Object> rowData, String k1, String k2, String k3) {
        if (rowData == null) return "";
        Object v1 = rowData.get(k1), v2 = rowData.get(k2), v3 = rowData.get(k3);
        if (v1 == null && v2 == null && v3 == null) return "";
        return (v1 != null ? v1 : "") + "*" + (v2 != null ? v2 : "") + "*" + (v3 != null ? v3 : "");
    }

    private void enrichRow(Map<String, Object> row) {
        if (row == null) return;
        row.put("cartonSpec", fmtThreeDim(row, "cartonLength", "cartonWidth", "cartonHeight"));
        row.put("productSpec", fmtThreeDim(row, "sampleLength", "sampleWidth", "sampleHeight"));
        row.put("packageSpec", fmtThreeDim(row, "packageLength", "packageWidth", "packageHeight"));
        row.put("innerCartonSpec", fmtThreeDim(row, "innerCartonLength", "innerCartonWidth", "innerCartonHeight"));
        String cartonSpec = fmtThreeDim(row, "cartonLength", "cartonWidth", "cartonHeight");
        String coding = row.getOrDefault("codingSpec", "").toString();
        row.put("fullSpec", cartonSpec + (coding.isEmpty() ? "" : " " + coding));
        // vendorContact：厂商名称 + 电话 + 手机
        Object name = null;
        if (row.get("name") != null) name = row.get("name");
        else if (row.get("vendor_name") != null) name = row.get("vendor_name");
        else name = row.get("supplier");
        Object phone = row.get("phone1") != null ? row.get("phone1") : row.get("phone");
        Object mobile = row.get("mobile1") != null ? row.get("mobile1") : row.get("mobile");
        StringBuilder vc = new StringBuilder();
        if (name != null && !name.toString().isEmpty()) vc.append(name);
        if (phone != null && !phone.toString().isEmpty()) vc.append(vc.length() > 0 ? "  " : "").append(phone);
        if (mobile != null && !mobile.toString().isEmpty()) vc.append(vc.length() > 0 ? "  " : "").append(mobile);
        row.put("vendorContact", vc.toString());
        // sampleStatus：择样状态（借样/展厅已补/不允许带走）
        Object borrowed = row.get("borrowedSample");
        Object showroom = row.get("showroomReplenished");
        if (borrowed instanceof Integer && ((Integer) borrowed) == 1) {
            row.put("sampleStatus", "借样");
        } else if (showroom instanceof Integer && ((Integer) showroom) == 1) {
            row.put("sampleStatus", "展厅已补");
        } else {
            row.put("sampleStatus", "不允许带走");
        }
    }

    private boolean containsImagePlaceholder(String val) {
        if (val == null || !val.contains("${")) return false;
        Matcher m = Pattern.compile("\\$\\{(\\w+)\\}").matcher(val);
        while (m.find()) {
            if (isImageField(m.group(1))) return true;
        }
        return false;
    }

    private void applyCellStyle(Cell cell, Map<String, Object> fmt,
                                java.util.function.Function<String, XSSFColor> parseColor,
                                Map<String, XSSFCellStyle> styleCache,
                                int defaultFontSize, String defaultFontFamily) {
        if (fmt == null) return; // 无格式就不写样式
        String cacheKey = new TreeMap<>(fmt).toString();
        XSSFCellStyle cached = styleCache.get(cacheKey);
        if (cached != null) {
            cell.setCellStyle(cached);
            return;
        }
        XSSFCellStyle cs = (XSSFCellStyle) cell.getSheet().getWorkbook().createCellStyle();
        // 字体
        XSSFFont font = (XSSFFont) cell.getSheet().getWorkbook().createFont();
        if (fmt.get("fontSize") != null) {
            font.setFontHeightInPoints(((Number) fmt.get("fontSize")).shortValue());
        } else {
            font.setFontHeightInPoints((short) defaultFontSize);
        }
        if (Boolean.TRUE.equals(fmt.get("bold"))) font.setBold(true);
        if (Boolean.TRUE.equals(fmt.get("italic"))) font.setItalic(true);
        if (Boolean.TRUE.equals(fmt.get("underline"))) font.setUnderline(FontUnderline.SINGLE);
        String fc = fmt.get("color") != null ? fmt.get("color").toString() : null;
        if (fc != null && !fc.isEmpty()) font.setColor(parseColor.apply(fc));
        if (fmt.get("fontFamily") != null && !fmt.get("fontFamily").toString().isEmpty()) {
            font.setFontName(fmt.get("fontFamily").toString().split(",")[0].trim());
        } else if (defaultFontFamily != null && !defaultFontFamily.isEmpty()) {
            font.setFontName(defaultFontFamily.split(",")[0].trim());
        }
        cs.setFont(font);
        // 背景色
        if (fmt.get("bgColor") != null && !fmt.get("bgColor").toString().isEmpty()) {
            cs.setFillForegroundColor(parseColor.apply(fmt.get("bgColor").toString()));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        // 对齐
        if (fmt.get("align") != null) {
            String al = fmt.get("align").toString();
            if ("center".equals(al)) cs.setAlignment(HorizontalAlignment.CENTER);
            else if ("left".equals(al)) cs.setAlignment(HorizontalAlignment.LEFT);
            else if ("right".equals(al)) cs.setAlignment(HorizontalAlignment.RIGHT);
        }
        // 垂直对齐
        if (fmt.get("verticalAlign") != null) {
            String va = fmt.get("verticalAlign").toString();
            if ("top".equals(va)) cs.setVerticalAlignment(VerticalAlignment.TOP);
            else if ("middle".equals(va)) cs.setVerticalAlignment(VerticalAlignment.CENTER);
            else if ("bottom".equals(va)) cs.setVerticalAlignment(VerticalAlignment.BOTTOM);
        }
        // 边框 — 对齐前端：模板设了 border 才画线，没设不画
        String borderMode = fmt.get("border") != null ? fmt.get("border").toString() : null;
        if (borderMode == null && (
                (fmt.get("borderWidth") instanceof Number && ((Number) fmt.get("borderWidth")).intValue() > 0) ||
                (fmt.get("borderColor") != null && !fmt.get("borderColor").toString().isEmpty()) ||
                (fmt.get("borderStyle") != null && !fmt.get("borderStyle").toString().isEmpty()))) {
            borderMode = "all";
        }
        if (borderMode != null && !"none".equals(borderMode)) {
            String bc = fmt.get("borderColor") != null ? fmt.get("borderColor").toString() : "#333333";
            XSSFColor borderColor = parseColor.apply(bc);
            int bw = fmt.get("borderWidth") instanceof Number ? ((Number) fmt.get("borderWidth")).intValue() : 1;
            String bStyle = fmt.get("borderStyle") != null ? fmt.get("borderStyle").toString() : "solid";
            BorderStyle bs = mapBorderStyle(bStyle, bw);
            if ("all".equals(borderMode) || "outer".equals(borderMode)) {
                cs.setBorderTop(bs); cs.setBorderBottom(bs);
                cs.setBorderLeft(bs); cs.setBorderRight(bs);
                cs.setTopBorderColor(borderColor); cs.setBottomBorderColor(borderColor);
                cs.setLeftBorderColor(borderColor); cs.setRightBorderColor(borderColor);
            } else {
                if ("top".equals(borderMode))    { cs.setBorderTop(bs);    cs.setTopBorderColor(borderColor); }
                if ("bottom".equals(borderMode)) { cs.setBorderBottom(bs); cs.setBottomBorderColor(borderColor); }
                if ("left".equals(borderMode))   { cs.setBorderLeft(bs);   cs.setLeftBorderColor(borderColor); }
                if ("right".equals(borderMode))  { cs.setBorderRight(bs);  cs.setRightBorderColor(borderColor); }
            }
        }
        // 换行
        if (Boolean.TRUE.equals(fmt.get("wordWrap"))) cs.setWrapText(true);
        cell.setCellStyle(cs);
        styleCache.put(cacheKey, cs);
    }

    /**
     * 映射前端 borderStyle + borderWidth 到 POI BorderStyle
     * 前端 ExcelJS: width 1=thin, 2=medium, 3/4=thick
     */
    private BorderStyle mapBorderStyle(String style, int width) {
        if ("dashed".equals(style))   return width >= 2 ? BorderStyle.MEDIUM_DASHED : BorderStyle.DASHED;
        if ("dotted".equals(style))   return BorderStyle.DOTTED;
        if ("double".equals(style))   return BorderStyle.DOUBLE;
        // solid (default)
        return width <= 1 ? BorderStyle.THIN : width == 2 ? BorderStyle.MEDIUM : BorderStyle.THICK;
    }

    /**
     * 撤销指定代号下所有入库和出库提交（恢复误添加）
     */
    @DeleteMapping("/{codeName}/revert-submissions")
    public Result<Map<String, Integer>> revertSubmissions(@PathVariable String codeName) {
        Map<String, Integer> result = clientSampleService.revertSubmissions(codeName);
        return Result.success(
            String.format("已撤销 %d 条入库、%d 条出库记录", result.getOrDefault("inventory", 0), result.getOrDefault("outbound", 0)),
            result
        );
    }

    /**
     * 确保合并区域内的所有单元格都已创建，并继承主单元格的样式（避免 addMergedRegion 静默失败 + 子单元格无边框）
     */
    private void ensureMergeCellsExist(Sheet sheet, int sR, int eR, int sC, int eC) {
        // 获取主单元格（左上角）的样式
        CellStyle masterStyle = null;
        Row masterRow = sheet.getRow(sR - 1);
        if (masterRow != null) {
            Cell masterCell = masterRow.getCell(sC - 1);
            if (masterCell != null) {
                masterStyle = masterCell.getCellStyle();
            }
        }
        for (int r = sR; r <= eR; r++) {
            Row row = sheet.getRow(r - 1);
            if (row == null) row = sheet.createRow(r - 1);
            for (int c = sC; c <= eC; c++) {
                if (r == sR && c == sC) continue; // 跳过主单元格
                Cell cell = row.getCell(c - 1);
                if (cell == null) {
                    cell = row.createCell(c - 1);
                    if (masterStyle != null) {
                        cell.setCellStyle(masterStyle);
                    }
                }
            }
        }
    }

    // ===== Inner classes for band computation =====
    private static class CellDef {
        int r, c;
        String v;
        Map<String, Object> fmt;
    }

    private static class BandResult {
        List<CellDef> cells;
        Set<Integer> allPlaceholderRows;
        Set<Integer> loopRows;
        int loopBaseR;
        int loopMaxR;
        Set<String> globalFields;
    }

    private static final Set<String> IMAGE_FIELDS = new HashSet<>(Arrays.asList(
        "thumbnail", "imagePath", "image_path", "image", "img", "photo", "pic", "picture", "imgUrl", "photoUrl"
    ));

    private boolean isImageField(String field) {
        return IMAGE_FIELDS.contains(field);
    }

    private BandResult computeBands(List<CellDef> cells) {
        Set<String> globalFields = new HashSet<>(Arrays.asList(
            "title", "logo", "currentPage", "page", "currentDate", "currentMonth",
            "total_pages", "printTime", "operatorName"
        ));

        // Collect rows with non-global placeholders
        Set<Integer> allPlaceholderRows = new TreeSet<>();
        Pattern ph = Pattern.compile("\\$\\{(\\w+)\\}");
        for (CellDef cd : cells) {
            if (cd.v == null || !cd.v.contains("${")) continue;
            Matcher mm = ph.matcher(cd.v);
            boolean hasField = false;
            while (mm.find()) {
                String field = mm.group(1);
                if (!globalFields.contains(field)) { hasField = true; break; }
            }
            if (hasField) allPlaceholderRows.add(cd.r);
        }

        int loopStart = allPlaceholderRows.isEmpty() ? 0 : Collections.min(allPlaceholderRows);
        List<Integer> sortedCandidates = new ArrayList<>(allPlaceholderRows);
        sortedCandidates.removeIf(r -> r < loopStart);
        Collections.sort(sortedCandidates);

        // Cluster by continuity (gap > 1 = new cluster)
        List<List<Integer>> clusters = new ArrayList<>();
        List<Integer> curCluster = new ArrayList<>();
        for (int r : sortedCandidates) {
            if (!curCluster.isEmpty() && r - curCluster.get(curCluster.size() - 1) > 1) {
                clusters.add(new ArrayList<>(curCluster));
                curCluster.clear();
            }
            curCluster.add(r);
        }
        if (!curCluster.isEmpty()) clusters.add(new ArrayList<>(curCluster));

        // Largest cluster by placeholder cell count = data loop
        List<Integer> largestCluster = clusters.isEmpty() ? new ArrayList<>() : clusters.get(0);
        int maxCount = 0;
        for (List<Integer> cl : clusters) {
            int cnt = 0;
            for (int rr : cl) {
                for (CellDef cd : cells) {
                    if (cd.r == rr && cd.v != null && cd.v.contains("${")) cnt++;
                }
            }
            if (cnt > maxCount) { largestCluster = cl; maxCount = cnt; }
        }

        BandResult result = new BandResult();
        result.cells = cells;
        result.allPlaceholderRows = allPlaceholderRows;
        result.loopRows = new HashSet<>(largestCluster);
        result.loopBaseR = largestCluster.isEmpty() ? 0 : Collections.min(largestCluster);
        result.loopMaxR = largestCluster.isEmpty() ? 0 : Collections.max(largestCluster);
        result.globalFields = globalFields;
        return result;
    }

    /**
     * Read image from disk, resize to maxSize (longest edge), encode as high-quality JPEG.
     * Uses JPEG subsampling to avoid full-resolution decode — skips 80-95% CPU overhead.
     */
    // Cached JPEG ImageWriter (thread-safe, reused across all calls)
    private static final ImageWriter JPEG_WRITER = ImageIO.getImageWritersByFormatName("jpeg").next();

    private byte[] readAndResizeImage(Path filePath, int maxSize) throws IOException {
        // Detect actual format by magic bytes (NOT file extension - some .jpg files are actually PNG)
        byte[] magic = new byte[4];
        try (InputStream fis = Files.newInputStream(filePath)) {
            int n = fis.read(magic);
            if (n < 4) return null;
        }

        // JPEG: 0xFF 0xD8
        if (magic[0] == (byte) 0xFF && magic[1] == (byte) 0xD8) {
            return readAndResizeJpeg(filePath, maxSize);
        }

        // PNG: 0x89 0x50 0x4E 0x47
        if (magic[0] == (byte) 0x89 && magic[1] == (byte) 0x50) {
            return readAndResizeGeneric(filePath, maxSize, "png");
        }

        // GIF: 0x47 0x49 0x46
        if (magic[0] == (byte) 0x47 && magic[1] == (byte) 0x49) {
            return readAndResizeGeneric(filePath, maxSize, "gif");
        }

        // BMP: 0x42 0x4D
        if (magic[0] == (byte) 0x42 && magic[1] == (byte) 0x4D) {
            return readAndResizeGeneric(filePath, maxSize, "bmp");
        }

        // Unknown format: try generic fallback
        return readAndResizeGeneric(filePath, maxSize, null);
    }

    /** JPEG: use subsampling to read at reduced resolution, very fast. Falls back on corrupt metadata. */
    private byte[] readAndResizeJpeg(Path filePath, int maxSize) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(filePath.toFile())) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
            reader.setInput(iis, true, true);

            int srcW = 0, srcH = 0;
            try {
                srcW = reader.getWidth(0);
                srcH = reader.getHeight(0);
            } catch (Exception e) {
                reader.dispose();
                return readAndResizeFallback(filePath, maxSize);
            }
            int longer = Math.max(srcW, srcH);
            int subsample = 1;
            while (longer / subsample > maxSize) {
                subsample *= 2;
            }
            if (subsample == 1 && longer <= maxSize) {
                reader.dispose();
                return Files.readAllBytes(filePath);
            }
            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceSubsampling(subsample, subsample, 0, 0);

            BufferedImage decoded;
            try {
                decoded = reader.read(0, param);
            } catch (Exception e) {
                // "Bogus input colorspace" or other corrupt metadata
                reader.dispose();
                return readAndResizeFallback(filePath, maxSize);
            }
            reader.dispose();
            if (decoded == null) return readAndResizeFallback(filePath, maxSize);

            int dw = decoded.getWidth(), dh = decoded.getHeight();
            int dLonger = Math.max(dw, dh);
            boolean needFinalScale = dLonger > maxSize;

            BufferedImage target;
            if (needFinalScale) {
                double scale = (double) maxSize / dLonger;
                int tw = (int) (dw * scale), th = (int) (dh * scale);
                target = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = target.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(decoded, 0, 0, tw, th, null);
                g.dispose();
                decoded.flush();
            } else {
                target = decoded;
            }

            byte[] result = writeJpeg(target, 0.88f);
            target.flush();
            return result;
        }
    }

    /** PNG/GIF/BMP: try fast decode path first, fallback to ImageIO.read + Graphics2D scale. */
    private byte[] readAndResizeGeneric(Path filePath, int maxSize, String formatName) throws IOException {
        try {
            return readAndResizeGenericFast(filePath, maxSize, formatName);
        } catch (Exception e) {
            // setSourceRenderSize not supported by this reader, fallback to slow path
            return readAndResizeFallback(filePath, maxSize);
        }
    }

    private byte[] readAndResizeGenericFast(Path filePath, int maxSize, String formatName) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(filePath.toFile())) {
            javax.imageio.ImageReader reader = null;
            if (formatName != null) {
                java.util.Iterator<javax.imageio.ImageReader> it = ImageIO.getImageReadersByFormatName(formatName);
                if (it.hasNext()) reader = it.next();
            }
            if (reader == null) {
                java.util.Iterator<javax.imageio.ImageReader> it = ImageIO.getImageReaders(iis);
                if (it.hasNext()) reader = it.next();
                else return readAndResizeFallback(filePath, maxSize);
            }

            reader.setInput(iis, true, true);
            int srcW = reader.getWidth(0), srcH = reader.getHeight(0);
            int longer = Math.max(srcW, srcH);

            if (longer <= maxSize) {
                BufferedImage img = reader.read(0);
                reader.dispose();
                if (img == null) return readAndResizeFallback(filePath, maxSize);
                byte[] result = writeJpeg(img, 0.88f);
                img.flush();
                return result;
            }

            double scale = (double) maxSize / longer;
            int tw = (int) (srcW * scale), th = (int) (srcH * scale);
            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRenderSize(new java.awt.Dimension(tw, th));

            BufferedImage decoded = reader.read(0, param);
            reader.dispose();
            if (decoded == null) return readAndResizeFallback(filePath, maxSize);

            BufferedImage rgb = decoded;
            if (decoded.getType() != BufferedImage.TYPE_INT_RGB) {
                rgb = new BufferedImage(decoded.getWidth(), decoded.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                g.drawImage(decoded, 0, 0, null);
                g.dispose();
                decoded.flush();
            }

            byte[] result = writeJpeg(rgb, 0.88f);
            rgb.flush();
            return result;
        }
    }

    /** Universal fallback: try AffineTransformOp (fast), then Graphics2D (slow but works for any image). */
    private byte[] readAndResizeFallback(Path filePath, int maxSize) throws IOException {
        BufferedImage img = ImageIO.read(filePath.toFile());
        if (img == null) return null;

        int srcW = img.getWidth(), srcH = img.getHeight();
        int longer = Math.max(srcW, srcH);

        BufferedImage target;
        if (longer <= maxSize) {
            target = img;
        } else {
            double scale = (double) maxSize / longer;
            int tw = Math.max(1, (int) (srcW * scale)), th = Math.max(1, (int) (srcH * scale));
            target = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);

            // Always convert to TYPE_INT_RGB before any scale operation
            BufferedImage src;
            if (img.getType() == BufferedImage.TYPE_INT_RGB) {
                src = img;
            } else {
                src = new BufferedImage(srcW, srcH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = src.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                img.flush();
            }

            // Try fast path first, fallback to Graphics2D if color model is incompatible
            try {
                java.awt.geom.AffineTransform at = java.awt.geom.AffineTransform.getScaleInstance(scale, scale);
                java.awt.image.AffineTransformOp op = new java.awt.image.AffineTransformOp(
                        at, java.awt.image.AffineTransformOp.TYPE_BILINEAR);
                op.filter(src, target);
            } catch (Exception e) {
                // AffineTransformOp failed (e.g., incompatible color model), use Graphics2D
                Graphics2D g = target.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(src, 0, 0, tw, th, null);
                g.dispose();
            }
            src.flush();
        }

        if (target.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgb = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.drawImage(target, 0, 0, null);
            g.dispose();
            target.flush();
            target = rgb;
        }

        byte[] result = writeJpeg(target, 0.88f);
        target.flush();
        return result;
    }

    /** Write BufferedImage as high-quality JPEG (cached writer). */
    private byte[] writeJpeg(BufferedImage image, float quality) throws IOException {
        synchronized (JPEG_WRITER) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(32768);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
                JPEG_WRITER.setOutput(ios);
                ImageWriteParam param = JPEG_WRITER.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
                JPEG_WRITER.write(null, new javax.imageio.IIOImage(image, null, null), param);
                ios.flush();
            }
            return bos.toByteArray();
        }
    }
}
