import request from '@/utils/request'

// 订单列表
export function getOrderList(params: {
  page?: number
  size?: number
  orderNo?: string
  status?: number
  startTime?: string
  endTime?: string
}) {
  return request.get('/order/list', { params })
}

// 订单详情
export function getOrderDetail(id: number) {
  return request.get(`/order/${id}`)
}

// 发货
export function shipOrder(id: number, data: { trackingNo: string; logisticsCompany: string }) {
  return request.put(`/order/${id}/ship`, data)
}

// 取消订单
export function cancelOrder(id: number, reason: string) {
  return request.put(`/order/${id}/cancel`, null, { params: { reason } })
}

// 完成订单
export function completeOrder(id: number) {
  return request.put(`/order/${id}/complete`)
}

// 退款处理
export function handleRefund(id: number, data: { agree: boolean; remark?: string }) {
  return request.put(`/order/${id}/refund`, data)
}
