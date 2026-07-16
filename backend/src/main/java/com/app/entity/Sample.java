package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;

@TableName("samples")
public class Sample extends BaseEntity {

    private String sampleCode;
    private String manufacturerCode;
    private String sampleName;
    private String englishName;
    private String category;
    private String categoryCode;
    private String factoryCode;
    private String sampleUnit;
    private String sampleUnitEn;
    private String packagingCn;
    private String packagingEn;
    private String packageCode;
    private String color;
    private String colorEn;
    private String size;
    private String origin;
    private String name;
    private String boothNo;
    private String contact1;
    private String phone1;
    private String mobile1;
    private String fax;
    private String qq;
    private String visitorMobile;
    private String smsNumber;
    private BigDecimal factoryPrice;
    private BigDecimal taxPrice;
    private BigDecimal sampleLength;
    private BigDecimal sampleWidth;
    private BigDecimal sampleHeight;
    private BigDecimal sampleGrossWeight;
    private BigDecimal sampleNetWeight;
    private BigDecimal cartonLength;
    private BigDecimal cartonWidth;
    private BigDecimal cartonHeight;
    private BigDecimal cartonMaterialVolume;
    private BigDecimal cartonVolume;
    private Integer innerBoxCount;
    private Integer cartonCapacity;
    private String packingUnit;
    private BigDecimal cartonGrossWeight;
    private BigDecimal cartonNetWeight;
    private BigDecimal packageLength;
    private BigDecimal packageWidth;
    private BigDecimal packageHeight;
    private String certification;
    private Integer certificationCount;
    private String remark;
    private String remarkEn;
    private String otherRemark;
    private String registrant;
    private String modifier;
    private Integer status;
    private String infringement;
    private String batteryInfo;
    private String boxCount;
    private String hideFromXzx;
    private Long createBy;
    private Long updateBy;

    @TableField(exist = false)
    private String thumbnail;

    @TableField(exist = false)
    private Long firstImageId;

    @TableField(exist = false)
    private String firstImageHash;

    @TableField(exist = false)
    private java.math.BigDecimal calculatedPrice;

    @TableField(exist = false)
    @JsonIgnore
    private java.math.BigDecimal calculatedPrice2;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addDate;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyDate;

    @TableField(exist = false)
    private Long itemId;

    @TableField(exist = false)
    private Integer showroomReplenished;

    @TableField(exist = false)
    private Integer borrowedSample;

    @TableField(exist = false)
    private String sampleStatus;

    @TableField(exist = false)
    private Integer checked;

    @TableField(exist = false)
    private Integer submitted;

    @TableField(exist = false)
    private String codeName;

    @TableField(exist = false)
    private Boolean hasVideo;

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public Long getFirstImageId() { return firstImageId; }
    public void setFirstImageId(Long firstImageId) { this.firstImageId = firstImageId; }
    public String getFirstImageHash() { return firstImageHash; }
    public void setFirstImageHash(String firstImageHash) { this.firstImageHash = firstImageHash; }
    public java.math.BigDecimal getCalculatedPrice() { return calculatedPrice; }
    public void setCalculatedPrice(java.math.BigDecimal calculatedPrice) { this.calculatedPrice = calculatedPrice; }
    public java.math.BigDecimal getCalculatedPrice2() { return calculatedPrice2; }
    public void setCalculatedPrice2(java.math.BigDecimal calculatedPrice2) { this.calculatedPrice2 = calculatedPrice2; }

