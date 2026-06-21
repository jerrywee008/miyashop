import request from '@/utils/request'

/**
 * 上传图片到 miyashop-image-server
 * @param file 图片文件
 * @returns 上传结果 { url, key, thumbnails, size, width, height }
 */
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/images/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
