package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("inventory_group")
public class InventoryGroup extends BaseEntity {

    private String codeName;
    private String image;
    private String companyCode;
    private String factoryNo;
    private String sampleName;
    private String chinesePackage;
    private String boothNumber;
    private String manufacturerName;
    private String mobile;
    private String telephone;
    private String manufacturerCode;
    private String floor;
    private String remark;

    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getFactoryNo() { return factoryNo; }
    public void setFactoryNo(String factoryNo) { this.factoryNo = factoryNo; }
    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }
    public String getChinesePackage() { return chinesePackage; }
    public void setChinesePackage(String chinesePackage) { this.chinesePackage = chinesePackage; }
    public String getBoothNumber() { return boothNumber; }
    public void setBoothNumber(String boothNumber) { this.boothNumber = boothNumber; }
    public String getManufacturerName() { return manufacturerName; }
    public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getManufacturerCode() { return manufacturerCode; }
    public void setManufacturerCode(String manufacturerCode) { this.manufacturerCode = manufacturerCode; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
