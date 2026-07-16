package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 短信/微信/企业微信 模板
 */
@TableName("sms_templates")
public class SmsTemplateEntity extends BaseEntity {

    /** 模板名称 */
    private String templateName;

    /** 类型: sms / wechat / wework */
    private String type;

    /** 短信类型: 通知 / 营销 (仅 type=sms 时有效) */
    private String smsType;

    /** 联麓签名ID (仅 type=sms 时有效) */
    private Integer signId;

    /** 用途类型 (如 B06-其他业务管理服务类) */
    private String applyPurpose;

    /** 变量类型: number_letter=数字+字母, letter=仅字母 */
    private String variableType;

    /** 联麓模板ID (仅 type=sms 时有效) */
    private Integer lianluTemplateId;

    /** 模板内容 (可含变量如 {%变量1%}) */
    private String content;

    /** 模板状态: 0=待审核, 1=审核通过, 2=审核驳回 */
    private Integer status;

    /** 审核拒绝原因 */
    private String refuseReason;

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSmsType() { return smsType; }
    public void setSmsType(String smsType) { this.smsType = smsType; }
    public Integer getSignId() { return signId; }
    public void setSignId(Integer signId) { this.signId = signId; }
    public String getApplyPurpose() { return applyPurpose; }
    public void setApplyPurpose(String applyPurpose) { this.applyPurpose = applyPurpose; }
    public String getVariableType() { return variableType; }
    public void setVariableType(String variableType) { this.variableType = variableType; }
    public Integer getLianluTemplateId() { return lianluTemplateId; }
    public void setLianluTemplateId(Integer lianluTemplateId) { this.lianluTemplateId = lianluTemplateId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRefuseReason() { return refuseReason; }
    public void setRefuseReason(String refuseReason) { this.refuseReason = refuseReason; }
}
