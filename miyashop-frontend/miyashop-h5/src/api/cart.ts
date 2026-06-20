import request from '@/utils/request'

// 获取购物车列表
export function getCartList() {
  return request.get('/cart/list')
}

// 添加商品到购物车
export function addToCart(data: { skuId: number; quantity: number }) {
  return request.post('/cart/add', data)
}

// 更新购物车商品数量
export function updateCartItem(id: number, data: { quantity: number }) {
  return request.put(`/cart/${id}`, data)
}

// 更新购物车选中状态
export function updateCartItemSelected(id: number, selected: number) {
  return request.put(`/cart/${id}/selected`, null, { params: { selected } })
}

// 删除购物车商品
export function deleteCartItem(id: number) {
  return request.delete(`/cart/${id}`)
}

// 全选/取消全选
export function selectAllCart(selected: number) {
  return request.put('/cart/select-all', null, { params: { selected } })
}

// 获取购物车统计
export function getCartCount() {
  return request.get('/cart/count')
}
