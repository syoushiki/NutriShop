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
          </div>
          <div class="price-inputs">
            <el-input-number v-model="priceRange[0]" :min="0" :max="priceRange[1]" :controls="false" placeholder="最低" />
            <span class="separator">-</span>
            <el-input-number v-model="priceRange[1]" :min="priceRange[0]" :max="600" :controls="false" placeholder="最高" />
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

    <button class="ai-float-btn" @click="toggleAiDialog" aria-label="打开 AI 助手">
      AI
    </button>

    <transition name="ai-pop">
      <section v-if="aiOpen" class="ai-chat-panel card-soft">
        <header class="ai-chat-header">
          <strong>AI 助手</strong>
          <button class="ai-close-btn" @click="aiOpen = false" aria-label="关闭 AI 助手">×</button>
        </header>

        <div class="ai-chat-body">
          <div
            v-for="(msg, index) in aiMessages"
            :key="index"
            class="ai-message"
            :class="`is-${msg.role}`"
          >
            {{ msg.content }}
          </div>
        </div>

        <footer class="ai-chat-footer">
          <input
            v-model="aiInput"
            class="ai-input"
            placeholder="例如：我最近有些失眠，帮我加2件"
            @keydown.enter.exact.prevent="handleAiSend"
          />
          <button class="ai-send-btn" :disabled="aiSending" @click="handleAiSend">发送</button>
        </footer>
      </section>
    </transition>
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
const keywordLabelMap: Record<string, string> = {
  sleep: '助眠',
  cardio: '心血管',
  eyes: '护眼',
  fitness: '健身营养',
  nutrition: '维生素/综合营养'
}

type ChatRole = 'assistant' | 'user'
type ChatMessage = {
  role: ChatRole
  content: string
}

type PendingBudgetAction = {
  keyword: string
  candidates: any[]
  setCount: number
}

const aiOpen = ref(false)
const aiInput = ref('')
const aiSending = ref(false)
const aiMessages = ref<ChatMessage[]>([
  { role: 'assistant', content: '您好，我是 AI 助手。您可以说“我最近有些失眠”或“我想买点维生素，有什么推荐”。' }
])
const pendingBudgetAction = ref<PendingBudgetAction | null>(null)

const keywordGroups: Record<string, string[]> = {
  sleep: ['失眠', '睡眠', '睡不着', '入睡困难', '助眠', '褪黑素', 'sleep'],
  cardio: ['心血管', '血压', '血脂', '心脏', 'cardio'],
  eyes: ['视力', '眼睛', '护眼', '叶黄素', 'eyes'],
  fitness: ['运动', '健身', '增肌', '蛋白', 'fitness'],
  nutrition: ['维生素', '营养', '矿物质', '营养素', 'nutrition']
}

const zhNumMap: Record<string, number> = {
  一: 1,
  二: 2,
  两: 2,
  三: 3,
  四: 4,
  五: 5,
  六: 6,
  七: 7,
  八: 8,
  九: 9,
  十: 10
}

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

function toggleAiDialog() {
  aiOpen.value = !aiOpen.value
}

function parseQuantity(text: string) {
  const numericMatch = text.match(/(\d+)\s*(?:件|个|盒|瓶|份|袋|支|粒|罐)/)
  if (numericMatch) {
    const n = Number(numericMatch[1])
    if (Number.isFinite(n) && n > 0) return n
  }

  const zhMatch = text.match(/([一二两三四五六七八九十])\s*(?:件|个|盒|瓶|份|袋|支|粒|罐|套)/)
  if (zhMatch) {
    return zhNumMap[zhMatch[1]] || 1
  }

  return 1
}

function parseSetCount(text: string) {
  const numericMatch = text.match(/(\d+)\s*(?:套|组)/)
  if (numericMatch) {
    const n = Number(numericMatch[1])
    if (Number.isFinite(n) && n > 0) return n
  }

  const zhMatch = text.match(/([一二两三四五六七八九十])\s*(?:套|组)/)
  if (zhMatch) return zhNumMap[zhMatch[1]] || 1
  return 1
}

