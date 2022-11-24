package com.os.component.idworker.dao;

import com.os.component.idworker.model.Segment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Segment Mapper
 *
 * @author pengjunjie
 */
public interface SegmentMapper {

    @Select("select biz_tag from segment")
    List<String> getAllTags();

    @Select("select biz_tag as bizTag, max_id as maxId, step from segment where biz_tag = #{tag}")
    Segment getSegment(@Param("tag") String tag);

    @Update("update segment set max_id = max_id + step where biz_tag = #{tag}")
    void updateMaxId(@Param("tag") String tag);
}
