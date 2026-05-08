import service from '@/utils/request'

export function listScores() {
  return service.get('/scores')
}

export function getScore(id) {
  return service.get(`/scores/${id}`)
}

export function deleteScore(id) {
  return service.delete(`/scores/${id}`)
}

// 带文件的创建：用 FormData，axios 会自动设 Content-Type: multipart/form-data
export function createScore(formData) {
  return service.post('/scores', formData)
}

export function updateScoreStatus(id, status) {
  return service.put(`/scores/${id}/status`, { status })
}

export function updateScore(id, data) {
  return service.put(`/scores/${id}`, data)
}
