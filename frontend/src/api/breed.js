import request from './request'

export function getBreeds(petType) {
  return request.get('/breeds', { params: petType ? { petType } : {} })
}
