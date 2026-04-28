package com.lyrascore.user.service;

import com.lyrascore.common.BusinessException;
import com.lyrascore.user.dto.LoginRequest;
import com.lyrascore.user.dto.LoginResponse;
import com.lyrascore.user.dto.RegisterRequest;
import com.lyrascore.user.entity.User;
import com.lyrascore.user.mapper.UserMapper;
import com.lyrascore.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public Long register(RegisterRequest req) {
        if (userMapper.selectByUsername(req.getUsername()) != null) {
            throw new BusinessException(1002, "用户名已被占用");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setRole("user");
        user.setStatus(0);
        userMapper.insert(user);
        return user.getId();
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectByUsername(req.getUsername());
        if (user == null) {
            throw new BusinessException(1003, "用户名或密码错误");
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(1003, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(1004, "账号已被封禁");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
