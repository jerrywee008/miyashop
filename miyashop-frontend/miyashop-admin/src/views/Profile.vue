<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <div class="profile-content">
      <div class="profile-card">
        <div class="profile-avatar">
          <el-avatar :size="80" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        </div>
        <div class="profile-info">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userStore.userInfo?.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">姓名</span>
            <span class="info-value">{{ userStore.userInfo?.realName || '管理员' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">角色</span>
            <span class="info-value">
              <el-tag type="danger" size="small">管理员</el-tag>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
</script>

<style scoped>
.profile-content {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.profile-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 40px 60px;
  text-align: center;
  min-width: 400px;
}

.profile-avatar {
  margin-bottom: 28px;
}

.profile-info {
  text-align: left;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
}
</style>
