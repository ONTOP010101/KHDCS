package com.app.service;

import cn.hutool.core.io.FileUtil;
import com.app.common.BusinessException;
import com.app.entity.Video;
import com.app.mapper.VideoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

    @Value("${app.upload.video-path}")
    private String videoPath;

    @Value("${app.upload.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Autowired
    private VideoMapper videoMapper;

    @Transactional
    public Video upload(MultipartFile file, Long sampleId) {
        if (file.isEmpty()) throw new BusinessException(400, "文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName).toLowerCase();
        if (!isVideo(ext)) throw new BusinessException(400, "不支持的视频格式: " + ext);

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BusinessException(400, "视频文件大小不能超过50MB");
        }

        try {
            String shard = String.format("%02d/%02d", sampleId % 100, (sampleId / 100) % 100);
            String fileName = originalName;
            String relPath = shard + "/" + sampleId + "/" + fileName;
            Path fullPath = Paths.get(videoPath, relPath);
            fullPath.getParent().toFile().mkdirs();

            if (Files.exists(fullPath)) {
                int suffix = 1;
                String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
                do {
                    String newName = baseName + "(" + suffix + ")." + ext;
                    relPath = shard + "/" + sampleId + "/" + newName;
                    fullPath = Paths.get(videoPath, relPath);
                    suffix++;
                } while (Files.exists(fullPath));
            }
            String storedName = fullPath.getFileName().toString();

            Files.copy(file.getInputStream(), fullPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            long fileSize = Files.size(fullPath);

            Video video = new Video();
            video.setSampleId(sampleId);
            video.setFileName(storedName);
            video.setFilePath(relPath);
            video.setFileSize(fileSize);
            video.setFileType(ext);
            video.setCreateBy(com.app.util.UserContext.getUserId());
            videoMapper.insert(video);

            final Path targetPath = fullPath;
            final Long videoId = video.getId();
            new Thread(() -> asyncCompressIfNeeded(targetPath, videoId, ext)).start();

            return video;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "视频保存失败: " + e.getMessage());
        }
    }

    public List<Video> batchUpload(MultipartFile file, List<Long> sampleIds) {
        if (file.isEmpty()) throw new BusinessException(400, "文件不能为空");
        String ext = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        if (!isVideo(ext)) throw new BusinessException(400, "不支持的视频格式: " + ext);
        if (file.getSize() > MAX_VIDEO_SIZE) throw new BusinessException(400, "视频文件大小不能超过50MB");
        if (sampleIds == null || sampleIds.isEmpty()) throw new BusinessException(400, "样品ID列表不能为空");

        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException(500, "读取文件失败: " + e.getMessage());
        }

        final String finalExt = ext;
        final String originalName = file.getOriginalFilename();
        final Long userId = com.app.util.UserContext.getUserId();
        new Thread(() -> {
            for (Long sampleId : sampleIds) {
                try {
                    String shard = String.format("%02d/%02d", sampleId % 100, (sampleId / 100) % 100);
                    String fileName = originalName;
                    String relPath = shard + "/" + sampleId + "/" + fileName;
                    Path fullPath = Paths.get(videoPath, relPath);
                    fullPath.getParent().toFile().mkdirs();

                    if (Files.exists(fullPath)) {
                        int suffix = 1;
                        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
                        do {
                            String newName = baseName + "(" + suffix + ")." + finalExt;
                            relPath = shard + "/" + sampleId + "/" + newName;
                            fullPath = Paths.get(videoPath, relPath);
                            suffix++;
                        } while (Files.exists(fullPath));
                    }
                    String storedName = fullPath.getFileName().toString();

                    Files.write(fullPath, fileBytes);
                    long fileSize = Files.size(fullPath);

                    Video video = new Video();
                    video.setSampleId(sampleId);
                    video.setFileName(storedName);
                    video.setFilePath(relPath);
                    video.setFileSize(fileSize);
                    video.setFileType(finalExt);
                    video.setCreateBy(userId);
                    videoMapper.insert(video);
                } catch (Exception e) {
                    log.error("批量复制视频到样品 {} 失败", sampleId, e);
                }
            }
            log.info("批量视频复制完成，共 {} 个样品", sampleIds.size());
        }).start();

        return new java.util.ArrayList<>();
    }

    private void asyncCompressIfNeeded(Path targetPath, Long videoId, String originalExt) {
        try {
            if ("mp4".equalsIgnoreCase(originalExt) && isH264Codec(targetPath)) {
                Video update = new Video();
                update.setId(videoId);
                update.setFileType("mp4");
                videoMapper.updateById(update);
                return;
            }
            asyncCompress(targetPath, videoId);
        } catch (Exception e) {
            log.warn("视频异步处理异常: {}", targetPath.getFileName(), e);
        }
    }

    private void asyncCompress(Path targetPath, Long videoId) {
        Path tempInput = null;
        Path tempOutput = null;
        try {
            tempInput = Files.createTempFile("video_compress_", ".mp4");
            Files.copy(targetPath, tempInput, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            tempOutput = Files.createTempFile("video_compressed_", ".mp4");

            ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", tempInput.toString(),
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "23",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                "-y",
                tempOutput.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while (reader.readLine() != null) {}
                } catch (IOException ignored) {}
            }).start();

            boolean finished = process.waitFor(300, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("FFmpeg 异步压缩失败，退出码: {}", exitCode);
                return;
            }

            Files.move(tempOutput, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            long newSize = Files.size(targetPath);
            Video update = new Video();
            update.setId(videoId);
            update.setFileSize(newSize);
            update.setFileType("mp4");
            videoMapper.updateById(update);

            log.info("视频异步压缩完成: {}", targetPath.getFileName());
        } catch (Exception e) {
            log.warn("视频异步压缩异常: {}", targetPath.getFileName(), e);
        } finally {
            try { if (tempInput != null) Files.deleteIfExists(tempInput); } catch (IOException ignored) {}
            try { if (tempOutput != null) Files.deleteIfExists(tempOutput); } catch (IOException ignored) {}
        }
    }

    private boolean isH264Codec(Path videoPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", videoPath.toString(),
                "-c:v", "copy",
                "-f", "null",
                "-"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Video> listBySampleId(Long sampleId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Video>();
        wrapper.eq(Video::getSampleId, sampleId)
              .orderByDesc(Video::getCreateTime);
        return videoMapper.selectList(wrapper);
    }

    public void delete(Long id) {
        Video v = videoMapper.selectById(id);
        if (v != null && v.getFilePath() != null) {
            try { Files.deleteIfExists(Paths.get(videoPath, v.getFilePath())); }
            catch (IOException ignored) {}
        }
        videoMapper.deleteById(id);
    }

    public void deleteBySampleId(Long sampleId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Video>();
        wrapper.eq(Video::getSampleId, sampleId);
        List<Video> videos = videoMapper.selectList(wrapper);
        for (Video v : videos) {
            try { Files.deleteIfExists(Paths.get(videoPath, v.getFilePath())); }
            catch (IOException ignored) {}
        }
        videoMapper.delete(wrapper);
    }

    private static boolean isVideo(String ext) {
        return ext.equals("mp4") || ext.equals("mov") || ext.equals("avi") || ext.equals("wmv") || ext.equals("mkv");
    }
}
