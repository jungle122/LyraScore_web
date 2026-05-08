package com.lyrascore.admin.mapper;

/**
 * 管理员后台专用：全站聚合统计
 */
public interface AdminMapper {

    long countUsers();
    long countScores();
    long countPlans();
    long countLogs();
    long countSetlists();
    long countFriendships();
    long countSongRequests();
    long countUserBadges();
    Long sumAllPracticeMinutes();
}
