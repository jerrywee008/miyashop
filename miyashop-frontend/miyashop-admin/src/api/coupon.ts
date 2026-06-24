import request from '@/utils/request'

// 优惠券列表
export function getCouponList(params?: {
  page?: number; size?: number; name?: string; type?: number; status?: number
}) {
  return request.get('/coupon/list', { params })
}

// 优惠券详情
export function getCouponDetail(id: number) {
  return request.get(`/coupon/${id}`)
}

// 新增优惠券
export function addCoupon(data: any) {
  return request.post('/coupon', data)
}

// 修改优惠券
export function updateCoupon(id: number, data: any) {
  return request.put(`/coupon/${id}`, data)
}

// 删除优惠券
export function deleteCoupon(id: number) {
  return request.delete(`/coupon/${id}`)
}

// 更新优惠券状态
export function updateCouponStatus(id: number, status: number) {
  return request.put(`/coupon/${id}/status`, null, { params: { status } })
}
