import service from '@/utils/request'

export const listSetlists  = ()                 => service.get('/setlists')
export const getSetlist    = (id)               => service.get(`/setlists/${id}`)
export const createSetlist = (data)             => service.post('/setlists', data)
export const deleteSetlist = (id)               => service.delete(`/setlists/${id}`)
export const addSetlistItem    = (id, scoreId)  => service.post(`/setlists/${id}/items`, { scoreId })
export const removeSetlistItem = (id, scoreId)  => service.delete(`/setlists/${id}/items/${scoreId}`)
