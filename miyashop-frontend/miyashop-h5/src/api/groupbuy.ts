import request from '@/utils/request'

// 获取团购活动列表
export function getGroupBuyActivities() {
  return request.get('/groupbuy/activities')
}

// 获取团购队伍详情
export function getTeamDetail(teamId: number | string) {
  return request.get(`/groupbuy/team/${teamId}`)
}

// 创建团购队伍
export function createTeam(data: { activityId: number; skuId: number }) {
  return request.post('/groupbuy/team/create', data)
}

// 加入团购队伍
export function joinTeam(teamId: number | string) {
  return request.post(`/groupbuy/team/${teamId}/join`)
}
