import request from '@/utils/request'

// 用户列表（调用后端 /member/list）
export function getUserList(params?: {
  page?: number
  size?: number
  keyword?: string
  mobile?: string
  status?: number
}) {
  return request.get('/member/list', { params })
}
