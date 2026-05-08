package com.lyrascore.song.mapper;

import com.lyrascore.song.entity.SongRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SongRequestMapper {

    int insert(SongRequest req);

    SongRequest selectById(@Param("id") Long id);

    List<SongRequest> selectSentBy(@Param("userId") Long userId);

    List<SongRequest> selectReceivedBy(@Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("receiverId") Long receiverId, @Param("status") Integer status);

    int updateAiResult(@Param("id") Long id, @Param("aiSong") String aiSong, @Param("aiArtist") String aiArtist, @Param("status") Integer status);
}
