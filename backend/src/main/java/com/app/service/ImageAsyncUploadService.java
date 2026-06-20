package com.app.service;

import com.app.dto.UploadTask;
import com.app.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * 异步图片上传队列 — 削峰填谷，防止并发上传撑爆连接池和CPU
 */
@Service
public class ImageAsyncUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageAsyncUploadService.class);

    /** 工作线程数：4 个（8核CPU留一半给其他请求） */
    private static final int WORKER_COUNT = 4;

    /** 队列容量：防止无限堆积 */
    private static final int QUEUE_CAPACITY = 500;

    /** 任务超时（分钟），超时自动清理 */
    private static final int TASK_TTL_MINUTES = 30;

    private final ExecutorService executor;
    private final ConcurrentHashMap<String, UploadTask> taskStore = new ConcurrentHashMap<>();

    @Autowired
    private ImageService imageService;

    public ImageAsyncUploadService() {
        this.executor = new ThreadPoolExecutor(
                WORKER_COUNT, WORKER_COUNT,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用线程执行，防止丢任务
        );
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 提交单个上传任务，立即返回 taskId
     */
    public UploadTask submit(MultipartFile file, Long galleryId, Long sampleId,
                             String description, String tags) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        UploadTask task = new UploadTask();
        task.setTaskId(taskId);
        task.setFileName(file.getOriginalFilename());
        task.setStatus("PENDING");
        taskStore.put(taskId, task);

        // 提前读取文件字节，避免 Spring 清理 MultipartFile 临时文件后数据丢失
        byte[] fileBytes;
        String originalFilename;
        String contentType;
        try {
            fileBytes = file.getBytes();
            originalFilename = file.getOriginalFilename();
            contentType = file.getContentType();
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMsg("读取文件失败: " + e.getMessage());
            return task;
        }

        executor.submit(() -> processTask(taskId, fileBytes, originalFilename, contentType,
                galleryId, sampleId, description, tags));
        return task;
    }

    /**
     * 批量提交
     */
    public List<UploadTask> submitBatch(List<MultipartFile> files, Long galleryId) {
        List<UploadTask> tasks = new ArrayList<>();
        for (MultipartFile file : files) {
            tasks.add(submit(file, galleryId, null, null, null));
        }
        return tasks;
    }

    /**
     * 查询单个任务状态
     */
    public UploadTask getTask(String taskId) {
        UploadTask task = taskStore.get(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在或已过期: " + taskId);
        }
        return task;
    }

    /**
     * 批量查询任务状态
     */
    public List<UploadTask> getTasks(List<String> taskIds) {
        List<UploadTask> result = new ArrayList<>();
        for (String id : taskIds) {
            UploadTask t = taskStore.get(id);
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 获取队列深度（用于监控）
     */
    public int getQueueDepth() {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getQueue().size();
        }
        return 0;
    }

    // ---- 内部处理 ----

    private void processTask(String taskId, byte[] fileBytes, String originalFilename,
                             String contentType, Long galleryId, Long sampleId,
                             String description, String tags) {
        UploadTask task = taskStore.get(taskId);
        if (task == null) return;

        task.setStatus("PROCESSING");
        try {
            MultipartFile file = new ByteArrayMultipartFile(
                    "file", originalFilename, contentType, fileBytes);
            var image = imageService.upload(file, galleryId, sampleId, description, tags);
            task.setStatus("SUCCESS");
            task.setImageId(image.getId());
            task.setThumbnailPath(image.getThumbnailPath());
            task.setHash(image.getHash());
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMsg(e.getMessage() != null ? e.getMessage() : "未知错误");
            log.warn("[AsyncUpload] task {} ({}) failed: {}", taskId, task.getFileName(), e.getMessage());
        }
    }

    /**
     * 简单的 MultipartFile 实现，基于字节数组，不依赖 spring-test
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return bytes == null || bytes.length == 0; }
        @Override public long getSize() { return bytes != null ? bytes.length : 0; }
        @Override public byte[] getBytes() { return bytes; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
        @Override public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(dest);
            fos.write(bytes);
            fos.close();
        }
    }
}
