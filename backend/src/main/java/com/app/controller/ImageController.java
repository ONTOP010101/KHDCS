package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.Image;
import com.app.util.DeepFeatureExtractor;
import com.app.service.ImageService;
import com.app.service.PgVectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired(required = false)
    private PgVectorService pgVectorService;

    @PostMapping("/upload")
    public Result<Image> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long galleryId,
            @RequestParam(required = false) Long sampleId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tags) {
        return Result.success("上传成功", imageService.upload(file, galleryId, sampleId, description, tags));
    }

    @PostMapping("/upload/batch")
    public Result<List<Image>> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) Long galleryId) {
        return Result.success("批量上传成功", imageService.uploadBatch(files, galleryId));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewById(@PathVariable Long id) {
        Image image = imageService.loadImageInfo(id);
        byte[] data = imageService.loadImageById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS).cachePublic());
        headers.setContentType(getMediaType(image.getFileType()));
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/view/hash/{hash}")
    public ResponseEntity<byte[]> viewByHash(@PathVariable String hash) {
        byte[] data = imageService.loadImage(hash);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS).cachePublic());
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<byte[]> thumbnail(@PathVariable Long id) {
        byte[] data = imageService.loadThumbnail(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS).cachePublic());
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/sample/{sampleId}")
    public Result<List<Image>> listBySample(@PathVariable Long sampleId) {
        return Result.success(imageService.listBySampleId(sampleId));
    }

    @GetMapping("/gallery/{galleryId}")
    public Result<List<Image>> listByGallery(@PathVariable Long galleryId) {
        return Result.success(imageService.listByGallery(galleryId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imageService.delete(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping("/sample/{sampleId}")
    public Result<Void> deleteBySampleId(@PathVariable Long sampleId) {
        imageService.deleteBySampleId(sampleId);
        return Result.ok("删除成功");
    }

    @GetMapping("/sample-images")
    public Result<java.util.Map<Long, java.util.Map<String, Object>>> sampleImages(@RequestParam("ids") String ids) {
        String[] parts = ids.split(",");
        java.util.List<Long> idList = new java.util.ArrayList<>();
        for (String p : parts) {
            try { idList.add(Long.parseLong(p.trim())); } catch (NumberFormatException ignored) {}
        }
        return Result.success(imageService.findFirstImageIdAndThumbBySampleIds(idList));
    }

    @PostMapping("/sample-images")
    public Result<java.util.Map<Long, java.util.Map<String, Object>>> sampleImagesPost(@RequestBody java.util.List<Long> ids) {
        return Result.success(imageService.findFirstImageIdAndThumbBySampleIds(ids));
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody List<Map<String, Object>> items) {
        imageService.deleteBatch(items);
        return Result.ok("批量删除成功");
    }

    @PostMapping("/reorder")
    public Result<Void> reorder(@RequestBody List<Map<String, Object>> items) {
        imageService.reorder(items);
        return Result.ok("排序成功");
    }

    @PostMapping("/set-position")
    public Result<Void> setPosition(@RequestBody Map<String, Object> body) {
        Long imageId = body.get("imageId") != null ? ((Number) body.get("imageId")).longValue() : null;
        int position = body.get("position") != null ? ((Number) body.get("position")).intValue() : -1;
        if (imageId == null || position < 0) {
            return Result.error("参数无效");
        }
        boolean ok = imageService.setPosition(imageId, position);
        return ok ? Result.ok("设置成功") : Result.error("图片未找到");
    }

    @PostMapping("/swap-sort")
    public Result<Void> swapSort(@RequestBody Map<String, Object> body) {
        Long id1 = body.get("id1") != null ? ((Number) body.get("id1")).longValue() : null;
        Long id2 = body.get("id2") != null ? ((Number) body.get("id2")).longValue() : null;
        if (id1 == null || id2 == null) return Result.error("参数无效");
        imageService.swapSortOrder(id1, id2);
        return Result.ok("交换成功");
    }

    @PostMapping("/search-by-image")
    public Result<java.util.List<java.util.Map<String, Object>>> searchByImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int maxDistance) {
        return Result.success(imageService.searchByImage(file, maxDistance));
    }

    @PostMapping("/backfill-dhash")
    public Result<Map<String, Object>> backfillDhash() {
        return Result.success(imageService.backfillDhash());
    }

    @GetMapping("/has-dhash")
    public Result<Boolean> hasDhash() {
        return Result.success(imageService.hasDhashData());
    }

    @PostMapping("/backfill-buckets")
    public Result<Map<String, Object>> backfillBuckets() {
        return Result.success(imageService.backfillBuckets());
    }

    @PostMapping("/reset-features")
    public Result<Map<String, Object>> resetFeatures() {
        return Result.success(imageService.resetFeatures());
    }

    @PostMapping("/backfill-features")
    public Result<Map<String, Object>> backfillFeatures() {
        return Result.success(imageService.backfillFeatures());
    }

    @PostMapping("/backfill-deep-features")
    public Result<Map<String, Object>> backfillDeepFeatures() {
        return Result.success(imageService.backfillDeepFeatures());
    }

    @PostMapping("/reset-model")
    public Result<String> resetModel() {
        DeepFeatureExtractor.reset();
        return Result.success("Model reset, will reload on next request");
    }

    @GetMapping("/pgvector-status")
    public Result<Map<String, Object>> pgVectorStatus() {
        Map<String, Object> status = new java.util.LinkedHashMap<>();
        if (pgVectorService != null) {
            status.put("available", pgVectorService.isAvailable());
            status.put("count", pgVectorService.count());
        } else {
            status.put("available", false);
            status.put("count", 0);
        }
        return Result.success(status);
    }

    @PostMapping("/backfill-pgvector")
    public Result<Map<String, Object>> backfillPgVector() {
        return Result.success(imageService.backfillPgVector());
    }

    @PostMapping("/test-pgvector-insert")
    public Result<Map<String, Object>> testPgVectorInsert() {
        return Result.success(imageService.testPgVectorInsert());
    }

    @GetMapping("/pgvector-check/{id}")
    public Result<Map<String, Object>> pgVectorCheck(@PathVariable long id, @RequestParam(required = false) String shardPrefix) {
        return Result.success(imageService.pgVectorCheck(id, shardPrefix));
    }

    private MediaType getMediaType(String ext) {
        if (ext == null) return MediaType.IMAGE_JPEG;
        switch (ext.toLowerCase()) {
            case "png": return MediaType.IMAGE_PNG;
            case "gif": return MediaType.IMAGE_GIF;
            case "webp": return MediaType.parseMediaType("image/webp");
            case "bmp": return MediaType.parseMediaType("image/bmp");
            default: return MediaType.IMAGE_JPEG;
        }
    }
}
