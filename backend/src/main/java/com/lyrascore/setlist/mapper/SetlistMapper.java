package com.lyrascore.setlist.mapper;

import com.lyrascore.setlist.entity.Setlist;
import com.lyrascore.setlist.entity.SetlistItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SetlistMapper {

    int insert(Setlist setlist);

    List<Setlist> selectByUser(@Param("userId") Long userId);

    Setlist selectById(@Param("id") Long id);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int insertItem(@Param("setlistId") Long setlistId, @Param("scoreId") Long scoreId);

    int deleteItem(@Param("setlistId") Long setlistId, @Param("scoreId") Long scoreId);

    List<SetlistItem> selectItemsBySetlist(@Param("setlistId") Long setlistId);
}
