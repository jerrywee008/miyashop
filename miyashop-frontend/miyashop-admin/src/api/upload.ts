import request from '@/utils/request'

/**
 * 上传图片到图片服务器
 * 返回 Result 包裹的上传数据: { code: 200, data: { url, key, thumbnails, size, width, height } }
 */
export function uploadImage(file: File): Promise<any> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/images/upload', formData, {
    timeout: 30000
  })
}
