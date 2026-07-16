package com.app.mapper;

import com.app.entity.ClientSampleItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ClientSampleItemMapper extends BaseMapper<ClientSampleItem> {

    /**
     * 按代号查询所有明细（含快照数据），用于页面展示
     */
    @Select("SELECT * FROM client_sample_items WHERE code_name = #{codeName} AND deleted = 0 ORDER BY create_time DESC, id DESC")
    List<ClientSampleItem> selectItemsByCodeName(@Param("codeName") String codeName);

    /**
     * 按代号和样品ID列表查询（避免查全量再过滤）
     */
    @Select("<script>SELECT * FROM client_sample_items WHERE code_name = #{codeName} AND deleted = 0 AND sample_id IN <foreach collection='sampleIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach> ORDER BY create_time DESC, id DESC</script>")
    List<ClientSampleItem> selectItemsByCodeNameAndSampleIds(@Param("codeName") String codeName, @Param("sampleIds") List<Long> sampleIds);

    @Delete("DELETE FROM client_sample_items WHERE code_name = #{codeName} AND sample_id = #{sampleId}")
    int deleteByCodeNameAndSampleId(@Param("codeName") String codeName, @Param("sampleId") Long sampleId);

    @Select("SELECT COUNT(*) FROM client_sample_items WHERE code_name = #{codeName} AND sample_id = #{sampleId} AND deleted = 0")
    int countByCodeNameAndSampleId(@Param("codeName") String codeName, @Param("sampleId") Long sampleId);

    @Select("SELECT * FROM client_sample_items WHERE code_name = #{codeName} AND sample_id = #{sampleId} AND deleted = 0 LIMIT 1")
    ClientSampleItem selectByCodeNameAndSampleId(@Param("codeName") String codeName, @Param("sampleId") Long sampleId);

    /**
     * 批量统计：按代号统计样品数和厂商数
     */
    @Select("<script>SELECT csi.code_name, COUNT(DISTINCT csi.sample_id) as sample_count, COUNT(DISTINCT s.manufacturer_code) as manufacturer_count FROM client_sample_items csi JOIN samples s ON s.id = csi.sample_id WHERE csi.code_name IN <foreach collection='codeNames' item='cn' open='(' separator=',' close=')'>#{cn}</foreach> AND csi.deleted = 0 GROUP BY csi.code_name</script>")
    List<Map<String, Object>> countByCodeNames(@Param("codeNames") List<String> codeNames);

    /** 查询代号下的去重厂商编号（用于短信页厂商计数） */
    @Select("SELECT COUNT(DISTINCT s.manufacturer_code) FROM client_sample_items csi JOIN samples s ON s.id = csi.sample_id WHERE csi.code_name = #{codeName} AND csi.deleted = 0 AND s.manufacturer_code IS NOT NULL")
    int countDistinctManufacturers(@Param("codeName") String codeName);

    /** 查询已删除的样品ID（绕过 MyBatis-Plus 逻辑删除过滤） */
    @Select("<script>SELECT sample_id FROM client_sample_items WHERE code_name = #{codeName} AND deleted = 1 AND sample_id IN <foreach collection='sampleIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach></script>")
    List<Long> findDeletedSampleIds(@Param("codeName") String codeName, @Param("sampleIds") List<Long> sampleIds);

    /** 物理删除已删除的记录（绕过逻辑删除过滤），用于重新添加 */
    @Delete("<script>DELETE FROM client_sample_items WHERE code_name = #{codeName} AND deleted = 1 AND sample_id IN <foreach collection='sampleIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach></script>")
    int physicallyDeleteByCodeAndIds(@Param("codeName") String codeName, @Param("sampleIds") List<Long> sampleIds);

    /** 查询已删除的明细记录（绕过逻辑删除过滤） */
    @Select("SELECT * FROM client_sample_items WHERE code_name = #{codeName} AND deleted = 1 ORDER BY update_time DESC")
    List<ClientSampleItem> selectDeletedItemsByCodeName(@Param("codeName") String codeName);

    /** 批量更新报价1 */
    @Update("<script>UPDATE client_sample_items SET calculated_price = CASE id " +
        "<foreach collection='items' item='it'>WHEN #{it.id} THEN #{it.calculatedPrice} </foreach>" +
        "END WHERE id IN <foreach collection='items' item='it' open='(' separator=',' close=')'>#{it.id}</foreach></script>")
    int batchUpdateCalculatedPrice(@Param("items") List<ClientSampleItem> items);

    /** 批量更新报价2 */
    @Update("<script>UPDATE client_sample_items SET calculated_price2 = CASE id " +
        "<foreach collection='items' item='it'>WHEN #{it.id} THEN #{it.calculatedPrice2} </foreach>" +
        "END WHERE id IN <foreach collection='items' item='it' open='(' separator=',' close=')'>#{it.id}</foreach></script>")
    int batchUpdateCalculatedPrice2(@Param("items") List<ClientSampleItem> items);

    /** 批量插入 */
    @Insert("<script>" +
        "INSERT INTO client_sample_items (code_name, sample_id, sort_order, snapshot_data, calculated_price, calculated_price2, showroom_replenished, borrowed_sample, checked, create_time, update_time) VALUES " +
        "<foreach collection='list' item='item' separator=','>" +
        "(#{item.codeName}, #{item.sampleId}, #{item.sortOrder}, #{item.snapshotData}, #{item.calculatedPrice}, #{item.calculatedPrice2}, #{item.showroomReplenished}, #{item.borrowedSample}, #{item.checked}, #{item.createTime}, #{item.updateTime})" +
        "</foreach>" +
    "</script>")
    int insertBatch(@Param("list") List<ClientSampleItem> items);
}
