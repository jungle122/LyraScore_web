package com.lyrascore.badge.controller;

import com.lyrascore.badge.entity.Badge;
import com.lyrascore.badge.service.BadgeService;
import com.lyrascore.common.R;
import com.lyrascore.user.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /** 当前用户视角：所有徽章 + 是否已解锁 + 解锁时间 */
    @GetMapping
    public R<List<Badge>> listMine() {
        return R.ok(badgeService.listForUser(UserContext.getUserId()));
    }

    /** 手动触发评估（演示用，正常情况下事件监听器自动跑） */
    @PostMapping("/evaluate")
    public R<List<Badge>> evaluate() {
        Long userId = UserContext.getUserId();
        badgeService.evaluateAndAward(userId);
        return R.ok(badgeService.listForUser(userId));
    }
}
