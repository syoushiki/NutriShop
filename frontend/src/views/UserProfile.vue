<template>
  <div class="profile-page">
    <div class="container">
      <el-card class="profile-card address-card">
        <template #header>
          <div class="card-header">
            <div>
              <h2>收货地址管理</h2>
              <p>支持新增多个地址，结算将使用默认地址</p>
            </div>
          </div>
        </template>

        <div class="address-create">
          <el-input v-model="newAddressLabel" placeholder="地址名称（如：家里 / 公司）" maxlength="20" />
          <el-input
            v-model="newAddressDetail"
            type="textarea"
            :rows="2"
            placeholder="请输入详细收货地址"
            maxlength="120"
            show-word-limit
          />
          <div class="create-actions">
            <el-checkbox v-model="newAddressAsDefault">设为默认地址</el-checkbox>
            <el-button type="primary" :disabled="!newAddressDetail.trim()" @click="addAddressCard">新增地址</el-button>
          </div>
        </div>

        <div v-if="addressCards.length" class="address-list">
          <article
            v-for="item in addressCards"
            :key="item.id"
            class="address-item"
            :class="{ active: item.isDefault }"
          >
            <div class="address-head">
              <div class="title-wrap">
                <strong>{{ item.label || '常用地址' }}</strong>
                <span v-if="item.isDefault" class="badge">默认</span>
              </div>
              <div class="ops">
                <el-button size="small" plain :disabled="item.isDefault" @click="setDefaultAddress(item.id)">设为默认</el-button>
                <el-button size="small" type="danger" plain @click="removeAddress(item.id)">删除</el-button>
              </div>
            </div>
            <p class="address-text">{{ item.detail }}</p>
          </article>
        </div>

        <el-empty v-else description="还没有地址，先新增一个吧" />
      </el-card>

      <el-card class="profile-card survey-card">
        <template #header>
          <div class="card-header survey-head">
            <div>
              <h3>个人画像</h3>
              <p>你可以随时重新填写问卷，系统会覆盖旧画像</p>
            </div>
            <el-button type="primary" plain @click="router.push('/profile-survey')">重新填写画像</el-button>
          </div>
        </template>

        <div v-if="hasSurvey" class="survey-content">
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-label">年龄段</div>
              <div class="stat-value">{{ survey.ageGroup || '-' }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">性别</div>
              <div class="stat-value">{{ survey.gender === 'male' ? '男' : survey.gender === 'female' ? '女' : '-' }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">预算偏好</div>
              <div class="stat-value">{{ budgetLabel(survey.budget) }}</div>
            </div>
          </div>

          <div class="goal-wrap">
            <div class="goal-title">健康诉求</div>
            <div class="goal-list">
              <span v-for="g in goalLabels" :key="g" class="goal-chip">{{ g }}</span>
              <span v-if="goalLabels.length === 0" class="goal-empty">暂无</span>
            </div>
          </div>

          <div class="print-box">
            <div class="print-title">画像数据（调试视图）</div>
            <pre class="print-block">{{ printableSurvey }}</pre>
          </div>
        </div>

        <div v-else class="empty-survey">
          <el-empty description="你还没有画像数据，填写后可获得更精准推荐">
            <el-button type="primary" @click="router.push('/profile-survey')">去填写推荐问卷</el-button>
          </el-empty>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

type AddressCard = {
  id: string
  label: string
  detail: string
  isDefault: boolean
}

const router = useRouter()
const savingAddress = ref(false)
const userId = ref<number | null>(null)

const addressCards = ref<AddressCard[]>([])
const newAddressLabel = ref('')
const newAddressDetail = ref('')
const newAddressAsDefault = ref(false)

const survey = reactive({
  id: null as number | null,
  ageGroup: '',
  gender: '',
  budget: '',
  healthGoals: ''
})
const hasSurvey = ref(false)

const goalLabelMap: Record<string, string> = {
  sleep: '睡眠改善',
  eyes: '护眼',
  fitness: '健身增肌/减脂',
  nutrition: '营养补充',
  cardio: '心血管健康'
}

const goalLabels = computed(() => {
  if (!survey.healthGoals) return []
  return survey.healthGoals
    .split(',')
    .map((g) => g.trim())
    .filter(Boolean)
    .map((g) => goalLabelMap[g] || g)
})

const budgetLabel = (budget: string) => {
  if (budget === 'low') return '¥100以内'
  if (budget === 'mid') return '¥100-¥300'
  if (budget === 'high') return '¥300以上'
  return budget || '-'
}

const printableSurvey = computed(() => {
  const payload = {
    ageGroup: survey.ageGroup || null,
    gender: survey.gender || null,
    budget: survey.budget || null,
    healthGoals: survey.healthGoals ? survey.healthGoals.split(',').map((g) => g.trim()).filter(Boolean) : []
  }
  return JSON.stringify(payload, null, 2)
})

const resolveUserId = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return null
  }

  const localId = Number(localStorage.getItem('userId'))
  if (Number.isFinite(localId) && localId > 0) {
    return localId
  }

  const { data } = await axios.get('/api/auth/me')
  if (data?.id) {
    localStorage.setItem('userId', String(data.id))
    return Number(data.id)
  }
  return null
}

