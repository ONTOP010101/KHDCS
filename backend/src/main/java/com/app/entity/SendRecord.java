package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 短信/微信/企业微信 发送记录
 */
@TableName("send_records")
public class SendRecord extends BaseEntity {

    /** 本次代号 */
    private String codeName;

    /** 客户名称 */
    private String clientName;

    /** 类型: sms / wechat / wework */
    private String type;

    /** 联麓模板ID */
    private Integer lianluTemplateId;

    /** 模板名称 */
    private String templateName;

    /** 厂商编号 */
    private String manufacturerCode;

    /** 厂商名称 */
    private String manufacturerName;

    /** 摊位号 */
    private String boothNo;

    /** 手机号 */
    private String phone;

    /** 实际发送内容 */
    private String content;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /** 发送状态: pending=待发送, success=发送成功, fail=发送失败 */
    private String sendStatus;

    /** 是否阅读: 0=未读, 1=已读 */
    private Integer readStatus;

    /** 联麓任务ID */
    private String taskId;

    /** 自定义标签 */
    private String tag;

    /** 联麓回执码 */
    private String respCode;

    /** 回执描述 */
    private String codeDesc;

    /** 回执时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime respTime;

    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getLianluTemplateId() { return lianluTemplateId; }
    public void setLianluTemplateId(Integer lianluTemplateId) { this.lianluTemplateId = lianluTemplateId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getManufacturerCode() { return manufacturerCode; }
    public void setManufacturerCode(String manufacturerCode) { this.manufacturerCode = manufacturerCode; }
    public String getManufacturerName() { return manufacturerName; }
    public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
    public String getBoothNo() { return boothNo; }
    public void setBoothNo(String boothNo) { this.boothNo = boothNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    public String getSendStatus() { return sendStatus; }
    public void setSendStatus(String sendStatus) { this.sendStatus = sendStatus; }
    public Integer getReadStatus() { return readStatus; }
    public void setReadStatus(Integer readStatus) { this.readStatus = readStatus; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getRespCode() { return respCode; }
    public void setRespCode(String respCode) { this.respCode = respCode; }
    public String getCodeDesc() { return codeDesc; }
    public void setCodeDesc(String codeDesc) { this.codeDesc = codeDesc; }
    public LocalDateTime getRespTime() { return respTime; }
    public void setRespTime(LocalDateTime respTime) { this.respTime = respTime; }
}
