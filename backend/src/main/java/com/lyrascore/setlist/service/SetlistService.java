package com.lyrascore.setlist.service;

import com.lyrascore.badge.service.BadgeService;
import com.lyrascore.common.BusinessException;
import com.lyrascore.score.entity.Score;
import com.lyrascore.score.mapper.ScoreMapper;
import com.lyrascore.setlist.dto.SetlistCreateRequest;
import com.lyrascore.setlist.dto.SetlistDetailVO;
import com.lyrascore.setlist.entity.Setlist;
import com.lyrascore.setlist.entity.SetlistItem;
import com.lyrascore.setlist.mapper.SetlistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetlistService {

    private final SetlistMapper setlistMapper;
    private final ScoreMapper scoreMapper;
    private final BadgeService badgeService;

    public Long create(SetlistCreateRequest req, Long userId) {
        Setlist s = new Setlist();
        s.setUserId(userId);
        s.setName(req.getName());
        s.setDescription(req.getDescription());
        s.setCoverUrl(req.getCoverUrl());
        setlistMapper.insert(s);
        badgeService.evaluateAndAward(userId);
        return s.getId();
    }

    public List<Setlist> listMine(Long userId) {
        return setlistMapper.selectByUser(userId);
    }

    public SetlistDetailVO detail(Long id, Long userId) {
        Setlist s = setlistMapper.selectById(id);
        if (s == null || !s.getUserId().equals(userId)) {
            throw new BusinessException(1401, "歌单不存在或无权访问");
        }
        List<SetlistItem> items = setlistMapper.selectItemsBySetlist(id);
        return SetlistDetailVO.from(s, items);
    }

    public void delete(Long id, Long userId) {
        int rows = setlistMapper.deleteByIdAndUser(id, userId);
        if (rows == 0) throw new BusinessException(1402, "歌单不存在或无权删除");
    }

    public void addItem(Long setlistId, Long scoreId, Long userId) {
        Setlist s = setlistMapper.selectById(setlistId);
        if (s == null || !s.getUserId().equals(userId)) {
            throw new BusinessException(1403, "歌单不存在或无权操作");
        }
        Score sc = scoreMapper.selectById(scoreId);
        if (sc == null) throw new BusinessException(1404, "乐谱不存在");
        // 收藏不要求是自己的乐谱（公开乐谱可以收藏到歌单）
        if (!sc.getUserId().equals(userId) && (sc.getIsPublic() == null || sc.getIsPublic() != 1)) {
            throw new BusinessException(1405, "无权收藏该乐谱");
        }
        try {
            setlistMapper.insertItem(setlistId, scoreId);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(1406, "已在歌单中");
        }
    }

    public void removeItem(Long setlistId, Long scoreId, Long userId) {
        Setlist s = setlistMapper.selectById(setlistId);
        if (s == null || !s.getUserId().equals(userId)) {
            throw new BusinessException(1407, "歌单不存在或无权操作");
        }
        setlistMapper.deleteItem(setlistId, scoreId);
    }
}
