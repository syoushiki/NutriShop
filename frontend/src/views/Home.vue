<template>
  <div class="home-page">
    <div class="container home-layout">
      <aside class="left-sidebar card-soft">
        <section class="sidebar-section">
          <h3 class="sidebar-title">分类筛选</h3>
          <div class="chip-wrap">
            <button
              class="chip"
              :class="{ active: activeCategory === 'all' }"
              @click="activeCategory = 'all'"
            >全部</button>
            <button
              v-for="c in categories"
              :key="c.key"
              class="chip"
              :class="{ active: activeCategory === c.key }"
              @click="activeCategory = c.key"
            >{{ c.label }}</button>
          </div>
        </section>

        <section class="sidebar-section">
          <div class="slider-head">
            <h3 class="sidebar-title">价格区间</h3>
            <span>¥{{ priceRange[0] }} - ¥{{ priceRange[1] }}</span>
          </div>
          <el-slider v-model="priceRange" range :min="0" :max="600" :step="10" />
          <el-button class="reset-btn" @click="resetFilter">重置筛选</el-button>
        </section>
      </aside>

      <section class="right-content">
        <div class="recommend-tip card-soft">
          <div class="tip-title">个性推荐模型运行中</div>
          <div class="tip-subtitle">系统依据年龄、性别、健康诉求与预算综合打分，优先展示更匹配商品。</div>
        </div>

        <div class="top-banner card-soft">
          <img src="https://placehold.co/1200x220/eafaf1/0f5132?text=NutriShop+Personalized+Recommendations" alt="banner" />
        </div>

        <div class="sort-bar card-soft">
          <div class="left">
            <span class="sort-label">排序</span>
            <select v-model="sortBy" class="sort-select">
              <option value="default">综合排序</option>
              <option value="price_asc">价格从低到高</option>
              <option value="price_desc">价格从高到低</option>
            </select>
          </div>
          <div class="count">共 {{ displayedProducts.length }} 件</div>
        </div>

        <transition-group name="fade-up" tag="div" class="products-grid">
          <div class="product-item" v-for="p in displayedProducts" :key="p.id">
            <product-card :item="p" @view="go" @add="add" />
          </div>
        </transition-group>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/ProductCard.vue'

const products = ref<any[]>([])
const router = useRouter()
const route = useRoute()

const sortBy = ref('default')
const activeCategory = ref('all')
const priceRange = ref<[number, number]>([0, 600])

const categories = [
  { key: 'cardio', label: '心血管健康' },
  { key: 'eyes', label: '视力护眼' },
  { key: 'fitness', label: '运动营养' },
  { key: 'nutrition', label: '综合维矿' },
  { key: 'sleep', label: '睡眠改善' }
]

function resetFilter() {
  activeCategory.value = 'all'
  priceRange.value = [0, 600]
  sortBy.value = 'default'
}

function go(id: number) {
  router.push(`/product/${id}`)
}

async function add(id: number) {
  try {
    await axios.post('/api/cart/add', { productId: id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch {
    ElMessage.error('加入购物车失败，请先登录')
  }
}

const resolveUserId = async (): Promise<number | null> => {
  const rawUserId = Number(localStorage.getItem('userId'))
  if (Number.isFinite(rawUserId) && rawUserId > 0) return rawUserId

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

const normalizeTags = (raw?: string) => {
  if (!raw) return [] as string[]
  return String(raw).split(',').map((s) => s.trim()).filter(Boolean)
}

const loadProducts = async () => {
  const userId = await resolveUserId()
  const url = userId ? `/api/products/recommend?userId=${userId}` : '/api/products'
  const { data } = await axios.get(url)
  products.value = data || []
}

const displayedProducts = computed(() => {
  let result = [...products.value]

  const q = String(route.query.q || '').trim().toLowerCase()
  if (q) {
    result = result.filter((p) => String(p.name || '').toLowerCase().includes(q))
  }

  const [min, max] = priceRange.value
  result = result.filter((p) => Number(p.price) >= min && Number(p.price) <= max)

  if (activeCategory.value !== 'all') {
    result = result.filter((p) => normalizeTags(p.tags).includes(activeCategory.value))
  }

  if (sortBy.value === 'price_asc') {
    result.sort((a, b) => Number(a.price) - Number(b.price))
  } else if (sortBy.value === 'price_desc') {
    result.sort((a, b) => Number(b.price) - Number(a.price))
  }

  return result
})

onMounted(loadProducts)
</script>

<style scoped>
.home-page {
  background: var(--bg-page);
  min-height: 100vh;
  padding: 22px 0 28px;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.home-layout {
  display: flex;
  gap: 22px;
}

.card-soft {
  background: var(--bg-card);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-soft);
}

.left-sidebar {
  width: 268px;
  padding: 16px;
  height: fit-content;
  position: sticky;
  top: 126px;
}

.sidebar-section + .sidebar-section {
  margin-top: 18px;
}

.sidebar-title {
  font-size: 15px;
  margin: 0 0 10px;
  color: #1f2937;
}

.chip-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border: 1px solid var(--line-soft);
  background: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip:hover {
  border-color: #bdebd0;
  background: #f3fcf7;
}

.chip.active {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
}

.slider-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
}

.slider-head span {
  color: #64748b;
  font-size: 12px;
}

.reset-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 10px;
}

.right-content {
  flex: 1;
  min-width: 0;
}

.recommend-tip {
  padding: 14px 16px;
  margin-bottom: 14px;
  background: linear-gradient(120deg, rgba(46, 204, 113, 0.12), rgba(34, 197, 94, 0.08));
}

.tip-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f5132;
}

.tip-subtitle {
  margin-top: 4px;
  color: #3f6b59;
  font-size: 13px;
}

.top-banner {
  margin-bottom: 14px;
  overflow: hidden;
}

.top-banner img {
  width: 100%;
  display: block;
}

.sort-bar {
  margin-bottom: 14px;
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sort-bar .left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  color: #64748b;
}

.sort-select {
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  padding: 6px 10px;
  color: #334155;
  background: #fff;
}

.count {
  color: #64748b;
  font-size: 13px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.product-item {
  min-width: 0;
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 14px;
  }
}

@media (max-width: 960px) {
  .home-layout {
    flex-direction: column;
  }

  .left-sidebar {
    width: 100%;
    position: static;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
