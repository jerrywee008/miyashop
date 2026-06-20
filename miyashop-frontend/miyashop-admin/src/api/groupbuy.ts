import request from '@/utils/request'

// 团购活动列表
export function getGroupBuyActivities(params?: { page?: number; size?: number; status?: number }) {
  return request.get('/groupbuy/activities', { params })
}

// 团购活动详情
export function getGroupBuyActivity(id: number) {
  return request.get(`/groupbuy/activity/${id}`)
}

// 创建团购活动
export function createGroupBuyActivity(data: any) {
  return request.post('/groupbuy/activity', data)
}

// 修改团购活动
export function updateGroupBuyActivity(id: number, data: any) {
  return request.put(`/groupbuy/activity/${id}`, data)
}

// 删除团购活动
export function deleteGroupBuyActivity(id: number) {
  return request.delete(`/groupbuy/activity/${id}`)
}

// 团购队伍列表
export function getTeamList(activityId: number, params?: { page?: number; size?: number; status?: number }) {
  return request.get(`/groupbuy/activity/${activityId}/teams`, { params })
}

// 团购队伍详情
export function getTeamDetail(teamId: number) {
  return request.get(`/groupbuy/team/${teamId}`)
}
