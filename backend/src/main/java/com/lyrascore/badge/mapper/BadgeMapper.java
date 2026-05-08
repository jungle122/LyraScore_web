package com.lyrascore.badge.mapper;

import com.lyrascore.badge.entity.Badge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BadgeMapper {

    List<Badge> selectAll();

    /** 当前用户视角：左连 t_user_badge，附 unlocked + unlocked_at */
    List<Badge> selectAllWithUserState(@Param("userId") Long userId);

    /** 用户已持有的徽章 ID 集合 */
    List<Long> selectBadgeIdsOfUser(@Param("userId") Long userId);

    /** 授予徽章（依赖 uk_user_badge_pair 唯一键，重复授予返回 0 行） */
    int award(@Param("userId") Long userId, @Param("badgeId") Long badgeId);

    /** 用户上传的乐谱总数 —— 评估 score_count 类徽章用 */
    Long countScoresOfUser(@Param("userId") Long userId);

    Long countLogsOfUser(@Param("userId") Long userId);

    Long countPlansOfUser(@Param("userId") Long userId);

    Long countSetlistsOfUser(@Param("userId") Long userId);

    Long countFriendsOfUser(@Param("userId") Long userId);

    Long countSongRequestsOfUser(@Param("userId") Long userId);
}
