package com.app.dto;

/**
 * 异步上传任务
 */
public class UploadTask {
    private String taskId;
    private String fileName;
    private String status;  // PENDING, PROCESSING, SUCCESS, FAILED
    private String errorMsg;
    private Long imageId;
    private String thumbnailPath;
    private String hash;

    public UploadTask() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}
