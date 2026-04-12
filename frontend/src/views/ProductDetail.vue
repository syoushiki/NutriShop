<template>
  <div class="detail-page" v-if="product">
    <div class="container">
      <el-card class="hero-card" shadow="never">
        <div class="hero-grid">
          <section class="media-col">
            <div class="main-image-box">
              <img :src="currentImage || product.imageUrl" class="main-image" :alt="product.name" />
            </div>
            <div class="thumbs">
              <button
                v-for="(img, index) in thumbnails"
                :key="index"
                class="thumb-btn"
                :class="{ active: currentImage === img }"
                @mouseenter="currentImage = img"
                @click="currentImage = img"
              >
                <img :src="img" :alt="`thumb-${index}`" />
              </button>
            </div>
          </section>

          <section class="info-col">
            <h1 class="title">{{ product.name }}</h1>
            <p class="subtitle">{{ product.description || '高品质营养补充，建议根据个人情况科学食用。' }}</p>

            <div class="meta-row">
              <span class="meta-pill" v-if="showRelevanceScore">匹配度 {{ relevancePercent }}%</span>
              <span class="meta-pill soft">正品保障</span>
              <span class="meta-pill soft">极速发货</span>
            </div>

            <div class="tag-row" v-if="normalizedTags.length">
              <span v-for="t in normalizedTags" :key="t" class="tag">{{ t }}</span>
            </div>

            <div class="price-card">
              <div class="label">到手价</div>
              <div class="price">¥{{ Number(product.price || 0).toFixed(2) }}</div>
              <div class="note">支持七天无理由退换</div>
            </div>

            <div class="purchase-row">
              <span class="field-label">数量</span>
              <el-input-number v-model="qty" :min="1" :max="99" />
              <span class="stock">库存充足</span>
            </div>

            <div class="actions">
              <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
              <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
            </div>
          </section>
        </div>
      </el-card>

      <el-row :gutter="16" class="content-row">
        <el-col :xs="24" :md="6">
          <el-card class="side-card" shadow="never">
            <template #header>
              <div class="side-title">高权重个性推荐</div>
            </template>
            <div v-for="item in recommendList" :key="item.id" class="side-item" @click="goDetail(item.id)">
              <img :src="item.imageUrl || 'https://placehold.co/180x140/f8fafc/64748b?text=No+Image'" />
              <div class="side-name">{{ item.name }}</div>
              <div class="side-price">¥{{ Number(item.price || 0).toFixed(2) }}</div>
            </div>
            <el-empty v-if="recommendList.length === 0" description="暂无推荐" />
          </el-card>
        </el-col>

        <el-col :xs="24" :md="18">
          <el-card class="tabs-card" shadow="never">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="商品详情" name="detail">
                <div class="desc-block">
                  <el-alert title="温馨提示：保健品不能代替药物治疗" type="warning" show-icon :closable="false" />
                  <p>{{ product.description || '暂无详细介绍' }}</p>
                  <img src="https://placehold.co/900x360/e2e8f0/1e293b?text=Detail+Banner" />
                </div>
              </el-tab-pane>

              <el-tab-pane label="营养成分" name="nutrition">
                <el-table :data="nutritionData" border>
                  <el-table-column prop="name" label="成分" width="220" />
                  <el-table-column prop="amount" label="每份含量" />
                  <el-table-column prop="nrv" label="NRV%" width="120" />
                </el-table>
              </el-tab-pane>

              <el-tab-pane label="用户评价" name="comments">
                <div v-for="i in 3" :key="i" class="comment-item">
                  <div class="comment-head">
                    <el-avatar :size="30" icon="UserFilled" />
                    <span>用户***{{ i }}</span>
                    <el-rate :model-value="5" disabled size="small" />
                  </div>
                  <div class="comment-body">口感不错，物流很快，体验很好。</div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const product = ref<any>(null)
const qty = ref(1)
const activeTab = ref('detail')
const currentImage = ref('')
const recommendList = ref<any[]>([])
const showRelevanceScore = computed(() => {
  const score = Number(product.value?.relevanceScore)
  return Number.isFinite(score) && score > 0
})
const relevancePercent = computed(() => {
  const score = Number(product.value?.relevanceScore)
  if (!Number.isFinite(score) || score <= 0) return 0
  return Math.round(score * 100)
})

const normalizedTags = computed(() => {
  if (!product.value?.tags) return []
  return String(product.value.tags)
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
})

const thumbnails = computed(() => {
  if (!product.value) return []
  const list: string[] = []
  if (product.value.imageGallery) {
    try {
      const parsed = JSON.parse(product.value.imageGallery)
      if (Array.isArray(parsed)) {
        parsed.forEach((url) => list.push(String(url)))
      }
    } catch {
      product.value.imageGallery
        .split(',')
        .map((s: string) => s.trim())
        .filter(Boolean)
        .forEach((url: string) => list.push(url))
    }
  }
  if (product.value.imageUrl && !list.includes(product.value.imageUrl)) {
    list.unshift(product.value.imageUrl)
  }
  if (list.length === 0) {
    list.push(
      'https://placehold.co/420x420/f8fafc/64748b?text=Angle+1',
      'https://placehold.co/420x420/f1f5f9/64748b?text=Angle+2',
      'https://placehold.co/420x420/e2e8f0/64748b?text=Package'
    )
  }
  return list
})

const nutritionData = computed(() => {
  if (Array.isArray(product.value?.nutrition) && product.value.nutrition.length) {
    return product.value.nutrition
  }
  return [
    { name: 'Omega-3 / 复合营养', amount: '1000mg', nrv: '--' },
    { name: '维生素E / 辅助成分', amount: '10mg', nrv: '--' }
  ]
})

