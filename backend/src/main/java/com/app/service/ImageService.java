package com.app.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.app.common.BusinessException;
import com.app.entity.Gallery;
import com.app.entity.Image;
import com.app.entity.SampleThumbnail;
import com.app.entity.Sample;
import com.app.mapper.GalleryMapper;
import com.app.mapper.ImageMapper;
import com.app.mapper.SampleMapper;
import com.app.mapper.SampleThumbnailMapper;
import com.app.util.ImageHashUtil;
import com.app.util.ImageShardContext;
import com.app.util.FeatureExtractor;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    @Value("${app.upload.image-path}")
    private String imagePath;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailPath;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private GalleryMapper galleryMapper;

    @Autowired
    private SampleThumbnailMapper sampleThumbnailMapper;

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSampleCode(Long sampleId) {
        if (sampleId == null) return null;
        Sample s = sampleMapper.selectById(sampleId);
        if (s == null) {
            log.error("[ImageService] Sample not found for id: {}", sampleId);
            return null;
        }
        String code = s.getSampleCode();
        return code;
    }

    private static final String SHARD_HEX = "0123456789abcdef";

    private final ExecutorService searchExecutor = Executors.newFixedThreadPool(32);

    private void applyDhashBuckets(Image image, long dhash) {
        image.setDhash(dhash);
        int[] buckets = ImageHashUtil.computeBuckets(dhash);
        image.setDhBucket0(buckets[0]);
        image.setDhBucket1(buckets[1]);
        image.setDhBucket2(buckets[2]);
        image.setDhBucket3(buckets[3]);
    }

    @Transactional
    public Image upload(MultipartFile file, Long galleryId, Long sampleId, String description, String tags) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "\u6587\u4ef6\u4e0d\u80fd\u4e3a\u7a7a");
        }

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        if (!isImage(ext)) {
            throw new BusinessException(400, "\u4e0d\u652f\u6301\u7684\u6587\u4ef6\u7c7b\u578b: " + ext);
        }

        try {
            byte[] fileBytes = file.getBytes();

            boolean compressed = false;
            if (fileBytes.length > 1024 * 1024) {
                try {
                    byte[] compressedBytes = compressImage(fileBytes);
                    if (compressedBytes.length < fileBytes.length) {
                        fileBytes = compressedBytes;
                        ext = "jpg";
                        compressed = true;
                    }
                } catch (Exception ignored) {}
            }

            String hash = DigestUtil.sha256Hex(fileBytes);
            String hashPrefix = hash.substring(0, 2).toLowerCase();

            ImageShardContext.setHashPrefix(hashPrefix);
            try {
                LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Image::getHash, hash);
                wrapper.last("LIMIT 1");
                Image existing = imageMapper.selectOne(wrapper);
                if (existing != null) {
                    String existFilePath = existing.getFilePath();

                    String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    String hashDir = hash.substring(0, 2) + "/" + hash.substring(2, 4);
                    String codePrefix = getSampleCode(sampleId);
                    if (codePrefix != null) {
                        String newStoreName = codePrefix + "_" + hash + "." + ext;
                        String newRelativePath = dateDir + "/" + hashDir + "/" + newStoreName;
                        Path newFullPath = Paths.get(imagePath, newRelativePath);
                        Path existFullPath = Paths.get(imagePath, existFilePath);
                        if (!newFullPath.toFile().exists()) {
                            newFullPath.getParent().toFile().mkdirs();
                            try {
                                java.nio.file.Files.copy(existFullPath, newFullPath);
                            } catch (java.io.IOException e) {
                                log.error("[ImageService] Copy failed: {}", e.getMessage());
                            }
                        }
                        if (!newFullPath.toFile().exists()) {
                            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(newFullPath.toFile())) {
                                fos.write(fileBytes);
                                fos.flush();
                            }
                        }
                        if (newFullPath.toFile().exists()) {
                            existFilePath = newRelativePath;
                        }
                    }

                    String thumbDir = hash.substring(0, 2) + "/" + hash.substring(2, 4);

                    BufferedImage bi = ImageIO.read(new ByteArrayInputStream(fileBytes));
                    int tw = Math.min(bi.getWidth(), 300);
                    int th = (int) ((double) bi.getHeight() / bi.getWidth() * tw);
                    if (th > 300) { th = 300; tw = (int) ((double) bi.getWidth() / bi.getHeight() * th); }
                    BufferedImage thumb = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
                    Graphics2D tg = thumb.createGraphics();
                    tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    tg.drawImage(bi, 0, 0, tw, th, null);
                    tg.dispose();

                    ByteArrayOutputStream tbos = new ByteArrayOutputStream();
                    ImageIO.write(thumb, "jpg", tbos);
                    byte[] thumbBytes = tbos.toByteArray();
                    String thumbHash = DigestUtil.md5Hex(thumbBytes);
                    String thumbName = thumbHash + "_" + System.currentTimeMillis() + ".jpg";
                    String thumbRelPath = thumbDir + "/" + thumbName;
                    Path thumbFullPath = Paths.get(thumbnailPath, thumbRelPath);
                    thumbFullPath.getParent().toFile().mkdirs();
                    java.io.FileOutputStream thumbFos = new java.io.FileOutputStream(thumbFullPath.toFile());
                    thumbFos.write(thumbBytes);
                    thumbFos.flush();
                    thumbFos.close();

                    Image newImage = new Image();
                    newImage.setGalleryId(galleryId);
                    newImage.setSampleId(sampleId);
                    newImage.setFileName(originalName);
                    newImage.setFilePath(existFilePath);
                    newImage.setThumbnailPath(thumbRelPath);
                    newImage.setFileSize((long) fileBytes.length);
                    newImage.setFileType(ext);
                    newImage.setWidth(bi.getWidth());
                    newImage.setHeight(bi.getHeight());
                    newImage.setHash(hash);
                    newImage.setDescription(description);
                    newImage.setTags(tags);
                    newImage.setSortOrder(0);
                    applyDhashBuckets(newImage, ImageHashUtil.computeDHashFromBytes(fileBytes));
                    newImage.setFeatureVector(FeatureExtractor.toBytes(
                            FeatureExtractor.extract(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(fileBytes)))));
                    Long userId = UserContext.getUserId();
                    newImage.setCreateBy(userId);

                    imageMapper.insert(newImage);

                    if (sampleId != null) {
                        syncSampleThumbnail(sampleId, hash);
                    }

                    return newImage;
                }
            } finally {
                ImageShardContext.clear();
            }

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String hashDir = hash.substring(0, 2) + "/" + hash.substring(2, 4);
            String codePrefix = sampleId != null ? getSampleCode(sampleId) : null;
            String storeName = (codePrefix != null ? codePrefix + "_" : "") + hash + "." + ext;

            String relativePath = dateDir + "/" + hashDir + "/" + storeName;
            Path fullPath = Paths.get(imagePath, relativePath);
            Path parentDir = fullPath.getParent();

            java.io.File parentFile = parentDir.toFile();
            if (!parentFile.exists()) {
                boolean created = parentFile.mkdirs();
                log.info("[ImageService] mkdirs for {} returned {}, exists={}", parentFile, created, parentFile.exists());
            }

            java.io.File targetFile = fullPath.toFile();
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                fos.write(fileBytes);
                fos.flush();
                log.info("[ImageService] File written via FileOutputStream: {}", targetFile);
            }

            int width = 0, height = 0;
            try {
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(fileBytes));
                if (bi != null) { width = bi.getWidth(); height = bi.getHeight(); }
            } catch (Exception ignored) {}

            String thumbnailRelPath = null;
            try {
                thumbnailRelPath = generateThumbnail(fileBytes, hash, ext, width, height);
            } catch (Exception ignored) {}

            Image image = new Image();
            image.setGalleryId(galleryId);
            image.setSampleId(sampleId);
            image.setFileName(originalName);
            image.setFilePath(relativePath);
            image.setThumbnailPath(thumbnailRelPath);
            image.setFileSize((long) fileBytes.length);
            image.setFileType(ext);
            image.setWidth(width);
            image.setHeight(height);
            image.setHash(hash);
            image.setDescription(description);
            image.setTags(tags);
            image.setSortOrder(0);
            applyDhashBuckets(image, ImageHashUtil.computeDHashFromBytes(fileBytes));
            image.setFeatureVector(FeatureExtractor.toBytes(
                    FeatureExtractor.extract(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(fileBytes)))));
            image.setCreateBy(UserContext.getUserId());

            ImageShardContext.setHashPrefix(hashPrefix);
            try {
                imageMapper.insert(image);
            } finally {
                ImageShardContext.clear();
            }

            if (galleryId != null) {
                Gallery gallery = galleryMapper.selectById(galleryId);
                if (gallery != null) {
                    gallery.setImageCount(gallery.getImageCount() + 1);
                    galleryMapper.updateById(gallery);
                }
            }

            if (sampleId != null) {
                syncSampleThumbnail(sampleId, hash);
            }

            return image;
        } catch (BusinessException e) { throw e; }
        catch (Exception e) {
            System.err.println("[ImageService] 上传异常详情: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException(500, "\u4e0a\u4f20\u5931\u8d25: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    @Transactional
    public List<Image> uploadBatch(MultipartFile[] files, Long galleryId) {
        List<Image> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(upload(file, galleryId, null, null, null));
        }
        return results;
    }

    public String getImageExtByHash(String hash) {
        String hashPrefix = hash.substring(0, 2).toLowerCase();
        ImageShardContext.setHashPrefix(hashPrefix);
        try {
            LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Image::getHash, hash).select(Image::getFilePath);
            Image image = imageMapper.selectOne(wrapper);
            if (image != null && image.getFilePath() != null) {
                String name = image.getFilePath();
                int dot = name.lastIndexOf('.');
                return dot > 0 ? name.substring(dot + 1) : "jpg";
            }
            return "jpg";
        } finally {
            ImageShardContext.clear();
        }
    }

    public byte[] loadImage(String hash) {
        String hashPrefix = hash.substring(0, 2).toLowerCase();
        ImageShardContext.setHashPrefix(hashPrefix);
        try {
            LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Image::getHash, hash);
            Image image = imageMapper.selectOne(wrapper);
            if (image == null) throw new BusinessException(404, "\u56fe\u7247\u4e0d\u5b58\u5728");

            Path fullPath = Paths.get(imagePath, image.getFilePath());
            return Files.readAllBytes(fullPath);
        } catch (BusinessException e) { throw e; }
        catch (IOException e) { throw new BusinessException(500, "\u8bfb\u53d6\u56fe\u7247\u5931\u8d25"); }
        finally { ImageShardContext.clear(); }
    }

    public byte[] loadImageById(Long id) {
        Image image = findImageById(id);
        if (image == null) throw new BusinessException(404, "\u56fe\u7247\u4e0d\u5b58\u5728");

        try {
            Path fullPath = Paths.get(imagePath, image.getFilePath());
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            throw new BusinessException(500, "\u8bfb\u53d6\u56fe\u7247\u5931\u8d25");
        }
    }

    public Image loadImageInfo(Long id) {
        Image image = findImageById(id);
        if (image == null) throw new BusinessException(404, "\u56fe\u7247\u4e0d\u5b58\u5728");
        return image;
    }

    public byte[] loadThumbnail(Long id) {
        Image image = findImageById(id);
        if (image == null) return null;

        if (image.getThumbnailPath() != null) {
            try {
                Path fullPath = Paths.get(thumbnailPath, image.getThumbnailPath());
                return Files.readAllBytes(fullPath);
            } catch (IOException ignored) {}
        }
        return loadImageById(id);
    }

    public List<Image> listByGallery(Long galleryId) {
        List<Image> allImages = new ArrayList<>();
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Image::getGalleryId, galleryId)
                            .orderByAsc(Image::getSortOrder)
                            .orderByDesc(Image::getCreateTime);
                    allImages.addAll(imageMapper.selectList(wrapper));
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        allImages.sort((a, b) -> {
            int so1 = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int so2 = b.getSortOrder() != null ? b.getSortOrder() : 0;
            if (so1 != so2) return Integer.compare(so1, so2);
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        return allImages;
    }

    public List<Image> listBySampleId(Long sampleId) {
        if (sampleId == null) return new ArrayList<>();
        List<Image> allImages = new ArrayList<>();
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Image::getSampleId, sampleId)
                            .orderByAsc(Image::getSortOrder)
                            .orderByDesc(Image::getCreateTime);
                    allImages.addAll(imageMapper.selectList(wrapper));
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        allImages.sort((a, b) -> {
            int so1 = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int so2 = b.getSortOrder() != null ? b.getSortOrder() : 0;
            if (so1 != so2) return Integer.compare(so1, so2);
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        return allImages;
    }

    public List<Map<String, Object>> searchByImage(MultipartFile file, int maxDistance) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException(400, "无法读取上传的图片");
        }

        java.awt.image.BufferedImage image;
        try {
            image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            throw new BusinessException(400, "无法解析图片");
        }
        if (image == null) {
            throw new BusinessException(400, "无法解析图片");
        }

        long queryDHash = ImageHashUtil.computeDHashFromImage(image);
        boolean isScreenshot = ImageHashUtil.isScreenshotLike(image);

        java.awt.image.BufferedImage featureImage = image;
        long croppedDHash = 0;
        List<Long> croppedMultiHashes = null;
        boolean wasCropped = false;

        java.awt.image.BufferedImage cropped = FeatureExtractor.autoCropScreenshot(image);
        if (cropped != image) {
            featureImage = cropped;
            croppedDHash = ImageHashUtil.computeDHashFromImage(cropped);
            wasCropped = true;
            if (isScreenshot) {
                croppedMultiHashes = ImageHashUtil.computeMultiCropDHashes(cropped);
            }
        }

        float[] queryFeature = FeatureExtractor.extract(featureImage);

        float[] originalFeature = null;
        if (isScreenshot && wasCropped) {
            originalFeature = FeatureExtractor.extract(image);
        }

        if (queryDHash == 0 && queryFeature.length == 0) {
            throw new BusinessException(400, "无法计算图片哈希值");
        }

        Map<String, Map<String, Object>> merged = new ConcurrentHashMap<>();

        List<Callable<Void>> allTasks = new ArrayList<>();

        long primaryDHash = (croppedDHash != 0) ? croppedDHash : queryDHash;
        addFeatureSearchTasks(allTasks, primaryDHash, queryFeature, maxDistance, merged, isScreenshot);

        if (croppedMultiHashes != null) {
            for (long ch : croppedMultiHashes) {
                if (ch != 0 && ch != primaryDHash) {
                    addFeatureSearchTasks(allTasks, ch, queryFeature, maxDistance, merged, isScreenshot);
                }
            }
        }

        if (queryDHash != 0 && queryDHash != primaryDHash) {
            addFeatureSearchTasks(allTasks, queryDHash, queryFeature, maxDistance + 10, merged, isScreenshot);
        }

        if (isScreenshot) {
            List<Long> fullMultiHashes = ImageHashUtil.computeMultiCropDHashes(image);
            if (fullMultiHashes != null) {
                for (long fh : fullMultiHashes) {
                    if (fh != 0 && fh != primaryDHash) {
                        addFeatureSearchTasks(allTasks, fh, queryFeature, maxDistance + 8, merged, isScreenshot);
                    }
                }
            }
        }

        try {
            searchExecutor.invokeAll(allTasks, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (merged.size() < 5 || isScreenshot) {
            List<Callable<Void>> fallbackTasks = new ArrayList<>();
            addFallbackSearchTasks(fallbackTasks, queryFeature, maxDistance + 15, primaryDHash, merged, isScreenshot);
            if (wasCropped && croppedDHash != 0) {
                addFallbackSearchTasks(fallbackTasks, queryFeature, maxDistance + 20, croppedDHash, merged, isScreenshot);
            }
            if (originalFeature != null) {
                addFallbackSearchTasks(fallbackTasks, originalFeature, maxDistance + 25, queryDHash, merged, isScreenshot);
            }
            try {
                searchExecutor.invokeAll(fallbackTasks, 45, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<Map<String, Object>> results = new ArrayList<>(merged.values());
        results.sort((a, b) -> {
            double s1 = ((Number) a.getOrDefault("similarity", 0.0)).doubleValue();
            double s2 = ((Number) b.getOrDefault("similarity", 0.0)).doubleValue();
            return Double.compare(s2, s1);
        });

        if (results.size() > 500) {
            return results.subList(0, 500);
        }
        return results;
    }

    private void addFeatureSearchTasks(List<Callable<Void>> allTasks, long queryDHash,
                                        float[] queryFeature, int maxDistance,
                                        Map<String, Map<String, Object>> merged, boolean isScreenshot) {
        if (queryDHash == 0) return;
        int[] buckets = ImageHashUtil.computeBuckets(queryDHash);
        String bucketWhere = ImageHashUtil.buildBucketWhereClause(buckets);

        double dhashWeight = isScreenshot ? 0.05 : 0.1;
        double featureWeight = 1.0 - dhashWeight;

        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                final String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                allTasks.add(() -> {
                    ImageShardContext.setHashPrefix(prefix);
                    try {
                        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                        wrapper.isNotNull(Image::getDhash)
                                .isNotNull(Image::getFeatureVector)
                                .apply(bucketWhere)
                                .select(Image::getId, Image::getSampleId, Image::getFilePath,
                                        Image::getThumbnailPath, Image::getFileName,
                                        Image::getDhash, Image::getFeatureVector);
                        List<Image> images = imageMapper.selectList(wrapper);
                        for (Image img : images) {
                            byte[] fvBytes = img.getFeatureVector();
                            if (fvBytes == null || fvBytes.length == 0) continue;

                            Long imgDhash = img.getDhash();
                            int dDist = (imgDhash != null) ? ImageHashUtil.hammingDistance(queryDHash, imgDhash) : 99;
                            if (dDist > maxDistance) continue;

                            float[] dbFeature = FeatureExtractor.fromBytes(fvBytes);
                            double sim = FeatureExtractor.cosineSimilarity(queryFeature, dbFeature);
                            double dhashScore = Math.max(0, 1.0 - dDist / 64.0);
                            double score = sim * featureWeight + dhashScore * dhashWeight;

                            Long imgId = img.getId();
                            String mergeKey = prefix + "_" + imgId;
                            Map<String, Object> existing = merged.get(mergeKey);
                            if (existing != null && ((Number) existing.getOrDefault("similarity", 0.0)).doubleValue() >= score) {
                                continue;
                            }

                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("imageId", imgId);
                            item.put("sampleId", img.getSampleId());
                            item.put("thumbnailPath", img.getThumbnailPath());
                            item.put("filePath", img.getFilePath());
                            item.put("fileName", img.getFileName());
                            item.put("distance", dDist);
                            item.put("similarity", score);

                            if (img.getSampleId() != null) {
                                Sample s = sampleMapper.selectById(img.getSampleId());
                                if (s != null) {
                                    item.put("sampleCode", s.getSampleCode());
                                    item.put("sampleName", s.getSampleName());
                                    item.put("category", s.getCategory());
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }

                            merged.put(mergeKey, item);
                        }
                    } finally {
                        ImageShardContext.clear();
                    }
                    return null;
                });
            }
        }
    }

    private void addFallbackSearchTasks(List<Callable<Void>> allTasks, float[] queryFeature,
                                         int maxDistance, long queryDHash,
                                         Map<String, Map<String, Object>> merged,
                                         boolean isScreenshot) {
        if (queryFeature == null || queryFeature.length == 0) return;

        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                final String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                allTasks.add(() -> {
                    ImageShardContext.setHashPrefix(prefix);
                    try {
                        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                        wrapper.select(Image::getId, Image::getSampleId, Image::getFilePath,
                                        Image::getThumbnailPath, Image::getFileName,
                                        Image::getDhash, Image::getFeatureVector);
                        List<Image> images = imageMapper.selectList(wrapper);
                        for (Image img : images) {
                            byte[] fvBytes = img.getFeatureVector();
                            if (fvBytes == null || fvBytes.length == 0) {
                                java.awt.image.BufferedImage imgFile = null;
                                try {
                                    java.nio.file.Path fullPath = java.nio.file.Paths.get(imagePath, img.getFilePath());
                                    if (java.nio.file.Files.exists(fullPath)) {
                                        imgFile = javax.imageio.ImageIO.read(fullPath.toFile());
                                    }
                                } catch (Exception ee) {
                                    continue;
                                }
                                if (imgFile == null) continue;
                                float[] feat = FeatureExtractor.extract(imgFile);
                                fvBytes = FeatureExtractor.toBytes(feat);
                            }

                            float[] dbFeature = FeatureExtractor.fromBytes(fvBytes);
                            double sim = FeatureExtractor.cosineSimilarity(queryFeature, dbFeature);
                            if (sim < 0.55) continue;

                            Long imgDhash = img.getDhash();
                            int dDist = 99;
                            double dhashScore = 0.0;
                            if (imgDhash != null && imgDhash != 0 && queryDHash != 0) {
                                dDist = Long.bitCount(queryDHash ^ imgDhash);
                                dhashScore = Math.max(0, 1.0 - dDist / 64.0);
                            }
                            double score = sim * 0.95 + dhashScore * 0.05;

                            Long imgId = img.getId();
                            String mergeKey = prefix + "_" + imgId;
                            Map<String, Object> existing = merged.get(mergeKey);
                            if (existing != null && ((Number) existing.getOrDefault("similarity", 0.0)).doubleValue() >= score) {
                                continue;
                            }

                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("imageId", imgId);
                            item.put("sampleId", img.getSampleId());
                            item.put("thumbnailPath", img.getThumbnailPath());
                            item.put("filePath", img.getFilePath());
                            item.put("fileName", img.getFileName());
                            item.put("distance", dDist);
                            item.put("similarity", score);

                            if (img.getSampleId() != null) {
                                Sample s = sampleMapper.selectById(img.getSampleId());
                                if (s != null) {
                                    item.put("sampleCode", s.getSampleCode());
                                    item.put("sampleName", s.getSampleName());
                                    item.put("category", s.getCategory());
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }

                            merged.put(mergeKey, item);
                        }
                    } finally {
                        ImageShardContext.clear();
                    }
                    return null;
                });
            }
        }
    }

    public Map<String, Object> backfillDhash() {
        int total = 0;
        int updated = 0;
        int skipped = 0;
        int errors = 0;

        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.isNull(Image::getDhash)
                            .select(Image::getId, Image::getFilePath);
                    List<Image> images = imageMapper.selectList(wrapper);
                    for (Image img : images) {
                        total++;
                        try {
                            Path fullPath = Paths.get(imagePath, img.getFilePath());
                            if (!Files.exists(fullPath)) {
                                skipped++;
                                continue;
                            }
                            long dhash = ImageHashUtil.computeDHash(fullPath);
                            if (dhash == 0) {
                                errors++;
                                continue;
                            }
                            int[] buckets = ImageHashUtil.computeBuckets(dhash);
                            java.awt.image.BufferedImage buf = javax.imageio.ImageIO.read(fullPath.toFile());
                            byte[] featureBytes = (buf != null) ? FeatureExtractor.toBytes(FeatureExtractor.extract(buf)) : null;
                            String tableName = "images_" + prefix;
                            jdbcTemplate.update(
                                    "UPDATE " + tableName + " SET dhash = ?, dh_bucket0 = ?, dh_bucket1 = ?, dh_bucket2 = ?, dh_bucket3 = ?, feature_vector = ? WHERE id = ?",
                                    dhash, buckets[0], buckets[1], buckets[2], buckets[3], featureBytes, img.getId()
                            );
                            updated++;
                        } catch (Exception e) {
                            errors++;
                        }
                    }
                } finally {
                    ImageShardContext.clear();
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("updated", updated);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    public Map<String, Object> backfillBuckets() {
        int total = 0;
        int updated = 0;
        int skipped = 0;

        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.isNotNull(Image::getDhash)
                            .isNull(Image::getDhBucket0)
                            .select(Image::getId, Image::getDhash);
                    List<Image> images = imageMapper.selectList(wrapper);
                    for (Image img : images) {
                        total++;
                        Long dhash = img.getDhash();
                        if (dhash == null || dhash == 0) {
                            skipped++;
                            continue;
                        }
                        int[] buckets = ImageHashUtil.computeBuckets(dhash);
                        String tableName = "images_" + prefix;
                        jdbcTemplate.update(
                                "UPDATE " + tableName + " SET dh_bucket0 = ?, dh_bucket1 = ?, dh_bucket2 = ?, dh_bucket3 = ? WHERE id = ?",
                                buckets[0], buckets[1], buckets[2], buckets[3], img.getId()
                        );
                        updated++;
                    }
                } finally {
                    ImageShardContext.clear();
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("updated", updated);
        result.put("skipped", skipped);
        return result;
    }

    public Map<String, Object> resetFeatures() {
        int nulled = 0;
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                String tableName = "images_" + prefix;
                ImageShardContext.setHashPrefix(prefix);
                try {
                    int n = jdbcTemplate.update("UPDATE " + tableName + " SET feature_vector = NULL WHERE feature_vector IS NOT NULL");
                    nulled += n;
                } catch (Exception e) {
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nulled", nulled);
        return result;
    }

    public Map<String, Object> backfillFeatures() {
        int total = 0;
        int updated = 0;
        int skipped = 0;

        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.isNotNull(Image::getDhash)
                            .isNull(Image::getFeatureVector)
                            .select(Image::getId, Image::getFilePath);
                    List<Image> images = imageMapper.selectList(wrapper);
                    for (Image img : images) {
                        total++;
                        try {
                            Path fullPath = Paths.get(imagePath, img.getFilePath());
                            if (!Files.exists(fullPath)) { skipped++; continue; }
                            java.awt.image.BufferedImage buf = javax.imageio.ImageIO.read(fullPath.toFile());
                            if (buf == null) { skipped++; continue; }
                            byte[] featureBytes = FeatureExtractor.toBytes(FeatureExtractor.extract(buf));
                            String tableName = "images_" + prefix;
                            jdbcTemplate.update(
                                    "UPDATE " + tableName + " SET feature_vector = ? WHERE id = ?",
                                    featureBytes, img.getId()
                            );
                            updated++;
                        } catch (Exception e) {
                            skipped++;
                        }
                    }
                } catch (Exception e) {
                } finally {
                    ImageShardContext.clear();
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("updated", updated);
        result.put("skipped", skipped);
        return result;
    }

    public boolean hasDhashData() {
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.isNotNull(Image::getDhash);
                    wrapper.last("LIMIT 1");
                    if (imageMapper.selectCount(wrapper) > 0) {
                        return true;
                    }
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        return false;
    }

    public Image findFirstBySampleId(Long sampleId) {
        if (sampleId == null) return null;
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Image::getSampleId, sampleId)
                            .orderByAsc(Image::getSortOrder)
                            .last("LIMIT 1");
                    List<Image> list = imageMapper.selectList(wrapper);
                    if (list != null && !list.isEmpty()) {
                        return list.get(0);
                    }
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        return null;
    }



    public Map<Long, Map<String, Object>> findFirstImageIdAndThumbBySampleIds(List<Long> sampleIds) {
        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();
        if (sampleIds == null || sampleIds.isEmpty()) return result;

        int totalNeeded = sampleIds.size();
        int found = 0;

        for (int i = 0; i < SHARD_HEX.length() && found < totalNeeded; i++) {
            for (int j = 0; j < SHARD_HEX.length() && found < totalNeeded; j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(Image::getSampleId, sampleIds)
                            .select(Image::getId, Image::getSampleId, Image::getThumbnailPath, Image::getHash, Image::getFileName)
                            .orderByAsc(Image::getSortOrder);
                    List<Image> list = imageMapper.selectList(wrapper);
                    for (Image img : list) {
                        Long sid = img.getSampleId();
                        if (!result.containsKey(sid)) {
                            Map<String, Object> info = new HashMap<>();
                            info.put("id", img.getId());
                            info.put("thumbnailPath", img.getThumbnailPath());
                            info.put("hash", img.getHash());
                            info.put("fileName", img.getFileName());
                            result.put(sid, info);
                            found++;
                        }
                    }
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        for (Long sid : sampleIds) {
            result.putIfAbsent(sid, null);
        }
        return result;
    }

    @Transactional
    public Image replace(Long id, MultipartFile file) {
        Image existing = findImageById(id);
        if (existing == null) {
            throw new BusinessException(404, "\u56fe\u7247\u4e0d\u5b58\u5728");
        }

        if (file.isEmpty()) {
            throw new BusinessException(400, "\u6587\u4ef6\u4e0d\u80fd\u4e3a\u7a7a");
        }

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        if (!isImage(ext)) {
            throw new BusinessException(400, "\u4e0d\u652f\u6301\u7684\u6587\u4ef6\u7c7b\u578b: " + ext);
        }

        try {
            byte[] fileBytes = file.getBytes();

            if (fileBytes.length > 1024 * 1024) {
                try {
                    byte[] compressedBytes = compressImage(fileBytes);
                    if (compressedBytes.length < fileBytes.length) {
                        fileBytes = compressedBytes;
                        ext = "jpg";
                    }
                } catch (Exception ignored) {}
            }

            String hash = DigestUtil.sha256Hex(fileBytes);

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String hashDir = hash.substring(0, 2) + "/" + hash.substring(2, 4);
            String codePrefix = getSampleCode(existing.getSampleId());
            String storeName = (codePrefix != null ? codePrefix + "_" : "") + hash + "." + ext;

            String relativePath = dateDir + "/" + hashDir + "/" + storeName;
            Path fullPath = Paths.get(imagePath, relativePath);
            fullPath.getParent().toFile().mkdirs();
            java.io.FileOutputStream replaceFos = new java.io.FileOutputStream(fullPath.toFile());
            replaceFos.write(fileBytes);
            replaceFos.flush();
            replaceFos.close();

            int width = 0, height = 0;
            try {
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(fileBytes));
                if (bi != null) { width = bi.getWidth(); height = bi.getHeight(); }
            } catch (Exception ignored) {}

            String thumbnailRelPath = null;
            try {
                thumbnailRelPath = generateThumbnail(fileBytes, hash, ext, width, height);
            } catch (Exception ignored) {}

            String oldHashForShard = existing.getHash();
            existing.setFileName(originalName);
            existing.setFilePath(relativePath);
            existing.setThumbnailPath(thumbnailRelPath);
            existing.setFileSize((long) fileBytes.length);
            existing.setFileType(ext);
            existing.setWidth(width);
            existing.setHeight(height);
            existing.setHash(hash);

            if (oldHashForShard != null && !oldHashForShard.isEmpty()) {
                ImageShardContext.setHashPrefix(oldHashForShard.substring(0, 2).toLowerCase());
                try {
                    imageMapper.deleteById(id);
                } finally {
                    ImageShardContext.clear();
                }
            }
            String newHashPrefix = hash.substring(0, 2).toLowerCase();
            ImageShardContext.setHashPrefix(newHashPrefix);
            try {
                imageMapper.insert(existing);
            } finally {
                ImageShardContext.clear();
            }

            if (existing.getSampleId() != null) {
                syncSampleThumbnail(existing.getSampleId(), hash);
            }

            return existing;
        } catch (BusinessException e) { throw e; }
        catch (Exception e) { throw new BusinessException(500, "\u66ff\u6362\u5931\u8d25: " + e.getMessage()); }
    }

    @Transactional
    public void delete(Long id) {
        Image image = findImageById(id);
        if (image == null) return;
        doDelete(image);
    }

    @Transactional
    public void delete(Long id, String hash) {
        if (id == null || hash == null || hash.length() < 2) return;
        Image image = findImageByIdAndHash(id, hash);
        if (image == null) return;
        doDelete(image);
    }

    private void doDelete(Image image) {
        if (image.getGalleryId() != null) {
            Gallery gallery = galleryMapper.selectById(image.getGalleryId());
            if (gallery != null && gallery.getImageCount() > 0) {
                gallery.setImageCount(gallery.getImageCount() - 1);
                galleryMapper.updateById(gallery);
            }
        }

        String hashPrefix = image.getHash() != null ? image.getHash().substring(0, 2).toLowerCase() : null;
        if (hashPrefix != null) {
            ImageShardContext.setHashPrefix(hashPrefix);
            try {
                imageMapper.deleteById(image.getId());
            } finally {
                ImageShardContext.clear();
            }
        }

        if (image.getSampleId() != null) {
            syncSampleThumbnail(image.getSampleId(), image.getHash());
        }
    }

    @Transactional
    public void deleteBySampleId(Long sampleId) {
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> qw = new LambdaQueryWrapper<>();
                    qw.eq(Image::getSampleId, sampleId);
                    imageMapper.delete(qw);
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        SampleThumbnail st = sampleThumbnailMapper.selectById(sampleId);
        if (st != null) {
            sampleThumbnailMapper.deleteById(sampleId);
        }
    }

    @Transactional
    public void deleteBatch(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) return;
        Long sampleId = null;
        for (Map<String, Object> item : items) {
            Long id = item.get("id") != null ? ((Number) item.get("id")).longValue() : null;
            String hash = (String) item.get("hash");
            if (id != null && hash != null && hash.length() >= 2) {
                if (sampleId == null) {
                    Image img = findImageByIdAndHash(id, hash);
                    if (img != null) sampleId = img.getSampleId();
                }
                delete(id, hash);
            }
        }
        if (sampleId != null) {
            syncSampleThumbnailAllShards(sampleId);
        }
    }

    public void reorder(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) return;
        log.info("[ImageService] reorder called with {} items", items.size());
        Long sampleId = null;
        String sampleHash = null;
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Long id = item.get("id") != null ? ((Number) item.get("id")).longValue() : null;
            String hash = (String) item.get("hash");
            if (id == null || hash == null || hash.length() < 2) {
                log.warn("[ImageService] reorder: invalid item at index {}, id={}, hash={}", i, id, hash);
                continue;
            }
            String prefix = hash.substring(0, 2).toLowerCase();
            String tableName = "images_" + prefix;
            int rows = jdbcTemplate.update(
                "UPDATE " + tableName + " SET sort_order=? WHERE id=? AND deleted=0", i, id);
            if (rows > 0) {
                log.info("[ImageService] reorder: {} set sort_order={} for id={}, hash={}", tableName, i, id, hash);
                if (sampleId == null || sampleHash == null) {
                    List<Map<String, Object>> row = jdbcTemplate.queryForList(
                        "SELECT sample_id, hash FROM " + tableName + " WHERE id=? AND deleted=0", id);
                    if (!row.isEmpty()) {
                        sampleId = ((Number) row.get(0).get("sample_id")).longValue();
                        sampleHash = (String) row.get(0).get("hash");
                    }
                }
            } else {
                log.warn("[ImageService] reorder: update affected 0 rows for id={}, hash={}, table={}", id, hash, tableName);
            }
        }
        log.info("[ImageService] reorder complete, sampleId={}, sampleHash={}", sampleId, sampleHash);
        if (sampleId != null && sampleHash != null) {
            syncSampleThumbnail(sampleId, sampleHash);
        }
    }

    public boolean setPosition(Long imageId, int position) {
        if (imageId == null) return false;
        log.info("[ImageService] setPosition: imageId={}, position={}", imageId, position);
        for (int si = 0; si < SHARD_HEX.length(); si++) {
            for (int sj = 0; sj < SHARD_HEX.length(); sj++) {
                String prefix = "" + SHARD_HEX.charAt(si) + SHARD_HEX.charAt(sj);
                String tableName = "images_" + prefix;
                int rows = jdbcTemplate.update(
                    "UPDATE " + tableName + " SET sort_order=? WHERE id=? AND deleted=0", position, imageId);
                if (rows > 0) {
                    log.info("[ImageService] setPosition: updated {} sort_order={}", tableName, position);
                    if (position == 0) {
                        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                            "SELECT sample_id, hash FROM " + tableName + " WHERE id=? AND deleted=0", imageId);
                        if (!result.isEmpty()) {
                            Long sampleId = ((Number) result.get(0).get("sample_id")).longValue();
                            String hash = (String) result.get(0).get("hash");
                            syncSampleThumbnail(sampleId, hash);
                        }
                    }
                    return true;
                }
            }
        }
        log.warn("[ImageService] setPosition: image not found for id={}", imageId);
        return false;
    }

    public void swapSortOrder(Long id1, Long id2) {
        if (id1 == null || id2 == null || id1.equals(id2)) return;
        log.info("[ImageService] swapSortOrder: {} <-> {}", id1, id2);
        Image img1 = findImageById(id1);
        Image img2 = findImageById(id2);
        if (img1 == null || img2 == null) {
            log.warn("[ImageService] swapSortOrder: image not found, id1={}, id2={}", id1, id2);
            return;
        }
        int so1 = img1.getSortOrder() != null ? img1.getSortOrder() : 0;
        int so2 = img2.getSortOrder() != null ? img2.getSortOrder() : 0;
        String p1 = img1.getHash().substring(0, 2).toLowerCase();
        String p2 = img2.getHash().substring(0, 2).toLowerCase();
        jdbcTemplate.update("UPDATE images_" + p1 + " SET sort_order=? WHERE id=? AND deleted=0", so2, id1);
        jdbcTemplate.update("UPDATE images_" + p2 + " SET sort_order=? WHERE id=? AND deleted=0", so1, id2);
        log.info("[ImageService] swapSortOrder: done, id1={} sort={}->{}, id2={} sort={}->{}", id1, so1, so2, id2, so2, so1);
        Long sampleId = img1.getSampleId();
        if (sampleId != null) {
            syncSampleThumbnail(sampleId, so2 == 0 ? img1.getHash() : img2.getHash());
        }
    }

    private void syncSampleThumbnail(Long sampleId, String hash) {
        if (hash == null || hash.length() < 2) return;
        String hashPrefix = hash.substring(0, 2).toLowerCase();
        ImageShardContext.setHashPrefix(hashPrefix);
        try {
            LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Image::getSampleId, sampleId)
                    .orderByAsc(Image::getSortOrder)
                    .orderByDesc(Image::getCreateTime)
                    .last("LIMIT 1");
            Image firstImage = imageMapper.selectOne(wrapper);
            if (firstImage != null) {
                SampleThumbnail st = new SampleThumbnail();
                st.setSampleId(sampleId);
                st.setImageId(firstImage.getId());
                st.setThumbnail(firstImage.getThumbnailPath());
                st.setHash(firstImage.getHash());
                st.setFileName(firstImage.getFileName());
                SampleThumbnail existing = sampleThumbnailMapper.selectById(sampleId);
                if (existing != null) {
                    st.setCreateTime(existing.getCreateTime());
                    sampleThumbnailMapper.updateById(st);
                } else {
                    sampleThumbnailMapper.insert(st);
                }
            } else {
                LambdaQueryWrapper<SampleThumbnail> w = new LambdaQueryWrapper<>();
                w.eq(SampleThumbnail::getSampleId, sampleId);
                sampleThumbnailMapper.delete(w);
            }
        } finally {
            ImageShardContext.clear();
        }
    }

    private void syncSampleThumbnailAllShards(Long sampleId) {
        if (sampleId == null) return;
        List<Image> allImages = listBySampleId(sampleId);
        if (!allImages.isEmpty()) {
            Image firstImage = allImages.get(0);
            SampleThumbnail st = new SampleThumbnail();
            st.setSampleId(sampleId);
            st.setImageId(firstImage.getId());
            st.setThumbnail(firstImage.getThumbnailPath());
            st.setHash(firstImage.getHash());
            st.setFileName(firstImage.getFileName());
            SampleThumbnail existing = sampleThumbnailMapper.selectById(sampleId);
            if (existing != null) {
                st.setCreateTime(existing.getCreateTime());
                sampleThumbnailMapper.updateById(st);
            } else {
                sampleThumbnailMapper.insert(st);
            }
        } else {
            LambdaQueryWrapper<SampleThumbnail> w = new LambdaQueryWrapper<>();
            w.eq(SampleThumbnail::getSampleId, sampleId);
            sampleThumbnailMapper.delete(w);
        }
    }

    private Image findImageById(Long id) {
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    Image image = imageMapper.selectById(id);
                    if (image != null) return image;
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        return null;
    }

    private Image findImageByIdAndHash(Long id, String hash) {
        if (hash == null || hash.length() < 2) return null;
        String prefix = hash.substring(0, 2).toLowerCase();
        ImageShardContext.setHashPrefix(prefix);
        try {
            return imageMapper.selectById(id);
        } finally {
            ImageShardContext.clear();
        }
    }

    private String generateThumbnail(byte[] imageBytes, String hash, String ext, int origWidth, int origHeight) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (source == null) return null;

        int thumbWidth = 300, thumbHeight = 300;
        if (origWidth > 0 && origHeight > 0) {
            double ratio = Math.min((double) thumbWidth / origWidth, (double) thumbHeight / origHeight);
            thumbWidth = (int) (origWidth * ratio);
            thumbHeight = (int) (origHeight * ratio);
        }

        BufferedImage thumb = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, thumbWidth, thumbHeight, null);
        g.dispose();

        String relPath = hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash + "_thumb.jpg";
        Path fullPath = Paths.get(thumbnailPath, relPath);
        fullPath.getParent().toFile().mkdirs();

        ImageIO.write(thumb, "jpg", fullPath.toFile());

        return relPath;
    }

    private byte[] compressImage(byte[] imageBytes) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (source == null) return imageBytes;

        BufferedImage rgbImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, null);
        g.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) return imageBytes;
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float[] qualities = {0.7f, 0.5f, 0.3f};
        for (float q : qualities) {
            param.setCompressionQuality(q);
            bos.reset();
            ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgbImage, null, null), param);
            ios.flush();
            if (bos.size() <= 1024 * 1024) break;
        }
        writer.dispose();
        return bos.toByteArray();
    }

    private boolean isImage(String ext) {
        if (ext == null) return false;
        ext = ext.toLowerCase();
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext)
                || "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext);
    }
}
