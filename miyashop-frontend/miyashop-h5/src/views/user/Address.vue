<template>
  <div class="page">
    <!-- 导航 -->
    <div class="nav">
      <span @click="goBack">← 返回</span>
      <span class="nav-title">收货地址</span>
      <span class="nav-add" @click="handleAdd">+ 新增</span>
    </div>

    <!-- 空状态 -->
    <div v-if="list.length === 0" class="empty">
      <div class="empty-icon">📍</div>
      <div class="empty-text">暂无收货地址</div>
      <button class="btn btn-pink" @click="handleAdd">新增地址</button>
    </div>

    <!-- 地址列表 -->
    <div class="list" v-else>
      <div class="card" v-for="addr in list" :key="addr.id" @click="selectAddr(addr)">
        <div class="card-top">
          <span class="card-name">{{ addr.name }}</span>
          <span class="card-phone">{{ addr.phone }}</span>
          <span v-if="addr.isDefault" class="tag">默认</span>
        </div>
        <div class="card-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
        <div class="card-actions">
          <label class="radio-label" @click.stop="setDefault(addr)">
            <span class="radio" :class="{ on: addr.isDefault }"></span> 设为默认
          </label>
          <div class="card-btns">
            <button class="btn btn-small btn-outline" @click.stop="handleEdit(addr)">编辑</button>
            <button class="btn btn-small btn-danger" @click.stop="handleDelete(addr)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div class="overlay" v-if="showPopup" @click.self="showPopup = false">
      <div class="popup">
        <div class="popup-head">
          <span @click="showPopup = false">取消</span>
          <span class="popup-title">{{ isEdit ? '编辑地址' : '新增地址' }}</span>
          <span></span>
        </div>
        <div class="popup-body">
          <div class="field">
            <label>收货人</label>
            <input v-model="form.name" placeholder="请输入收货人姓名" />
          </div>
          <div class="field">
            <label>手机号</label>
            <input v-model="form.phone" type="tel" placeholder="请输入手机号" />
          </div>
          <div class="field" @click="showRegion = true">
            <label>所在地区</label>
            <span class="field-val" :class="{ placeholder: !regionText }">{{ regionText || '请选择省市区' }}</span>
            <span class="arrow">›</span>
          </div>
          <div class="field">
            <label>详细地址</label>
            <input v-model="form.detail" placeholder="街道、门牌号等" />
          </div>
          <div class="switch-row">
            <span>设为默认地址</span>
            <div class="switch" :class="{ on: form.isDefault }" @click="form.isDefault = !form.isDefault">
              <div class="switch-knob"></div>
            </div>
          </div>
          <button class="btn btn-pink btn-block" @click="handleSave">保存</button>
        </div>
      </div>
    </div>

    <!-- 地区选择 -->
    <div class="overlay" v-if="showRegion" @click.self="showRegion = false">
      <div class="popup region-popup">
        <div class="popup-head">
          <span @click="showRegion = false">取消</span>
          <span class="popup-title">选择地区</span>
          <span class="confirm" @click="confirmRegion">确定</span>
        </div>
        <div class="region-picker">
          <div class="col">
            <div v-for="p in provinces" :key="p" class="col-item" :class="{ sel: p === selProvince }" @click="selProvince = p; selCity = cities[0]; selDistrict = districts[0]">{{ p }}</div>
          </div>
          <div class="col">
            <div v-for="c in cities" :key="c" class="col-item" :class="{ sel: c === selCity }" @click="selCity = c; selDistrict = districtMap[c]?.[0] || ''">{{ c }}</div>
          </div>
          <div class="col">
            <div v-for="d in districts" :key="d" class="col-item" :class="{ sel: d === selDistrict }" @click="selDistrict = d">{{ d }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast" v-if="toast" @click="toast = ''">{{ toast }}</div>
    <!-- Confirm -->
    <div class="overlay" v-if="confirmMsg" @click.self="confirmMsg = ''">
      <div class="confirm-box">
        <div class="confirm-text">{{ confirmMsg }}</div>
        <div class="confirm-btns">
          <button class="btn btn-outline" @click="confirmMsg = ''">取消</button>
          <button class="btn btn-pink" @click="onConfirm">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'AddressPage' })
import { ref, reactive, computed } from 'vue'

const toast = ref('')
const showMsg = (m: string) => { toast.value = m; setTimeout(() => toast.value = '', 2000) }

