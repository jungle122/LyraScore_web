package com.lyrascore.friend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long id;
    private Long userId1;   // 发起方
    private Long userId2;   // 接收方
    private Integer status; // 0待通过 / 1已成为好友
    private LocalDateTime createdAt;

    // JOIN 出来的对端用户名（看上下文是哪一端）
    private Long otherUserId;
    private String otherUsername;
    private String otherAvatarUrl;
}
