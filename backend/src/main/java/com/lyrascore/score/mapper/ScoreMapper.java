package com.lyrascore.score.mapper;

import com.lyrascore.score.entity.Score;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreMapper {

    int insert(Score score);

    List<Score> selectByUser(@Param("userId") Long userId);

    Score selectById(@Param("id") Long id);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);

    int updateMeta(Score score);
}
