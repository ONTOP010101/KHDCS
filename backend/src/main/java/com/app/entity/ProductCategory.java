package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("product_categories")
public class ProductCategory extends BaseEntity {

    /** 种类编号，如 "1", "101", "2201" */
    private String code;

    /** 种类名称 */
    private String name;

    /** 匹配关键词，逗号分隔 */
    private String keywords;

    /** 层级：1=一级类目，2=二级类目 */
    private Integer level;

    /** 父级编号（二级类目关联的一级编号，如 "1"），一级类目为 null */
    private String parentCode;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }
}
