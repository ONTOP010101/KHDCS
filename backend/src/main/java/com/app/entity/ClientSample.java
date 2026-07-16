package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("client_samples")
public class ClientSample extends BaseEntity {

    @TableField(exist = false)
    private Integer sampleCount;

    @TableField(exist = false)
    private Integer manufacturerCount;

    @TableField(exist = false)
    private Long sourceId;

    @TableField(exist = false)
    private Boolean copyPrice;

    @TableField(exist = false)
    private Boolean copyItems;

    private String codeName;
    private String selectionId;
    private String clientCode;
    private String clientName;
    private String selectionDate;
    private String orderPhone;
    private String recorder;
    private String recordDate;
    private String modifier;
    private String modifyDate;
    private String remark;
    private String discount;
    private String labelTemplateIds;
    private Long createBy;
    private Long updateBy;

    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public String getSelectionId() { return selectionId; }
    public void setSelectionId(String selectionId) { this.selectionId = selectionId; }
    public String getClientCode() { return clientCode; }
    public void setClientCode(String clientCode) { this.clientCode = clientCode; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getSelectionDate() { return selectionDate; }
    public void setSelectionDate(String selectionDate) { this.selectionDate = selectionDate; }
    public String getOrderPhone() { return orderPhone; }
    public void setOrderPhone(String orderPhone) { this.orderPhone = orderPhone; }
    public String getRecorder() { return recorder; }
    public void setRecorder(String recorder) { this.recorder = recorder; }
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }
    public String getModifyDate() { return modifyDate; }
    public void setModifyDate(String modifyDate) { this.modifyDate = modifyDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }
    public String getLabelTemplateIds() { return labelTemplateIds; }
    public void setLabelTemplateIds(String labelTemplateIds) { this.labelTemplateIds = labelTemplateIds; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public Integer getManufacturerCount() { return manufacturerCount; }
    public void setManufacturerCount(Integer manufacturerCount) { this.manufacturerCount = manufacturerCount; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public Boolean getCopyPrice() { return copyPrice; }
    public void setCopyPrice(Boolean copyPrice) { this.copyPrice = copyPrice; }
    public Boolean getCopyItems() { return copyItems; }
    public void setCopyItems(Boolean copyItems) { this.copyItems = copyItems; }
}
