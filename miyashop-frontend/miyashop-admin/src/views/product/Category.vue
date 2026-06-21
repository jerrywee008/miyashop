<template>
  <div class="page-container">
    <div class="action-section">
      <el-button type="primary" @click="handleAdd(0, 1)">新增一级分类</el-button>
      <el-button-group style="margin-left: 12px">
        <el-button :type="expandAll ? 'primary' : 'default'" size="default" @click="handleExpandAll">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button :type="!expandAll ? 'primary' : 'default'" size="default" @click="handleCollapseAll">
          <el-icon><Fold /></el-icon>
          折叠全部
        </el-button>
      </el-button-group>
      <el-button @click="refreshTree" :icon="'Refresh'">刷新</el-button>
    </div>

    <!-- 分类树 -->
    <div v-loading="loading" class="tree-container">
      <el-empty v-if="!loading && treeData.length === 0" description="暂无分类数据" />
      <el-tree
        v-else
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        :expand-on-click-node="false"
        :indent="32"
        lazy
        :load="loadNode"
        @node-expand="onNodeExpand"
        @node-collapse="onNodeCollapse"
      >
        <template #default="{ data }">
          <div class="tree-node-content">
            <!-- 加载中指示 -->
            <el-icon v-if="data._loading" class="node-loading is-loading">
              <Loading />
            </el-icon>
            <el-image
              v-else-if="data.icon"
              :src="data.icon"
              class="node-icon"
              fit="cover"
            />
            <span v-else class="node-icon-placeholder">
              <el-icon><Folder /></el-icon>
            </span>

            <!-- 分类信息 -->
            <div class="node-info">
              <span class="node-name">{{ data.name }}</span>
              <span class="node-meta">
                <el-tag :type="(getLevelType(data.level) as 'info' | 'success' | 'warning' | 'danger')" size="small">
                  {{ getLevelLabel(data.level) }}
                </el-tag>
                <span class="node-sort">排序: {{ data.sort }}</span>
                <span class="node-id">ID: {{ data.id }}</span>
              </span>
            </div>

            <!-- 状态 & 操作 -->
            <div class="node-actions">
              <el-switch
                :model-value="data.showStatus === 1"
                active-color="#FF6B95"
                size="small"
                @click.stop
                @change="(val: any) => handleStatusChange(data, val ? 1 : 0)"
              />
              <el-button size="small" type="primary" link @click.stop="handleEdit(data)">
                编辑
              </el-button>
              <el-button
                size="small"
                type="success"
                link
                @click.stop="handleAdd(data.id, data.level + 1)"
                v-if="data.level < 3"
              >
                添加子分类
              </el-button>
              <el-popconfirm
                title="确定删除该分类吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleDelete(data)"
              >
                <template #reference>
                  <el-button size="small" type="danger" link @click.stop>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="父级分类">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="分类层级">
          <el-tag :type="(getLevelType(formData.level) as 'info' | 'success' | 'warning' | 'danger')">{{ getLevelLabel(formData.level) }}</el-tag>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标">
          <el-input v-model="formData.icon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="显示状态">
          <el-radio-group v-model="formData.showStatus">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type Node from 'element-plus/es/components/tree/src/model/node'
import {
  getCategoryList, getCategoryDetail,
  addCategory, updateCategory, deleteCategory, updateCategoryStatus
} from '@/api/category'

// ---------- 树配置 ----------
const treeRef = ref()
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'leaf'
}

// ---------- 展开/折叠控制 ----------
const expandAll = ref(true)

const collectLoadedNonLeafKeys = (): number[] => {
  if (!treeRef.value) return []
  const keys: number[] = []
  const traverse = (node: Node) => {
    if (node.isLeaf || node.data?.level >= 3) return
    if (!node.expanded) {
      keys.push(node.key as number)
    }
    if (node.childNodes) {
      node.childNodes.forEach((child: Node) => traverse(child))
    }
  }
  treeRef.value.root.childNodes.forEach((node: Node) => traverse(node))
  return keys
}

const handleExpandAll = async () => {
  expandAll.value = true
  const maxRounds = 4
  for (let round = 0; round < maxRounds; round++) {
    await nextTick()
    await new Promise(r => setTimeout(r, 100))
    const keys = collectLoadedNonLeafKeys()
    if (keys.length === 0) break
    keys.forEach((key) => {
      const node = treeRef.value.getNode(key)
      if (node && !node.expanded && !node.isLeaf) {
        node.expand()
      }
    })
  }
}

const handleCollapseAll = () => {
  expandAll.value = false
  if (!treeRef.value) return
  const collapseNode = (node: Node) => {
    if (node.childNodes) {
      node.childNodes.forEach((child: Node) => {
        collapseNode(child)
      })
    }
    if (node.expanded) {
      node.collapse()
    }
  }
  treeRef.value.root.childNodes.forEach((node: Node) => collapseNode(node))
}

