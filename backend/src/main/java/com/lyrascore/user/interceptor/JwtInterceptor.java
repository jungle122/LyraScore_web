package com.lyrascore.user.interceptor;

import com.lyrascore.common.BusinessException;
import com.lyrascore.user.annotation.RequireRole;
import com.lyrascore.user.context.UserContext;
import com.lyrascore.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            throw new BusinessException(401, "未登录或 token 缺失");
        }
        String token = header.substring(PREFIX.length()).trim();

        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            throw new BusinessException(401, "token 无效或已过期");
        }

        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        UserContext.set(userId, username, role);

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !requireRole.value().equals(role)) {
            throw new BusinessException(403, "权限不足，需要角色: " + requireRole.value());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
