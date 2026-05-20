package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.Image;
import com.app.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

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

    @GetMapping("/gallery/{galleryId}")
    public Result<List<Image>> listByGallery(@PathVariable Long galleryId) {
        return Result.success(imageService.listByGallery(galleryId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imageService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody Long[] ids) {
        imageService.deleteBatch(ids);
        return Result.ok("批量删除成功");
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
