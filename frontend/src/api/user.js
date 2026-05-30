import request from './request'

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}
