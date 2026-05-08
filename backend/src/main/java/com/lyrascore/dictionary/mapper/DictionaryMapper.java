package com.lyrascore.dictionary.mapper;

import com.lyrascore.dictionary.entity.Dictionary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DictionaryMapper {
    List<Dictionary> selectActiveByType(@Param("dictType") String dictType);

    List<Dictionary> selectAll();

    int insert(Dictionary d);

    int update(Dictionary d);

    int deleteById(@Param("id") Long id);
}
