package com.lyrascore.friend.mapper;

import com.lyrascore.friend.entity.Friendship;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FriendshipMapper {

    int insert(@Param("user1") Long user1, @Param("user2") Long user2);

    Friendship selectByPair(@Param("user1") Long user1, @Param("user2") Long user2);

    Friendship selectById(@Param("id") Long id);

    int accept(@Param("id") Long id, @Param("receiverId") Long receiverId);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 我的好友（status=1）：自引用 N:M 双向查询 */
    List<Friendship> selectFriendsOf(@Param("userId") Long userId);

    /** 待我处理的申请：user_id_2=me AND status=0 */
    List<Friendship> selectPendingForMe(@Param("userId") Long userId);
}
