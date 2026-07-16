package com.app.mapper;

import com.app.entity.ClientSample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClientSampleMapper extends BaseMapper<ClientSample> {

    @Select("SELECT code_name FROM client_samples")
    List<String> selectAllCodeNames();

    @Select("SELECT * FROM client_samples WHERE code_name = #{codeName} AND deleted = 0 LIMIT 1")
    ClientSample selectByCodeName(@Param("codeName") String codeName);
}
