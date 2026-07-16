package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("outbound_code")
public class OutboundCode extends BaseEntity {

    private String outboundCode;
    private String codeName;
    private String createDate;
    private String creator;
    private String floor;
    private String remark;

    @TableField(exist = false)
    private Integer totalCount;
    @TableField(exist = false)
    private Integer submittedCount;

    public String getOutboundCode() { return outboundCode; }
    public void setOutboundCode(String outboundCode) { this.outboundCode = outboundCode; }
    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }
}
