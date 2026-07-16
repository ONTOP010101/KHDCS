package com.app.mapper;

import com.app.entity.UserPreference;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {

    @Select("SELECT * FROM user_preferences WHERE user_id = #{userId} AND page_key = #{pageKey}")
    UserPreference findByUserAndPage(@Param("userId") Long userId, @Param("pageKey") String pageKey);
}