const onNodeExpand = () => { /* tracked by el-tree */ }
const onNodeCollapse = () => { /* tracked by el-tree */ }

// ---------- 懒加载 ----------
const loading = ref(false)
const treeData = ref<any[]>([])

const loadNode = (node: Node, resolve: (data: any[]) => void) => {
  // node.level === 0: root already provided by treeData, skip
  if (node.level === 0) {
    resolve([])
    return
  }
  const parentId = node.key as number

  getCategoryList({ parentId })
    .then((res) => {
      if (res.code === 200) {
        const children = (res.data || []).map((item: any) => ({
          ...item,
          leaf: item.level >= 3
        }))
        resolve(children)
      } else {
        resolve([])
      }
    })
    .catch(() => {
      ElMessage.error('加载分类失败')
      resolve([])
    })
}

// Refresh: reload root categories from API
const refreshTree = async () => {
  loading.value = true
  try {
    const res = await getCategoryList({ parentId: 0 })
    if (res.code === 200) {
      treeData.value = (res.data || []).map((item: any) => ({ ...item, leaf: item.level >= 3 }))
    }
  } catch {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
    await nextTick()
    if (expandAll.value) {
      handleExpandAll()
    }
  }
}

// ---------- 辅助 ----------
const getLevelType = (level: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[level] || 'info'
}

const getLevelLabel = (level: number) => {
  const map: Record<number, string> = { 1: '一级', 2: '二级', 3: '三级' }
  return map[level] || ''
}

const findCategoryInTree = (nodes: any[], id: number): any => {
  for (const item of nodes) {
    if (item.id === id) return item
    if (item.children) {
      const found = findCategoryInTree(item.children, id)
      if (found) return found
    }
  }
  return null
}

// ---------- 状态切换 ----------
const handleStatusChange = async (row: any, showStatus: number) => {
  try {
    const res = await updateCategoryStatus(row.id, showStatus)
    if (res.code === 200) {
      ElMessage.success(showStatus === 1 ? '已显示' : '已隐藏')
      refreshTree()
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// ---------- 新增/编辑 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>()
const parentName = ref('')

const formData = reactive({
  parentId: 0,
  name: '',
  level: 1,
  icon: '',
  sort: 0,
  showStatus: 1
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const resetForm = () => {
  formData.parentId = 0
  formData.name = ''
  formData.level = 1
  formData.icon = ''
  formData.sort = 0
  formData.showStatus = 1
  parentName.value = ''
}

const handleAdd = (parentId: number, level: number) => {
  isEdit.value = false
  dialogTitle.value = level === 1 ? '新增一级分类' : '新增子分类'
  resetForm()
  formData.parentId = parentId
  formData.level = level
  if (parentId > 0) {
    const parent = findCategoryInTree(treeData.value, parentId)
    parentName.value = parent?.name || '未知'
  } else {
    parentName.value = '顶级分类'
  }
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑分类'
  try {
    const res = await getCategoryDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      formData.parentId = data.parentId || 0
      formData.name = data.name || ''
      formData.level = data.level || 1
      formData.icon = data.icon || ''
      formData.sort = data.sort || 0
      formData.showStatus = data.showStatus ?? 1
      if (data.parentId > 0) {
        const parent = findCategoryInTree(treeData.value, data.parentId)
        parentName.value = parent?.name || '未知'
      } else {
        parentName.value = '顶级分类'
      }
    }
  } catch {
    ElMessage.error('获取分类详情失败')
    return
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let res
      if (isEdit.value && editId.value) {
        res = await updateCategory(editId.value, formData)
      } else {
        res = await addCategory(formData)
      }
      if (res && res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        refreshTree()
      }
    } catch {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// ---------- 删除 ----------
const handleDelete = async (row: any) => {
  try {
    const res = await deleteCategory(row.id)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      refreshTree()
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  refreshTree()
})
</script>

<style scoped>
.action-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.tree-container {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  padding: 16px 0;
  min-height: 400px;
}

/* ── 树节点样式 ── */
.tree-node-content {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
  gap: 10px;
}

.tree-node-content:hover {
  background-color: #fdf0f4;
}

.node-loading {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 18px;
}

.node-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid #f0f0f0;
}

.node-icon-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 18px;
}

.node-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.node-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
}

.node-sort,
.node-id {
  color: #bbb;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

/* ── 树组件全局覆盖 ── */
:deep(.el-tree-node__content) {
  height: auto;
  padding: 0;
}

:deep(.el-tree-node__content:hover) {
  background-color: transparent;
}

:deep(.el-tree) {
  padding: 0 16px;
}

:deep(.el-tree-node) {
  margin: 2px 0;
}
</style>
