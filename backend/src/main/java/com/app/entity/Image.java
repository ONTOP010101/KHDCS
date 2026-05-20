package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("images")
public class Image extends BaseEntity {

    private Long galleryId;
    private Long sampleId;
    private String fileName;
    private String filePath;
    private String thumbnailPath;
    private Long fileSize;
    private String fileType;
    private Integer width;
    private Integer height;
    private String hash;
    private String description;
    private String tags;
    private Integer sortOrder;
    private Long createBy;

    public Long getGalleryId() { return galleryId; }
    public void setGalleryId(Long galleryId) { this.galleryId = galleryId; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
}
