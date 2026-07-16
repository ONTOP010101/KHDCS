package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("bluetooth_label_templates")
public class BluetoothLabelTemplate extends BaseEntity {

    private String name;
    private Integer width;
    private String fields;
    private Long createBy;
    private Long updateBy;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public String getFields() { return fields; }
    public void setFields(String fields) { this.fields = fields; }

    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }

    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
