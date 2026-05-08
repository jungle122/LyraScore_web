package com.lyrascore.score.service;

import com.lyrascore.badge.service.BadgeService;
import com.lyrascore.common.BusinessException;
import com.lyrascore.score.dto.ScoreCreateRequest;
import com.lyrascore.score.dto.ScoreVO;
import com.lyrascore.score.entity.Score;
import com.lyrascore.score.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final int MAX_IMAGES = 12;

    private final ScoreMapper scoreMapper;
    private final BadgeService badgeService;

    @Value("${lyrascore.upload-dir:./uploads}")
    private String uploadDir;

    public Long create(MultipartFile[] files, ScoreCreateRequest req, Long userId) {
        if (files == null || files.length == 0) {
            throw new BusinessException(1101, "请上传至少一张乐谱图片");
        }
        if (files.length > MAX_IMAGES) {
            throw new BusinessException(1102, "最多上传 " + MAX_IMAGES + " 张图片");
        }

        Path dir = Paths.get(uploadDir, "scores").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException(1103, "目录创建失败：" + e.getMessage());
        }

        List<String> savedPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                    : "";
            if (!ALLOWED_EXT.contains(ext)) {
                throw new BusinessException(1104, "仅支持 jpg/png/gif/webp 格式图片");
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            try {
                file.transferTo(dir.resolve(filename));
            } catch (IOException e) {
                log.error("保存乐谱图片失败", e);
                throw new BusinessException(1105, "图片保存失败：" + e.getMessage());
            }
            savedPaths.add("/uploads/scores/" + filename);
        }
        if (savedPaths.isEmpty()) {
            throw new BusinessException(1106, "请上传有效图片");
        }

        Score score = new Score();
        score.setUserId(userId);
        score.setTitle(req.getTitle());
        score.setArtist(req.getArtist());
        score.setImageUrl(String.join(",", savedPaths));
        score.setTuning(req.getTuning());
        score.setCapo(req.getCapo());
        score.setBpm(req.getBpm());
        score.setIsPublic(req.getIsPublic() == null ? 0 : req.getIsPublic());
        score.setPracticeStatus(0);
        score.setInstrument(req.getInstrument());
        score.setStyle(req.getStyle());
        score.setMemo(req.getMemo());
        scoreMapper.insert(score);

        badgeService.evaluateAndAward(userId);
        return score.getId();
    }

    public List<ScoreVO> listMine(Long userId) {
        return scoreMapper.selectByUser(userId).stream()
                .map(ScoreVO::from)
                .collect(Collectors.toList());
    }

    public ScoreVO getById(Long id, Long userId) {
        Score s = scoreMapper.selectById(id);
        if (s == null) {
            throw new BusinessException(1107, "乐谱不存在");
        }
        if (!s.getUserId().equals(userId) && (s.getIsPublic() == null || s.getIsPublic() != 1)) {
            throw new BusinessException(403, "无权查看该乐谱");
        }
        return ScoreVO.from(s);
    }

    public void delete(Long id, Long userId) {
        try {
            int rows = scoreMapper.deleteByIdAndUser(id, userId);
            if (rows == 0) {
                throw new BusinessException(1108, "乐谱不存在或无权删除");
            }
        } catch (DataIntegrityViolationException e) {
            // 此时多半是 t_practice_log 的 RESTRICT 外键挡住了——历史日志不允许丢
            throw new BusinessException(1111,
                    "该乐谱已有练习日志记录，无法删除（历史数据需保留）。如需清理可先到日志页删除相关记录。");
        }
    }

    public void update(Long id, Long userId, com.lyrascore.score.dto.ScoreUpdateRequest req) {
        Score s = scoreMapper.selectById(id);
        if (s == null || !s.getUserId().equals(userId)) {
            throw new BusinessException(1112, "乐谱不存在或无权修改");
        }
        s.setTitle(req.getTitle());
        s.setArtist(req.getArtist());
        s.setTuning(req.getTuning());
        s.setCapo(req.getCapo());
        s.setBpm(req.getBpm());
        s.setIsPublic(req.getIsPublic() == null ? 0 : req.getIsPublic());
        s.setInstrument(req.getInstrument());
        s.setStyle(req.getStyle());
        s.setMemo(req.getMemo());
        scoreMapper.updateMeta(s);
    }

    public void updateStatus(Long id, Long userId, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException(1109, "状态值非法");
        }
        int rows = scoreMapper.updateStatus(id, userId, status);
        if (rows == 0) {
            throw new BusinessException(1110, "乐谱不存在或无权修改");
        }
    }
}
