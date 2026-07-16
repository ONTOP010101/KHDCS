package com.app.es.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 样品 Elasticsearch 索引实体
 * 用于综合查询加速
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@esIndexName}")
public class SampleES {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    /** 公司编号 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String sampleCode;

    /** 厂商编号 - 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String manufacturerCode;

    /** 样品名称 - IK 中文分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String sampleName;

    /** 英文名称 */
    @Field(type = FieldType.Text)
    private String englishName;

    /** 种类名称 */
    @Field(type = FieldType.Keyword)
    private String category;

    /** 种类编号 */
    @Field(type = FieldType.Keyword)
    private String categoryCode;

    /** 出厂货号 */
    @Field(type = FieldType.Keyword)
    private String factoryCode;

    /** 厂商名称 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /** 摊位号 */
    @Field(type = FieldType.Keyword)
    private String boothNo;

    /** 联系人 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String contact1;

    /** 联系电话 */
    @Field(type = FieldType.Keyword)
    private String phone1;

    /** 手机 */
    @Field(type = FieldType.Keyword)
    private String mobile1;

    /** 短信号 */
    @Field(type = FieldType.Keyword)
    private String smsNumber;

    /** 出厂价 */
    @Field(type = FieldType.Float)
    private BigDecimal factoryPrice;

    /** 样品长度 */
    @Field(type = FieldType.Float)
    private BigDecimal sampleLength;

    /** 样品宽度 */
    @Field(type = FieldType.Float)
    private BigDecimal sampleWidth;

    /** 样品高度 */
    @Field(type = FieldType.Float)
    private BigDecimal sampleHeight;

    /** 中文包装 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String packagingCn;

    /** 包装编号 */
    @Field(type = FieldType.Keyword)
    private String packageCode;

    /** 产品认证 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String certification;

    /** 侵权状态 */
    @Field(type = FieldType.Keyword)
    private String infringement;

    /** 电池信息 */
    @Field(type = FieldType.Text)
    private String batteryInfo;

    /** 不在小竹熊显示 */
    @Field(type = FieldType.Keyword)
    private String hideFromXzx;

    /** 外箱装量 */
    @Field(type = FieldType.Integer)
    private Integer cartonCapacity;

    /** 内盒个数 */
    @Field(type = FieldType.Integer)
    private Integer innerBoxCount;

    /** 包装长度 */
    @Field(type = FieldType.Float)
    private BigDecimal packageLength;

    /** 包装宽度 */
    @Field(type = FieldType.Float)
    private BigDecimal packageWidth;

    /** 包装高度 */
    @Field(type = FieldType.Float)
    private BigDecimal packageHeight;

    /** 外箱长度 */
    @Field(type = FieldType.Float)
    private BigDecimal cartonLength;

    /** 外箱宽度 */
    @Field(type = FieldType.Float)
    private BigDecimal cartonWidth;

    /** 外箱高度 */
    @Field(type = FieldType.Float)
    private BigDecimal cartonHeight;

    /** 登记人 */
    @Field(type = FieldType.Keyword)
    private String registrant;

    /** 修改人 */
    @Field(type = FieldType.Keyword)
    private String modifier;

    /** 创建时间 */
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd['T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]]")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd['T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]]")
    private LocalDateTime updateTime;

    /** 备注 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String remark;

    /** 是否删除 */
    @Field(type = FieldType.Integer)
    private Integer deleted;

    /** 首图ID - 用于判断是否有图片，有则非空 */
    @Field(type = FieldType.Long)
    private Long firstImageId;
}
