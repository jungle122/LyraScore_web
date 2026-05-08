package com.lyrascore.admin.controller;

import com.lyrascore.admin.dto.DashboardStatsVO;
import com.lyrascore.admin.mapper.AdminMapper;
import com.lyrascore.common.BusinessException;
import com.lyrascore.common.R;
import com.lyrascore.dictionary.entity.Dictionary;
import com.lyrascore.dictionary.mapper.DictionaryMapper;
import com.lyrascore.user.annotation.RequireRole;
import com.lyrascore.user.entity.User;
import com.lyrascore.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequireRole("admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final DictionaryMapper dictionaryMapper;

    // ──────────── 数据大盘 ────────────
    @GetMapping("/dashboard")
    public R<DashboardStatsVO> dashboard() {
        DashboardStatsVO v = new DashboardStatsVO();
        v.setUserCount(adminMapper.countUsers());
        v.setScoreCount(adminMapper.countScores());
        v.setPlanCount(adminMapper.countPlans());
        v.setLogCount(adminMapper.countLogs());
        v.setSetlistCount(adminMapper.countSetlists());
        v.setFriendshipCount(adminMapper.countFriendships());
        v.setSongRequestCount(adminMapper.countSongRequests());
        v.setBadgeAwardedCount(adminMapper.countUserBadges());
        Long total = adminMapper.sumAllPracticeMinutes();
        v.setTotalPracticeMinutes(total == null ? 0 : total);
        return R.ok(v);
    }

    // ──────────── 用户管理 ────────────
    @GetMapping("/users")
    public R<List<User>> listUsers() {
        List<User> users = userMapper.selectAll();
        // 抹掉密码哈希返回前端
        users.forEach(u -> u.setPasswordHash(null));
        return R.ok(users);
    }

    /** 冻结/解冻：body { status: 0=正常, 1=封禁 } */
    @PutMapping("/users/{id}/status")
    public R<Void> setUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(2001, "状态值非法：仅接受 0（正常）或 1（封禁）");
        }
        int rows = userMapper.updateStatus(id, status);
        if (rows == 0) throw new BusinessException(2002, "用户不存在");
        return R.ok(null);
    }

    // ──────────── 字典管理 ────────────
    @GetMapping("/dictionary")
    public R<List<Dictionary>> listDict() {
        return R.ok(dictionaryMapper.selectAll());
    }

    @PostMapping("/dictionary")
    public R<Long> createDict(@RequestBody Dictionary d) {
        if (d.getDictType() == null || d.getDictType().isBlank())
            throw new BusinessException(2003, "字典类型不能为空");
        if (d.getDictKey() == null || d.getDictKey().isBlank())
            throw new BusinessException(2004, "字典键不能为空");
        if (d.getDictValue() == null || d.getDictValue().isBlank())
            throw new BusinessException(2005, "字典展示值不能为空");
        if (d.getSortOrder() == null) d.setSortOrder(0);
        if (d.getIsActive() == null) d.setIsActive(1);
        try {
            dictionaryMapper.insert(d);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(2006, "该 dict_type + dict_key 已存在");
        }
        return R.ok(d.getId());
    }

    @PutMapping("/dictionary/{id}")
    public R<Void> updateDict(@PathVariable Long id, @RequestBody Dictionary d) {
        d.setId(id);
        try {
            int rows = dictionaryMapper.update(d);
            if (rows == 0) throw new BusinessException(2007, "字典记录不存在");
        } catch (DuplicateKeyException e) {
            throw new BusinessException(2008, "该 dict_type + dict_key 已被占用");
        }
        return R.ok(null);
    }

    @DeleteMapping("/dictionary/{id}")
    public R<Void> deleteDict(@PathVariable Long id) {
        int rows = dictionaryMapper.deleteById(id);
        if (rows == 0) throw new BusinessException(2009, "字典记录不存在");
        return R.ok(null);
    }
}
