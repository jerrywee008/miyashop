<template>
  <div class="team-page">
    <van-nav-bar title="拼团详情" left-arrow @click-left="$router.back()" fixed>
      <template #right>
        <van-icon name="share-o" size="20" @click="showShare" />
      </template>
    </van-nav-bar>

    <!-- 拼团状态条 -->
    <div class="team-status-bar" :class="teamStatusClass">
      <div class="status-content">
        <span class="status-icon">👥</span>
        <span class="status-text">{{ teamStatusText }}</span>
      </div>
      <van-count-down
        v-if="team.status < 2"
        :time="team.expireTime || countdownMs"
        format="HH:mm:ss"
        class="status-countdown"
      >
        <template #default="timeData">
          <span class="time-block">{{ timeData.hours.toString().padStart(2, '0') }}</span>
          <span>:</span>
          <span class="time-block">{{ timeData.minutes.toString().padStart(2, '0') }}</span>
          <span>:</span>
          <span class="time-block">{{ timeData.seconds.toString().padStart(2, '0') }}</span>
        </template>
      </van-count-down>
    </div>

    <!-- 商品信息 -->
    <div class="product-card">
      <img :src="team.productImage" :alt="team.productName" class="product-image" />
      <div class="product-info">
        <div class="product-name">{{ team.productName }}</div>
        <div class="product-price-row">
          <div class="groupbuy-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ team.groupbuyPrice }}</span>
          </div>
          <div class="original-price">¥{{ team.originalPrice }}</div>
          <div class="discount-tag">{{ discountPercent }}折</div>
        </div>
      </div>
    </div>

    <!-- 拼团进度 -->
    <div class="progress-section">
      <div class="section-header">
        <span class="section-title">拼团进度</span>
        <span class="progress-text">
          还差 <span class="highlight">{{ team.minPeople - team.currentPeople }}</span> 人成团
        </span>
      </div>
      <van-progress
        :percentage="teamProgress"
        color="#FF6B95"
        stroke-width="10"
        :show-pivot="true"
        pivot-color="#FF6B95"
        :pivot-text="`${team.currentPeople}/${team.minPeople}人`"
      />
    </div>

    <!-- 团员列表 -->
    <div class="members-section">
      <div class="section-header">
        <span class="section-title">已参团成员 ({{ team.currentPeople }}/{{ team.maxPeople || team.minPeople }})</span>
      </div>

      <div class="member-list">
        <div class="member-item" v-for="member in team.members" :key="member.id">
          <img :src="member.avatar" :alt="member.nickname" class="member-avatar" />
          <div class="member-info">
            <div class="member-name">
              {{ member.nickname }}
              <van-tag v-if="member.isLeader" type="danger" size="mini" round>团长</van-tag>
            </div>
            <div class="member-time">{{ member.joinTime }}</div>
          </div>
        </div>
      </div>

      <!-- 空位 -->
      <div class="empty-slots" v-if="team.maxPeople && team.currentPeople < team.maxPeople">
        <div
          class="empty-slot"
          v-for="i in (team.maxPeople - team.currentPeople)"
          :key="'slot-' + i"
        >
          <div class="slot-avatar">
            <van-icon name="plus" size="20" color="#ccc" />
          </div>
          <div class="slot-text">虚位以待</div>
        </div>
      </div>
    </div>

    <!-- 拼团规则 -->
    <div class="rules-section">
      <div class="section-title">拼团规则</div>
      <div class="rule-item">1. 在有效期内，达到成团人数即可享受拼团价</div>
      <div class="rule-item">2. 若未在有效期内成团，订单将自动取消并退款</div>
      <div class="rule-item">3. 每位用户仅可参与一次该商品的拼团活动</div>
      <div class="rule-item">4. 拼团成功后，商品将在48小时内发货</div>
    </div>

    <!-- 更多拼团 -->
    <div class="more-teams" v-if="otherTeams.length > 0">
      <div class="section-title">其他拼团队伍</div>
      <div
        class="other-team-item"
        v-for="ot in otherTeams"
        :key="ot.id"
        @click="$router.push(`/groupbuy/team/${ot.id}`)"
      >
        <div class="other-team-leader">
          <img :src="ot.leaderAvatar" class="leader-avatar" />
          <span>{{ ot.leaderName }}</span>
        </div>
        <div class="other-team-progress">
          <van-progress :percentage="ot.progress" color="#FF6B95" :show-pivot="false" stroke-width="6" />
          <span class="other-team-count">{{ ot.currentPeople }}/{{ ot.minPeople }}人</span>
        </div>
        <van-button size="small" type="danger" round custom-style="background: #FF6B95; border-color: #FF6B95">
          去参团
        </van-button>
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="bottom-action" v-if="team.status < 2">
      <van-button
        type="danger"
        block
        round
        @click="handleJoin"
        :loading="joining"
        custom-style="background: #FF6B95; border-color: #FF6B95"
      >
        ¥{{ team.groupbuyPrice }} 立即参团
      </van-button>
    </div>
    <div class="bottom-action ended" v-else>
      <van-button type="default" block round disabled>
        {{ team.status === 3 ? '拼团失败' : '拼团已结束' }}
      </van-button>
    </div>

    <!-- 分享面板 -->
    <van-share-sheet
      v-model:show="showShareVisible"
      title="分享给好友"
      :options="shareOptions"
      @select="onShareSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()

const joining = ref(false)
const countdownMs = ref(5 * 60 * 60 * 1000) // 5 hours

