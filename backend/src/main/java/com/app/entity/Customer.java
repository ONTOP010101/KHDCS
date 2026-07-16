package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("customers")
public class Customer extends BaseEntity {

    private String customerCode;
    private String customerName;
    private String country;
    private String address;
    private String contactPerson1;
    private String mobile1;
    private String phone1;
    private String email;
    private String qq;
    private String modifier;
    private String contactPerson2;
    private String mobile2;
    private String phone2;
    private String remark1;
    private String registrant;
    private String contactPerson3;
    private String mobile3;
    private String phone3;
    private String remark2;
    private String region;
    private String smsNumber;
    private String registerDate;
    private String modifyDate;
    private String certificate;
    private Long createBy;
    private Long updateBy;

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactPerson1() { return contactPerson1; }
    public void setContactPerson1(String contactPerson1) { this.contactPerson1 = contactPerson1; }
    public String getMobile1() { return mobile1; }
    public void setMobile1(String mobile1) { this.mobile1 = mobile1; }
    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }
    public String getContactPerson2() { return contactPerson2; }
    public void setContactPerson2(String contactPerson2) { this.contactPerson2 = contactPerson2; }
    public String getMobile2() { return mobile2; }
    public void setMobile2(String mobile2) { this.mobile2 = mobile2; }
    public String getPhone2() { return phone2; }
    public void setPhone2(String phone2) { this.phone2 = phone2; }
    public String getRemark1() { return remark1; }
    public void setRemark1(String remark1) { this.remark1 = remark1; }
    public String getRegistrant() { return registrant; }
    public void setRegistrant(String registrant) { this.registrant = registrant; }
    public String getContactPerson3() { return contactPerson3; }
    public void setContactPerson3(String contactPerson3) { this.contactPerson3 = contactPerson3; }
    public String getMobile3() { return mobile3; }
    public void setMobile3(String mobile3) { this.mobile3 = mobile3; }
    public String getPhone3() { return phone3; }
    public void setPhone3(String phone3) { this.phone3 = phone3; }
    public String getRemark2() { return remark2; }
    public void setRemark2(String remark2) { this.remark2 = remark2; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getSmsNumber() { return smsNumber; }
    public void setSmsNumber(String smsNumber) { this.smsNumber = smsNumber; }
    public String getRegisterDate() { return registerDate; }
    public void setRegisterDate(String registerDate) { this.registerDate = registerDate; }
    public String getModifyDate() { return modifyDate; }
    public void setModifyDate(String modifyDate) { this.modifyDate = modifyDate; }
    public String getCertificate() { return certificate; }
    public void setCertificate(String certificate) { this.certificate = certificate; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
