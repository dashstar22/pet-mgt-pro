import axios from 'axios'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/api/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(localStorage.getItem('token') ? { Authorization: `Bearer ${localStorage.getItem('token')}` } : {}),
    },
  }).then(res => {
    if (res.data.code === 200) return res.data.data
    throw new Error(res.data.msg)
  })
}
