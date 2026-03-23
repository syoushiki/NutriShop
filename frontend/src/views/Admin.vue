<template>
  <div class="admin-container">
    <el-card class="admin-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <h2>商品管理</h2>
          <p>共 {{ products.length }} 件商品</p>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="openAddDialog">新增商品</el-button>
        </div>
      </div>

      <div class="dictionary-wrapper">
        <el-card shadow="never" class="dictionary-card">
          <template #header>
            <div class="dict-title">Tags 对照表（用于 healthGoals 匹配）</div>
          </template>
          <el-table :data="tagDictionary" size="small" border>
            <el-table-column prop="tag" label="tag" width="140" />
            <el-table-column prop="meaning" label="含义" />
            <el-table-column prop="scene" label="典型商品类型" />
          </el-table>
        </el-card>
        <el-card shadow="never" class="dictionary-card">
          <template #header>
            <div class="dict-title">targetAudience 对照表</div>
          </template>
          <el-table :data="audienceDictionary" size="small" border>
            <el-table-column prop="token" label="token" width="140" />
            <el-table-column prop="meaning" label="含义" />
          </el-table>
        </el-card>
      </div>

      <el-table :data="products" v-loading="loading" class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column prop="tags" label="标签" min-width="160" show-overflow-tooltip />
        <el-table-column prop="targetAudience" label="适用人群" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="价格(¥)" width="120">
          <template #default="{ row }">
            ¥{{ Number(row.price || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="imageUrl" label="图片" width="120">
          <template #default="{ row }">
            <img v-if="row.imageUrl" :src="row.imageUrl" alt="商品图片" class="product-img" />
            <span v-else class="empty-tip">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="220" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button size="small" type="primary" plain :icon="Edit" @click="openEditDialog(row)">修改</el-button>
              <el-button size="small" type="danger" plain :icon="Delete" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="新增商品" v-model="addDialogVisible" width="620px" destroy-on-close class="product-dialog">
      <el-form ref="addFormRef" :model="addForm" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="addForm.price" :min="0" :precision="2" :step="1" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="addForm.tags" placeholder="例如：sleep,eyes,fitness" />
        </el-form-item>
        <el-form-item label="适用人群" prop="targetAudience">
          <el-input v-model="addForm.targetAudience" placeholder="例如：male,18-30 或 all,31-50" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="addForm.imageUrl" placeholder="请输入图片链接" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addSubmitting" @click="submitAddForm">确认新增</el-button>
      </template>
    </el-dialog>

    <el-dialog title="修改商品" v-model="editDialogVisible" width="620px" destroy-on-close class="product-dialog">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" :step="1" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="editForm.tags" placeholder="例如：sleep,eyes,fitness" />
        </el-form-item>
        <el-form-item label="适用人群" prop="targetAudience">
          <el-input v-model="editForm.targetAudience" placeholder="例如：male,18-30 或 all,31-50" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="editForm.imageUrl" placeholder="请输入图片链接" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEditForm">确认修改</el-button>
      </template>
    </el-dialog>

    <el-dialog title="确认删除" v-model="deleteDialogVisible" width="420px" destroy-on-close>
      <p>确认删除该商品吗？删除后无法恢复。</p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleteLoading" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import axios from 'axios'

interface Product {
  id?: number
  name: string
  description: string
  imageUrl: string
  price: number
  tags: string
  targetAudience: string
}

const createEmptyProduct = (): Product => ({
  name: '',
  description: '',
  imageUrl: '',
  price: 0,
  tags: '',
  targetAudience: ''
})

const products = ref<Product[]>([])
const loading = ref(false)

const addDialogVisible = ref(false)
const addSubmitting = ref(false)
const addFormRef = ref<FormInstance>()
const addForm = ref<Product>(createEmptyProduct())

const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref<Product>(createEmptyProduct())

const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const productToDelete = ref<number | null>(null)

const rules: FormRules<Product> = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }]
}

