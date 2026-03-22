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

      <el-table :data="products" v-loading="loading" class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="180" />
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

    <el-dialog
      title="新增商品"
      v-model="addDialogVisible"
      width="560px"
      destroy-on-close
      class="product-dialog"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="addForm.price" :min="0" :precision="2" :step="1" :controls="false" style="width: 100%" />
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

    <el-dialog
      title="修改商品"
      v-model="editDialogVisible"
      width="560px"
      destroy-on-close
      class="product-dialog"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" :step="1" :controls="false" style="width: 100%" />
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
}

const createEmptyProduct = (): Product => ({
  name: '',
  description: '',
  imageUrl: '',
  price: 0
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
  editForm.value = { ...row }
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
  imageUrl: toNullableText(form.imageUrl)
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
    console.error('新增失败:', error)
    if (error.response) {
      const errorMsg = error.response.data?.message || error.response.data || error.response.statusText || '内部服务器错误'
      ElMessage.error(`新增失败: ${errorMsg} (状态码: ${error.response.status})`)
    } else {
      ElMessage.error(`新增失败: ${error.message}`)
    }
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
    console.error('修改失败:', error)
    if (error.response) {
      const errorMsg = error.response.data?.message || error.response.data || error.response.statusText || '内部服务器错误'
      ElMessage.error(`修改失败: ${errorMsg} (状态码: ${error.response.status})`)
    } else {
      ElMessage.error(`修改失败: ${error.message}`)
    }
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
}
</style>
