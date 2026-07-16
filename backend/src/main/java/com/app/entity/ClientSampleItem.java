package com.app.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

@TableName("client_sample_items")
public class ClientSampleItem {

    private Long id;
    private String codeName;
    private Long sampleId;
    private Integer sortOrder;
    private String snapshotData;
    private BigDecimal calculatedPrice;
    private BigDecimal calculatedPrice2;
    private Integer showroomReplenished;
    private Integer borrowedSample;
    private Integer checked;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Integer deleted;
    private String deletedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }
    public BigDecimal getCalculatedPrice() { return calculatedPrice; }
    public void setCalculatedPrice(BigDecimal calculatedPrice) { this.calculatedPrice = calculatedPrice; }
    public BigDecimal getCalculatedPrice2() { return calculatedPrice2; }
    public void setCalculatedPrice2(BigDecimal calculatedPrice2) { this.calculatedPrice2 = calculatedPrice2; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
    public Integer getShowroomReplenished() { return showroomReplenished; }
    public void setShowroomReplenished(Integer showroomReplenished) { this.showroomReplenished = showroomReplenished; }
    public Integer getBorrowedSample() { return borrowedSample; }
    public void setBorrowedSample(Integer borrowedSample) { this.borrowedSample = borrowedSample; }
    public Integer getChecked() { return checked; }
    public void setChecked(Integer checked) { this.checked = checked; }

    public String getSampleStatus() {
        if (borrowedSample != null && borrowedSample == 1) return "借样";
        if (showroomReplenished != null && showroomReplenished == 1) return "展厅已补";
        return "不允许带走";
    }
}