const tagDictionary = [
  { tag: 'sleep', meaning: '睡眠改善', scene: '褪黑素、安神类' },
  { tag: 'eyes', meaning: '护眼', scene: '叶黄素、蓝莓类' },
  { tag: 'fitness', meaning: '健身塑形', scene: '运动营养类' },
  { tag: 'nutrition', meaning: '综合营养', scene: '多维、矿物质类' },
  { tag: 'cardio', meaning: '心血管支持', scene: '鱼油、辅酶Q10类' },
  { tag: 'energy', meaning: '精力补充', scene: '提神补能类' },
  { tag: 'beauty', meaning: '养颜抗衰', scene: '胶原蛋白类' },
  { tag: 'joints', meaning: '关节支持', scene: '关节灵活类' },
  { tag: 'bone', meaning: '骨骼支持', scene: '钙/骨骼营养类' }
]

const audienceDictionary = [
  { token: 'all', meaning: '通用人群' },
  { token: 'male', meaning: '男性' },
  { token: 'female', meaning: '女性' },
  { token: '18-30', meaning: '18-30岁' },
  { token: '31-50', meaning: '31-50岁' },
  { token: '50+', meaning: '50岁及以上' }
]

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/products')
    products.value = res.data
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  addForm.value = createEmptyProduct()
  addDialogVisible.value = true
  addFormRef.value?.clearValidate()
}

const openEditDialog = (row: Product) => {
  editForm.value = {
    ...row,
    tags: row.tags || '',
    targetAudience: row.targetAudience || ''
  }
  editDialogVisible.value = true
  editFormRef.value?.clearValidate()
}

const toNullableText = (value?: string) => {
  if (!value) return null
  const trimmed = value.trim()
  return trimmed === '' ? null : trimmed
}

const buildProductPayload = (form: Product) => ({
  name: form.name.trim(),
  price: form.price,
  description: toNullableText(form.description),
  imageUrl: toNullableText(form.imageUrl),
  tags: toNullableText(form.tags),
  targetAudience: toNullableText(form.targetAudience)
})

const submitAddForm = async () => {
  if (!addFormRef.value) return
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return

  addSubmitting.value = true
  try {
    const payload = buildProductPayload(addForm.value)
    await axios.post('/api/products', payload)
    ElMessage.success('新增成功')
    addDialogVisible.value = false
    await fetchProducts()
  } catch (error: any) {
    const errorMsg = error.response?.data?.message || error.response?.data || error.response?.statusText || error.message || '服务器错误'
    ElMessage.error(`新增失败: ${errorMsg}`)
  } finally {
    addSubmitting.value = false
  }
}

const submitEditForm = async () => {
  if (!editFormRef.value) return
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid || !editForm.value.id) return

  editSubmitting.value = true
  try {
    const payload = buildProductPayload(editForm.value)
    await axios.put(`/api/products/${editForm.value.id}`, payload)
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    await fetchProducts()
  } catch (error: any) {
    const errorMsg = error.response?.data?.message || error.response?.data || error.response?.statusText || error.message || '服务器错误'
    ElMessage.error(`修改失败: ${errorMsg}`)
  } finally {
    editSubmitting.value = false
  }
}

const handleDelete = (id?: number) => {
  if (!id) return
  productToDelete.value = id
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!productToDelete.value) return
  deleteLoading.value = true
  try {
    await axios.delete(`/api/products/${productToDelete.value}`)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    await fetchProducts()
  } catch {
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
    productToDelete.value = null
  }
}

onMounted(fetchProducts)
</script>

<style scoped>
.admin-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 24px;
}

.admin-card {
  border-radius: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.toolbar-left h2 {
  margin: 0;
  font-size: 22px;
  color: #222;
}

.toolbar-left p {
  margin: 6px 0 0;
  color: #76839a;
  font-size: 13px;
}

.admin-table {
  width: 100%;
}

.dictionary-wrapper {
  margin-bottom: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.dictionary-card {
  border-radius: 10px;
}

.dict-title {
  font-weight: 600;
}

.product-img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #ebeef5;
}

.empty-tip {
  color: #9aa4b2;
  font-size: 13px;
}

.action-group {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .admin-container {
    padding: 12px;
  }

  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .dictionary-wrapper {
    grid-template-columns: 1fr;
  }
}
</style>
