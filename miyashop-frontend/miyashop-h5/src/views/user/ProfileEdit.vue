<template>
  <div class="profile-page">
    <van-nav-bar title="编辑资料" left-arrow @click="goBack" fixed />

    <div class="profile-content">
      <!-- 头像 -->
      <div class="avatar-section" @click="changeAvatar">
        <van-image round width="72" height="72" :src="formData.avatar || defaultAvatar" />
        <span class="avatar-hint">点击更换头像</span>
      </div>

      <!-- 表单 -->
      <van-form @submit="handleSave" class="form-section">
        <van-cell-group inset>
          <van-field
            v-model="formData.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称"
            maxlength="12"
            :rules="[{ required: true, message: '请输入昵称' }]"
          />
          <van-field
            v-model="formData.mobile"
            name="mobile"
            label="手机号"
            readonly
          >
            <template #extra>
              <span class="bound-text">已绑定</span>
            </template>
          </van-field>
          <van-field
            v-model="formData.email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱"
            type="email"
          />
          <van-field
            name="gender"
            label="性别"
          >
            <template #input>
              <van-radio-group v-model="formData.gender" direction="horizontal">
                <van-radio :name="0" checked-color="#FF6B95">保密</van-radio>
                <van-radio :name="1" checked-color="#FF6B95">男</van-radio>
                <van-radio :name="2" checked-color="#FF6B95">女</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="formData.birthday"
            name="birthday"
            label="生日"
            placeholder="请选择生日"
            readonly
            is-link
            @click="showBirthdayPicker = true"
          />
        </van-cell-group>

        <div style="margin: 24px 16px">
          <van-button
            type="danger"
            block
            round
            native-type="submit"
            :loading="saving"
            custom-style="background: #FF6B95; border-color: #FF6B95"
          >
            保存
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 生日选择器 -->
    <van-popup v-model:show="showBirthdayPicker" position="bottom" round>
      <van-date-picker
        v-model="birthdayValues"
        title="选择生日"
        :min-date="new Date(1950, 0, 1)"
        :max-date="new Date()"
        @confirm="onBirthdayConfirm"
        @cancel="showBirthdayPicker = false"
      />
    </van-popup>

    <!-- 头像选择面板 -->
    <van-action-sheet
      v-model:show="showAvatarSheet"
      :actions="avatarActions"
      cancel-text="取消"
      @select="onAvatarSelect"
    />
  </div>
</template>

<script setup lang="ts">
const goBack = () => { window.location.hash = '#/' }
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const saving = ref(false)
const showBirthdayPicker = ref(false)
const birthdayValues = ref(['2000', '01', '01'])

const formData = reactive({
  avatar: '',
  nickname: '',
  mobile: '',
  email: '',
  gender: 0,
  birthday: ''
})

const showAvatarSheet = ref(false)
const avatarActions = [
  { name: '拍照' },
  { name: '从相册选择' }
]

const changeAvatar = () => {
  showAvatarSheet.value = true
}

const onAvatarSelect = () => {
  showAvatarSheet.value = false
  showToast('头像更换功能开发中')
}

const onBirthdayConfirm = ({ selectedValues }: any) => {
  formData.birthday = selectedValues.join('-')
  showBirthdayPicker.value = false
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await fetch('/api/member/info', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${userStore.token}`
      },
      body: JSON.stringify(formData)
    }).then(r => r.json())

    if (res.code === 200) {
      userStore.setUserInfo({ ...userStore.userInfo, ...formData })
      showSuccessToast('保存成功')
      window.location.hash = '#/'
    } else {
      showToast(res.message || '保存失败')
    }
  } catch {
    showToast('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  const info = userStore.userInfo
  if (info) {
    formData.avatar = info.avatar || ''
    formData.nickname = info.nickname || ''
    formData.mobile = info.mobile || ''
    formData.email = info.email || ''
    formData.gender = info.gender ?? 0
    formData.birthday = info.birthday || ''
    if (info.birthday) {
      birthdayValues.value = info.birthday.split('-')
    }
  }
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 0 20px;
  gap: 10px;
}

.avatar-hint {
  font-size: 13px;
  color: var(--text-secondary);
}

.form-section {
  margin-top: 8px;
}

.bound-text {
  color: var(--success-color);
  font-size: 13px;
}
</style>
