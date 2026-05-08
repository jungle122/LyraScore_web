package com.lyrascore.song.service;

import com.lyrascore.ai.AiService;
import com.lyrascore.common.BusinessException;
import com.lyrascore.song.dto.SongRequestCreate;
import com.lyrascore.song.entity.SongRequest;
import com.lyrascore.song.mapper.SongRequestMapper;
import com.lyrascore.user.entity.User;
import com.lyrascore.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongRequestService {

    private final SongRequestMapper songMapper;
    private final UserMapper userMapper;
    private final AiService aiService;

    public Long create(SongRequestCreate req, Long senderId) {
        User receiver = userMapper.selectById(req.getReceiverId());
        if (receiver == null) throw new BusinessException(1601, "对方用户不存在");
        if (receiver.getId().equals(senderId)) throw new BusinessException(1602, "不能给自己点歌");

        // 调 AI 清洗（失败不抛异常，按 CLAUDE.md 降级）
        String[] cleaned = aiService.cleanSongRequest(req.getRawInput());

        SongRequest s = new SongRequest();
        s.setSenderId(senderId);
        s.setReceiverId(req.getReceiverId());
        s.setRawInput(req.getRawInput());
        s.setRequestMessage(req.getRequestMessage());

        if (cleaned == null || (cleaned[0].isEmpty() && cleaned[1].isEmpty())) {
            // 降级：留 null + 状态 2 待人工审核
            s.setAiSong(null);
            s.setAiArtist(null);
            s.setStatus(2);
            log.info("点歌请求 AI 清洗失败/未启用，落库为待审核");
        } else {
            s.setAiSong(cleaned[0].isEmpty() ? null : cleaned[0]);
            s.setAiArtist(cleaned[1].isEmpty() ? null : cleaned[1]);
            s.setStatus(0);
        }
        songMapper.insert(s);
        return s.getId();
    }

    public List<SongRequest> sent(Long userId) {
        return songMapper.selectSentBy(userId);
    }

    public List<SongRequest> received(Long userId) {
        return songMapper.selectReceivedBy(userId);
    }

    public void fulfill(Long id, Long receiverId) {
        int rows = songMapper.updateStatus(id, receiverId, 1);
        if (rows == 0) throw new BusinessException(1603, "记录不存在或无权操作");
    }

    /** 重新清洗：发送方对结果不满意时用 raw_input 再调一次 AI */
    public SongRequest retry(Long id, Long senderId) {
        SongRequest s = songMapper.selectById(id);
        if (s == null || !s.getSenderId().equals(senderId)) {
            throw new BusinessException(1604, "记录不存在或无权操作");
        }
        if (s.getStatus() == 1) {
            throw new BusinessException(1605, "已还愿的请求不能重新清洗");
        }
        String[] cleaned = aiService.cleanSongRequest(s.getRawInput());
        if (cleaned == null || (cleaned[0].isEmpty() && cleaned[1].isEmpty())) {
            songMapper.updateAiResult(id, null, null, 2);
        } else {
            songMapper.updateAiResult(id,
                    cleaned[0].isEmpty() ? null : cleaned[0],
                    cleaned[1].isEmpty() ? null : cleaned[1],
                    0);
        }
        return songMapper.selectById(id);
    }
}
