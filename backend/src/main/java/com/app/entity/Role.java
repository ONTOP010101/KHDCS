package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("roles")
public class Role extends BaseEntity {

    private String name;
    private String code;
    private String description;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