const confirmMsg = ref('')
let confirmCb: (() => void) | null = null
const showConfirm = (m: string, cb: () => void) => { confirmMsg.value = m; confirmCb = cb }
const onConfirm = () => { confirmCb?.(); confirmMsg.value = '' }

const list = ref<any[]>([
  { id: 1, name: '小美', phone: '138****0001', province: '广东省', city: '广州市', district: '天河区', detail: '珠江新城街道100号', isDefault: true },
  { id: 2, name: '丽丽', phone: '139****0002', province: '广东省', city: '深圳市', district: '南山区', detail: '科技园路50号', isDefault: false }
])

const setDefault = (addr: any) => {
  list.value.forEach(a => a.isDefault = a.id === addr.id)
  showMsg('已设为默认地址')
}

const selectAddr = (addr: any) => {
  localStorage.setItem('selectedAddress', JSON.stringify(addr))
  goBack()
}

const handleDelete = (addr: any) => {
  showConfirm('确定删除该地址吗？', () => {
    list.value = list.value.filter(a => a.id !== addr.id)
    showMsg('已删除')
  })
}

// Form
const showPopup = ref(false)
const isEdit = ref(false)
const editingId = ref<number>(0)
const form = reactive({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

const resetForm = () => { Object.assign(form, { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }) }

const handleAdd = () => { isEdit.value = false; editingId.value = 0; resetForm(); showPopup.value = true }
const handleEdit = (addr: any) => {
  isEdit.value = true; editingId.value = addr.id
  Object.assign(form, { name: addr.name, phone: addr.phone, province: addr.province, city: addr.city, district: addr.district, detail: addr.detail, isDefault: addr.isDefault })
  showPopup.value = true
}

const handleSave = () => {
  if (!form.name) { showMsg('请输入收货人姓名'); return }
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) { showMsg('请输入正确的手机号'); return }
  if (!form.province) { showMsg('请选择所在地区'); return }
  if (!form.detail) { showMsg('请输入详细地址'); return }

  if (isEdit.value) {
    const idx = list.value.findIndex(a => a.id === editingId.value)
    if (idx > -1) Object.assign(list.value[idx], { ...form })
  } else {
    list.value.push({ id: Date.now(), ...form })
  }
  if (form.isDefault) list.value.forEach(a => a.isDefault = a.id === (editingId.value || list.value[list.value.length - 1].id))

  showPopup.value = false
  showMsg(isEdit.value ? '已更新' : '已添加')
}

// Region picker
const showRegion = ref(false)
const selProvince = ref('广东省')
const selCity = ref('广州市')
const selDistrict = ref('天河区')

const provinces = ['广东省', '北京市', '上海市', '浙江省', '江苏省']
const cityMap: Record<string, string[]> = {
  '广东省': ['广州市', '深圳市', '东莞市', '佛山市'],
  '北京市': ['朝阳区', '海淀区', '丰台区'],
  '上海市': ['浦东新区', '黄浦区', '徐汇区'],
  '浙江省': ['杭州市', '宁波市', '温州市'],
  '江苏省': ['南京市', '苏州市', '无锡市']
}
const districtMap: Record<string, string[]> = {
  '广州市': ['天河区', '越秀区', '海珠区', '白云区', '番禺区'],
  '深圳市': ['南山区', '福田区', '罗湖区', '宝安区'],
  '东莞市': ['莞城区', '南城区', '东城区'],
  '佛山市': ['禅城区', '南海区', '顺德区'],
  '朝阳区': ['望京', '三里屯', '国贸'],
  '海淀区': ['中关村', '五道口', '西二旗'],
  '丰台区': ['方庄', '科技园', '丽泽'],
  '浦东新区': ['陆家嘴', '张江', '金桥'],
  '黄浦区': ['南京东路', '外滩', '豫园'],
  '徐汇区': ['徐家汇', '漕河泾', '衡山路'],
  '杭州市': ['西湖区', '上城区', '拱墅区'],
  '宁波市': ['海曙区', '鄞州区', '江北区'],
  '温州市': ['鹿城区', '龙湾区', '瓯海区'],
  '南京市': ['鼓楼区', '玄武区', '秦淮区'],
  '苏州市': ['虎丘区', '吴中区', '相城区'],
  '无锡市': ['梁溪区', '锡山区', '惠山区']
}

const cities = computed(() => cityMap[selProvince.value] || [])
const districts = computed(() => districtMap[selCity.value] || [])
const regionText = computed(() => form.province ? `${form.province} ${form.city} ${form.district}` : '')

const confirmRegion = () => {
  form.province = selProvince.value
  form.city = selCity.value
  form.district = selDistrict.value
  showRegion.value = false
}

const goBack = () => { window.location.hash = '#/' }
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20px; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 46px; padding: 0 16px; background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.nav-title { font-size: 16px; font-weight: 600; }
.nav-add { font-size: 14px; color: #FF6B95; cursor: pointer; }

/* Empty */
.empty { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { font-size: 14px; color: #999; margin-bottom: 20px; }

/* List */
.list { padding: 12px; }
.card { background: #fff; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.card-top { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.card-name { font-size: 15px; font-weight: 600; }
.card-phone { font-size: 13px; color: #999; flex: 1; }
.tag { background: #FF6B95; color: #fff; font-size: 10px; padding: 2px 6px; border-radius: 3px; }
.card-detail { font-size: 13px; color: #666; line-height: 1.5; margin-bottom: 12px; }
.card-actions { display: flex; justify-content: space-between; align-items: center; padding-top: 10px; border-top: 1px solid #f0f0f0; }
.radio-label { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #666; cursor: pointer; }
.radio { width: 16px; height: 16px; border: 2px solid #ddd; border-radius: 50%; flex-shrink: 0; }
.radio.on { border-color: #FF6B95; background: #FF6B95; box-shadow: inset 0 0 0 3px #fff; }
.card-btns { display: flex; gap: 8px; }

/* Buttons */
.btn { border: none; border-radius: 6px; cursor: pointer; font-size: 13px; }
.btn-pink { background: #FF6B95; color: #fff; padding: 10px 20px; }
.btn-block { display: block; width: 100%; height: 44px; font-size: 15px; border-radius: 22px; margin-top: 20px; }
.btn-small { padding: 4px 10px; font-size: 12px; }
.btn-outline { border: 1px solid #ddd; background: #fff; color: #666; }
.btn-danger { border: 1px solid #F56C6C; background: #fff; color: #F56C6C; }

/* Overlay & Popup */
.overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 200; display: flex; align-items: flex-end; }
.popup { width: 100%; max-height: 80vh; background: #fff; border-radius: 16px 16px 0 0; overflow: hidden; display: flex; flex-direction: column; }
.popup-head { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; border-bottom: 1px solid #eee; flex-shrink: 0; }
.popup-title { font-size: 16px; font-weight: 600; }
.popup-body { padding: 16px; overflow-y: auto; flex: 1; }
.confirm { color: #FF6B95; font-size: 14px; cursor: pointer; }

/* Field */
.field { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.field label { width: 70px; font-size: 14px; color: #333; flex-shrink: 0; }
.field input { flex: 1; border: none; outline: none; font-size: 14px; color: #333; }
.field-val { flex: 1; font-size: 14px; color: #333; }
.field-val.placeholder { color: #ccc; }
.arrow { font-size: 20px; color: #ccc; }

/* Switch */
.switch-row { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; font-size: 14px; }
.switch { width: 44px; height: 24px; background: #e5e5e5; border-radius: 12px; position: relative; cursor: pointer; transition: background .2s; }
.switch.on { background: #FF6B95; }
.switch-knob { width: 20px; height: 20px; background: #fff; border-radius: 50%; position: absolute; top: 2px; left: 2px; transition: left .2s; }
.switch.on .switch-knob { left: 22px; }

/* Region Picker */
.region-popup { height: 60vh; }
.region-picker { display: flex; flex: 1; overflow: hidden; }
.col { flex: 1; overflow-y: auto; border-right: 1px solid #f0f0f0; }
.col:last-child { border-right: none; }
.col-item { padding: 12px 8px; text-align: center; font-size: 14px; color: #333; cursor: pointer; }
.col-item.sel { color: #FF6B95; font-weight: 600; background: #FFF0F3; }

/* Toast & Confirm */
.toast { position: fixed; top: 50%; left: 50%; transform: translate(-50%,-50%); background: rgba(0,0,0,0.75); color: #fff; padding: 12px 24px; border-radius: 8px; font-size: 14px; z-index: 300; }
.confirm-box { width: 280px; background: #fff; border-radius: 12px; padding: 24px 20px; text-align: center; margin: auto; }
.confirm-text { font-size: 15px; color: #333; margin-bottom: 20px; }
.confirm-btns { display: flex; gap: 12px; justify-content: center; }
.confirm-btns .btn { padding: 8px 32px; font-size: 14px; }
</style>