const team = ref<any>({
  id: 1,
  teamId: 'GB001',
  productName: '优雅碎花连衣裙',
  productImage: 'https://via.placeholder.com/300/FF6B95/FFFFFF?text=Team1',
  groupbuyPrice: 199,
  originalPrice: 399,
  minPeople: 3,
  maxPeople: 5,
  currentPeople: 2,
  status: 0, // 0-待成团 1-进行中 2-成功 3-失败
  expireTime: 5 * 60 * 60 * 1000,
  leaderId: 1,
  members: [
    { id: 1, nickname: '小美', avatar: 'https://via.placeholder.com/50/FF6B95/FFFFFF?text=L', joinTime: '2024-06-03 10:30', isLeader: true },
    { id: 2, nickname: '丽丽', avatar: 'https://via.placeholder.com/50/FFB6C1/FFFFFF?text=M2', joinTime: '2024-06-03 14:20', isLeader: false }
  ]
})

const otherTeams = ref([
  { id: 2, teamId: 'GB002', leaderName: '芳芳', leaderAvatar: 'https://via.placeholder.com/30/FFC0CB/FFFFFF', currentPeople: 2, minPeople: 3, progress: 66 },
  { id: 3, teamId: 'GB003', leaderName: '小红', leaderAvatar: 'https://via.placeholder.com/30/FF69B4/FFFFFF', currentPeople: 1, minPeople: 3, progress: 33 }
])

const teamProgress = computed(() => {
  return Math.round(team.value.currentPeople / team.value.minPeople * 100)
})

const teamStatusClass = computed(() => {
  const map: Record<number, string> = { 0: 'pending', 1: 'active', 2: 'success', 3: 'failed' }
  return `status-${map[team.value.status] || 'pending'}`
})

const teamStatusText = computed(() => {
  const map: Record<number, string> = { 0: '待成团', 1: '拼团进行中', 2: '拼团成功', 3: '拼团失败' }
  return map[team.value.status] || ''
})

const discountPercent = computed(() => {
  if (team.value.originalPrice && team.value.groupbuyPrice) {
    return (team.value.groupbuyPrice / team.value.originalPrice * 10).toFixed(1)
  }
  return '5.0'
})

const showShareVisible = ref(false)
const shareOptions = [
  { name: '微信好友', icon: 'wechat' },
  { name: '朋友圈', icon: 'wechat-moments' },
  { name: '复制链接', icon: 'link' }
]

const showShare = () => {
  showShareVisible.value = true
}

const onShareSelect = () => {
  showShareVisible.value = false
  showToast('分享功能开发中')
}

const handleJoin = async () => {
  joining.value = true
  setTimeout(() => {
    joining.value = false
    showToast('已加入拼团')

    // Update team state
    team.value.currentPeople++
    team.value.members.push({
      id: Date.now(),
      nickname: '我',
      avatar: 'https://via.placeholder.com/50/FF6B95/FFFFFF?text=ME',
      joinTime: new Date().toLocaleString(),
      isLeader: false
    })

    if (team.value.currentPeople >= team.value.minPeople) {
      team.value.status = 2
    }
  }, 800)
}

onMounted(async () => {
  const teamId = route.params.id
  try {
    const res = await fetch(`/api/groupbuy/team/${teamId}`).then(r => r.json())
    if (res.code === 200 && res.data) {
      team.value = res.data
    }
  } catch { /* use mock */ }
})
</script>

<style scoped>
.team-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
  padding-bottom: 80px;
}

/* 状态条 */
.team-status-bar {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.team-status-bar.status-pending {
  background: linear-gradient(135deg, #E6A23C, #F59E0B);
}

.team-status-bar.status-active {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
}

.team-status-bar.status-success {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.team-status-bar.status-failed {
  background: linear-gradient(135deg, #909399, #C0C4CC);
}

.status-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-icon {
  font-size: 24px;
}

.status-text {
  font-size: 16px;
  font-weight: 600;
}

.status-countdown {
  font-size: 16px;
  font-weight: 600;
}

.time-block {
  background: rgba(0, 0, 0, 0.25);
  padding: 2px 6px;
  border-radius: 4px;
}

/* 商品卡片 */
.product-card {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  display: flex;
  gap: 12px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.groupbuy-price .price-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
}

.original-price {
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.discount-tag {
  background: linear-gradient(135deg, #FF6B95, #FF5580);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

/* 进度 */
.progress-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-text .highlight {
  color: var(--primary-color);
  font-weight: 600;
}

/* 成员 */
.members-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.member-name {
  font-size: 13px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.member-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.empty-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
}

.empty-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.slot-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px dashed var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.slot-text {
  font-size: 11px;
  color: var(--text-secondary);
}

/* 规则 */
.rules-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.rule-item {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
  padding-left: 4px;
}

/* 更多拼团 */
.more-teams {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.other-team-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.other-team-item:last-child {
  border-bottom: none;
}

.other-team-leader {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-primary);
  min-width: 80px;
}

.leader-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.other-team-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
}

.other-team-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 16px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.bottom-action.ended {
  background: var(--bg-secondary);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

/* ========== PC 宽屏 ========== */
@media screen and (min-width: 768px) {
  .team-page {
    padding-bottom: 0;
    padding-top: 56px;
  }

  .team-status-bar,
  .product-card,
  .progress-section,
  .members-section,
  .rules-section,
  .more-teams {
    max-width: 800px;
    margin-left: auto;
    margin-right: auto;
  }

  .product-card {
    display: flex;
    align-items: center;
  }

  .product-card .product-image {
    width: 160px;
    height: 160px;
  }

  .bottom-action {
    max-width: 800px;
    left: 50% !important;
    transform: translateX(-50%);
  }
}
</style>
