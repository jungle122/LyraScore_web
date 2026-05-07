package com.lyrascore.score.service;

import com.lyrascore.common.BusinessException;
import com.lyrascore.score.dto.ScoreCreateRequest;
import com.lyrascore.score.dto.ScoreVO;
import com.lyrascore.score.entity.Score;
import com.lyrascore.score.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final ScoreMapper scoreMapper;

    @Value("${lyrascore.upload-dir:./uploads}")
    private String uploadDir;

    public Long create(MultipartFile file, ScoreCreateRequest req, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(1101, "请上传乐谱图片");
        }
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(1102, "仅支持 jpg/png/gif/webp 格式图片");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir, "scores").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            log.error("保存乐谱图片失败", e);
            throw new BusinessException(1103, "图片保存失败：" + e.getMessage());
        }

        Score score = new Score();
        score.setUserId(userId);
        score.setTitle(req.getTitle());
        score.setArtist(req.getArtist());
        score.setImageUrl("/uploads/scores/" + filename);
        score.setTuning(req.getTuning());
        score.setCapo(req.getCapo());
        score.setBpm(req.getBpm());
        score.setIsPublic(req.getIsPublic() == null ? 0 : req.getIsPublic());
        score.setMemo(req.getMemo());
        scoreMapper.insert(score);
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
            throw new BusinessException(1104, "乐谱不存在");
        }
        if (!s.getUserId().equals(userId) && (s.getIsPublic() == null || s.getIsPublic() != 1)) {
            throw new BusinessException(403, "无权查看该乐谱");
        }
        return ScoreVO.from(s);
    }

    public void delete(Long id, Long userId) {
        int rows = scoreMapper.deleteByIdAndUser(id, userId);
        if (rows == 0) {
            throw new BusinessException(1105, "乐谱不存在或无权删除");
        }
    }
}
