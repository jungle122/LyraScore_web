import service from '@/utils/request'

export const listBadges    = () => service.get('/badges')
export const evaluateBadges = () => service.post('/badges/evaluate')
