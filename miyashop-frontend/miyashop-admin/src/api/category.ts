import request from '@/utils/request'

// 获取分类树
export function getCategoryTree() {
  return request.get('/category/tree')
}

// 获取分类列表
export function getCategoryList(params?: { parentId?: number; level?: number }) {
  return request.get('/category/list', { params })
}

// 获取分类详情
export function getCategoryDetail(id: number) {
  return request.get(`/category/${id}`)
}

// 新增分类
export function addCategory(data: { parentId: number; name: string; level: number; icon?: string; sort?: number }) {
  return request.post('/category', data)
}

// 修改分类
export function updateCategory(id: number, data: any) {
  return request.put(`/category/${id}`, data)
}

// 删除分类
export function deleteCategory(id: number) {
  return request.delete(`/category/${id}`)
}

// 更新分类状态
export function updateCategoryStatus(id: number, showStatus: number) {
  return request.put(`/category/${id}/status`, null, { params: { showStatus } })
}