function addressStorageKey(id: number) {
  return `address-book-${id}`
}

function normalizeAddressCards(cards: AddressCard[]) {
  const cleaned = cards
    .filter((c) => c && c.detail && String(c.detail).trim())
    .map((c, idx) => ({
      id: c.id || `${Date.now()}-${idx}`,
      label: c.label || '常用地址',
      detail: String(c.detail).trim(),
      isDefault: !!c.isDefault
    }))

  if (cleaned.length === 0) return cleaned
  const hasDefault = cleaned.some((c) => c.isDefault)
  if (!hasDefault) cleaned[0].isDefault = true
  if (hasDefault) {
    let seen = false
    cleaned.forEach((c) => {
      if (c.isDefault && !seen) {
        seen = true
      } else if (c.isDefault && seen) {
        c.isDefault = false
      }
    })
  }
  return cleaned
}

function saveAddressBookLocal() {
  if (!userId.value) return
  localStorage.setItem(addressStorageKey(userId.value), JSON.stringify(addressCards.value))
}

async function syncDefaultAddressToServer(showToast = false) {
  const defaultCard = addressCards.value.find((c) => c.isDefault)
  if (!defaultCard) return

  try {
    savingAddress.value = true
    await axios.put('/api/auth/me/address', { address: defaultCard.detail })
    if (showToast) ElMessage.success('默认地址已同步')
  } catch {
    ElMessage.error('同步默认地址失败')
  } finally {
    savingAddress.value = false
  }
}

