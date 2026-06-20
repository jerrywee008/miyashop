import request from '@/utils/request'

// 用户登录
export function login(data: { mobile: string; code?: string; password?: string }) {
  return request.post('/member/login', data)
}

// 发送验证码
export function sendSms(mobile: string) {
  return request.post('/member/send-sms', null, { params: { mobile } })
}

// 获取用户信息
export function getUserInfo() {
  return request.get('/member/info')
}

// 更新用户信息
export function updateUserInfo(data: any) {
  return request.put('/member/info', data)
}

// 获取收货地址列表
export function getAddressList() {
  return request.get('/member/address/list')
}

// 新增收货地址
export function addAddress(data: any) {
  return request.post('/member/address', data)
}

// 修改收货地址
export function updateAddress(id: number, data: any) {
  return request.put(`/member/address/${id}`, data)
}

// 删除收货地址
export function deleteAddress(id: number) {
  return request.delete(`/member/address/${id}`)
}

// 设置默认地址
export function setDefaultAddress(id: number) {
  return request.put(`/member/address/${id}/default`)
}

// 获取优惠券列表
export function getCouponList(params?: { status?: number }) {
  return request.get('/member/coupons', { params })
}

// 获取收藏列表
export function getFavoriteList(params?: { page?: number; size?: number }) {
  return request.get('/member/favorites', { params })
}
