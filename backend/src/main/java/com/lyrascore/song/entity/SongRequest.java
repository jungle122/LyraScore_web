package com.lyrascore.song.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SongRequest {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String rawInput;
    private String aiSong;
    private String aiArtist;
    private String requestMessage;
    private Integer status;     // 0未弹 / 1已还愿 / 2 AI失败待审
    private LocalDateTime createdAt;

    // JOIN 出来的对端用户名
    private String senderName;
    private String receiverName;
}
