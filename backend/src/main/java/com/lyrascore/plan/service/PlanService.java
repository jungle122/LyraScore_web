package com.lyrascore.plan.service;

import com.lyrascore.common.BusinessException;
import com.lyrascore.plan.dto.PlanCreateRequest;
import com.lyrascore.plan.dto.PlanDetailVO;
import com.lyrascore.plan.entity.Plan;
import com.lyrascore.plan.entity.PlanItem;
import com.lyrascore.plan.mapper.PlanMapper;
import com.lyrascore.score.entity.Score;
import com.lyrascore.score.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;
    private final ScoreMapper scoreMapper;

    public Long create(PlanCreateRequest req, Long userId) {
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new BusinessException(1201, "结束日期不能早于起始日期");
        }
        Plan plan = new Plan();
        plan.setUserId(userId);
        plan.setTitle(req.getTitle());
        plan.setStatus(0);
        plan.setStartDate(req.getStartDate());
        plan.setEndDate(req.getEndDate());
        planMapper.insertPlan(plan);
        return plan.getId();
    }

    public List<Plan> listMine(Long userId) {
        return planMapper.selectPlansByUser(userId);
    }

    public PlanDetailVO detail(Long planId, Long userId) {
        Plan p = planMapper.selectPlanById(planId);
        if (p == null || !p.getUserId().equals(userId)) {
            throw new BusinessException(1202, "计划不存在或无权访问");
        }
        List<PlanItem> items = planMapper.selectItemsByPlan(planId);
        return PlanDetailVO.from(p, items);
    }

    public void delete(Long planId, Long userId) {
        int rows = planMapper.deletePlanByIdAndUser(planId, userId);
        if (rows == 0) {
            throw new BusinessException(1203, "计划不存在或无权删除");
        }
    }

    public void updateStatus(Long planId, Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(1204, "状态值非法");
        }
        int rows = planMapper.updateStatus(planId, userId, status);
        if (rows == 0) {
            throw new BusinessException(1205, "计划不存在或无权修改");
        }
    }

    public void addItem(Long planId, Long scoreId, Long userId) {
        // 校验计划归属
        Plan p = planMapper.selectPlanById(planId);
        if (p == null || !p.getUserId().equals(userId)) {
            throw new BusinessException(1206, "计划不存在或无权操作");
        }
        // 校验乐谱归属
        Score s = scoreMapper.selectById(scoreId);
        if (s == null || !s.getUserId().equals(userId)) {
            throw new BusinessException(1207, "乐谱不存在或无权使用");
        }
        try {
            planMapper.insertItem(planId, scoreId, 0);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(1208, "该乐谱已在计划中");
        }
    }

    public void removeItem(Long planId, Long scoreId, Long userId) {
        Plan p = planMapper.selectPlanById(planId);
        if (p == null || !p.getUserId().equals(userId)) {
            throw new BusinessException(1209, "计划不存在或无权操作");
        }
        planMapper.deleteItem(planId, scoreId);
    }
}
