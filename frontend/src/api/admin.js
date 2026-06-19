import request from './request'

// Dashboard
export function getStats() {
  return request.get('/admin/stats')
}

// User management
export function getUserList(params) {
  return request.get('/admin/users', { params })
}

export function getUserById(id) {
  return request.get(`/admin/users/${id}`)
}

export function createUser(data) {
  return request.post('/admin/users', data)
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

// Pet management
export function getAdminPetList(params) {
  return request.get('/admin/pets', { params })
}

export function createPet(formData) {
  return request.post('/admin/pets', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function updatePet(id, formData) {
  return request.put(`/admin/pets/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deletePet(id) {
  return request.delete(`/admin/pets/${id}`)
}

export function updatePetStatus(id, status) {
  return request.patch(`/admin/pets/${id}/status`, null, { params: { status } })
}

// Breed management
export function getAdminBreedList(params) {
  return request.get('/admin/breeds', { params })
}

export function createBreed(data) {
  return request.post('/admin/breeds', data)
}

export function updateBreed(id, data) {
  return request.put(`/admin/breeds/${id}`, data)
}

export function deleteBreed(id) {
  return request.delete(`/admin/breeds/${id}`)
}

// Application review
export function getAppList(params) {
  return request.get('/admin/applications', { params })
}

export function getAppDetail(id) {
  return request.get(`/admin/applications/${id}`)
}

export function approveApp(id, comment) {
  return request.put(`/admin/applications/${id}/approve`, { comment: comment || '' })
}

export function rejectApp(id, reason) {
  return request.put(`/admin/applications/${id}/reject`, { reason })
}

export function deleteApp(id) {
  return request.delete(`/admin/applications/${id}`)
}
