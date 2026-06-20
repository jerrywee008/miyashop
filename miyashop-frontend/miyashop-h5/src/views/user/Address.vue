<template>
  <div class="address-page">
    <van-nav-bar title="收货地址" left-arrow @click-left="$router.back()" fixed>
      <template #right>
        <span class="nav-add" @click="handleAdd">新增</span>
      </template>
    </van-nav-bar>

    <!-- 空状态 -->
    <van-empty v-if="addressList.length === 0" description="暂无收货地址">
      <van-button type="danger" round @click="handleAdd" custom-style="background: #FF6B95; border-color: #FF6B95">
        新增地址
      </van-button>
    </van-empty>

    <!-- 地址列表 -->
    <div class="address-list" v-else>
      <div
        class="address-card"
        v-for="addr in addressList"
        :key="addr.id"
        :class="{ selected: selectMode && selectedId === addr.id }"
        @click="selectAddress(addr)"
      >
        <div class="address-header">
          <span class="address-name">{{ addr.name }}</span>
          <span class="address-phone">{{ addr.phone }}</span>
          <van-tag v-if="addr.isDefault" type="danger" size="mini" round>默认</van-tag>
        </div>
        <div class="address-detail">
          {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
        </div>
        <div class="address-actions">
          <van-radio-group v-model="defaultId" v-if="!selectMode">
            <van-radio
              :name="addr.id"
              checked-color="#FF6B95"
              @click="setDefault(addr)"
            >
              设为默认地址
            </van-radio>
          </van-radio-group>
          <div class="action-btns">
            <van-button size="small" plain type="primary" @click.stop="handleEdit(addr)">编辑</van-button>
            <van-button size="small" plain type="danger" @click.stop="handleDelete(addr)">删除</van-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址编辑弹窗 -->
    <van-popup
      v-model:show="showPopup"
      position="bottom"
      :style="{ height: '75%' }"
      round
      closeable
      close-icon-position="top-right"
    >
      <div class="popup-title">{{ isEdit ? '编辑地址' : '新增地址' }}</div>
      <div class="popup-content">
        <van-form @submit="handleSave">
          <van-cell-group inset>
            <van-field
              v-model="formData.name"
              name="name"
              label="收货人"
              placeholder="请输入收货人姓名"
              :rules="[{ required: true, message: '请输入收货人姓名' }]"
            />
            <van-field
              v-model="formData.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              :rules="[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
              ]"
            />
            <van-field
              v-model="addressText"
              name="region"
              label="所在地区"
              placeholder="请选择省市区"
              readonly
              is-link
              @click="showRegionPicker = true"
              :rules="[{ required: true, message: '请选择所在地区' }]"
            />
            <van-field
              v-model="formData.detail"
              name="detail"
              label="详细地址"
              placeholder="街道、门牌号等"
              type="textarea"
              :rows="2"
              :rules="[{ required: true, message: '请输入详细地址' }]"
            />
          </van-cell-group>

          <div class="default-switch">
            <span>设为默认地址</span>
            <van-switch v-model="formData.isDefault" active-color="#FF6B95" size="22" />
          </div>

          <div style="margin: 16px">
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
    </van-popup>

    <!-- 地区选择器 -->
    <van-popup v-model:show="showRegionPicker" position="bottom" round :style="{ height: '60%' }">
      <van-picker
        title="选择地区"
        :columns="regionColumns"
        @confirm="onRegionConfirm"
        @cancel="showRegionPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'

const router = useRouter()
const route = useRoute()

const selectMode = computed(() => route.query.select === '1')
const selectedId = ref<number>()

// ---------- 地址列表 ----------
const addressList = ref<any[]>([])
const defaultId = ref<number>()

const setDefault = async (addr: any) => {
  defaultId.value = addr.id
  addressList.value.forEach(a => a.isDefault = a.id === addr.id)
  showSuccessToast('已设为默认地址')
}

const selectAddress = (addr: any) => {
  if (selectMode.value) {
    selectedId.value = addr.id
    // 返回结算页
    localStorage.setItem('selectedAddress', JSON.stringify(addr))
    router.back()
  }
}

const handleDelete = async (addr: any) => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除该地址吗？' })
    addressList.value = addressList.value.filter(a => a.id !== addr.id)
    showSuccessToast('已删除')
  } catch { /* canceled */ }
}

// ---------- 新增/编辑 ----------
const showPopup = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const showRegionPicker = ref(false)

const formData = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const addressText = computed(() => {
  if (!formData.province) return ''
  return `${formData.province} ${formData.city} ${formData.district}`
})

const regionColumns = ref([
  {
    values: ['广东省', '北京市', '上海市', '浙江省', '江苏省'],
    defaultIndex: 0
  },
  {
    values: ['广州市', '深圳市', '东莞市', '佛山市'],
    defaultIndex: 0
  },
  {
    values: ['天河区', '越秀区', '海珠区', '白云区', '番禺区'],
    defaultIndex: 0
  }
])

const onRegionConfirm = ({ selectedOptions }: any) => {
  formData.province = selectedOptions[0]
  formData.city = selectedOptions[1]
  formData.district = selectedOptions[2]
  showRegionPicker.value = false
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showPopup.value = true
}

const handleEdit = (addr: any) => {
  isEdit.value = true
  formData.name = addr.name
  formData.phone = addr.phone
  formData.province = addr.province
  formData.city = addr.city
  formData.district = addr.district
  formData.detail = addr.detail
  formData.isDefault = addr.isDefault
  showPopup.value = true
}

const resetForm = () => {
  formData.name = ''
  formData.phone = ''
  formData.province = ''
  formData.city = ''
  formData.district = ''
  formData.detail = ''
  formData.isDefault = false
}

const handleSave = async () => {
  saving.value = true
  setTimeout(() => {
    if (isEdit.value) {
      const idx = addressList.value.findIndex(a => a.id === selectedId.value)
      if (idx > -1) {
        Object.assign(addressList.value[idx], { ...formData })
      }
    } else {
      const newAddr = {
        id: Date.now(),
        ...formData
      }
      addressList.value.push(newAddr)
    }
    if (formData.isDefault) {
      addressList.value.forEach(a => a.isDefault = a.id === (isEdit.value ? selectedId.value : addressList.value[addressList.value.length - 1].id))
    }
    saving.value = false
    showPopup.value = false
    showSuccessToast(isEdit.value ? '已更新' : '已添加')
  }, 500)
}

onMounted(() => {
  // Load addresses
  addressList.value = [
    { id: 1, name: '小美', phone: '138****0001', province: '广东省', city: '广州市', district: '天河区', detail: '珠江新城街道100号', isDefault: true },
    { id: 2, name: '丽丽', phone: '139****0002', province: '广东省', city: '深圳市', district: '南山区', detail: '科技园路50号', isDefault: false }
  ]
  defaultId.value = addressList.value.find(a => a.isDefault)?.id
})
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-top: 46px;
}

.nav-add {
  font-size: 14px;
  color: var(--primary-color);
}

.address-list {
  padding: 12px;
}

.address-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.address-card.selected {
  border-color: var(--primary-color);
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.address-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.address-phone {
  font-size: 14px;
  color: var(--text-secondary);
  flex: 1;
}

.address-detail {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 12px;
}

.address-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.action-btns {
  display: flex;
  gap: 8px;
}

/* Popup */
.popup-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 16px;
}

.popup-content {
  overflow-y: auto;
}

.default-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin: 12px;
  font-size: 14px;
  color: var(--text-primary);
  background: white;
  border-radius: 8px;
}
</style>
