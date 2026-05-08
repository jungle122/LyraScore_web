import service from '@/utils/request'

// 数据大盘
export const getAdminDashboard = () => service.get('/admin/dashboard')

// 用户管理
export const adminListUsers     = ()              => service.get('/admin/users')
export const adminSetUserStatus = (id, status)    => service.put(`/admin/users/${id}/status`, { status })

// 字典管理
export const adminListDict   = ()              => service.get('/admin/dictionary')
export const adminCreateDict = (data)          => service.post('/admin/dictionary', data)
export const adminUpdateDict = (id, data)      => service.put(`/admin/dictionary/${id}`, data)
export const adminDeleteDict = (id)            => service.delete(`/admin/dictionary/${id}`)
