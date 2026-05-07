import service from '@/utils/request'

export const listPlans  = ()             => service.get('/plans')
export const getPlan    = (id)           => service.get(`/plans/${id}`)
export const createPlan = (data)         => service.post('/plans', data)
export const deletePlan = (id)           => service.delete(`/plans/${id}`)
export const updatePlanStatus = (id, status) => service.put(`/plans/${id}/status`, { status })
export const addPlanItem    = (id, scoreId)   => service.post(`/plans/${id}/items`, { scoreId })
export const removePlanItem = (id, scoreId)   => service.delete(`/plans/${id}/items/${scoreId}`)