    public String getSampleCode() { return sampleCode; }
    public void setSampleCode(String sampleCode) { this.sampleCode = sampleCode; }
    public String getManufacturerCode() { return manufacturerCode; }
    public void setManufacturerCode(String manufacturerCode) { this.manufacturerCode = manufacturerCode; }
    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getFactoryCode() { return factoryCode; }
    public void setFactoryCode(String factoryCode) { this.factoryCode = factoryCode; }
    public String getSampleUnit() { return sampleUnit; }
    public void setSampleUnit(String sampleUnit) { this.sampleUnit = sampleUnit; }
    public String getSampleUnitEn() { return sampleUnitEn; }
    public void setSampleUnitEn(String sampleUnitEn) { this.sampleUnitEn = sampleUnitEn; }
    public String getPackagingCn() { return packagingCn; }
    public void setPackagingCn(String packagingCn) { this.packagingCn = packagingCn; }
    public String getPackagingEn() { return packagingEn; }
    public void setPackagingEn(String packagingEn) { this.packagingEn = packagingEn; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getColorEn() { return colorEn; }
    public void setColorEn(String colorEn) { this.colorEn = colorEn; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSupplier() { return name; }
    public void setSupplier(String supplier) { this.name = supplier; }
    public String getBoothNo() { return boothNo; }
    public void setBoothNo(String boothNo) { this.boothNo = boothNo; }
    public String getContact1() { return contact1; }
    public void setContact1(String contact1) { this.contact1 = contact1; }
    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }
    public String getMobile1() { return mobile1; }
    public void setMobile1(String mobile1) { this.mobile1 = mobile1; }
    public String getContactPerson() { return contact1; }
    public void setContactPerson(String contactPerson) { this.contact1 = contactPerson; }
    public String getContactPhone() { return phone1; }
    public void setContactPhone(String contactPhone) { this.phone1 = contactPhone; }
    public String getMobile() { return mobile1; }
    public void setMobile(String mobile) { this.mobile1 = mobile; }
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getVisitorMobile() { return visitorMobile; }
    public void setVisitorMobile(String visitorMobile) { this.visitorMobile = visitorMobile; }
    public String getSmsNumber() { return smsNumber; }
    public void setSmsNumber(String smsNumber) { this.smsNumber = smsNumber; }
    public BigDecimal getFactoryPrice() { return factoryPrice; }
    public void setFactoryPrice(BigDecimal factoryPrice) { this.factoryPrice = factoryPrice; }
    public BigDecimal getTaxPrice() { return taxPrice; }
    public void setTaxPrice(BigDecimal taxPrice) { this.taxPrice = taxPrice; }
    // 报出价2 映射到 calculatedPrice2（从 client_sample_items 关联查询）
    public BigDecimal getTaxPrice2() { return calculatedPrice2; }
    public void setTaxPrice2(BigDecimal v) { this.calculatedPrice2 = v; }
    // 添加日期（从 client_sample_items.create_time 关联查询）
    public Date getAddDate() { return addDate; }
    public void setAddDate(Date addDate) { this.addDate = addDate; }
    public Date getModifyDate() { return modifyDate; }
    public void setModifyDate(Date modifyDate) { this.modifyDate = modifyDate; }
    public BigDecimal getSampleLength() { return sampleLength; }
    public void setSampleLength(BigDecimal sampleLength) { this.sampleLength = sampleLength; }
    public BigDecimal getSampleWidth() { return sampleWidth; }
    public void setSampleWidth(BigDecimal sampleWidth) { this.sampleWidth = sampleWidth; }
    public BigDecimal getSampleHeight() { return sampleHeight; }
    public void setSampleHeight(BigDecimal sampleHeight) { this.sampleHeight = sampleHeight; }
    public BigDecimal getSampleGrossWeight() { return sampleGrossWeight; }
    public void setSampleGrossWeight(BigDecimal sampleGrossWeight) { this.sampleGrossWeight = sampleGrossWeight; }
    public BigDecimal getSampleNetWeight() { return sampleNetWeight; }
    public void setSampleNetWeight(BigDecimal sampleNetWeight) { this.sampleNetWeight = sampleNetWeight; }
    public BigDecimal getCartonLength() { return cartonLength; }
    public void setCartonLength(BigDecimal cartonLength) { this.cartonLength = cartonLength; }
    public BigDecimal getCartonWidth() { return cartonWidth; }
    public void setCartonWidth(BigDecimal cartonWidth) { this.cartonWidth = cartonWidth; }
    public BigDecimal getCartonHeight() { return cartonHeight; }
    public void setCartonHeight(BigDecimal cartonHeight) { this.cartonHeight = cartonHeight; }
    public BigDecimal getCartonMaterialVolume() { return cartonMaterialVolume; }
    public void setCartonMaterialVolume(BigDecimal cartonMaterialVolume) { this.cartonMaterialVolume = cartonMaterialVolume; }
    public BigDecimal getCartonVolume() { return cartonVolume; }
    public void setCartonVolume(BigDecimal cartonVolume) { this.cartonVolume = cartonVolume; }
    public Integer getInnerBoxCount() { return innerBoxCount; }
    public void setInnerBoxCount(Integer innerBoxCount) { this.innerBoxCount = innerBoxCount; }
    public Integer getCartonCapacity() { return cartonCapacity; }
    public void setCartonCapacity(Integer cartonCapacity) { this.cartonCapacity = cartonCapacity; }
    public String getPackingUnit() { return packingUnit; }
    public void setPackingUnit(String packingUnit) { this.packingUnit = packingUnit; }
    public BigDecimal getCartonGrossWeight() { return cartonGrossWeight; }
    public void setCartonGrossWeight(BigDecimal cartonGrossWeight) { this.cartonGrossWeight = cartonGrossWeight; }
    public BigDecimal getCartonNetWeight() { return cartonNetWeight; }
    public void setCartonNetWeight(BigDecimal cartonNetWeight) { this.cartonNetWeight = cartonNetWeight; }
    public BigDecimal getPackageLength() { return packageLength; }
    public void setPackageLength(BigDecimal packageLength) { this.packageLength = packageLength; }
    public BigDecimal getPackageWidth() { return packageWidth; }
    public void setPackageWidth(BigDecimal packageWidth) { this.packageWidth = packageWidth; }
    public BigDecimal getPackageHeight() { return packageHeight; }
    public void setPackageHeight(BigDecimal packageHeight) { this.packageHeight = packageHeight; }
    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
    public Integer getCertificationCount() { return certificationCount; }
    public void setCertificationCount(Integer certificationCount) { this.certificationCount = certificationCount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getRemarkEn() { return remarkEn; }
    public void setRemarkEn(String remarkEn) { this.remarkEn = remarkEn; }
    public String getOtherRemark() { return otherRemark; }
    public void setOtherRemark(String otherRemark) { this.otherRemark = otherRemark; }
    public String getRegistrant() { return registrant; }
    public void setRegistrant(String registrant) { this.registrant = registrant; }
    public String getModifier() { return modifier; }
    public void setModifier(String modifier) { this.modifier = modifier; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getInfringement() { return infringement; }
    public void setInfringement(String infringement) { this.infringement = infringement; }
    public String getBatteryInfo() { return batteryInfo; }
    public void setBatteryInfo(String batteryInfo) { this.batteryInfo = batteryInfo; }
    public String getBoxCount() { return boxCount; }
    public void setBoxCount(String boxCount) { this.boxCount = boxCount; }
    public String getHideFromXzx() { return hideFromXzx; }
    public void setHideFromXzx(String hideFromXzx) { this.hideFromXzx = hideFromXzx; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Integer getShowroomReplenished() { return showroomReplenished; }
    public void setShowroomReplenished(Integer showroomReplenished) { this.showroomReplenished = showroomReplenished; }
    public Integer getBorrowedSample() { return borrowedSample; }
    public void setBorrowedSample(Integer borrowedSample) { this.borrowedSample = borrowedSample; }
    public Integer getChecked() { return checked; }
    public void setChecked(Integer checked) { this.checked = checked; }

    public String getSampleStatus() {
        if (sampleStatus != null) return sampleStatus;
        if (borrowedSample != null && borrowedSample == 1) return "借样";
        if (showroomReplenished != null && showroomReplenished == 1) return "展厅已补";
        return "不允许带走";
    }
    public void setSampleStatus(String sampleStatus) { this.sampleStatus = sampleStatus; }

    public Integer getSubmitted() { return submitted; }
    public void setSubmitted(Integer submitted) { this.submitted = submitted; }
    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public Boolean getHasVideo() { return hasVideo; }
    public void setHasVideo(Boolean hasVideo) { this.hasVideo = hasVideo; }
}
