import service from '@/utils/request'

export const createSongRequest = (data) => service.post('/song-requests', data)
export const sentRequests      = ()     => service.get('/song-requests/sent')
export const receivedRequests  = ()     => service.get('/song-requests/received')
export const fulfillRequest    = (id)   => service.put(`/song-requests/${id}/fulfill`)
export const retryRequest      = (id)   => service.put(`/song-requests/${id}/retry`)
export const getDictionary     = (type) => service.get(`/dictionary/${type}`)
