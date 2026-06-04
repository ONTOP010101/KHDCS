package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("samples")
public class Sample extends BaseEntity {

    private String sampleCode;
    private String manufacturerCode;
    private String sampleName;
    private String englishName;
    private String category;
    private String factoryCode;
    private String sampleUnit;
    private String sampleUnitEn;
    private String packagingCn;
    private String packagingEn;
    private String packageCode;
    private String material;
    private String color;
    private String colorEn;
    private String size;
    private String weight;
    private String origin;
    private String supplier;
    private String boothNo;
    private String contactPerson;
    private String contactPhone;
    private String mobile;
    private String fax;
    private String qq;
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
    private String description;
    private String remark;
    private String remarkEn;
    private String registrant;
    private String modifier;
    private Integer status;
    private String infringement;
    private String batteryInfo;
    private Long createBy;
    private Long updateBy;

    @TableField(exist = false)
    private String thumbnail;

    @TableField(exist = false)
    private Long firstImageId;

    @TableField(exist = false)
    private String firstImageHash;

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public Long getFirstImageId() { return firstImageId; }
    public void setFirstImageId(Long firstImageId) { this.firstImageId = firstImageId; }
    public String getFirstImageHash() { return firstImageHash; }
    public void setFirstImageHash(String firstImageHash) { this.firstImageHash = firstImageHash; }

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
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getColorEn() { return colorEn; }
    public void setColorEn(String colorEn) { this.colorEn = colorEn; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getBoothNo() { return boothNo; }
    public void setBoothNo(String boothNo) { this.boothNo = boothNo; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public BigDecimal getFactoryPrice() { return factoryPrice; }
    public void setFactoryPrice(BigDecimal factoryPrice) { this.factoryPrice = factoryPrice; }
    public BigDecimal getTaxPrice() { return taxPrice; }
    public void setTaxPrice(BigDecimal taxPrice) { this.taxPrice = taxPrice; }
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getRemarkEn() { return remarkEn; }
    public void setRemarkEn(String remarkEn) { this.remarkEn = remarkEn; }
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
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
}
