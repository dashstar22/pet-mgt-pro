import request from './request'

export function getPetList(params) {
  return request.get('/pets', { params })
}

export function getPetDetail(id) {
  return request.get(`/pets/${id}`)
}

export function getPetImages(id) {
  return request.get(`/pets/${id}/images`)
}
