package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 异步导出任务管理：避免长时间 HTTP 请求被 nginx 超时断开。
 * 任务生成 Excel 到临时文件，前端轮询状态，完成后下载。
 */
@Service
public class ExportTaskService {

    private static final Logger log = LoggerFactory.getLogger(ExportTaskService.class);

    private final Map<String, ExportTask> tasks = new ConcurrentHashMap<>();

    @Value("${app.upload.temp-path:#{systemProperties['java.io.tmpdir']}}")
    private String tempDir;

    /** 单线程顺序执行导出，避免同时跑多个大导出把服务器打满 */
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "export-worker");
        t.setDaemon(true);
        return t;
    });

    /**
     * 提交一个导出任务，立即返回 taskId。
     * @param codeName 客户代号
     * @param runner  实际导出逻辑，接收 progress(0-100) 和 message 回调，返回生成的文件路径
     * @return taskId
     */
    public String submit(String codeName, ExportRunner runner) {
        String taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ExportTask task = new ExportTask();
        task.taskId = taskId;
        task.codeName = codeName;
        task.status = "PROCESSING";
        task.progress = 0;
        task.progressMessage = "正在准备数据...";
        task.createdAt = System.currentTimeMillis();
        tasks.put(taskId, task);

        CompletableFuture.runAsync(() -> {
            try {
                Path filePath = runner.run(
                    progress -> {
                        task.progress = progress;
                    },
                    message -> {
                        task.progressMessage = message;
                    }
                );
                task.filePath = filePath;
                task.fileName = runner.getFileName();
                task.status = "DONE";
                task.progress = 100;
                task.progressMessage = "导出完成";
                log.info("ExportTask {} done: {} ({} rows)", taskId, task.fileName,
                        runner.getDescription());
            } catch (Exception e) {
                log.error("ExportTask {} failed: {}", taskId, e.getMessage(), e);
                task.status = "ERROR";
                task.error = e.getMessage() != null ? e.getMessage() : "未知错误";
                task.progressMessage = "导出失败: " + task.error;
            }
        }, exportExecutor);

        return taskId;
    }

    /** 查询导出任务状态 */
    public ExportTask getStatus(String taskId) {
        ExportTask task = tasks.get(taskId);
        if (task == null) return null;
        // 返回快照，避免外部修改
        ExportTask snapshot = new ExportTask();
        snapshot.taskId = task.taskId;
        snapshot.status = task.status;
        snapshot.progress = task.progress;
        snapshot.progressMessage = task.progressMessage;
        snapshot.codeName = task.codeName;
        snapshot.error = task.error;
        snapshot.fileName = task.fileName;
        snapshot.createdAt = task.createdAt;
        return snapshot;
    }

    /** 获取生成的文件路径，仅 DONE 状态可用 */
    public Path getDownloadFile(String taskId) {
        ExportTask task = tasks.get(taskId);
        if (task == null || !"DONE".equals(task.status) || task.filePath == null) return null;
        if (!Files.exists(task.filePath)) return null;
        return task.filePath;
    }

    /** 获取文件名 */
    public String getFileName(String taskId) {
        ExportTask task = tasks.get(taskId);
        return task != null ? task.fileName : null;
    }

    /** 删除任务和临时文件 */
    public void delete(String taskId) {
        ExportTask task = tasks.remove(taskId);
        if (task != null && task.filePath != null) {
            try {
                Files.deleteIfExists(task.filePath);
            } catch (IOException ignored) {}
        }
    }

    /** 每 10 分钟清理超过 30 分钟的旧任务 */
    @Scheduled(fixedRate = 600_000)
    public void cleanExpiredTasks() {
        long now = System.currentTimeMillis();
        long expireMs = 30 * 60 * 1000; // 30 minutes
        tasks.entrySet().removeIf(entry -> {
            ExportTask task = entry.getValue();
            if (now - task.createdAt > expireMs) {
                if (task.filePath != null) {
                    try { Files.deleteIfExists(task.filePath); } catch (IOException ignored) {}
                }
                log.info("ExportTask {} expired and cleaned", task.taskId);
                return true;
            }
            return false;
        });
    }

    // ===== 内部类 =====

    public static class ExportTask {
        public String taskId;
        public volatile String status;       // PROCESSING | DONE | ERROR
        public volatile int progress;        // 0-100
        public volatile String progressMessage;
        public String codeName;
        public String fileName;
        public volatile String error;
        public long createdAt;
        // 内部使用，导出线程写入，下载线程读取
        private volatile Path filePath;
    }

    /**
     * 导出执行器接口 — 由 Controller 提供实际业务逻辑
     */
    @FunctionalInterface
    public interface ExportRunner {
        /**
         * @param onProgress  进度回调 0-100
         * @param onMessage   进度文字回调
         * @return 生成的文件路径
         */
        Path run(Consumer<Integer> onProgress, Consumer<String> onMessage) throws Exception;

        /** 文件名，如 "报价明细.xlsx" */
        default String getFileName() { return "export.xlsx"; }

        /** 简短描述，用于日志 */
        default String getDescription() { return "unknown"; }
    }
}
