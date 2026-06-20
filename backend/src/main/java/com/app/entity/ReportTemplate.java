package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 报表模板表 - 所有用户共享
 */
@TableName("report_templates")
public class ReportTemplate extends BaseEntity {

    private String title;
    private String description;
    private String templateData;
    private Long createBy;
    private Long updateBy;

    @TableField(exist = false)
    private String updateByName;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTemplateData() { return templateData; }
    public void setTemplateData(String templateData) { this.templateData = templateData; }

    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }

    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }

    public String getUpdateByName() { return updateByName; }
    public void setUpdateByName(String updateByName) { this.updateByName = updateByName; }
}
