import request from '@/utils/request'

// 商品列表
export function getProductList(params: {
  page?: number
  size?: number
  name?: string
  categoryId?: number
  status?: number
}) {
  return request.get('/product/list', { params })
}

// 商品详情
export function getProductDetail(id: number) {
  return request.get(`/product/${id}`)
}

// 新增商品
export function addProduct(data: any) {
  return request.post('/product', data)
}

// 修改商品
export function updateProduct(id: number, data: any) {
  return request.put(`/product/${id}`, data)
}

// 删除商品
export function deleteProduct(id: number) {
  return request.delete(`/product/${id}`)
}

// 商品上下架
export function updateProductStatus(id: number, status: number) {
  return request.put(`/product/${id}/status`, null, { params: { status } })
}
