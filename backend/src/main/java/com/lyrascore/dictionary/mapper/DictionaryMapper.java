package com.lyrascore.dictionary.mapper;

import com.lyrascore.dictionary.entity.Dictionary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DictionaryMapper {
    List<Dictionary> selectActiveByType(@Param("dictType") String dictType);
}
