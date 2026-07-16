package com.app.dto;

/**
 * 库存汇总 - 按公司编号去重，汇总入库/出库数量
 */
public class InventorySummary {

    private String companyCode;
    private String codeName;
    private String factoryNo;
    private String sampleName;
    private String chinesePackage;
    private String boothNumber;
    private String manufacturerName;
    private String mobile;
    private String telephone;
    private String image;
    private String floor;
    private String stockInTime;
    private String lastOutboundTime;
    private int inboundCount;
    private int outboundCount;
    private int onDisplayCount;

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
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
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getStockInTime() { return stockInTime; }
    public void setStockInTime(String stockInTime) { this.stockInTime = stockInTime; }
    public String getLastOutboundTime() { return lastOutboundTime; }
    public void setLastOutboundTime(String lastOutboundTime) { this.lastOutboundTime = lastOutboundTime; }
    public int getInboundCount() { return inboundCount; }
    public void setInboundCount(int inboundCount) { this.inboundCount = inboundCount; }
    public int getOutboundCount() { return outboundCount; }
    public void setOutboundCount(int outboundCount) { this.outboundCount = outboundCount; }
    public int getOnDisplayCount() { return onDisplayCount; }
    public void setOnDisplayCount(int onDisplayCount) { this.onDisplayCount = onDisplayCount; }
}