function parseBudget(text: string) {
  const match = text.match(/(\d+(?:\.\d+)?)/)
  if (!match) return null
  const value = Number(match[1])
  return Number.isFinite(value) && value > 0 ? value : null
}

function normalizeText(value: unknown) {
  return String(value || '').toLowerCase()
}

function extractKeyword(text: string) {
  const lower = text.toLowerCase()
  const direct = Object.entries(keywordGroups).find(([, aliases]) => aliases.some((alias) => lower.includes(alias.toLowerCase())))
  return direct?.[0] || ''
}

function getSearchTokens(keyword: string) {
  return keywordGroups[keyword] || [keyword]
}

function sortByRelevanceAndPrice(list: any[]) {
  return [...list].sort((a, b) => {
    const scoreDiff = Number(b?.relevanceScore || 0) - Number(a?.relevanceScore || 0)
    if (scoreDiff !== 0) return scoreDiff
    return Number(a?.price || 0) - Number(b?.price || 0)
  })
}

function findProductsByKeyword(keyword: string) {
  const tokens = getSearchTokens(keyword).map((s) => s.toLowerCase())
  const matched = products.value.filter((p) => {
    const fullText = [p?.name, p?.description, p?.tags].map(normalizeText).join(' ')
    return tokens.some((token) => fullText.includes(token))
  })
  return sortByRelevanceAndPrice(matched)
}

function isRecommendationIntent(text: string) {
  return /(推荐|有什么|有哪些|买点|想买|求推荐|推荐下)/.test(text)
}

function formatCandidateLines(list: any[]) {
  return list
    .map((item: any, idx: number) => `${idx + 1}. ${item.name}（¥${Number(item.price || 0).toFixed(2)}）`)
    .join('\n')
}

function buildSetWithBudget(candidates: any[], budget: number, size = 3) {
  const pool = sortByRelevanceAndPrice(candidates)
  const selected: any[] = []
  let total = 0

  for (const item of pool) {
    const price = Number(item?.price || 0)
    if (selected.length >= size) break
    if (total + price <= budget) {
      selected.push(item)
      total += price
    }
  }

  if (selected.length === 0 && pool.length > 0) {
    selected.push(pool[0])
    total = Number(pool[0]?.price || 0)
  }

  return { selected, total }
}

function buildDefaultSet(candidates: any[], size = 2) {
  return sortByRelevanceAndPrice(candidates).slice(0, size)
}

async function addSetToCart(items: any[], setCount: number) {
  for (const item of items) {
    await axios.post('/api/cart/add', { productId: item.id, quantity: setCount })
  }
}

