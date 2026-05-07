package com.lyrascore.friend.controller;

import com.lyrascore.common.R;
import com.lyrascore.friend.entity.Friendship;
import com.lyrascore.friend.service.FriendshipService;
import com.lyrascore.user.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService service;

    @PostMapping("/request")
    public R<Void> request(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return R.fail(400, "用户名不能为空");
        }
        service.requestByUsername(username.trim(), UserContext.getUserId());
        return R.ok(null);
    }

    @GetMapping
    public R<List<Friendship>> myFriends() {
        return R.ok(service.myFriends(UserContext.getUserId()));
    }

    @GetMapping("/pending")
    public R<List<Friendship>> pending() {
        return R.ok(service.pending(UserContext.getUserId()));
    }

    @PutMapping("/{id}/accept")
    public R<Void> accept(@PathVariable Long id) {
        service.accept(id, UserContext.getUserId());
        return R.ok(null);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id, UserContext.getUserId());
        return R.ok(null);
    }
}
