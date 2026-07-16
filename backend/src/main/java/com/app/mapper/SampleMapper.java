package com.app.mapper;

import com.app.entity.Sample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SampleMapper extends BaseMapper<Sample> {

    IPage<Sample> searchByFulltext(Page<Sample> page,
                                   @Param("keyword") String keyword,
                                   @Param("keywordRaw") String keywordRaw,
                                   @Param("category") String category,
                                   @Param("name") String name,
                                   @Param("sortField") String sortField,
                                   @Param("sortOrder") String sortOrder);

    IPage<Sample> searchByKeyword(Page<Sample> page,
                                  @Param("ftTerms") List<String> ftTerms,
                                  @Param("likeTerms") List<String> likeTerms,
                                  @Param("sortField") String sortField,
                                  @Param("sortOrder") String sortOrder);

    @Select("SELECT * FROM samples WHERE sample_code = #{code} AND deleted = 0 LIMIT 1")
    Sample findBySampleCode(@Param("code") String code);

    @Select("SELECT * FROM samples WHERE sample_code = #{code} LIMIT 1")
    Sample findByCodeIncludeDeleted(@Param("code") String code);

    @Select("SELECT sample_code FROM samples WHERE sample_code IS NOT NULL AND deleted = 1")
    List<String> findDeletedCodes();

    @Select("SELECT * FROM samples WHERE deleted = 1 ORDER BY update_time DESC")
    IPage<Sample> selectDeleted(Page<Sample> page);

    @Select("SELECT * FROM samples WHERE deleted = 1")
    List<Sample> selectAllDeleted();

    int restoreByIds(@Param("ids") List<Long> ids);

    /** 获取厂商统计：每个 manufacturer_code 的样品数量 */
    List<Map<String, Object>> getFactoryStats(@Param("minCount") int minCount);

    /** 获取指定厂商编号的前N条样品ID */
    List<Long> getSampleIdsBySupplier(@Param("manufacturerCode") String manufacturerCode, @Param("limit") int limit);

    /** 关键词搜索返回所有匹配ID（不分页） */
    List<Long> searchIdsByKeyword(@Param("ftTerms") List<String> ftTerms,
                                  @Param("likeTerms") List<String> likeTerms);

    /** 指定ID范围内的厂商统计 */
    List<Map<String, Object>> getFactoryStatsByIds(@Param("ids") List<Long> ids);

    /** 指定ID范围内获取指定厂商编号的前N条样品ID */
    List<Long> getSampleIdsBySupplierFiltered(@Param("manufacturerCode") String manufacturerCode,
                                              @Param("limit") int limit,
                                              @Param("ids") List<Long> ids);

    /** 获取所有有样品的厂商编号（去重） */
    @Select("SELECT DISTINCT manufacturer_code FROM samples WHERE manufacturer_code IS NOT NULL AND manufacturer_code != '' AND deleted = 0")
    List<String> findDistinctManufacturerCodes();

    /** 有样品的厂商编号总数 */
    @Select("SELECT COUNT(DISTINCT manufacturer_code) FROM samples WHERE manufacturer_code IS NOT NULL AND manufacturer_code != '' AND deleted = 0")
    int countDistinctManufacturerCodes();
}
