package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("manufacturers")
public class Manufacturer extends BaseEntity {

    private String manufacturerCode;
    private String name;
    private String boothNo;
    private String phone1;
    private String mobile1;
    private String contact1;
    private String visitorMobile;
    private String phone2;
    private String mobile2;
    private String contact2;
    private String address;
    private String phone3;
    private String mobile3;
    private String qq;
    private String otherRemark;
    private String certificate;
    private String smsNumber;
    private String boothMeters;
    private String boothType;
    private String floorArea;
    private String boothArea;
    private String lastExpiry;
    private String expiryDate;
    private String mainCard;
    private String subCard;
    private String registrant;
    private String modifier;
    private String remark;
    private Long createBy;
    private Long updateBy;

    public String getManufacturerCode() { return manufacturerCode; }
    public void setManufacturerCode(String manufacturerCode) { this.manufacturerCode = manufacturerCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBoothNo() { return boothNo; }
    public void setBoothNo(String boothNo) { this.boothNo = boothNo; }
    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }
    public String getMobile1() { return mobile1; }
    public void setMobile1(String mobile1) { this.mobile1 = mobile1; }
    public String getContact1() { return contact1; }
    public void setContact1(String contact1) { this.contact1 = contact1; }
    public String getVisitorMobile() { return visitorMobile; }
    public void setVisitorMobile(String visitorMobile) { this.visitorMobile = visitorMobile; }
    public String getPhone2() { return phone2; }
    public void setPhone2(String phone2) { this.phone2 = phone2; }
    public String getMobile2() { return mobile2; }
    public void setMobile2(String mobile2) { this.mobile2 = mobile2; }
    public String getContact2() { return contact2; }
    public void setContact2(String contact2) { this.contact2 = contact2; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone3() { return phone3; }
    public void setPhone3(String phone3) { this.phone3 = phone3; }
    public String getMobile3() { return mobile3; }
    public void setMobile3(String mobile3) { this.mobile3 = mobile3; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getOtherRemark() { return otherRemark; }
    public void setOtherRemark(String otherRemark) { this.otherRemark = otherRemark; }
    public String getCertificate() { return certificate; }
    public void setCertificate(String certificate) { this.certificate = certificate; }
    public String getSmsNumber() { return smsNumber; }
    public void setSmsNumber(String smsNumber) { this.smsNumber = smsNumber; }
    public String getBoothMeters() { return boothMeters; }
    public void setBoothMeters(String boothMeters) { this.boothMeters = boothMeters; }
    public String getBoothType() { return boothType; }
    public void setBoothType(String boothType) { this.boothType = boothType; }
    public String getFloorArea() { return floorArea; }
    public void setFloorArea(String floorArea) { this.floorArea = floorArea; }
    public String getBoothArea() { return boothArea; }
    public void setBoothArea(String boothArea) { this.boothArea = boothArea; }
    public String getLastExpiry() { return lastExpiry; }
    public void setLastExpiry(String lastExpiry) { this.lastExpiry = lastExpiry; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getMainCard() { return mainCard; }
    public void setMainCard(String mainCard) { this.mainCard = mainCard; }
    public String getSubCard() { return subCard; }
    public void setSubCard(String subCard) { this.subCard = subCard; }
    public String getRegistrant() { return registrant; }
    public void setRegistrant(String registrant) { this.registrant = registrant; }
    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
