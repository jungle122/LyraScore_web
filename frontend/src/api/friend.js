import service from '@/utils/request'

export const myFriends     = ()         => service.get('/friends')
export const pending       = ()         => service.get('/friends/pending')
export const requestFriend = (username) => service.post('/friends/request', { username })
export const acceptFriend  = (id)       => service.put(`/friends/${id}/accept`)
export const deleteFriend  = (id)       => service.delete(`/friends/${id}`)
