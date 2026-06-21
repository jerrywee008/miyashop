import request from '@/utils/request'

// 获取仪表盘数据
export function getDashboard(): Promise<any> {
  return request.get('/dashboard')
}
