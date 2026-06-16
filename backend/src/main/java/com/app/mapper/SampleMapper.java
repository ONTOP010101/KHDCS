package com.app.mapper;

import com.app.entity.Sample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SampleMapper extends BaseMapper<Sample> {

    IPage<Sample> searchByFulltext(Page<Sample> page,
                                   @Param("keyword") String keyword,
                                   @Param("keywordRaw") String keywordRaw,
                                   @Param("category") String category,
                                   @Param("supplier") String supplier,
                                   @Param("sortField") String sortField,
                                   @Param("sortOrder") String sortOrder);

    IPage<Sample> searchByKeyword(Page<Sample> page,
                                  @Param("ftTerms") List<String> ftTerms,
                                  @Param("likeTerms") List<String> likeTerms,
                                  @Param("sortField") String sortField,
                                  @Param("sortOrder") String sortOrder);

    @Select("SELECT * FROM samples WHERE sample_code = #{code} LIMIT 1")
    Sample findByCodeIncludeDeleted(@Param("code") String code);

    @Select("SELECT sample_code FROM samples WHERE sample_code IS NOT NULL AND deleted = 1")
    List<String> findDeletedCodes();

    @Select("SELECT * FROM samples WHERE deleted = 1 ORDER BY update_time DESC")
    IPage<Sample> selectDeleted(Page<Sample> page);

    @Select("SELECT * FROM samples WHERE deleted = 1")
    List<Sample> selectAllDeleted();

    int restoreByIds(@Param("ids") List<Long> ids);
}