const loadProduct = async () => {
  try {
    const { data } = await axios.get(`/api/products/${route.params.id}`)
    product.value = data
    currentImage.value = data.imageUrl
    await loadPersonalizedRecommendations()
  } catch {
    ElMessage.error('加载商品失败')
  }
}

onMounted(loadProduct)
watch(() => route.params.id, loadProduct)

const resolveUserId = async (): Promise<number | null> => {
  const fromStorage = Number(localStorage.getItem('userId'))
  if (Number.isFinite(fromStorage) && fromStorage > 0) return fromStorage
  const token = localStorage.getItem('token')
  if (!token) return null
  try {
    const { data } = await axios.get('/api/auth/me')
    if (data?.id) {
      localStorage.setItem('userId', String(data.id))
      return Number(data.id)
    }
  } catch {
    return null
  }
  return null
}

const loadPersonalizedRecommendations = async () => {
  try {
    const userId = await resolveUserId()
    const url = userId ? `/api/products/recommend?userId=${userId}` : '/api/products'
    const { data } = await axios.get(url)
    const allItems = Array.isArray(data) ? data : []
    const currentId = product.value?.id
    const currentScored = allItems.find((item: any) => item.id === currentId)
    const currentScore = Number(currentScored?.relevanceScore)
    if (Number.isFinite(currentScore)) {
      product.value = {
        ...product.value,
        relevanceScore: currentScore
      }
    }
    recommendList.value = allItems
      .filter((item: any) => item.id !== product.value?.id)
      .sort((a: any, b: any) => Number(b.relevanceScore || 0) - Number(a.relevanceScore || 0))
      .slice(0, 3)
  } catch {
    recommendList.value = []
  }
}

async function addToCart() {
  try {
    await axios.post('/api/cart/add', { productId: product.value.id, quantity: qty.value })
    ElMessage.success('已加入购物车')
  } catch {
    ElMessage.error('请先登录')
  }
}

async function buyNow() {
  try {
    await addToCart()
    const amount = Number(product.value.price) * qty.value
    const { data } = await axios.post('/api/pay/create', { amount })
    window.location.href = data.payUrl
  } catch {
    ElMessage.error('购买请求失败，请稍后重试')
  }
}

function goDetail(id: number) {
  if (!id) return
  router.push(`/product/${id}`)
}
</script>

<style scoped>
.detail-page {
  background:
    radial-gradient(circle at 8% 0%, rgba(46, 204, 113, 0.14), transparent 30%),
    radial-gradient(circle at 90% 6%, rgba(30, 171, 97, 0.1), transparent 26%),
    var(--bg-page);
  min-height: 100vh;
  padding: 20px 0 30px;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.hero-card,
.side-card,
.tabs-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.9);
}

.hero-grid {
  display: grid;
  grid-template-columns: 520px 1fr;
  gap: 24px;
}

.main-image-box {
  border-radius: var(--radius);
  background: #ffffff;
  border: 1px solid #dcefe5;
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.thumbs {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.thumb-btn {
  border: 1px solid #d6eadf;
  border-radius: 10px;
  background: #fff;
  padding: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.thumb-btn:hover {
  border-color: #2ecc71;
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.15);
  transform: translateY(-1px);
}

.thumb-btn.active {
  border-color: #2ecc71;
  box-shadow: 0 0 0 3px rgba(46, 204, 113, 0.18);
}

.thumb-btn:active {
  transform: translateY(1px);
}

.thumb-btn img {
  width: 100%;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
}

.title {
  margin: 0;
  font-size: 30px;
  color: #153e30;
}

.subtitle {
  margin-top: 10px;
  color: #4e675c;
  line-height: 1.6;
}

.meta-row,
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.meta-pill,
.tag {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.meta-pill {
  background: linear-gradient(135deg, #2ecc71 0%, #23ad62 100%);
  color: #fff;
}

.meta-pill.soft {
  background: #edf7f2;
  color: #2f5546;
}

.tag {
  background: #f0fbf5;
  border: 1px solid #caead9;
  color: #2f6a52;
}

.price-card {
  margin-top: 16px;
  background: linear-gradient(130deg, #ecfbf3 0%, #dbf6e8 100%);
  border: 1px solid #bae8cf;
  border-radius: 12px;
  padding: 12px 14px;
}

.label {
  font-size: 13px;
  color: #276345;
}

.price {
  color: #158a4f;
  font-size: 34px;
  font-weight: 800;
}

.note {
  font-size: 12px;
  color: #2f6a52;
}

.purchase-row {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.field-label {
  width: 44px;
  color: #355549;
}

.stock {
  color: #1d8f54;
  font-size: 12px;
}

.actions {
  margin-top: 18px;
  display: flex;
  gap: 12px;
}

.content-row {
  margin-top: 16px;
}

.side-title {
  font-weight: 700;
}

.side-item {
  padding: 10px 0;
  border-bottom: 1px solid #eef2f7;
  cursor: pointer;
}

.side-item:last-child {
  border-bottom: none;
}

.side-item img {
  width: 100%;
  border-radius: 10px;
}

.side-name {
  margin-top: 8px;
  color: #355549;
}

.side-price {
  color: #188c51;
  font-weight: 700;
}

.desc-block {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.desc-block img {
  width: 100%;
  border-radius: 12px;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-body {
  margin-top: 8px;
  color: #4e675c;
}

:deep(.tabs-card .el-tabs__item.is-active) {
  color: #1d8f54;
}

:deep(.tabs-card .el-tabs__active-bar) {
  background-color: #2ecc71;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 12px;
  }
}

@media (max-width: 960px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }

  .main-image-box {
    height: 320px;
  }
}
</style>

