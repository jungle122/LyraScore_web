package com.lyrascore.score.controller;

import com.lyrascore.common.R;
import com.lyrascore.score.dto.ScoreCreateRequest;
import com.lyrascore.score.dto.ScoreUpdateRequest;
import com.lyrascore.score.dto.ScoreVO;
import com.lyrascore.score.service.ScoreService;
import com.lyrascore.user.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    public R<Long> create(@RequestParam("files") MultipartFile[] files,
                          @Valid @ModelAttribute ScoreCreateRequest req) {
        Long userId = UserContext.getUserId();
        Long id = scoreService.create(files, req, userId);
        return R.ok(id);
    }

    @GetMapping
    public R<List<ScoreVO>> listMine() {
        return R.ok(scoreService.listMine(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public R<ScoreVO> detail(@PathVariable Long id) {
        return R.ok(scoreService.getById(id, UserContext.getUserId()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        scoreService.delete(id, UserContext.getUserId());
        return R.ok(null);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        scoreService.updateStatus(id, UserContext.getUserId(), body.get("status"));
        return R.ok(null);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ScoreUpdateRequest req) {
        scoreService.update(id, UserContext.getUserId(), req);
        return R.ok(null);
    }
}
