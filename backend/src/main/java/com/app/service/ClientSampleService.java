package com.app.service;

import com.app.common.PageResult;
import com.app.dto.ImportResult;
import com.app.dto.SearchCondition;
import com.app.entity.ClientSample;
import com.app.entity.ClientSampleItem;
import com.app.entity.ClientSamplePriceSetting;
import com.app.entity.Sample;
import com.app.entity.SampleThumbnail;
import com.app.entity.Video;
import com.app.mapper.ClientSampleItemMapper;
import com.app.mapper.ClientSampleMapper;
import com.app.mapper.ClientSamplePriceSettingMapper;
import com.app.mapper.InventoryMapper;
import com.app.mapper.OutboundMapper;
import com.app.mapper.SampleMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.app.mapper.VideoMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ClientSampleService {

    private static final Logger log = LoggerFactory.getLogger(ClientSampleService.class);

    private static final Pattern CODE_PATTERN = Pattern.compile("^100(\\d+)Y$");

    @Autowired
    private ClientSampleMapper clientSampleMapper;

    @Autowired
    private ClientSampleItemMapper clientSampleItemMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    @Autowired
    private ClientSamplePriceSettingMapper priceSettingMapper;

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private OutboundMapper outboundMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private VideoMapper videoMapper;

    public PageResult<ClientSample> list(long current, long size, String keyword, String sortField, String sortOrder, String dateFrom, String dateTo) {
        Page<ClientSample> page = new Page<>(current, size);
        LambdaQueryWrapper<ClientSample> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w
                .like(ClientSample::getCodeName, keyword)
                .or()
                .like(ClientSample::getSelectionId, keyword)
                .or()
                .like(ClientSample::getClientCode, keyword)
                .or()
                .like(ClientSample::getClientName, keyword)
                .or()
                .like(ClientSample::getOrderPhone, keyword)
                .or()
                .like(ClientSample::getRecorder, keyword)
                .or()
                .like(ClientSample::getRemark, keyword));
        }

        if (StringUtils.hasText(dateFrom)) {
            qw.ge(ClientSample::getSelectionDate, dateFrom);
        }
        if (StringUtils.hasText(dateTo)) {
            qw.le(ClientSample::getSelectionDate, dateTo);
        }

        if (StringUtils.hasText(sortField)) {
            boolean asc = !"desc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "codeName": qw.orderBy(true, asc, ClientSample::getCodeName); break;
                case "selectionId": qw.orderBy(true, asc, ClientSample::getSelectionId); break;
                case "clientCode": qw.orderBy(true, asc, ClientSample::getClientCode); break;
                case "clientName": qw.orderBy(true, asc, ClientSample::getClientName); break;
                case "selectionDate": qw.orderBy(true, asc, ClientSample::getSelectionDate); break;
                case "recordDate": qw.orderBy(true, asc, ClientSample::getRecordDate); break;
                case "modifyDate": qw.orderBy(true, asc, ClientSample::getModifyDate); break;
                default: qw.orderByDesc(ClientSample::getCreateTime); break;
            }
        } else {
            qw.orderByDesc(ClientSample::getCreateTime);
        }

        clientSampleMapper.selectPage(page, qw);

        // 批量填充样品数/厂商数
        List<ClientSample> records = page.getRecords();
        if (!records.isEmpty()) {
            List<String> codeNames = records.stream().map(ClientSample::getCodeName).filter(cn -> cn != null).collect(Collectors.toList());
            if (!codeNames.isEmpty()) {
                List<Map<String, Object>> counts = clientSampleItemMapper.countByCodeNames(codeNames);
                Map<String, Map<String, Object>> countMap = counts.stream()
                    .collect(Collectors.toMap(m -> (String) m.get("code_name"), m -> m));
                for (ClientSample cs : records) {
                    Map<String, Object> cnt = countMap.get(cs.getCodeName());
                    if (cnt != null) {
                        cs.setSampleCount(cnt.get("sample_count") != null ? ((Number) cnt.get("sample_count")).intValue() : 0);
                        cs.setManufacturerCount(cnt.get("manufacturer_count") != null ? ((Number) cnt.get("manufacturer_count")).intValue() : 0);
                    } else {
                        cs.setSampleCount(0);
                        cs.setManufacturerCount(0);
                    }
                }
            }
        }

        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public ClientSample getById(Long id) {
        return clientSampleMapper.selectById(id);
    }

    @Transactional
    public ClientSample create(ClientSample sample) {
        Long userId = UserContext.getUserId();

        String codeName = sample.getCodeName();
        if (!StringUtils.hasText(codeName)) {
            codeName = generateNextCodeName();
        } else {
            LambdaQueryWrapper<ClientSample> qw = new LambdaQueryWrapper<>();
            qw.eq(ClientSample::getCodeName, codeName.trim());
            if (clientSampleMapper.selectCount(qw) > 0) {
                throw new RuntimeException("代号[" + codeName + "]已被使用");
            }
        }
        sample.setCodeName(codeName.trim());

        if (!StringUtils.hasText(sample.getSelectionId())) {
            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            sample.setSelectionId(datePart + "-" + codeName.trim());
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if (!StringUtils.hasText(sample.getSelectionDate())) {
            sample.setSelectionDate(now);
        }
        if (!StringUtils.hasText(sample.getRecordDate())) {
            sample.setRecordDate(now);
        }

        sample.setCreateBy(userId);
        sample.setUpdateBy(userId);

        clientSampleMapper.insert(sample);

        // 复制逻辑
        Long sourceId = sample.getSourceId();
        Boolean copyPrice = sample.getCopyPrice();
        Boolean copyItems = sample.getCopyItems();

        if (sourceId != null && (Boolean.TRUE.equals(copyPrice) || Boolean.TRUE.equals(copyItems))) {
            ClientSample source = clientSampleMapper.selectById(sourceId);
            if (source == null) {
                log.warn("复制来源记录不存在: sourceId={}", sourceId);
            } else {
                String sourceCodeName = source.getCodeName();
                String newCodeName = sample.getCodeName();

                // 复制报价设置
                if (Boolean.TRUE.equals(copyPrice)) {
                    LambdaQueryWrapper<ClientSamplePriceSetting> priceQw = new LambdaQueryWrapper<>();
                    priceQw.eq(ClientSamplePriceSetting::getCodeName, sourceCodeName);
                    List<ClientSamplePriceSetting> priceSettings = priceSettingMapper.selectList(priceQw);
                    if (priceSettings != null && !priceSettings.isEmpty()) {
                        for (ClientSamplePriceSetting ps : priceSettings) {
                            ps.setId(null);
                            ps.setCodeName(newCodeName);
                            ps.setApplyTo("continue");
                            ps.setCreateTime(null);
                            ps.setUpdateTime(null);
                            priceSettingMapper.insert(ps);
                        }
                        log.info("复制报价设置成功: {} -> {}, 共{}条", sourceCodeName, newCodeName, priceSettings.size());
                    }
                }

                // 复制已勾选的明细数据
                if (Boolean.TRUE.equals(copyItems)) {
                    LambdaQueryWrapper<ClientSampleItem> itemQw = new LambdaQueryWrapper<>();
                    itemQw.eq(ClientSampleItem::getCodeName, sourceCodeName)
                           .eq(ClientSampleItem::getChecked, 1);
                    List<ClientSampleItem> items = clientSampleItemMapper.selectList(itemQw);
                    if (items != null && !items.isEmpty()) {
                        for (ClientSampleItem item : items) {
                            item.setId(null);
                            item.setCodeName(newCodeName);
                            // 保留原数据添加时间
                            item.setUpdateTime(null);
                            // 未复制报价时，价格字段使用默认值
                            if (!Boolean.TRUE.equals(copyPrice)) {
                                item.setCalculatedPrice(BigDecimal.ZERO);
                                item.setCalculatedPrice2(BigDecimal.ZERO);
                            }
                            clientSampleItemMapper.insert(item);
                        }
                        log.info("复制明细数据成功: {} -> {}, 共{}条", sourceCodeName, newCodeName, items.size());
                    }
                }
            }
        }

        return sample;
    }

    public void update(Long id, ClientSample sample) {
        ClientSample existing = clientSampleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("记录不存在");
        }

        sample.setCodeName(existing.getCodeName());
        sample.setSelectionId(existing.getSelectionId());

        sample.setId(id);
        sample.setUpdateBy(UserContext.getUserId());
        sample.setModifyDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        clientSampleMapper.updateById(sample);
    }

    public void delete(Long id) {
        clientSampleMapper.deleteById(id);
    }

    public String getLabelTemplateIds(String codeName) {
        ClientSample cs = clientSampleMapper.selectByCodeName(codeName);
        return cs != null && cs.getLabelTemplateIds() != null ? cs.getLabelTemplateIds() : "";
    }

    public void saveLabelTemplateIds(String codeName, String templateIds) {
        ClientSample cs = clientSampleMapper.selectByCodeName(codeName);
        if (cs != null) {
            cs.setLabelTemplateIds(templateIds);
            clientSampleMapper.updateById(cs);
        }
    }

    public Map<String, String> nextCode() {
        String codeName = generateNextCodeName();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String selectionId = datePart + "-" + codeName;

        Map<String, String> result = new HashMap<>();
        result.put("codeName", codeName);
        result.put("selectionId", selectionId);
        return result;
    }

    private String generateNextCodeName() {
        List<String> allCodeNames = clientSampleMapper.selectAllCodeNames();
        int maxNum = 99;
        for (String cn : allCodeNames) {
            if (cn != null) {
                Matcher m = CODE_PATTERN.matcher(cn.trim());
                if (m.find()) {
                    int num = Integer.parseInt(m.group(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                }
            }
        }
        int nextNum = maxNum + 1;
        return "100" + nextNum + "Y";
    }

    @Transactional
    public ImportResult batchInsert(List<ClientSample> samples, boolean updateMode) {
        ImportResult result = new ImportResult();
        List<Map<String, String>> failedRows = new ArrayList<>();
        int successCount = 0;

        if (samples == null || samples.isEmpty()) {
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setDuplicateCount(0);
            result.setUpdatedCount(0);
            result.setFailedRows(failedRows);
            return result;
        }

        for (int i = 0; i < samples.size(); i++) {
            ClientSample s = samples.get(i);
            try {
                Long userId = UserContext.getUserId();
                s.setCreateBy(userId);
                s.setUpdateBy(userId);
                s.setId(null);
                create(s);
                successCount++;
            } catch (Exception e) {
                log.warn("批量导入客户择样第{}条失败: {}", i + 1, e.getMessage());
                Map<String, String> failRow = new LinkedHashMap<>();
                failRow.put("row", String.valueOf(i + 1));
                failRow.put("失败原因", e.getMessage() != null ? e.getMessage() : "未知错误");
                failedRows.add(failRow);
            }
        }

        result.setTotalCount(samples.size());
        result.setSuccessCount(successCount);
        result.setFailCount(failedRows.size());
        result.setDuplicateCount(0);
        result.setUpdatedCount(0);
        result.setFailedRows(failedRows);
        return result;
    }

    // ==================== 择样明细 ====================

    /**
     * 仅计算价格，不保存到数据库
     * 前端查询样品时调用，用户确认后再点保存
     */
    public List<Sample> calculatePrices(String codeName, List<Long> sampleIds) {
        List<Sample> result = new ArrayList<>();
        if (sampleIds == null || sampleIds.isEmpty()) return result;

        // 获取报价设置
        ClientSamplePriceSetting s1 = getPriceSetting(codeName, "1");
        ClientSamplePriceSetting s2 = getPriceSetting(codeName, "2");

        // 加载缩略图
        Map<Long, SampleThumbnail> thumbnailMap = new HashMap<>();
        if (!sampleIds.isEmpty()) {
            List<SampleThumbnail> thumbnails = sampleThumbnailMapper.selectBatchIds(sampleIds);
            for (SampleThumbnail t : thumbnails) {
                thumbnailMap.put(t.getSampleId(), t);
            }
        }

        for (Long sampleId : sampleIds) {
            Sample sample = sampleMapper.selectById(sampleId);
            if (sample == null) continue;

            sample.setId(sampleId);

            // 计算报价1；有设置但算不出来时显示0，无设置时显示出厂价
            if (s1 != null) {
                BigDecimal p1 = calculatePrice(sample, s1);
                sample.setCalculatedPrice(p1 != null ? p1 : BigDecimal.ZERO);
            } else {
                sample.setCalculatedPrice(sample.getFactoryPrice());
            }
            // 计算报价2
            if (s2 != null) {
                BigDecimal p2 = calculatePrice(sample, s2);
                sample.setTaxPrice2(p2);
            }

            // 加载缩略图
            SampleThumbnail t = thumbnailMap.get(sampleId);
            if (t != null) {
                sample.setThumbnail(t.getThumbnail());
                sample.setFirstImageId(t.getImageId());
                sample.setFirstImageHash(t.getHash());
            }

            result.add(sample);
        }
        return result;
    }

    /**
     * 查询代号下的所有样品明细（从快照读取，数据独立）
     */
    public List<Sample> getItemsByCodeName(String codeName) {
        List<ClientSampleItem> items = clientSampleItemMapper.selectItemsByCodeName(codeName);
        List<Sample> samples = new ArrayList<>();
        List<Long> sampleIds = new ArrayList<>();

        // 收集需要从 samples 表补充价格字段的 ID（快照可能为 null）
        List<Long> needPriceIds = new ArrayList<>();

        for (ClientSampleItem item : items) {
            Sample s;
            if (item.getSnapshotData() != null && !item.getSnapshotData().isEmpty()) {
                s = fromSnapshotJson(item.getSnapshotData());
            } else {
                // 兼容旧数据：如果没有快照，从 samples 表读取
                s = sampleMapper.selectById(item.getSampleId());
            }
            if (s == null) continue;

            // 用 sampleId 做缩略图关联
            s.setId(item.getSampleId());
            sampleIds.add(item.getSampleId());

            // 快照中的出厂价/退税价/见客手机可能为 null（旧快照无此字段），需要从 samples 表补充
            if (s.getFactoryPrice() == null || s.getTaxPrice() == null || s.getVisitorMobile() == null) {
                needPriceIds.add(item.getSampleId());
            }

            s.setItemId(item.getId());
            s.setShowroomReplenished(item.getShowroomReplenished());
            s.setBorrowedSample(item.getBorrowedSample());
            s.setSampleStatus(resolveSampleStatus(item.getShowroomReplenished(), item.getBorrowedSample()));
            s.setChecked(item.getChecked());
            s.setAddDate(item.getCreateTime());
            s.setModifyDate(item.getUpdateTime());
            s.setCodeName(codeName);
            samples.add(s);
        }

        // 从 samples 表批量补充出厂价/退税价（快照中可能为 null）
        if (!needPriceIds.isEmpty()) {
            List<Sample> liveSamples = sampleMapper.selectBatchIds(needPriceIds);
            Map<Long, Sample> liveMap = new HashMap<>();
            for (Sample ls : liveSamples) {
                liveMap.put(ls.getId(), ls);
            }
            for (Sample s : samples) {
                Sample live = liveMap.get(s.getId());
                if (live != null) {
                    if (s.getFactoryPrice() == null && live.getFactoryPrice() != null) {
                        s.setFactoryPrice(live.getFactoryPrice());
                    }
                    if (s.getTaxPrice() == null && live.getTaxPrice() != null) {
                        s.setTaxPrice(live.getTaxPrice());
                    }
                    if (s.getVisitorMobile() == null && live.getVisitorMobile() != null) {
                        s.setVisitorMobile(live.getVisitorMobile());
                    }
                }
            }
        }

        // 报出价1：有计算结果用计算结果，否则显示出厂价
        Map<Long, ClientSampleItem> itemMap = new HashMap<>();
        for (ClientSampleItem item : items) {
            itemMap.put(item.getId(), item);
        }
        for (Sample s : samples) {
            ClientSampleItem item = itemMap.get(s.getItemId());
            if (item != null) {
                if (item.getCalculatedPrice() != null) {
                    s.setCalculatedPrice(item.getCalculatedPrice());
                } else {
                    s.setCalculatedPrice(s.getFactoryPrice());
                }
                // 报出价2 仅来自公式计算结果
                s.setTaxPrice2(item.getCalculatedPrice2());
            }
        }

        // 加载缩略图
        if (!sampleIds.isEmpty()) {
            List<SampleThumbnail> thumbnails = sampleThumbnailMapper.selectBatchIds(sampleIds);
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

        // 检查视频存在性
        if (!sampleIds.isEmpty()) {
            List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                    .in(Video::getSampleId, sampleIds)
                    .select(Video::getSampleId)
            );
            Set<Long> videoSampleIds = videos.stream()
                .map(Video::getSampleId)
                .collect(Collectors.toSet());
            for (Sample s : samples) {
                s.setHasVideo(videoSampleIds.contains(s.getId()));
            }
        }

        // 检查入库/出库提交状态
        if (!samples.isEmpty()) {
            // 收集所有公司编号
            List<String> companyCodes = new ArrayList<>();
            for (Sample s : samples) {
                if (s.getSampleCode() != null) {
                    companyCodes.add(s.getSampleCode());
                }
            }
            // 批量查询入库记录
            Set<String> inventoryCodes = new HashSet<>();
            if (!companyCodes.isEmpty()) {
                List<com.app.entity.Inventory> inventoryList = inventoryMapper.selectList(
                    new LambdaQueryWrapper<com.app.entity.Inventory>()
                        .eq(com.app.entity.Inventory::getCodeName, codeName)
                        .in(com.app.entity.Inventory::getCompanyCode, companyCodes)
                );
                for (com.app.entity.Inventory inv : inventoryList) {
                    inventoryCodes.add(inv.getCompanyCode());
                }
            }
            // 批量查询出库记录
            Set<String> outboundCodes = new HashSet<>();
            if (!companyCodes.isEmpty()) {
                List<com.app.entity.Outbound> outboundList = outboundMapper.selectList(
                    new LambdaQueryWrapper<com.app.entity.Outbound>()
                        .eq(com.app.entity.Outbound::getCodeName, codeName)
                        .in(com.app.entity.Outbound::getCompanyCode, companyCodes)
                );
                for (com.app.entity.Outbound ob : outboundList) {
                    outboundCodes.add(ob.getCompanyCode());
                }
            }
            // 设置提交状态：入库或出库任一存在即为已提交
            for (Sample s : samples) {
                String code = s.getSampleCode();
                s.setSubmitted((code != null && (inventoryCodes.contains(code) || outboundCodes.contains(code))) ? 1 : 0);
            }
        }

        return samples;
    }

    /**
     * 获取择样明细样本数据（用于报表设计器发现字段结构）
     */
    public List<Sample> getSampleItems(int size) {
        // 查询最近的几个择样单
        List<ClientSample> sampleList = clientSampleMapper.selectList(
            new LambdaQueryWrapper<ClientSample>().orderByDesc(ClientSample::getCreateTime).last("LIMIT 3")
        );
        List<Sample> result = new ArrayList<>();
        for (ClientSample cs : sampleList) {
            List<Sample> items = getItemsByCodeName(cs.getCodeName());
            for (Sample s : items) {
                if (result.size() >= size) break;
                result.add(s);
            }
            if (result.size() >= size) break;
        }
        return result;
    }

    /**
     * 添加样品到择样明细：拷贝样品完整数据到快照
     */
    @Transactional
    public ClientSampleItem addItem(String codeName, Long sampleId, String boxCount, String otherRemark) {
        Sample sample = sampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new RuntimeException("样品不存在: " + sampleId);
        }

        // 前端传的非样品基础字段，写入快照
        if (boxCount != null) sample.setBoxCount(boxCount);
        if (otherRemark != null) sample.setOtherRemark(otherRemark);

        ClientSampleItem item = new ClientSampleItem();
        item.setCodeName(codeName);
        item.setSampleId(sampleId);
        item.setChecked(1);
        item.setSortOrder(0);
        item.setSnapshotData(toSnapshotJson(sample));
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        clientSampleItemMapper.insert(item);

        // 自动报价计算
        autoApplyPriceOnAdd(item);
        return item;
    }

    /**
     * 更新样品快照（修改样品数据时调用，仅影响当前代号）
     */
    @Transactional
    public void updateItemSnapshot(Long itemId, Sample updatedSample) {
        ClientSampleItem item = clientSampleItemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("记录不存在");
        }
        item.setSnapshotData(toSnapshotJson(updatedSample));
        item.setUpdateTime(new Date());
        clientSampleItemMapper.updateById(item);
    }

    /**
     * 添加样品后自动应用报价设置
     */
    private void autoApplyPriceOnAdd(ClientSampleItem item) {
        try {
            if (item.getSnapshotData() == null) return;

            Sample sample = fromSnapshotJson(item.getSnapshotData());
            if (sample == null) return;

            String codeName = item.getCodeName();

            // 报价1
            ClientSamplePriceSetting s1 = priceSettingMapper.selectByCodeNameAndType(codeName, "1");
            if (s1 != null && ("continue".equals(s1.getApplyTo()) || "allPriced".equals(s1.getApplyTo()))) {
                BigDecimal price1 = calculatePrice(sample, s1);
                if (price1 != null) {
                    item.setCalculatedPrice(price1);
                    clientSampleItemMapper.updateById(item);
                }
            }
            // 报价2
            ClientSamplePriceSetting s2 = priceSettingMapper.selectByCodeNameAndType(codeName, "2");
            if (s2 != null && ("continue".equals(s2.getApplyTo()) || "allPriced".equals(s2.getApplyTo()))) {
                BigDecimal price2 = calculatePrice(sample, s2);
                if (price2 != null) {
                    item.setCalculatedPrice2(price2);
                    clientSampleItemMapper.updateById(item);
                }
            }
        } catch (Exception e) {
            log.warn("自动报价计算失败: {}", e.getMessage());
        }
    }

    /**
     * 添加样品前自动应用报价设置（批量）—— 仅计算价格并设置到 item 对象上，
     * 由后续的 INSERT 语句随其他字段一起写入数据库。
     */
    private void autoApplyPriceOnAddBatch(String codeName, List<ClientSampleItem> items) {
        if (items.isEmpty()) return;

        // 报价设置只查一次
        ClientSamplePriceSetting s1 = priceSettingMapper.selectByCodeNameAndType(codeName, "1");
        ClientSamplePriceSetting s2 = priceSettingMapper.selectByCodeNameAndType(codeName, "2");
        log.info("autoApplyPriceOnAddBatch: codeName={}, s1={}, s1.applyTo={}, s2={}, s2.applyTo={}",
                codeName,
                s1 != null ? "found" : "null",
                s1 != null ? s1.getApplyTo() : "N/A",
                s2 != null ? "found" : "null",
                s2 != null ? s2.getApplyTo() : "N/A");

        boolean apply1 = s1 != null && ("continue".equals(s1.getApplyTo()) || "allPriced".equals(s1.getApplyTo()));
        boolean apply2 = s2 != null && ("continue".equals(s2.getApplyTo()) || "allPriced".equals(s2.getApplyTo()));
        if (!apply1 && !apply2) {
            log.info("autoApplyPriceOnAddBatch: 跳过，apply1={}, apply2={}", apply1, apply2);
            return;
        }

        for (ClientSampleItem item : items) {
            try {
                if (item.getSnapshotData() == null) continue;
                Sample sample = fromSnapshotJson(item.getSnapshotData());
                if (sample == null) continue;

                if (apply1) {
                    BigDecimal price1 = calculatePrice(sample, s1);
                    if (price1 != null) item.setCalculatedPrice(price1);
                }
                if (apply2) {
                    BigDecimal price2 = calculatePrice(sample, s2);
                    if (price2 != null) item.setCalculatedPrice2(price2);
                }
            } catch (Exception e) {
                log.warn("自动报价计算失败: {}", e.getMessage());
            }
        }
        log.info("autoApplyPriceOnAddBatch: 完成，共{}条，apply1={}, apply2={}", items.size(), apply1, apply2);
    }

    @Transactional
    public int addItems(String codeName, List<Long> sampleIds, String boxCount, String otherRemark, boolean force) {
        log.info("addItems 入参: codeName={}, sampleIds={}, force={}", codeName, sampleIds, force);
        // 去重：防止同一批次内重复 sampleId 导致唯一键冲突
        sampleIds = new ArrayList<>(new LinkedHashSet<>(sampleIds));
        // 查询已存在的记录
        List<ClientSampleItem> existing = clientSampleItemMapper.selectList(
                new LambdaQueryWrapper<ClientSampleItem>()
                        .eq(ClientSampleItem::getCodeName, codeName)
                        .in(ClientSampleItem::getSampleId, sampleIds));
        log.info("addItems 已存在记录数={}, 明细={}", existing.size(),
                existing.stream().map(e -> "id=" + e.getId() + ",sampleId=" + e.getSampleId() + ",deleted=" + e.getDeleted()).collect(java.util.stream.Collectors.toList()));

        if (force) {
            // 强制模式：允许重复添加，直接插入（需确保数据库无唯一约束）
            log.info("addItems force=true, 直接插入 {} 条", sampleIds.size());
        } else {
            // 非强制模式：跳过已存在的记录
            Set<Long> existingIds = existing.stream().map(ClientSampleItem::getSampleId)
                    .collect(java.util.stream.Collectors.toSet());
            sampleIds.removeIf(existingIds::contains);
            log.info("addItems force=false, 过滤后待插入 sampleIds={}", sampleIds);
        }

        if (sampleIds.isEmpty()) {
            log.info("addItems 无需插入，返回0");
            return 0;
        }
        // 批量查询所有样品，避免 N+1
        List<Sample> samples = sampleMapper.selectBatchIds(sampleIds);
        Map<Long, Sample> sampleMap = new HashMap<>();
        for (Sample s : samples) {
            if (boxCount != null) s.setBoxCount(boxCount);
            if (otherRemark != null) s.setOtherRemark(otherRemark);
            sampleMap.put(s.getId(), s);
        }

        Date now = new Date();
        List<ClientSampleItem> items = new ArrayList<>();
        for (Long sampleId : sampleIds) {
            Sample sample = sampleMap.get(sampleId);
            if (sample == null) continue;
            ClientSampleItem item = new ClientSampleItem();
            item.setCodeName(codeName);
            item.setSampleId(sampleId);
            item.setChecked(1);
            item.setSortOrder(0);
            item.setSnapshotData(toSnapshotJson(sample));
            item.setCreateTime(now);
            item.setUpdateTime(now);
            items.add(item);
        }

        // 批量报价计算（必须在 INSERT 之前，因为 INSERT SQL 已包含 price 列，且 insertBatch 不回填 ID）
        autoApplyPriceOnAddBatch(codeName, items);

        // 批量插入（单条 SQL，price 随 INSERT 一起写入）
        if (!items.isEmpty()) {
            clientSampleItemMapper.insertBatch(items);
        }

        return items.size();
    }

    /** 获取厂商总数 */
    public int getFactoryCount(String keyword, List<SearchCondition> conditions, String logic) {
        List<Long> filteredIds = sampleService.getFilteredSampleIds(keyword, conditions, logic);
        if (filteredIds != null && !filteredIds.isEmpty()) {
            return sampleMapper.getFactoryStatsByIds(filteredIds).size();
        }
        // 无过滤时直接用 COUNT(DISTINCT)，确保和厂商管理页一致
        return sampleMapper.countDistinctManufacturerCodes();
    }

    /** 获取按厂商选品预览（真实统计） */
    public Map<String, Object> previewFactorySelection(String keyword, List<SearchCondition> conditions,
                                                        String logic, int factoryCount, int productsPerFactory) {
        List<Long> filteredIds = sampleService.getFilteredSampleIds(keyword, conditions, logic);
        List<Map<String, Object>> stats;
        if (filteredIds != null && !filteredIds.isEmpty()) {
            stats = sampleMapper.getFactoryStatsByIds(filteredIds);
        } else {
            stats = sampleMapper.getFactoryStats(0);
        }

        int limit = Math.min(factoryCount, stats.size());
        int total = 0;
        int fullCount = 0;
        int partialCount = 0;
        int fullTotal = 0;
        int partialTotal = 0;
        int minCnt = Integer.MAX_VALUE;
        int maxCnt = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Map<String, Object> row = stats.get(i);
            int sampleCount = Integer.parseInt(
                (row.get("cnt") != null ? row.get("cnt") : row.get("sampleCount")).toString());
            int selected = Math.min(sampleCount, productsPerFactory);
            total += selected;
            if (sampleCount >= productsPerFactory) {
                fullCount++;
                fullTotal += selected;
            } else {
                partialCount++;
                partialTotal += selected;
            }
            if (sampleCount < minCnt) minCnt = sampleCount;
            if (sampleCount > maxCnt) maxCnt = sampleCount;
            Map<String, Object> detail = new HashMap<>();
            detail.put("manufacturerCode", row.get("manufacturer_code"));
            detail.put("sampleCount", sampleCount);
            detail.put("selected", selected);
            details.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("factoryCount", stats.size());
        result.put("selectedFactoryCount", limit);
        result.put("realTotal", total);
        if (limit > 0) {
            result.put("minSampleCount", minCnt);
            result.put("maxSampleCount", maxCnt);
        }
        result.put("fullCount", fullCount);
        result.put("partialCount", partialCount);
        result.put("fullTotal", fullTotal);
        result.put("partialTotal", partialTotal);
        result.put("details", details);
        return result;
    }

    /** 按厂商自动选品 */
    @Transactional
    public int selectByFactory(String codeName, int factoryCount, int productsPerFactory,
                               String keyword, List<SearchCondition> conditions, String logic, boolean force) {
        // 获取过滤后的样品ID列表
        List<Long> filteredIds = sampleService.getFilteredSampleIds(keyword, conditions, logic);

        // 从过滤后的样品中统计厂商
        List<Map<String, Object>> stats;
        if (filteredIds != null && !filteredIds.isEmpty()) {
            // 有过滤条件：在过滤后的ID范围内做厂商聚合
            stats = sampleMapper.getFactoryStatsByIds(filteredIds);
        } else {
            // 无过滤条件：全部样品
            stats = sampleMapper.getFactoryStats(0);
        }

        // 取前N家厂商编号
        int limit = Math.min(factoryCount, stats.size());
        List<String> selectedCodes = stats.subList(0, limit).stream()
                .map(m -> (String) m.get("manufacturer_code"))
                .collect(Collectors.toList());

        // 从每家厂商取 productsPerFactory 条样品ID
        List<Long> allSampleIds = new ArrayList<>();
        for (String manufacturerCode : selectedCodes) {
            List<Long> ids;
            if (filteredIds != null) {
                // 有过滤：只取在过滤范围内的该厂商样品
                ids = sampleMapper.getSampleIdsBySupplierFiltered(manufacturerCode, productsPerFactory, filteredIds);
            } else {
                ids = sampleMapper.getSampleIdsBySupplier(manufacturerCode, productsPerFactory);
            }
            allSampleIds.addAll(ids);
        }

        if (!allSampleIds.isEmpty()) {
            return addItems(codeName, allSampleIds, null, null, force);
        }
        return 0;
    }

    public void removeItem(Long itemId) {
        String operator = UserContext.getRealName();
        LambdaUpdateWrapper<ClientSampleItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ClientSampleItem::getId, itemId)
               .set(ClientSampleItem::getDeleted, 1)
               .set(ClientSampleItem::getDeletedBy, operator)
               .set(ClientSampleItem::getUpdateTime, new Date());
        clientSampleItemMapper.update(null, wrapper);
    }

    @Transactional
    public void removeItems(List<Long> itemIds) {
        String operator = UserContext.getRealName();
        LambdaUpdateWrapper<ClientSampleItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ClientSampleItem::getId, itemIds)
               .set(ClientSampleItem::getDeleted, 1)
               .set(ClientSampleItem::getDeletedBy, operator)
               .set(ClientSampleItem::getUpdateTime, new Date());
        clientSampleItemMapper.update(null, wrapper);
    }

    /**
     * 恢复已删除的择样明细（将 Sample 数据重新构造 ClientSampleItem 并写入数据库）
     */
    @Transactional
    public int restoreItems(String codeName, List<Sample> samples) {
        int count = 0;
        for (Sample s : samples) {
            ClientSampleItem item = new ClientSampleItem();
            item.setCodeName(codeName);
            item.setSampleId(s.getId());
            item.setSnapshotData(toSnapshotJson(s));
            item.setShowroomReplenished(s.getShowroomReplenished());
            item.setBorrowedSample(s.getBorrowedSample());
            item.setChecked(s.getChecked());
            item.setDeleted(0);
            clientSampleItemMapper.insert(item);
            count++;
        }
        log.info("restoreItems: 成功恢复 {} 条记录", count);
        return count;
    }

    /**
     * 查询已删除的明细记录（绕过逻辑删除过滤，反序列化快照数据）
     */
    public List<Map<String, Object>> getDeletedItems(String codeName) {
        List<ClientSampleItem> items = clientSampleItemMapper.selectDeletedItemsByCodeName(codeName);
        return items.stream().map(item -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(item.getSnapshotData(), Map.class);
                data.put("codeName", item.getCodeName());
                data.put("itemId", item.getId());
                data.put("creator", item.getDeletedBy());
                data.put("deletedAt", item.getUpdateTime() != null ?
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getUpdateTime()) : "");
                return data;
            } catch (Exception e) {
                log.error("反序列化删除记录快照失败: {}", e.getMessage());
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("codeName", item.getCodeName());
                fallback.put("itemId", item.getId());
                fallback.put("creator", item.getDeletedBy());
                fallback.put("deletedAt", item.getUpdateTime() != null ?
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getUpdateTime()) : "");
                return fallback;
            }
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateItemFlag(Long itemId, String field, Integer value) {
        LambdaUpdateWrapper<ClientSampleItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ClientSampleItem::getId, itemId);
        if ("showroomReplenished".equals(field)) {
            wrapper.set(ClientSampleItem::getShowroomReplenished, value);
        } else if ("borrowedSample".equals(field)) {
            wrapper.set(ClientSampleItem::getBorrowedSample, value);
        } else if ("checked".equals(field)) {
            wrapper.set(ClientSampleItem::getChecked, value);
        }
        wrapper.set(ClientSampleItem::getUpdateTime, new Date());
        clientSampleItemMapper.update(null, wrapper);
    }

    /** 批量更新明细标记 */
    @Transactional
    public void updateItemFlags(List<Map<String, Object>> flags) {
        for (Map<String, Object> flag : flags) {
            Long itemId = Long.valueOf(flag.get("itemId").toString());
            String field = (String) flag.get("field");
            Integer value = Integer.valueOf(flag.get("value").toString());
            updateItemFlag(itemId, field, value);
        }
    }

    // ==================== 报价设置 ====================

    @Transactional
    public ClientSamplePriceSetting savePriceSetting(String codeName, String type, ClientSamplePriceSetting setting, List<Long> sampleIds) {
        ClientSamplePriceSetting existing = priceSettingMapper.selectByCodeNameAndType(codeName, type);
        if (existing != null) {
            setting.setId(existing.getId());
            priceSettingMapper.updateById(setting);
        } else {
            setting.setCodeName(codeName);
            setting.setType(type);
            priceSettingMapper.insert(setting);
        }

        String applyTo = setting.getApplyTo();
        if ("allPriced".equals(applyTo)) {
            calculateAndSavePricesForAll(codeName, type, setting);
        } else if ("current".equals(applyTo) && sampleIds != null && !sampleIds.isEmpty()) {
            calculateAndSavePricesForIds(codeName, type, setting, sampleIds);
        }
        return setting;
    }

    /**
     * 计算全部已有明细的报价（基于快照数据）
     */
    private void calculateAndSavePricesForAll(String codeName, String type, ClientSamplePriceSetting setting) {
        List<ClientSampleItem> items = clientSampleItemMapper.selectItemsByCodeName(codeName);
        if (items == null || items.isEmpty()) return;

        List<ClientSampleItem> updatedItems = new ArrayList<>();
        for (ClientSampleItem item : items) {
            if (item.getSnapshotData() == null) continue;
            Sample sample = fromSnapshotJson(item.getSnapshotData());
            if (sample == null) continue;

            BigDecimal price = calculatePrice(sample, setting);
            if (price != null) {
                setPriceOnItem(item, type, price);
                updatedItems.add(item);
            }
        }
        // 批量更新，一条 SQL 搞定
        if (!updatedItems.isEmpty()) {
            if ("2".equals(type)) {
                clientSampleItemMapper.batchUpdateCalculatedPrice2(updatedItems);
            } else {
                clientSampleItemMapper.batchUpdateCalculatedPrice(updatedItems);
            }
        }
    }

    /**
     * 计算指定样品ID的报价（基于快照数据）
     */
    private void calculateAndSavePricesForIds(String codeName, String type, ClientSamplePriceSetting setting, List<Long> sampleIds) {
        List<ClientSampleItem> items = clientSampleItemMapper.selectItemsByCodeName(codeName);
        if (items == null) return;

        Map<Long, ClientSampleItem> itemMap = new HashMap<>();
        for (ClientSampleItem item : items) {
            itemMap.put(item.getSampleId(), item);
        }

        List<ClientSampleItem> updatedItems = new ArrayList<>();
        for (Long sampleId : sampleIds) {
            ClientSampleItem item = itemMap.get(sampleId);
            if (item == null || item.getSnapshotData() == null) continue;

            Sample sample = fromSnapshotJson(item.getSnapshotData());
            if (sample == null) continue;

            BigDecimal price = calculatePrice(sample, setting);
            if (price != null) {
                setPriceOnItem(item, type, price);
                updatedItems.add(item);
            }
        }
        // 批量更新
        if (!updatedItems.isEmpty()) {
            if ("2".equals(type)) {
                clientSampleItemMapper.batchUpdateCalculatedPrice2(updatedItems);
            } else {
                clientSampleItemMapper.batchUpdateCalculatedPrice(updatedItems);
            }
        }
    }

    private void setPriceOnItem(ClientSampleItem item, String type, BigDecimal price) {
        if ("2".equals(type)) {
            item.setCalculatedPrice2(price);
        } else {
            item.setCalculatedPrice(price);
        }
    }

    public ClientSamplePriceSetting getPriceSetting(String codeName, String type) {
        return priceSettingMapper.selectByCodeNameAndType(codeName, type);
    }

    private String resolveSampleStatus(Integer showroomReplenished, Integer borrowedSample) {
        if (borrowedSample != null && borrowedSample == 1) return "借样";
        if (showroomReplenished != null && showroomReplenished == 1) return "展厅已补";
        return "不允许带走";
    }

    // ==================== 快照序列化/反序列化 ====================

    private String toSnapshotJson(Sample sample) {
        try {
            return objectMapper.writeValueAsString(sample);
        } catch (Exception e) {
            log.error("序列化样品快照失败: {}", e.getMessage());
            throw new RuntimeException("序列化样品快照失败", e);
        }
    }

    private Sample fromSnapshotJson(String json) {
        try {
            return objectMapper.readValue(json, Sample.class);
        } catch (Exception e) {
            log.warn("解析样品快照失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 价格计算 ====================

    private BigDecimal calculatePrice(Sample sample, ClientSamplePriceSetting setting) {
        BigDecimal factoryPrice = sample.getFactoryPrice() != null ? sample.getFactoryPrice() : BigDecimal.ZERO;
        BigDecimal totalCost = setting.getTotalCost() != null ? setting.getTotalCost() : BigDecimal.ZERO;
        BigDecimal exchangeRate = setting.getExchangeRate() != null ? setting.getExchangeRate() : BigDecimal.ONE;
        BigDecimal profitRate = setting.getProfitRate() != null ? setting.getProfitRate() : BigDecimal.ZERO;
        BigDecimal markup = setting.getMarkup() != null ? setting.getMarkup() : BigDecimal.ZERO;
        Integer cartonSize = setting.getCartonSize() != null ? setting.getCartonSize() : 68;

        BigDecimal volume;
        if ("multiply".equals(setting.getFormulaType())) {
            volume = sample.getCartonMaterialVolume() != null ? sample.getCartonMaterialVolume() : BigDecimal.ZERO;
        } else if ("divide".equals(setting.getFormulaType()) && cartonSize > 100) {
            volume = sample.getCartonMaterialVolume() != null ? sample.getCartonMaterialVolume() : BigDecimal.ZERO;
        } else {
            volume = sample.getCartonVolume() != null ? sample.getCartonVolume() : BigDecimal.ZERO;
        }

        String customFormula = setting.getCustomFormula();
        boolean isCustom = customFormula != null && !customFormula.trim().isEmpty();

        if (!isCustom) {
            if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) return null;
        }

        BigDecimal freight = BigDecimal.ZERO;
        Integer cartonCapacityForCalc = sample.getCartonCapacity();
        if (cartonCapacityForCalc != null && cartonCapacityForCalc > 0
                && volume.compareTo(BigDecimal.ZERO) > 0 && cartonSize > 0) {
            BigDecimal denominator = BigDecimal.valueOf(cartonSize).multiply(BigDecimal.valueOf(cartonCapacityForCalc));
            freight = totalCost.multiply(volume).divide(denominator, 10, RoundingMode.HALF_UP);
        }

        BigDecimal base = factoryPrice.add(freight);

        BigDecimal result;

        if (isCustom) {
            result = calculateCustomFormula(factoryPrice, freight, exchangeRate, profitRate, markup, totalCost, base, customFormula, sample);
            if (result == null) return null;
        } else if ("multiply".equals(setting.getFormulaType())) {
            BigDecimal profitMultiplier = BigDecimal.ONE.add(
                profitRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            result = base.multiply(profitMultiplier).add(markup).divide(exchangeRate, 10, RoundingMode.HALF_UP);
        } else {
            BigDecimal profitDivisor = BigDecimal.ONE.subtract(
                profitRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            if (profitDivisor.compareTo(BigDecimal.ZERO) <= 0) return null;
            BigDecimal afterProfit = base.divide(profitDivisor, 10, RoundingMode.HALF_UP);
            result = afterProfit.add(markup).divide(exchangeRate, 10, RoundingMode.HALF_UP);
        }

        BigDecimal priceThreshold = setting.getPriceLessThan() != null ? setting.getPriceLessThan() : BigDecimal.ZERO;
        if (priceThreshold.compareTo(BigDecimal.ZERO) > 0 && result.compareTo(priceThreshold) < 0) {
            int decimals2 = setting.getPriceDecimals() != null ? setting.getPriceDecimals() : 2;
            return roundPrice(result, decimals2, setting.getRoundMode2());
        }
        int decimals = setting.getDecimals() != null ? setting.getDecimals() : 2;
        return roundPrice(result, decimals, setting.getRoundMode());
    }

    private BigDecimal roundPrice(BigDecimal value, int decimals, String mode) {
        if (mode == null) mode = "四舍五入";
        switch (mode) {
            case "全舍":
                return value.setScale(decimals, RoundingMode.DOWN);
            case "全收":
                return value.setScale(decimals, RoundingMode.UP);
            default:
                return value.setScale(decimals, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal calculateCustomFormula(BigDecimal factoryPrice, BigDecimal freight,
            BigDecimal exchangeRate, BigDecimal profitRate, BigDecimal markup,
            BigDecimal totalCost, BigDecimal base, String formula, Sample sample) {
        try {
            String expr = formula
                .replace("出厂价", factoryPrice.toPlainString())
                .replace("运费", freight.toPlainString())
                .replace("汇率", exchangeRate.toPlainString())
                .replace("利润率", profitRate.toPlainString())
                .replace("加价", markup.toPlainString())
                .replace("总费用", totalCost.toPlainString())
                .replace("基础价", base.toPlainString())
                .replace("外箱体积", nvl(sample.getCartonVolume()))
                .replace("外箱材积", nvl(sample.getCartonMaterialVolume()))
                .replace("外箱装量", nvlInt(sample.getCartonCapacity()))
                .replace("样品长", nvl(sample.getSampleLength()))
                .replace("样品宽", nvl(sample.getSampleWidth()))
                .replace("样品高", nvl(sample.getSampleHeight()))
                .replace("毛重", nvl(sample.getSampleGrossWeight()))
                .replace("净重", nvl(sample.getSampleNetWeight()))
                .replace("外箱长", nvl(sample.getCartonLength()))
                .replace("外箱宽", nvl(sample.getCartonWidth()))
                .replace("外箱高", nvl(sample.getCartonHeight()))
                .replace("外箱毛重", nvl(sample.getCartonGrossWeight()))
                .replace("外箱净重", nvl(sample.getCartonNetWeight()))
                .replace("税价", nvl(sample.getTaxPrice()))
                .replace("内盒数", nvlInt(sample.getInnerBoxCount()));
            return evaluateExpression(expr);
        } catch (Exception e) {
            log.warn("自定义公式计算失败: {}", e.getMessage());
            return null;
        }
    }

    private String nvl(BigDecimal val) {
        return (val != null ? val : BigDecimal.ZERO).toPlainString();
    }
    private String nvlInt(Integer val) {
        return String.valueOf(val != null ? val : 0);
    }

    private BigDecimal evaluateExpression(String expr) {
        return parseAddSub(new java.util.ArrayDeque<>(), expr.trim());
    }

    private BigDecimal parseAddSub(java.util.ArrayDeque<String> tokens, String expr) {
        tokenize(tokens, expr);
        BigDecimal left = parseMulDiv(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if ("+".equals(op) || "-".equals(op)) {
                tokens.poll();
                BigDecimal right = parseMulDiv(tokens);
                if ("+".equals(op)) left = left.add(right);
                else left = left.subtract(right);
            } else {
                break;
            }
        }
        return left;
    }

    private BigDecimal parseMulDiv(java.util.ArrayDeque<String> tokens) {
        BigDecimal left = parseAtom(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if ("*".equals(op) || "/".equals(op)) {
                tokens.poll();
                BigDecimal right = parseAtom(tokens);
                if ("*".equals(op)) left = left.multiply(right);
                else left = left.divide(right, 10, RoundingMode.HALF_UP);
            } else {
                break;
            }
        }
        return left;
    }

    private BigDecimal parseAtom(java.util.ArrayDeque<String> tokens) {
        String token = tokens.poll();
        if (token == null) throw new IllegalArgumentException("表达式不完整");
        if ("(".equals(token)) {
            BigDecimal val = parseAddSub(tokens, null);
            String close = tokens.poll();
            if (!")".equals(close)) throw new IllegalArgumentException("缺少右括号");
            return val;
        }
        if ("+".equals(token)) return parseAtom(tokens);
        if ("-".equals(token)) return parseAtom(tokens).negate();
        return new BigDecimal(token);
    }

    private void tokenize(java.util.ArrayDeque<String> tokens, String expr) {
        if (expr == null) return;
        tokens.clear();
        expr = expr
            .replace('（', '(').replace('）', ')')
            .replace('×', '*').replace('÷', '/')
            .replace('＋', '+').replace('－', '-');
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (c == ' ') { i++; continue; }
            if ("+-*/()".indexOf(c) >= 0) {
                tokens.add(String.valueOf(c));
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                int j = i;
                while (j < expr.length() && (Character.isDigit(expr.charAt(j)) || expr.charAt(j) == '.')) j++;
                tokens.add(expr.substring(i, j));
                i = j;
            } else {
                i++;
            }
        }
    }

    /**
     * 撤销指定代号下所有入库和出库提交（恢复误添加）
     */
    @Transactional
    public Map<String, Integer> revertSubmissions(String codeName) {
        int invCount = inventoryMapper.delete(
            new LambdaQueryWrapper<com.app.entity.Inventory>()
                .eq(com.app.entity.Inventory::getCodeName, codeName)
        );
        int obCount = outboundMapper.delete(
            new LambdaQueryWrapper<com.app.entity.Outbound>()
                .eq(com.app.entity.Outbound::getCodeName, codeName)
        );
        Map<String, Integer> result = new HashMap<>();
        result.put("inventory", invCount);
        result.put("outbound", obCount);
        return result;
    }
}
