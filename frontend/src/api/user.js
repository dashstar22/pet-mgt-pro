import request from './request'

export function getProfile({ silent = false } = {}) {
  const config = {}
  if (silent) {
    config.headers = { 'X-Silent': '1' }
  }
  return request.get('/user/profile', config)
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}
