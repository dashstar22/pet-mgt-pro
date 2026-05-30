import request from './request'

export function aiMatch(data) {
  return request.post('/ai-match', data)
}

export function getMatchHistory(params) {
  return request.get('/ai-match/history', { params })
}

export function deleteMatchRecord(id) {
  return request.delete(`/ai-match/history/${id}`)
}

export function aiChat(question) {
  return request.post('/ai-chat', { question })
}