async function handleAiSend() {
  const text = aiInput.value.trim()
  if (!text || aiSending.value) return

  aiMessages.value.push({ role: 'user', content: text })
  aiInput.value = ''
  aiSending.value = true

  const detectedKeyword = extractKeyword(text)
  const containsBudgetNumber = parseBudget(text) !== null

  // 用户在预算追问过程中提出了新的需求关键词时，优先切换到新需求，避免串到上一轮场景。
  if (pendingBudgetAction.value && detectedKeyword && detectedKeyword !== pendingBudgetAction.value.keyword) {
    pendingBudgetAction.value = null
  }

  if (
    pendingBudgetAction.value &&
    containsBudgetNumber &&
    (!detectedKeyword || detectedKeyword === pendingBudgetAction.value.keyword)
  ) {
    const budget = parseBudget(text)
    if (!budget) {
      aiMessages.value.push({ role: 'assistant', content: '我还没识别到预算金额，请回复如“200”或“预算300元”。' })
      aiSending.value = false
      return
    }

    const { keyword, candidates, setCount } = pendingBudgetAction.value
    const { selected, total } = buildSetWithBudget(candidates, budget, 3)
    if (selected.length === 0) {
      aiMessages.value.push({ role: 'assistant', content: '这个预算下还没筛到合适商品，您可以提高预算或换个需求。' })
      aiSending.value = false
      return
    }

    try {
      await addSetToCart(selected, setCount)
      aiMessages.value.push({
        role: 'assistant',
        content: `已为您把商品加入购物车\n已按“${keyword}”为您加入${setCount}套，共${selected.length}款（合计约¥${total.toFixed(2)}）。`
      })
    } catch {
      aiMessages.value.push({ role: 'assistant', content: '加入购物车失败，请先登录后再试。' })
    } finally {
      pendingBudgetAction.value = null
      aiSending.value = false
    }
    return
  }

  const keyword = detectedKeyword
  const setCount = Math.max(parseSetCount(text), parseQuantity(text))

  if (!keyword) {
    aiMessages.value.push({ role: 'assistant', content: '暂未识别到商品关键词，请补充例如“失眠、护眼、维生素”等需求。' })
    aiSending.value = false
    return
  }

  const candidates = findProductsByKeyword(keyword)
  if (candidates.length === 0) {
    aiMessages.value.push({ role: 'assistant', content: '已识别到需求，但暂未找到匹配商品，请换个说法再试试。' })
    aiSending.value = false
    return
  }

  if (isRecommendationIntent(text)) {
    const topList = candidates.slice(0, 5)
    const keywordLabel = keywordLabelMap[keyword] || '相关'
    aiMessages.value.push({
      role: 'assistant',
      content: `${keywordLabel}商品推荐如下：\n${formatCandidateLines(topList)}\n请问您的预算大概多少？我会按预算给您配一套并加入购物车。`
    })
    pendingBudgetAction.value = { keyword, candidates, setCount }
    aiSending.value = false
    return
  }

  const setItems = buildDefaultSet(candidates, 2)
  try {
    await addSetToCart(setItems, setCount)
    aiMessages.value.push({ role: 'assistant', content: '已为您把商品加入购物车' })
  } catch {
    aiMessages.value.push({ role: 'assistant', content: '加入购物车失败，请先登录后再试。' })
  } finally {
    aiSending.value = false
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
  transition: all 0.25s ease;
  font-weight: 500;
}

.chip:hover {
  border-color: #bdebd0;
  background: #f3fcf7;
  color: var(--brand);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
}

.chip.active {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

.chip:active {
  transform: translateY(1px);
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

.price-inputs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.price-inputs :deep(.el-input-number) {
  width: 45%;
}

.price-inputs :deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.separator {
  color: #64748b;
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

.ai-float-btn {
  position: fixed;
  right: 20px;
  bottom: 26px;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #2ecc71, #1f9b60);
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(31, 155, 96, 0.35);
  z-index: 30;
}

.ai-float-btn:hover {
  transform: translateY(-2px);
}

.ai-chat-panel {
  position: fixed;
  right: 20px;
  bottom: 94px;
  width: min(380px, calc(100vw - 24px));
  z-index: 30;
  overflow: hidden;
}

.ai-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid var(--line-soft);
  background: #f4fbf7;
}

.ai-close-btn {
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  background: #e5f5ec;
  color: #2f6a52;
}

.ai-chat-body {
  max-height: 360px;
  overflow: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-message {
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.5;
  font-size: 13px;
}

.ai-message.is-assistant {
  align-self: flex-start;
  background: #eef9f3;
  color: #245844;
}

.ai-message.is-user {
  align-self: flex-end;
  background: #2ecc71;
  color: #fff;
}

.ai-chat-footer {
  border-top: 1px solid var(--line-soft);
  padding: 10px;
  display: flex;
  gap: 8px;
}

.ai-input {
  flex: 1;
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  padding: 8px 10px;
  outline: none;
}

.ai-input:focus {
  border-color: #7ad6a5;
}

.ai-send-btn {
  border: none;
  border-radius: 10px;
  padding: 0 14px;
  cursor: pointer;
  color: #fff;
  background: #2aa965;
}

.ai-send-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.ai-pop-enter-active,
.ai-pop-leave-active {
  transition: all 0.2s ease;
}

.ai-pop-enter-from,
.ai-pop-leave-to {
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

  .ai-float-btn {
    right: 12px;
    bottom: 18px;
  }

  .ai-chat-panel {
    right: 12px;
    bottom: 84px;
  }
}
</style>
