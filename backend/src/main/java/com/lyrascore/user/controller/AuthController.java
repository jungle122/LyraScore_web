package com.lyrascore.user.controller;

import com.lyrascore.common.R;
import com.lyrascore.user.dto.LoginRequest;
import com.lyrascore.user.dto.LoginResponse;
import com.lyrascore.user.dto.RegisterRequest;
import com.lyrascore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public R<Long> register(@Valid @RequestBody RegisterRequest req) {
        Long userId = userService.register(req);
        return R.ok(userId);
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(userService.login(req));
    }
}
