<template>
  <div class="profile-survey-container">
    <el-card class="survey-card">
      <template #header>
        <div class="card-header">
          <h2>个人健康档案</h2>
          <p class="subtitle">定制您的专属营养方案</p>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center class="steps-bar">
        <el-step title="基础信息" description="建立用户基础模型"></el-step>
        <el-step title="健康诉求" description="生活状态与目标"></el-step>
        <el-step title="消费偏好" description="预算范围"></el-step>
      </el-steps>

      <div class="form-content">
        <el-form ref="formRef" :model="formData" label-position="top" size="large">
          
          <!-- Step 1: Basic Info -->
          <div v-show="activeStep === 0" class="step-content">
            <h3>第一部分：基础信息</h3>
            <p class="section-desc">用于建立用户基础模型，确定营养素剂量的基准权重</p>
            
            <el-form-item label="1. 您的年龄段" prop="ageGroup" :rules="[{ required: true, message: '请选择年龄段', trigger: 'change' }]">
              <el-radio-group v-model="formData.ageGroup" class="vertical-radio">
                <el-radio label="18-30">18-30岁 <span class="option-hint">(青年：关注效率、运动、颜值)</span></el-radio>
                <el-radio label="31-50">31-50岁 <span class="option-hint">(中年：关注压力管理、抗衰、体能)</span></el-radio>
                <el-radio label="50+">50岁以上 <span class="option-hint">(中老年：关注心脑健康、骨骼、免疫)</span></el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="2. 您的性别" prop="gender" :rules="[{ required: true, message: '请选择性别', trigger: 'change' }]">
              <el-radio-group v-model="formData.gender" class="vertical-radio">
                <el-radio label="male">男 <span class="option-hint">(偏向：精力、肌肉、前列腺健康)</span></el-radio>
                <el-radio label="female">女 <span class="option-hint">(偏向：皮肤、贫血补铁、卵巢保养)</span></el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <!-- Step 2: Health Goals -->
          <div v-show="activeStep === 1" class="step-content">
            <h3>第二部分：生活状态与健康诉求</h3>
            <p class="section-desc">多选题，我们将根据您的选择推荐对应分类的商品</p>
            
            <el-form-item prop="healthGoals" :rules="[{ required: true, message: '请至少选择一项健康诉求', trigger: 'change' }]">
              <el-checkbox-group v-model="formData.healthGoals" class="vertical-checkbox">
                <el-checkbox label="sleep" border>
                  <div class="checkbox-content">
                    <strong>经常熬夜，入睡困难或睡眠质量不佳</strong>
                    <span class="tag">改善睡眠</span>
                  </div>
                </el-checkbox>
                <el-checkbox label="eyes" border>
                  <div class="checkbox-content">
                    <strong>长时间面对电脑/手机，感觉眼睛干涩、疲劳</strong>
                    <span class="tag">视力护眼</span>
                  </div>
                </el-checkbox>
                <el-checkbox label="fitness" border>
                  <div class="checkbox-content">
                    <strong>有规律健身习惯，或近期计划增肌/减脂/提高耐力</strong>
                    <span class="tag">运动营养</span>
                  </div>
                </el-checkbox>
                <el-checkbox label="nutrition" border>
                  <div class="checkbox-content">
                    <strong>饮食不规律（外卖多、挑食），想补充日常营养缺失</strong>
                    <span class="tag">综合维矿</span>
                  </div>
                </el-checkbox>
                <el-checkbox label="cardio" border>
                  <div class="checkbox-content">
                    <strong>关注血压、血脂指标，或关注心脑血管保养</strong>
                    <span class="tag">心血管健康</span>
                  </div>
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </div>

          <!-- Step 3: Budget -->
          <div v-show="activeStep === 2" class="step-content">
            <h3>第三部分：消费偏好与预算</h3>
            <p class="section-desc">用于过滤搜索结果或排序，确保推荐符合您的消费能力</p>
            
            <el-form-item label="单件产品的月预算倾向" prop="budget" :rules="[{ required: true, message: '请选择预算范围', trigger: 'change' }]">
              <el-radio-group v-model="formData.budget" class="budget-cards">
                <el-radio label="low" border class="budget-card">
                  <div class="budget-content">
                    <strong>简约性价比</strong>
                    <span>¥100以内</span>
                  </div>
                </el-radio>
                <el-radio label="medium" border class="budget-card">
                  <div class="budget-content">
                    <strong>均衡品质感</strong>
                    <span>¥100 - ¥300</span>
                  </div>
                </el-radio>
                <el-radio label="high" border class="budget-card">
                  <div class="budget-content">
                    <strong>高端专业级</strong>
                    <span>¥300以上</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

        </el-form>
      </div>

      <div class="actions">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 2" type="success" @click="submitForm">生成推荐方案</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeStep = ref(0)
const formRef = ref()

const formData = reactive({
  ageGroup: '',
  gender: '',
  healthGoals: [],
  budget: ''
})

const nextStep = async () => {
  if (activeStep.value < 2) {
    let fieldsToValidate: string[] = []
    
    if (activeStep.value === 0) {
      fieldsToValidate = ['ageGroup', 'gender']
    } else if (activeStep.value === 1) {
      fieldsToValidate = ['healthGoals']
    }

    if (fieldsToValidate.length > 0) {
      try {
        await formRef.value.validateField(fieldsToValidate)
        activeStep.value++
      } catch (error) {
        // Validation failed
        return
      }
    } else {
      activeStep.value++
    }
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const submitForm = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      console.log('Form submitted:', formData)
      ElMessage.success('您的个人档案已建立，正在为您生成推荐方案...')
      // Simulate API call and redirect
      setTimeout(() => {
        router.push('/') // Redirect to home or recommendation page
      }, 1500)
    } else {
      ElMessage.warning('请填写完整信息')
      return false
    }
  })
}
</script>

<style scoped>
.profile-survey-container {
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.survey-card {
  width: 100%;
  max-width: 800px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.subtitle {
  color: #909399;
  margin: 8px 0 0;
  font-size: 14px;
}

.steps-bar {
  margin: 30px 0 40px;
}

.form-content {
  padding: 0 20px;
  min-height: 300px;
}

.step-content {
  animation: fadeIn 0.5s ease;
}

.section-desc {
  color: #606266;
  font-style: italic;
  margin-bottom: 20px;
  font-size: 14px;
}

.vertical-radio {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.option-hint {
  color: #909399;
  font-size: 13px;
  margin-left: 4px;
}

.vertical-checkbox {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vertical-checkbox .el-checkbox {
  margin-left: 0;
  width: 100%;
  height: auto;
  padding: 12px 16px;
}

.checkbox-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  white-space: normal;
  line-height: 1.5;
}

.checkbox-content strong {
  flex: 1;
  margin-right: 12px;
}

.tag {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
}

.budget-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.budget-card {
  width: 100%;
  margin-left: 0 !important;
  height: auto;
  padding: 16px;
}

.budget-content {
  display: flex;
  justify-content: space-between;
  width: 100%;
  align-items: center;
}

.actions {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
  padding: 20px 0 0;
  border-top: 1px solid #ebeef5;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 600px) {
  .form-content {
    padding: 0;
  }
  
  .checkbox-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .tag {
    margin-top: 4px;
  }
}
</style>
