package com.app.mapper;

import com.app.entity.ClientSamplePriceSetting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClientSamplePriceSettingMapper extends BaseMapper<ClientSamplePriceSetting> {

    @Select("SELECT * FROM client_sample_price_settings WHERE code_name = #{codeName} AND type = #{type} AND deleted = 0")
    ClientSamplePriceSetting selectByCodeNameAndType(@Param("codeName") String codeName, @Param("type") String type);
}
