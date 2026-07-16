package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("inventory")
public class Inventory extends BaseEntity {

    private String inventoryCode;
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
    private String createDate;
    private String creator;
    private String floor;
    private String stockInTime;
    private String remark;
    private Integer submitted;

    @TableField(exist = false)
    private Long imageId;

    public String getInventoryCode() { return inventoryCode; }
    public void setInventoryCode(String inventoryCode) { this.inventoryCode = inventoryCode; }
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
    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getStockInTime() { return stockInTime; }
    public void setStockInTime(String stockInTime) { this.stockInTime = stockInTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getSubmitted() { return submitted; }
    public void setSubmitted(Integer submitted) { this.submitted = submitted; }
    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }
}
