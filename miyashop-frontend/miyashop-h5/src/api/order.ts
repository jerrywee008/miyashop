import request from '@/utils/request'

// 创建订单
export function createOrder(data: {
  addressId: number
  items: Array<{ skuId: number; productId: number; quantity: number }>
  payType: number
  couponId?: number
  remark?: string
}) {
  return request.post('/order/create', data)
}

// 获取订单列表
export function getOrderList(params: { page?: number; size?: number; status?: number }) {
  return request.get('/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(id: number | string) {
  return request.get(`/order/${id}`)
}

// 取消订单
export function cancelOrder(id: number, reason?: string) {
  return request.put(`/order/${id}/cancel`, null, { params: { reason } })
}

// 确认收货
export function confirmOrder(id: number) {
  return request.put(`/order/${id}/confirm`)
}

// 获取物流信息
export function getOrderLogistics(id: number) {
  return request.get(`/order/${id}/logistics`)
}
