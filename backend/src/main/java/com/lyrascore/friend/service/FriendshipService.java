package com.lyrascore.friend.service;

import com.lyrascore.common.BusinessException;
import com.lyrascore.friend.entity.Friendship;
import com.lyrascore.friend.mapper.FriendshipMapper;
import com.lyrascore.user.entity.User;
import com.lyrascore.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;

    public void requestByUsername(String targetUsername, Long meId) {
        User target = userMapper.selectByUsername(targetUsername);
        if (target == null) throw new BusinessException(1501, "对方用户不存在");
        if (target.getId().equals(meId)) throw new BusinessException(1502, "不能加自己为好友");

        // 检查反向关系是否已存在（对方先发的申请）
        Friendship reverse = friendshipMapper.selectByPair(target.getId(), meId);
        if (reverse != null) {
            if (reverse.getStatus() == 1) throw new BusinessException(1503, "已经是好友了");
            if (reverse.getStatus() == 0) throw new BusinessException(1504, "对方已向你发起申请，请到「待处理」中接受");
        }

        try {
            friendshipMapper.insert(meId, target.getId());
        } catch (DuplicateKeyException e) {
            // 我已经发过申请了
            Friendship existing = friendshipMapper.selectByPair(meId, target.getId());
            if (existing != null && existing.getStatus() == 1) {
                throw new BusinessException(1503, "已经是好友了");
            }
            throw new BusinessException(1505, "申请已发出，请等待对方处理");
        }
    }

    public List<Friendship> myFriends(Long meId) {
        return friendshipMapper.selectFriendsOf(meId);
    }

    public List<Friendship> pending(Long meId) {
        return friendshipMapper.selectPendingForMe(meId);
    }

    public void accept(Long id, Long meId) {
        int rows = friendshipMapper.accept(id, meId);
        if (rows == 0) throw new BusinessException(1506, "申请不存在或无权处理");
    }

    public void delete(Long id, Long meId) {
        int rows = friendshipMapper.deleteByIdAndUser(id, meId);
        if (rows == 0) throw new BusinessException(1507, "记录不存在或无权操作");
    }
}
