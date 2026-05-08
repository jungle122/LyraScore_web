import service from '@/utils/request'

export const listLogs  = (limit)   => service.get('/logs', { params: { limit } })
export const createLog = (data)    => service.post('/logs', data)
export const deleteLog = (id)      => service.delete(`/logs/${id}`)
export const getStats  = ()        => service.get('/logs/stats')
