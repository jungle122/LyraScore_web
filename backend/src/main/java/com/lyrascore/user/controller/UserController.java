package com.lyrascore.user.controller;

import com.lyrascore.common.R;
import com.lyrascore.user.context.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public R<UserContext.UserInfo> me() {
        return R.ok(UserContext.get());
    }
}
