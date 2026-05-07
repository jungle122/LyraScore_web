package com.lyrascore.plan.mapper;

import com.lyrascore.plan.entity.Plan;
import com.lyrascore.plan.entity.PlanItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlanMapper {

    int insertPlan(Plan plan);

    List<Plan> selectPlansByUser(@Param("userId") Long userId);

    Plan selectPlanById(@Param("id") Long id);

    int deletePlanByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);

    // ── plan items ──
    int insertItem(@Param("planId") Long planId, @Param("scoreId") Long scoreId, @Param("sortOrder") Integer sortOrder);

    int deleteItem(@Param("planId") Long planId, @Param("scoreId") Long scoreId);

    List<PlanItem> selectItemsByPlan(@Param("planId") Long planId);
}
