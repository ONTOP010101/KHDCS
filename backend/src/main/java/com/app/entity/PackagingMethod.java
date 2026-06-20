package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("packaging_methods")
public class PackagingMethod extends BaseEntity {

    /** 包装编号 */
    private String code;

    /** 中文包装 */
    private String name;

    /** 英文包装 */
    private String nameEn;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
}
