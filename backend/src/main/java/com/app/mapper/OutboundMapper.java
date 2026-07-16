package com.app.mapper;

import com.app.entity.Outbound;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OutboundMapper extends BaseMapper<Outbound> {

    @Select("SELECT * FROM outbound WHERE code_name = #{codeName} AND deleted = 0 ORDER BY create_time DESC, id DESC")
    List<Outbound> selectByCodeName(@Param("codeName") String codeName);

    @Select("SELECT COUNT(*) FROM outbound WHERE code_name = #{codeName} AND company_code = #{companyCode} AND deleted = 0")
    int countByCodeNameAndCompanyCode(@Param("codeName") String codeName, @Param("companyCode") String companyCode);

    @Delete("DELETE FROM outbound WHERE code_name = #{codeName}")
    int deleteByCodeName(@Param("codeName") String codeName);
}
