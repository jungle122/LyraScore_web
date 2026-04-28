package com.lyrascore.user.context;

import lombok.AllArgsConstructor;
import lombok.Data;

public class UserContext {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    public static void set(Long userId, String username, String role) {
        HOLDER.set(new UserInfo(userId, username, role));
    }

    public static UserInfo get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    public static String getRole() {
        UserInfo u = HOLDER.get();
        return u == null ? null : u.getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
    }
}
