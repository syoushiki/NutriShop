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
        <input class="input" placeholder="用户名" v-model="loginForm.username" />
        <input class="input" type="password" placeholder="密码" v-model="loginForm.password" />
        <button class="button" @click="handleLogin" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>

      <div v-if="activeTab === 'register'">
        <input class="input" placeholder="用户名" v-model="registerForm.username" />
        <input class="input" type="email" placeholder="邮箱" v-model="registerForm.email" />
        <input class="input" type="tel" placeholder="手机号" v-model="registerForm.phone" />
        <input class="input" type="password" placeholder="密码（至少6位）" v-model="registerForm.password" />
        <input class="input" type="password" placeholder="确认密码" v-model="registerForm.confirmPassword" />
        <button class="button register-button" @click="handleRegister" :disabled="loading">
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
  password: ''
})

const registerForm = ref<RegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
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
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    localStorage.setItem('token', response.data.token)
    localStorage.setItem('username', loginForm.value.username)

    try {
      const meResponse = await axios.get('/api/auth/me', {
        headers: {
          Authorization: `Bearer ${response.data.token}`
        }
      })
      const role = meResponse.data.role || 'USER'
      localStorage.setItem('role', role)
      if (meResponse.data?.id) {
        localStorage.setItem('userId', String(meResponse.data.id))
      }

      ElMessage.success('登录成功')
      window.dispatchEvent(new Event('auth-change'))
      if (role === 'ADMIN') {
        router.push('/admin')
      } else {
        router.push('/')
      }
    } catch {
      localStorage.setItem('role', 'USER')
      localStorage.removeItem('userId')
      ElMessage.success('登录成功')
      window.dispatchEvent(new Event('auth-change'))
      router.push('/')
    }
  } catch (error: any) {
    if (error.response && error.response.status === 401) {
      ElMessage.error('用户名或密码不正确')
    } else {
      ElMessage.error('登录失败：' + (error.response?.data?.message || '服务器内部错误'))
    }
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
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    localStorage.setItem('token', response.data.token)
    localStorage.setItem('username', registerForm.value.username)
    localStorage.setItem('role', 'USER')

    try {
      const meResponse = await axios.get('/api/auth/me', {
        headers: {
          Authorization: `Bearer ${response.data.token}`
        }
      })
      if (meResponse.data?.id) {
        localStorage.setItem('userId', String(meResponse.data.id))
      }
    } catch {
      localStorage.removeItem('userId')
    }

    ElMessage.success('注册成功，即将为您跳转...')
    window.dispatchEvent(new Event('auth-change'))

    setTimeout(() => {
      router.push('/')
    }, 1000)
  } catch (error: any) {
    if (error.response) {
      let msg = '注册失败，请稍后重试'
      if (typeof error.response.data === 'string') {
        msg = error.response.data
      } else if (error.response.data && error.response.data.message) {
        msg = error.response.data.message
      }
      ElMessage.error('注册失败：' + msg)
    } else if (error.request) {
      ElMessage.error('注册失败：无法连接到服务器，请检查网络连接')
    } else {
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
  background:
    radial-gradient(circle at 8% 2%, rgba(46, 204, 113, 0.15), transparent 30%),
    radial-gradient(circle at 90% 12%, rgba(23, 139, 84, 0.1), transparent 24%),
    var(--bg-page);
  padding: 12px;
}

.card {
  width: 420px;
  padding: 30px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

.title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 22px;
  font-weight: 700;
  color: #173e30;
}

.tab-container {
  display: flex;
  margin-bottom: 20px;
}

.tab {
  flex: 1;
  padding: 10px 12px;
  cursor: pointer;
  border: 1px solid #d8ece1;
  background: #f4faf7;
  color: #2f5546;
  font-size: 14px;
  border-radius: 10px;
  transition: all 0.25s ease;
}

.tab:hover:not(.active-tab) {
  background: #eafaf1;
  color: var(--brand);
}

.active-tab {
  flex: 1;
  padding: 10px 12px;
  cursor: pointer;
  border: 1px solid #1f9b60;
  background: linear-gradient(135deg, #2ecc71 0%, #25b868 100%);
  color: #fff;
  font-size: 14px;
  border-radius: 10px;
  box-shadow: 0 8px 16px rgba(37, 184, 104, 0.24);
  transition: all 0.25s ease;
}

.input {
  width: 100%;
  padding: 11px 12px;
  margin-bottom: 15px;
  border-radius: 10px;
  border: 1px solid #d2e9dd;
  font-size: 14px;
  background: #fcfffd;
}

.input:focus {
  outline: none;
  border-color: #2ecc71;
  box-shadow: 0 0 0 3px rgba(46, 204, 113, 0.14);
}

.button {
  width: 100%;
  padding: 11px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #2ecc71 0%, #23ad62 100%);
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 10px 20px rgba(37, 184, 104, 0.22);
  transition: all 0.25s ease;
}

.button:hover:not(:disabled) {
  background: linear-gradient(135deg, #23ad62 0%, #1e9955 100%);
  box-shadow: 0 12px 24px rgba(37, 184, 104, 0.35);
  transform: translateY(-1px);
}

.button:active:not(:disabled) {
  transform: translateY(1px);
}

.register-button {
  background: linear-gradient(135deg, #2ecc71 0%, #23ad62 100%);
}

.button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
