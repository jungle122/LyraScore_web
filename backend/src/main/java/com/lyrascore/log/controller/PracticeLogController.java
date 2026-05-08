package com.lyrascore.log.controller;

import com.lyrascore.common.R;
import com.lyrascore.log.dto.LogCreateRequest;
import com.lyrascore.log.entity.PracticeLog;
import com.lyrascore.log.service.PracticeLogService;
import com.lyrascore.user.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class PracticeLogController {

    private final PracticeLogService logService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody LogCreateRequest req) {
        return R.ok(logService.create(req, UserContext.getUserId()));
    }

    @GetMapping
    public R<List<PracticeLog>> listMine(@RequestParam(required = false) Integer limit) {
        return R.ok(logService.listMine(UserContext.getUserId(), limit));
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(logService.stats(UserContext.getUserId()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        logService.delete(id, UserContext.getUserId());
        return R.ok(null);
    }
}
