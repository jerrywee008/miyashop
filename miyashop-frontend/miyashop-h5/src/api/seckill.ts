import request from '@/utils/request'

// 获取秒杀活动列表
export function getSeckillActivities() {
  return request.get('/seckill/activities')
}

// 获取秒杀活动详情
export function getSeckillActivity(id: number) {
  return request.get(`/seckill/activity/${id}`)
}

// 获取秒杀活动下的商品SKU列表
export function getSeckillSkus(activityId: number) {
  return request.get(`/seckill/activity/${activityId}/skus`)
}

// 秒杀下单
export function seckillOrder(data: { skuId: number; quantity: number; activityId: number }) {
  return request.post('/seckill/order', data)
}
