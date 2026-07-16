package com.app.mapper;

import com.app.entity.Inventory;
import com.app.dto.InventorySummary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("SELECT * FROM inventory WHERE code_name = #{codeName} AND deleted = 0 ORDER BY create_time DESC, id DESC")
    List<Inventory> selectByCodeName(@Param("codeName") String codeName);

    @Select("SELECT COUNT(*) FROM inventory WHERE code_name = #{codeName} AND company_code = #{companyCode} AND deleted = 0")
    int countByCodeNameAndCompanyCode(@Param("codeName") String codeName, @Param("companyCode") String companyCode);

    @Delete("DELETE FROM inventory WHERE code_name = #{codeName}")
    int deleteByCodeName(@Param("codeName") String codeName);

    /**
     * 按公司编号去重汇总库存 - 分页
     */
    @Select("<script>" +
        "SELECT i.company_code AS companyCode, " +
        "MAX(i.code_name) AS codeName, " +
        "MAX(i.factory_no) AS factoryNo, MAX(i.sample_name) AS sampleName, " +
        "MAX(i.chinese_package) AS chinesePackage, MAX(i.booth_number) AS boothNumber, " +
        "MAX(i.manufacturer_name) AS manufacturerName, " +
        "MAX(i.mobile) AS mobile, MAX(i.telephone) AS telephone, " +
        "MAX(i.image) AS image, MAX(i.floor) AS floor, " +
        "MAX(i.stock_in_time) AS stockInTime, " +
        "MAX(o.stock_out_time) AS lastOutboundTime, " +
        "COUNT(i.id) AS inboundCount, " +
        "COALESCE(MAX(o.outbound_count), 0) AS outboundCount, " +
        "(COUNT(i.id) - COALESCE(MAX(o.outbound_count), 0)) AS onDisplayCount " +
        "FROM inventory i " +
        "LEFT JOIN (" +
        "  SELECT company_code, COUNT(*) AS outbound_count, MAX(stock_out_time) AS stock_out_time " +
        "  FROM outbound WHERE deleted = 0 AND submitted = 1 GROUP BY company_code" +
        ") o ON i.company_code = o.company_code " +
        "WHERE i.deleted = 0 AND i.submitted = 1 " +
        "<if test='keyword != null and keyword != \"\"'>" +
        "  AND i.company_code = #{keyword}" +
        "</if>" +
        "<if test='boothNo != null and boothNo != \"\"'>" +
        "  AND i.booth_number = #{boothNo}" +
        "</if>" +
        "<if test='mobile != null and mobile != \"\"'>" +
        "  AND i.mobile = #{mobile}" +
        "</if>" +
        "<if test='manufacturerName != null and manufacturerName != \"\"'>" +
        "  AND i.manufacturer_name = #{manufacturerName}" +
        "</if>" +
        "<if test='floor != null and floor != \"\"'>" +
        "  AND i.floor = #{floor}" +
        "</if>" +
        "GROUP BY i.company_code " +
        "<if test='sortField != null and sortField != \"\" and sortOrder != null and sortOrder != \"\"'>" +
        "  ORDER BY " +
        "  <choose>" +
        "    <when test='sortField == \"onDisplayCount\"'>onDisplayCount ${sortOrder}</when>" +
        "    <when test='sortField == \"inboundCount\"'>inboundCount ${sortOrder}</when>" +
        "    <when test='sortField == \"outboundCount\"'>outboundCount ${sortOrder}</when>" +
        "    <when test='sortField == \"stockInTime\"'>stockInTime ${sortOrder}</when>" +
        "    <when test='sortField == \"lastOutboundTime\"'>lastOutboundTime ${sortOrder}</when>" +
        "    <otherwise>stockInTime DESC</otherwise>" +
        "  </choose>" +
        "</if>" +
        "<if test='sortField == null or sortField == \"\"'>" +
        "  ORDER BY stockInTime DESC" +
        "</if>" +
        "</script>")
    Page<InventorySummary> selectSummaryPage(Page<?> page,
                                            @Param("keyword") String keyword,
                                            @Param("sortField") String sortField,
                                            @Param("sortOrder") String sortOrder,
                                            @Param("boothNo") String boothNo,
                                            @Param("mobile") String mobile,
                                            @Param("manufacturerName") String manufacturerName,
                                            @Param("floor") String floor);
}
