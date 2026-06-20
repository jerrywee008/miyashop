<template>
  <div class="login-page">
    <van-nav-bar title="登录" left-arrow @click-left="$router.back()" />

    <div class="login-content">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-icon">🌸</div>
        <div class="logo-title">MiyaShop</div>
        <div class="logo-subtitle">女装电商 · 发现你的美</div>
      </div>

      <!-- 手机号登录 -->
      <div class="form-section">
        <van-form @submit="handleLogin">
          <van-cell-group inset>
            <van-field
              v-model="mobile"
              name="mobile"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              maxlength="11"
              :rules="[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
              ]"
            >
              <template #left-icon>
                <van-icon name="phone-o" />
              </template>
            </van-field>

            <van-field
              v-model="code"
              name="code"
              label="验证码"
              placeholder="请输入验证码"
              maxlength="6"
              :rules="[{ required: true, message: '请输入验证码' }]"
            >
              <template #left-icon>
                <van-icon name="shield-o" />
              </template>
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  plain
                  :disabled="countdown > 0"
                  @click.prevent="sendCode"
                  custom-style="border-color: #FF6B95; color: #FF6B95"
                >
                  {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <!-- 协议 -->
          <div class="agreement">
            <van-checkbox v-model="agreed" icon-size="16" checked-color="#FF6B95" />
            <span class="agreement-text">
              登录即表示同意
              <a @click.stop="showAgreement('service')">《用户服务协议》</a>
              和
              <a @click.stop="showAgreement('privacy')">《隐私政策》</a>
            </span>
          </div>

          <div style="margin: 16px">
            <van-button
              type="danger"
              block
              round
              native-type="submit"
              :loading="loading"
              :disabled="!agreed"
              custom-style="background: #FF6B95; border-color: #FF6B95; height: 44px"
            >
              登录
            </van-button>
          </div>
        </van-form>

        <!-- 其他登录方式 -->
        <div class="other-login">
          <div class="divider">
            <span class="divider-text">其他登录方式</span>
          </div>
          <div class="other-icons">
            <div class="other-item" @click="handleWechatLogin">
              <van-icon name="wechat" size="28" color="#07C160" />
              <span>微信</span>
            </div>
            <div class="other-item" @click="handleAlipayLogin">
              <van-icon name="alipay" size="28" color="#1677FF" />
              <span>支付宝</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const mobile = ref('')
const code = ref('')
const agreed = ref(true)
const loading = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const sendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(mobile.value)) {
    showToast('请输入正确的手机号')
    return
  }
  // Mock send SMS
  showSuccessToast('验证码已发送')
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (timer) clearInterval(timer)
    }
  }, 1000)
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/member/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mobile: mobile.value, code: code.value })
    }).then(r => r.json())

    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      showSuccessToast('登录成功')
      router.back()
    } else {
      showToast(res.message || '登录失败')
    }
  } catch {
    // Mock login success
    userStore.setToken('mock-token-' + Date.now())
    userStore.setUserInfo({
      id: 1,
      userId: 'U001',
      nickname: '小美',
      mobile: mobile.value,
      avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      gender: 2,
      level: 3,
      points: 1560,
      balance: 288.50,
      totalSpent: 3580
    })
    showSuccessToast('登录成功')
    router.back()
  } finally {
    loading.value = false
  }
}

const handleWechatLogin = () => showToast('微信登录功能开发中')
const handleAlipayLogin = () => showToast('支付宝登录功能开发中')
const showAgreement = (type: string) => showToast(type === 'service' ? '用户服务协议' : '隐私政策')

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.login-content {
  padding: 20px 16px;
}

.logo-section {
  text-align: center;
  padding: 40px 0 30px;
}

.logo-icon {
  font-size: 56px;
  margin-bottom: 12px;
}

.logo-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.logo-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.form-section {
  margin-top: 16px;
}

.agreement {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 8px;
}

.agreement-text {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.agreement-text a {
  color: var(--primary-color);
}

.other-login {
  margin-top: 32px;
}

.divider {
  display: flex;
  align-items: center;
  margin: 20px 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.divider-text {
  padding: 0 16px;
}

.other-icons {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-top: 20px;
}

.other-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
