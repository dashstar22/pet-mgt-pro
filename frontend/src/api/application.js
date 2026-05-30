import request from './request'

export function submit(data) {
  return request.post('/applications', data)
}

export function myList(params) {
  return request.get('/applications', { params })
}

export function getDetail(id) {
  return request.get(`/applications/${id}`)
}

export function cancel(id) {
  return request.put(`/applications/${id}/cancel`)
}

export function deleteApp(id) {
  return request.delete(`/applications/${id}`)
}
