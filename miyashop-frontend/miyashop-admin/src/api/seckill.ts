import request from '@/utils/request'

// 秒杀活动列表
export function getSeckillActivities(params?: { page?: number; size?: number; status?: number }): Promise<any> {
  return request.get('/seckill/activities', { params })
}

// 秒杀活动详情
export function getSeckillActivity(id: number): Promise<any> {
  return request.get(`/seckill/activity/${id}`)
}

// 创建秒杀活动
export function createSeckillActivity(data: any): Promise<any> {
  return request.post('/seckill/activity', data)
}

// 修改秒杀活动
export function updateSeckillActivity(id: number, data: any): Promise<any> {
  return request.put(`/seckill/activity/${id}`, data)
}

// 删除秒杀活动
export function deleteSeckillActivity(id: number): Promise<any> {
  return request.delete(`/seckill/activity/${id}`)
}

// 秒杀商品列表
export function getSeckillSkus(activityId: number): Promise<any> {
  return request.get(`/seckill/activity/${activityId}/skus`)
}

// 添加秒杀商品
export function addSeckillSku(activityId: number, data: any): Promise<any> {
  return request.post(`/seckill/activity/${activityId}/skus`, data)
}

// 删除秒杀商品
export function deleteSeckillSku(activityId: number, skuId: number): Promise<any> {
  return request.delete(`/seckill/activity/${activityId}/skus/${skuId}`)
}
