<template>
  <div class="container">
    <div class="card">
      <h2 class="title">用户中心</h2>

      <div class="tab-container">
        <button
          :class="['tab', { 'active-tab': activeTab === 'login' }]"
          @click="setActiveTab('login')"
        >
          登录
        </button>
        <button
          :class="['tab', { 'active-tab': activeTab === 'register' }]"
          @click="setActiveTab('register')"
        >
          注册
        </button>
      </div>

      <div v-if="activeTab === 'login'">
        <input
          class="input"
          placeholder="用户名"
          v-model="loginForm.username"
        />
        <input
          class="input"
          type="password"
          placeholder="密码"
          v-model="loginForm.password"
        />
        <button
          class="button"
          @click="handleLogin"
          :disabled="loading"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>

      <div v-if="activeTab === 'register'">
        <input
          class="input"
          placeholder="用户名"
          v-model="registerForm.username"
        />
        <input
          class="input"
          type="email"
          placeholder="邮箱"
          v-model="registerForm.email"
        />
        <input
          class="input"
          type="tel"
          placeholder="手机号"
          v-model="registerForm.phone"
        />
        <input
          class="input"
          type="password"
          placeholder="密码（至少6位）"
          v-model="registerForm.password"
        />
        <input
          class="input"
          type="password"
          placeholder="确认密码"
          v-model="registerForm.confirmPassword"
        />
        <button
          class="button register-button"
          @click="handleRegister"
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

interface LoginForm {
  username: string
  password: string
}

interface RegisterForm extends LoginForm {
  confirmPassword: string
  email: string
  phone: string
}

const router = useRouter()
const activeTab = ref<'login' | 'register'>('login')
const loading = ref(false)

const loginForm = ref<LoginForm>({
  username: '',
  password: '',
})

const registerForm = ref<RegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
})

const setActiveTab = (tab: 'login' | 'register') => {
  activeTab.value = tab
}

const validateLogin = (): boolean => {
  if (!loginForm.value.username.trim()) {
    ElMessage.error('请输入用户名')
    return false
  }
  if (!loginForm.value.password.trim()) {
    ElMessage.error('请输入密码')
    return false
  }
  return true
}

const validateRegister = (): boolean => {
  if (!registerForm.value.username.trim()) {
    ElMessage.error('请输入用户名')
    return false
  }
  if (!registerForm.value.email.trim()) {
    ElMessage.error('请输入邮箱')
    return false
  }
  if (!registerForm.value.phone.trim()) {
    ElMessage.error('请输入手机号')
    return false
  }
  if (registerForm.value.password.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return false
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return false
  }
  return true
}

const handleLogin = async () => {
  if (!validateLogin()) return

  try {
    loading.value = true
    const response = await axios.post('/api/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('username', loginForm.value.username)
    ElMessage.success('登录成功')
    window.dispatchEvent(new Event('auth-change'))
    router.push('/')
  } catch (error: any) {
    ElMessage.error('登录失败：' + (error.response?.data?.message || '用户名或密码错误'))
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!validateRegister()) return

  try {
    loading.value = true
    const response = await axios.post('/api/auth/register', {
      username: registerForm.value.username,
      password: registerForm.value.password,
      email: registerForm.value.email,
      phone: registerForm.value.phone
    })
    
    // Auto login
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('username', registerForm.value.username)
    ElMessage.success('注册成功，即将为您跳转...')
    window.dispatchEvent(new Event('auth-change'))
    
    setTimeout(() => {
      router.push('/profile-survey')
    }, 1000)
    
  } catch (error: any) {
    console.error('注册失败:', error)
    if (error.response) {
      // 服务器返回错误
      let msg = '注册失败，请稍后重试';
      if (typeof error.response.data === 'string') {
        msg = error.response.data;
      } else if (error.response.data && error.response.data.message) {
        msg = error.response.data.message;
      }
      ElMessage.error('注册失败：' + msg)
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('注册失败：无法连接到服务器，请检查网络连接')
    } else {
      // 请求配置出错
      ElMessage.error('注册失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}

.card {
  width: 380px;
  padding: 30px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08);
}

.title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.tab-container {
  display: flex;
  margin-bottom: 20px;
}

.tab {
  flex: 1;
  padding: 10px;
  cursor: pointer;
  border: none;
  background: #f0f2f5;
  color: #333;
  font-size: 14px;
}

.active-tab {
  flex: 1;
  padding: 10px;
  cursor: pointer;
  border: none;
  background: #000;
  color: #fff;
  font-size: 14px;
}

.input {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border-radius: 8px;
  border: 1px solid #ddd;
  font-size: 14px;
}

.button {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: none;
  background-color: #000;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
}

.register-button {
  background-color: #000;
}

.button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>