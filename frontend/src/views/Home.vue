<template>
  <div class="nutrishop-home-container">
    <div class="main-content container">
      <aside class="left-sidebar">
        <div class="sidebar-section">
          <h3 class="sidebar-title">商品分类</h3>
          <ul class="category-list">
            <li>心血管健康</li>
            <li>视力护眼</li>
            <li>运动营养</li>
            <li>综合维矿</li>
            <li>睡眠改善</li>
          </ul>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">价格区间</h3>
          <el-form :model="priceForm" :rules="priceRules" ref="priceFormRef">
            <el-form-item label="最低价格" prop="prodBegin">
              <el-input v-model="priceForm.prodBegin" placeholder="最低" />
            </el-form-item>
            <el-form-item label="最高价格" prop="prodEnd">
              <el-input v-model="priceForm.prodEnd" placeholder="最高" />
            </el-form-item>
            <el-button type="primary" size="small" @click="applyPriceFilter" class="apply-btn">应用</el-button>
          </el-form>
        </div>
      </aside>

      <section class="right-content">
        <div class="recommend-tip">
          <div class="tip-title">个性化推荐已启用</div>
          <div class="tip-subtitle">系统会根据你的年龄、性别、健康诉求和预算自动排序商品。</div>
        </div>

        <div class="top-banner">
          <img src="https://placehold.co/1200x220/131921/ffffff?text=NutriShop" alt="banner" />
        </div>

        <div class="sort-filter">
          <div class="sort-options">
            <span>排序方式：</span>
            <select v-model="sortBy" class="sort-select">
              <option value="default">综合排序</option>
              <option value="price_asc">价格从低到高</option>
              <option value="price_desc">价格从高到低</option>
            </select>
          </div>
        </div>

        <div class="products-grid">
          <div class="product-item" v-for="p in displayedProducts" :key="p.id">
            <product-card :item="p" @view="go" @add="add" />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/ProductCard.vue'

const products = ref<any[]>([])
const router = useRouter()

const sortBy = ref('default')

const priceForm = ref({
  prodBegin: '0',
  prodEnd: '999999'
})
const priceFormRef = ref<any>(null)
const priceFilterApplied = ref(false)

const isComplexMoney = (value: string): boolean => {
  const reg = /(^0$)|(^[1-9]\d{0,6}$)|(^[1-9]\d{0,6}\.\d{1,2}$)/
  return reg.test(value)
}

const priceRules = {
  prodBegin: [{
    validator: (_rule: any, value: string, callback: any) => {
      if (!isComplexMoney(value)) {
        callback(new Error('请输入正确格式的金额'))
      } else if (Number(value) >= Number(priceForm.value.prodEnd)) {
        callback(new Error('最低价格必须小于最高价格'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }],
  prodEnd: [{
    validator: (_rule: any, value: string, callback: any) => {
      if (!isComplexMoney(value)) {
        callback(new Error('请输入正确格式的金额'))
      } else if (Number(priceForm.value.prodBegin) >= Number(value)) {
        callback(new Error('最高价格必须大于最低价格'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }]
}

function applyPriceFilter() {
  priceFormRef.value.validate((valid: boolean) => {
    if (valid) {
      priceFilterApplied.value = true
      ElMessage.success('价格过滤已应用')
    } else {
      ElMessage.error('请输入正确的价格区间')
    }
  })
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
  if (Number.isFinite(rawUserId) && rawUserId > 0) {
    return rawUserId
  }

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

const loadProducts = async () => {
  const userId = await resolveUserId()
  const url = userId ? `/api/products/recommend?userId=${userId}` : '/api/products'
  const { data } = await axios.get(url)
  products.value = data
}

const displayedProducts = computed(() => {
  let result = [...products.value]

  if (priceFilterApplied.value) {
    const min = Number(priceForm.value.prodBegin)
    const max = Number(priceForm.value.prodEnd)
    result = result.filter((p) => Number(p.price) >= min && Number(p.price) <= max)
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
.nutrishop-home-container {
  background-color: #f5f5f5;
  min-height: 800px;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.main-content {
  padding: 20px 0;
  display: flex;
  gap: 20px;
}

.left-sidebar {
  width: 240px;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
}

.sidebar-section {
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 16px;
  margin-bottom: 10px;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  line-height: 28px;
  color: #555;
}

.apply-btn {
  width: 100%;
}

.right-content {
  flex: 1;
}

.recommend-tip {
  background: linear-gradient(120deg, #0f766e 0%, #0ea5e9 100%);
  color: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.tip-title {
  font-size: 16px;
  font-weight: 700;
}

.tip-subtitle {
  font-size: 13px;
  opacity: 0.92;
  margin-top: 2px;
}

.top-banner {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.top-banner img {
  width: 100%;
  display: block;
}

.sort-filter {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-select {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 6px 10px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-item {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 960px) {
  .main-content {
    flex-direction: column;
  }

  .left-sidebar {
    width: 100%;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
