<template>
  <div class="admin-container">
    <el-card class="admin-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <h2>商品管理</h2>
          <p>共 {{ products.length }} 件商品</p>
        </div>
        <div class="toolbar-right">
          <el-button @click="router.push('/admin/orders')">订单管理</el-button>
          <el-button type="primary" :icon="Plus" @click="goCreate">新增商品</el-button>
        </div>
      </div>

      <div class="dictionary-wrapper">
        <el-card shadow="never" class="dictionary-card">
          <template #header>
            <div class="dict-title">Tags 对照表（用于 healthGoals 匹配）</div>
          </template>
          <el-table :data="tagDictionary" size="small" border>
            <el-table-column prop="tag" label="tag" width="120" />
            <el-table-column prop="meaning" label="含义" />
            <el-table-column prop="scene" label="典型商品类型" />
          </el-table>
        </el-card>
        <el-card shadow="never" class="dictionary-card">
          <template #header>
            <div class="dict-title">targetAudience 对照表</div>
          </template>
          <el-table :data="audienceDictionary" size="small" border>
            <el-table-column prop="token" label="token" width="120" />
            <el-table-column prop="meaning" label="含义" />
          </el-table>
        </el-card>
      </div>

      <el-table :data="products" v-loading="loading" class="admin-table">
        <el-table-column prop="id" label="ID" width="76" />
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="tags" label="标签" min-width="160" show-overflow-tooltip />
        <el-table-column prop="targetAudience" label="适用人群" min-width="160" show-overflow-tooltip />
        <el-table-column label="价格(¥)" width="120">
          <template #default="{ row }">¥{{ Number(row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="主图" width="90">
          <template #default="{ row }">
            <img v-if="row.imageUrl" :src="row.imageUrl" class="product-img" />
            <span v-else class="empty-tip">无</span>
          </template>
        </el-table-column>
        <el-table-column label="多图" width="90">
          <template #default="{ row }">{{ countGallery(row.imageGallery) }} 张</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="220" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button size="small" type="primary" plain :icon="Edit" @click="goEdit(row.id)">修改</el-button>
              <el-button size="small" type="danger" plain :icon="Delete" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

interface Product {
  id?: number
  name: string
  description: string
  imageUrl: string
  imageGallery?: string
  price: number
  tags: string
  targetAudience: string
}

const router = useRouter()
const products = ref<Product[]>([])
const loading = ref(false)

const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const productToDelete = ref<number | null>(null)

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

const countGallery = (raw?: string) => {
  if (!raw) return 0
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) return parsed.length
  } catch {
    return raw.split(',').map((s) => s.trim()).filter(Boolean).length
  }
  return 0
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

const goCreate = () => router.push('/admin/products/new')
const goEdit = (id?: number) => {
  if (!id) return
  router.push(`/admin/products/${id}/edit`)
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
  max-width: 1260px;
  margin: 0 auto;
  padding: 24px 20px 30px;
}

.admin-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
}

:deep(.admin-card .el-card__body) {
  padding: 22px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.toolbar-left h2 {
  margin: 0;
  font-size: 24px;
  color: #123126;
}

.toolbar-left p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.dictionary-wrapper {
  margin-bottom: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.dictionary-card {
  border: none;
  border-radius: var(--radius);
  background: linear-gradient(160deg, #ffffff 0%, #f7fbf9 100%);
  box-shadow: 0 10px 22px rgba(35, 63, 50, 0.08);
}

.dict-title {
  font-weight: 600;
  color: #1d4b38;
}

.admin-table {
  width: 100%;
}

:deep(.admin-table .el-table__inner-wrapper)::before {
  display: none;
}

:deep(.admin-table th.el-table__cell) {
  background: #f3faf6;
  color: #2f5546;
  font-weight: 700;
}

:deep(.admin-table tr:hover > td.el-table__cell) {
  background: #f8fcfa;
}

.product-img {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid #d8ece1;
}

.empty-tip {
  color: #90a198;
  font-size: 12px;
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
