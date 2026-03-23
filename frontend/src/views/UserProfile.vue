<template>
  <div class="profile-page">
    <div class="container">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header"><h2>个人主页</h2></div>
        </template>

        <el-form label-position="top">
          <el-form-item label="收货地址">
            <el-input v-model="address" type="textarea" :rows="3" placeholder="请输入详细收货地址" />
          </el-form-item>
          <el-button type="primary" :loading="savingAddress" @click="saveAddress">保存地址</el-button>
        </el-form>
      </el-card>

      <el-card class="profile-card survey-card">
        <template #header>
          <div class="card-header"><h3>推荐画像结果</h3></div>
        </template>

        <div v-if="hasSurvey" class="survey-content">
          <p><strong>年龄段：</strong>{{ survey.ageGroup }}</p>
          <p><strong>性别：</strong>{{ survey.gender === 'male' ? '男' : survey.gender === 'female' ? '女' : '-' }}</p>
          <p><strong>预算偏好：</strong>{{ budgetLabel(survey.budget) }}</p>
          <p><strong>健康诉求：</strong>{{ goalLabels.length ? goalLabels.join('、') : '-' }}</p>

          <el-divider />
          <div class="print-title">问卷结果打印输出</div>
          <pre class="print-block">{{ printableSurvey }}</pre>
        </div>

        <div v-else class="empty-survey">
          <router-link to="/profile-survey">去填写推荐问卷</router-link>
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

const router = useRouter()
const savingAddress = ref(false)
const address = ref('')

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

const loadProfileData = async () => {
  try {
    const id = await resolveUserId()
    if (!id) return

    const meRes = await axios.get('/api/auth/me')
    address.value = meRes.data?.address || ''

    try {
      const surveyRes = await axios.get(`/api/user-profiles/${id}`)
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
  } catch {
    ElMessage.error('加载个人信息失败')
  }
}

const saveAddress = async () => {
  try {
    savingAddress.value = true
    const { data } = await axios.put('/api/auth/me/address', { address: address.value })
    address.value = data?.address || ''
    ElMessage.success('地址已保存')
  } catch {
    ElMessage.error('保存地址失败')
  } finally {
    savingAddress.value = false
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
  width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.profile-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.9);
}

.card-header h2,
.card-header h3 {
  margin: 0;
  color: #173e30;
}

.survey-content p {
  margin: 8px 0;
  color: #39594b;
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
  min-height: 140px;
  display: flex;
  align-items: center;
}

.empty-survey a {
  color: #1f9b60;
  text-decoration: none;
  font-weight: 600;
}

@media (max-width: 960px) {
  .container {
    width: 100%;
    padding: 0 12px;
    grid-template-columns: 1fr;
  }
}
</style>
