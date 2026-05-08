package com.lyrascore.song.controller;

import com.lyrascore.common.R;
import com.lyrascore.song.dto.SongRequestCreate;
import com.lyrascore.song.entity.SongRequest;
import com.lyrascore.song.service.SongRequestService;
import com.lyrascore.user.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/song-requests")
@RequiredArgsConstructor
public class SongRequestController {

    private final SongRequestService service;

    @PostMapping
    public R<Long> create(@Valid @RequestBody SongRequestCreate req) {
        return R.ok(service.create(req, UserContext.getUserId()));
    }

    @GetMapping("/sent")
    public R<List<SongRequest>> sent() {
        return R.ok(service.sent(UserContext.getUserId()));
    }

    @GetMapping("/received")
    public R<List<SongRequest>> received() {
        return R.ok(service.received(UserContext.getUserId()));
    }

    @PutMapping("/{id}/fulfill")
    public R<Void> fulfill(@PathVariable Long id) {
        service.fulfill(id, UserContext.getUserId());
        return R.ok(null);
    }

    @PutMapping("/{id}/retry")
    public R<SongRequest> retry(@PathVariable Long id) {
        return R.ok(service.retry(id, UserContext.getUserId()));
    }
}
