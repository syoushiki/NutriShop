<template>
  <div class="page">
    <div class="header">
      <div class="title">个性化营养问卷</div>
      <div class="subtitle">用于生成你的专属健康画像与推荐方案</div>
    </div>

    <div class="card">
      <el-form label-position="top">
        <div class="section">
          <div class="section-title">基础信息</div>

          <el-form-item label="您的年龄段">
            <el-radio-group v-model="form.age">
              <el-radio label="18-30">18-30岁</el-radio>
              <el-radio label="31-50">31-50岁</el-radio>
              <el-radio label="50+">50岁以上</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="您的性别">
            <el-radio-group v-model="form.gender">
              <el-radio label="male">男</el-radio>
              <el-radio label="female">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <div class="section">
          <div class="section-title">生活状态与健康诉求</div>

          <el-checkbox-group v-model="form.goals" class="checkbox-group">
            <el-checkbox label="sleep">经常熬夜 / 睡眠质量不佳</el-checkbox>
            <el-checkbox label="eyes">用眼疲劳 / 干涩</el-checkbox>
            <el-checkbox label="fitness">健身 / 增肌 / 减脂</el-checkbox>
            <el-checkbox label="nutrition">饮食不规律 / 营养缺失</el-checkbox>
            <el-checkbox label="cardio">心血管健康关注</el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="section">
          <div class="section-title">预算偏好</div>

          <el-form-item label="单件产品预算">
            <el-radio-group v-model="form.budget">
              <el-radio label="low">¥100以内</el-radio>
              <el-radio label="mid">¥100 - ¥300</el-radio>
              <el-radio label="high">¥300以上</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">
            保存画像并生成推荐
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const submitting = ref(false)

const form = reactive({
  age: '',
  gender: '',
  goals: [] as string[],
  budget: ''
})

const resolveUserId = async () => {
  const fromStorage = Number(localStorage.getItem('userId'))
  if (Number.isFinite(fromStorage) && fromStorage > 0) {
    return fromStorage
  }

  const { data } = await axios.get('/api/auth/me')
  if (!data?.id) {
    throw new Error('未获取到用户ID')
  }
  localStorage.setItem('userId', String(data.id))
  return Number(data.id)
}

const handleSubmit = async () => {
  if (!form.age || !form.gender || !form.budget || form.goals.length === 0) {
    ElMessage.warning('请先完整填写问卷')
    return
  }

  try {
    submitting.value = true
    const userId = await resolveUserId()

    await axios.post('/api/user-profiles', {
      userId,
      ageGroup: form.age,
      gender: form.gender,
      healthGoals: form.goals,
      budget: form.budget
    })

    ElMessage.success('画像已保存，首页将展示个性化推荐')
    router.push('/')
  } catch (error: any) {
    if (error.response?.status === 401) {
      ElMessage.error('请先登录后再提交问卷')
      router.push('/login')
      return
    }
    ElMessage.error('画像保存失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 12% 0%, rgba(46, 204, 113, 0.16), transparent 33%),
    radial-gradient(circle at 90% 8%, rgba(36, 172, 99, 0.12), transparent 24%),
    var(--bg-page);
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #173e30;
}

.subtitle {
  font-size: 14px;
  color: var(--text-muted);
}

.card {
  width: 100%;
  max-width: 560px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: var(--radius-lg);
  padding: 30px;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

.section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  position: relative;
  color: #1f4f3d;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #2ecc71;
  position: absolute;
  left: -10px;
  top: 3px;
  border-radius: 2px;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
}

:deep(.el-radio__input.is-checked .el-radio__inner),
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  border-color: #2ecc71;
  background: #2ecc71;
}

:deep(.el-radio__input.is-checked + .el-radio__label),
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #1f9b60;
}
</style>
