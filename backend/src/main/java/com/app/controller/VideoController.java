package com.app.controller;

import com.app.common.Result;
import com.app.entity.Video;
import com.app.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private com.app.mapper.VideoMapper videoMapper;

    @Value("${app.upload.video-path}")
    private String videoPath;

    @PostMapping("/upload")
    public Result<Video> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long sampleId) {
        return Result.success("上传成功", videoService.upload(file, sampleId));
    }

    @PostMapping("/batch-upload")
    public Result<List<Video>> batchUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sampleIds") String sampleIdsStr) {
        String[] parts = sampleIdsStr.split(",");
        List<Long> sampleIds = Arrays.stream(parts)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .toList();
        return Result.success("上传成功", videoService.batchUpload(file, sampleIds));
    }

    @GetMapping("/sample/{sampleId}")
    public Result<List<Video>> listBySample(@PathVariable Long sampleId) {
        return Result.success(videoService.listBySampleId(sampleId));
    }

    @GetMapping("/file/{sampleId}/{fileName:.+}")
    public void serveVideo(@PathVariable Long sampleId,
                           @PathVariable String fileName,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        com.app.entity.Video video = videoMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.app.entity.Video>()
                .eq(com.app.entity.Video::getSampleId, sampleId)
                .eq(com.app.entity.Video::getFileName, fileName)
        );
        Path filePath = null;
        if (video != null && video.getFilePath() != null) {
            filePath = Paths.get(videoPath, video.getFilePath());
            if (!Files.exists(filePath)) filePath = null;
        }
        if (filePath == null) {
            filePath = Paths.get(videoPath, String.valueOf(sampleId), fileName);
        }
        if (!Files.exists(filePath)) {
            response.sendError(404, "视频文件不存在");
            return;
        }

        long fileSize = Files.size(filePath);
        String contentType = getContentType(fileName);
        response.setContentType(contentType);

        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1;
            if (start >= fileSize) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileSize);
                return;
            }
            if (end >= fileSize) end = fileSize - 1;
            long contentLength = end - start + 1;

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            response.setContentLengthLong(contentLength);
            response.setHeader("Accept-Ranges", "bytes");

            try (InputStream input = Files.newInputStream(filePath);
                 OutputStream output = response.getOutputStream()) {
                input.skip(start);
                byte[] buffer = new byte[8192];
                long remaining = contentLength;
                int bytesRead;
                while (remaining > 0 && (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    output.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                output.flush();
            }
        } else {
            response.setContentLengthLong(fileSize);
            response.setHeader("Accept-Ranges", "bytes");
            Files.copy(filePath, response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    private String getContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".wmv")) return "video/x-ms-wmv";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        return "application/octet-stream";
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping("/sample/{sampleId}")
    public Result<Void> deleteBySampleId(@PathVariable Long sampleId) {
        videoService.deleteBySampleId(sampleId);
        return Result.ok("删除成功");
    }
}
