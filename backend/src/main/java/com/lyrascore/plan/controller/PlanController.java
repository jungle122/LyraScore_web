package com.lyrascore.plan.controller;

import com.lyrascore.common.R;
import com.lyrascore.plan.dto.PlanCreateRequest;
import com.lyrascore.plan.dto.PlanDetailVO;
import com.lyrascore.plan.entity.Plan;
import com.lyrascore.plan.service.PlanService;
import com.lyrascore.user.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody PlanCreateRequest req) {
        return R.ok(planService.create(req, UserContext.getUserId()));
    }

    @GetMapping
    public R<List<Plan>> listMine() {
        return R.ok(planService.listMine(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public R<PlanDetailVO> detail(@PathVariable Long id) {
        return R.ok(planService.detail(id, UserContext.getUserId()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        planService.delete(id, UserContext.getUserId());
        return R.ok(null);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        planService.updateStatus(id, UserContext.getUserId(), body.get("status"));
        return R.ok(null);
    }

    @PostMapping("/{id}/items")
    public R<Void> addItem(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        planService.addItem(id, body.get("scoreId"), UserContext.getUserId());
        return R.ok(null);
    }

    @DeleteMapping("/{id}/items/{scoreId}")
    public R<Void> removeItem(@PathVariable Long id, @PathVariable Long scoreId) {
        planService.removeItem(id, scoreId, UserContext.getUserId());
        return R.ok(null);
    }
}
