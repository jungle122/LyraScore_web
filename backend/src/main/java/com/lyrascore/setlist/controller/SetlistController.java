package com.lyrascore.setlist.controller;

import com.lyrascore.common.R;
import com.lyrascore.setlist.dto.SetlistCreateRequest;
import com.lyrascore.setlist.dto.SetlistDetailVO;
import com.lyrascore.setlist.entity.Setlist;
import com.lyrascore.setlist.service.SetlistService;
import com.lyrascore.user.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/setlists")
@RequiredArgsConstructor
public class SetlistController {

    private final SetlistService service;

    @PostMapping
    public R<Long> create(@Valid @RequestBody SetlistCreateRequest req) {
        return R.ok(service.create(req, UserContext.getUserId()));
    }

    @GetMapping
    public R<List<Setlist>> listMine() {
        return R.ok(service.listMine(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public R<SetlistDetailVO> detail(@PathVariable Long id) {
        return R.ok(service.detail(id, UserContext.getUserId()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id, UserContext.getUserId());
        return R.ok(null);
    }

    @PostMapping("/{id}/items")
    public R<Void> addItem(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        service.addItem(id, body.get("scoreId"), UserContext.getUserId());
        return R.ok(null);
    }

    @DeleteMapping("/{id}/items/{scoreId}")
    public R<Void> removeItem(@PathVariable Long id, @PathVariable Long scoreId) {
        service.removeItem(id, scoreId, UserContext.getUserId());
        return R.ok(null);
    }
}
