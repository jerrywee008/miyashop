<template>
  <div class="settings-page">
    <van-nav-bar title="设置" left-arrow @click-left="$router.back()" fixed />

    <!-- 账户设置 -->
    <div class="section">
      <div class="section-label">账户设置</div>
      <van-cell-group inset>
        <van-cell title="个人资料" is-link to="/user/profile" />
        <van-cell title="账号安全" is-link @click="showToast('账号安全')" />
        <van-cell title="收货地址" is-link to="/user/address" />
      </van-cell-group>
    </div>

    <!-- 通用设置 -->
    <div class="section">
      <div class="section-label">通用设置</div>
      <van-cell-group inset>
        <van-cell title="消息通知" is-link>
          <template #default>
            <van-switch v-model="settings.notification" active-color="#FF6B95" size="22" />
          </template>
        </van-cell>
        <van-cell title="隐私设置" is-link @click="showToast('隐私设置')" />
        <van-cell title="清除缓存" is-link @click="handleClearCache">
          <template #default>
            <span class="cell-value">{{ cacheSize }}</span>
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- 关于 -->
    <div class="section">
      <div class="section-label">关于</div>
      <van-cell-group inset>
        <van-cell title="检查更新" is-link>
          <template #default>
            <span class="cell-value">v1.0.0</span>
          </template>
        </van-cell>
        <van-cell title="关于 MiyaShop" is-link @click="showAbout" />
        <van-cell title="用户协议" is-link @click="showToast('用户协议')" />
        <van-cell title="隐私政策" is-link @click="showToast('隐私政策')" />
      </van-cell-group>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <van-button
        type="default"
        block
        round
        @click="handleLogout"
        custom-style="color: #F56C6C; border-color: #F56C6C"
      >
        退出登录
      </van-button>
    </div>

    <!-- 关于弹窗 -->
    <van-dialog
      v-model:show="showAboutDialog"
      title="关于 MiyaShop"
      :show-cancel-button="false"
      confirm-button-text="好的"
      confirm-button-color="#FF6B95"
    >
      <div class="about-content">
        <p>🌸 MiyaShop v1.0.0</p>
        <p>一个专注于女装、首饰、化妆用品的电商平台</p>
        <p>具备秒杀和团购功能</p>
        <br />
        <p style="color: #999; font-size: 12px">© 2024 MiyaShop. All rights reserved.</p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

const cacheSize = ref('2.3MB')
const showAboutDialog = ref(false)

const settings = reactive({
  notification: true
})

const handleClearCache = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定清除缓存吗？' })
    cacheSize.value = '0B'
    showSuccessToast('缓存已清除')
  } catch { /* canceled */ }
}

const showAbout = () => {
  showAboutDialog.value = true
}

const handleLogout = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定退出登录吗？' })
    userStore.logout()
    router.push('/')
    showSuccessToast('已退出登录')
  } catch { /* canceled */ }
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
}

.section {
  margin-bottom: 16px;
}

.section-label {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 12px 16px 8px;
}

.cell-value {
  font-size: 13px;
  color: var(--text-secondary);
}

.logout-section {
  padding: 24px 16px;
}

.about-content {
  text-align: center;
  padding: 16px;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.8;
}
</style>
