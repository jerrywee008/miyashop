import request from '@/utils/request'

// 获取商品列表
export function getProductList(params: {
  page?: number
  size?: number
  name?: string
  categoryId?: number
  sort?: string
}) {
  return request.get('/product/list', { params })
}

// 获取商品详情
export function getProductDetail(id: number | string) {
  return request.get(`/product/${id}`)
}

// 获取推荐商品
export function getRecommendProducts(params?: { page?: number; size?: number }) {
  return request.get('/product/recommend', { params })
}

// 获取分类列表
export function getCategoryList() {
  return request.get('/category/list')
}

// 获取商品评价
export function getProductReviews(productId: number, params?: { page?: number; size?: number }) {
  return request.get(`/product/${productId}/reviews`, { params })
}

// 获取SKU列表
export function getSkuList(params?: { page?: number; size?: number; productId?: number; status?: number }) {
  return request.get('/sku/list', { params })
}