function createAddressCard(detail: string, label = '常用地址', isDefault = false): AddressCard {
  return {
    id: `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
    label: label.trim() || '常用地址',
    detail: detail.trim(),
    isDefault
  }
}

const addAddressCard = async () => {
  const detail = newAddressDetail.value.trim()
  if (!detail) return

  const shouldDefault = newAddressAsDefault.value || addressCards.value.length === 0
  if (shouldDefault) {
    addressCards.value = addressCards.value.map((c) => ({ ...c, isDefault: false }))
  }

  addressCards.value.unshift(createAddressCard(detail, newAddressLabel.value, shouldDefault))
  addressCards.value = normalizeAddressCards(addressCards.value)
  saveAddressBookLocal()

  if (shouldDefault) {
    await syncDefaultAddressToServer(true)
  } else {
    ElMessage.success('地址已新增')
  }

  newAddressLabel.value = ''
  newAddressDetail.value = ''
  newAddressAsDefault.value = false
}

const setDefaultAddress = async (id: string) => {
  addressCards.value = addressCards.value.map((c) => ({ ...c, isDefault: c.id === id }))
  saveAddressBookLocal()
  await syncDefaultAddressToServer(true)
}

const removeAddress = async (id: string) => {
  const target = addressCards.value.find((c) => c.id === id)
  addressCards.value = addressCards.value.filter((c) => c.id !== id)
  addressCards.value = normalizeAddressCards(addressCards.value)
  saveAddressBookLocal()

  if (!target) return
  if (addressCards.value.length === 0) {
    ElMessage.success('地址已删除')
    return
  }

  if (target.isDefault) {
    await syncDefaultAddressToServer(true)
  } else {
    ElMessage.success('地址已删除')
  }
}

const loadAddressData = async () => {
  if (!userId.value) return
  const meRes = await axios.get('/api/auth/me')
  const serverAddress = String(meRes.data?.address || '').trim()

  const raw = localStorage.getItem(addressStorageKey(userId.value))
  let localCards: AddressCard[] = []
  if (raw) {
    try {
      localCards = JSON.parse(raw) || []
    } catch {
      localCards = []
    }
  }

  let merged = normalizeAddressCards(localCards)
  if (serverAddress && !merged.some((c) => c.detail === serverAddress)) {
    merged.unshift(createAddressCard(serverAddress, '默认地址', merged.length === 0))
    merged = normalizeAddressCards(merged)
  }

  addressCards.value = merged
  saveAddressBookLocal()
}

const loadSurveyData = async () => {
  if (!userId.value) return
  try {
    const surveyRes = await axios.get(`/api/user-profiles/${userId.value}`)
    survey.id = surveyRes.data?.id || null
    survey.ageGroup = surveyRes.data?.ageGroup || ''
    survey.gender = surveyRes.data?.gender || ''
    survey.budget = surveyRes.data?.budget || ''
    survey.healthGoals = surveyRes.data?.healthGoals || ''
    hasSurvey.value = !!survey.id
  } catch (error: any) {
    if (error.response?.status === 404) {
      hasSurvey.value = false
      return
    }
    throw error
  }
}

const loadProfileData = async () => {
  try {
    userId.value = await resolveUserId()
    if (!userId.value) return
    await loadAddressData()
    await loadSurveyData()
  } catch {
    ElMessage.error('加载个人信息失败')
  }
}

onMounted(loadProfileData)
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 8% 2%, rgba(46, 204, 113, 0.14), transparent 28%),
    radial-gradient(circle at 90% 12%, rgba(29, 161, 98, 0.1), transparent 24%),
    var(--bg-page);
  padding: 28px 0;
}

.container {
  width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 20px;
}

.profile-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.9);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-header h2,
.card-header h3 {
  margin: 0;
  color: #173e30;
}

.card-header p {
  margin: 6px 0 0;
  font-size: 12px;
  color: #5d796d;
}

.address-create {
  background: linear-gradient(120deg, #f4fcf7, #eef8f3);
  border: 1px solid #d8efe1;
  border-radius: 12px;
  padding: 12px;
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}

.create-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.address-list {
  display: grid;
  gap: 12px;
}

.address-item {
  border: 1px solid #dbece3;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.address-item.active {
  border-color: #63cf96;
  box-shadow: 0 0 0 3px rgba(99, 207, 150, 0.16);
}

.address-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge {
  font-size: 12px;
  color: #fff;
  background: #1fb26a;
  padding: 2px 8px;
  border-radius: 999px;
}

.ops {
  display: flex;
  gap: 8px;
}

.address-text {
  margin: 10px 0 0;
  color: #355549;
  line-height: 1.6;
  white-space: pre-wrap;
}

.survey-head {
  align-items: flex-start;
}

.survey-content {
  display: grid;
  gap: 14px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-item {
  border-radius: 12px;
  border: 1px solid #d9ede3;
  background: linear-gradient(150deg, #f4fbf7, #eff8f3);
  padding: 12px;
}

.stat-label {
  font-size: 12px;
  color: #618175;
}

.stat-value {
  margin-top: 6px;
  color: #173e30;
  font-weight: 700;
}

.goal-wrap {
  border: 1px solid #dbece3;
  border-radius: 12px;
  padding: 12px;
}

.goal-title {
  color: #1f4f3d;
  font-weight: 700;
  margin-bottom: 8px;
}

.goal-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.goal-chip {
  font-size: 12px;
  color: #1f6a4b;
  background: #e9f9f0;
  border: 1px solid #ccefdc;
  padding: 4px 10px;
  border-radius: 999px;
}

.goal-empty {
  color: #70897f;
  font-size: 13px;
}

.print-box {
  border: 1px dashed #d1e7dd;
  border-radius: 12px;
  padding: 12px;
}

.print-title {
  font-weight: 700;
  margin-bottom: 8px;
  color: #1f4f3d;
}

.print-block {
  margin: 0;
  background: #10261d;
  color: #d7efe3;
  border-radius: 10px;
  padding: 12px;
  font-size: 12px;
  line-height: 1.5;
  overflow-x: auto;
}

.empty-survey {
  min-height: 220px;
  display: grid;
  place-items: center;
}

@media (max-width: 1100px) {
  .container {
    width: 100%;
    padding: 0 12px;
    grid-template-columns: 1fr;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }

  .address-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .ops {
    width: 100%;
  }
}
</style>
