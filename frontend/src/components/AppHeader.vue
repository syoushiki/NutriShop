<template>
  <div class="nutrishop-top-bar">
    <div class="container">
      <div class="top-bar-left">
        <router-link to="/" class="nav-link"><el-icon><HomeFilled /></el-icon> 首页</router-link>
        <router-link to="/profile-survey" class="nav-link"><el-icon><Edit /></el-icon> 定制推荐</router-link>
      </div>

      <div class="top-bar-right">
        <router-link to="/login" class="nav-link" v-if="!isLoggedIn">
          <el-icon><User /></el-icon> 登录/注册
        </router-link>

        <el-dropdown v-else>
          <span class="nav-link">
            <el-icon><User /></el-icon> 欢迎, {{ username }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人主页</el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" @click="router.push('/admin')">后台管理</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <router-link to="/cart" class="nav-link cart-link">
          <el-icon><ShoppingCart /></el-icon> 购物车
          <el-badge :value="cartCount" class="item" type="danger" v-if="cartCount > 0" />
        </router-link>
      </div>
    </div>
  </div>

  <div class="nutrishop-search-section" v-if="isHomePage">
    <div class="container search-content">
      <router-link to="/" class="nutrishop-logo">
        <span class="logo-icon">🥦</span>
        <span class="logo-text">NutriShop</span>
      </router-link>

      <div class="search-bar">
        <div class="search-input-wrapper">
          <input v-model="q" @keydown.enter="search" placeholder="搜索健康好物..." class="search-input" />
          <button @click="search" class="search-btn">搜索</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShoppingCart, ArrowDown, User, HomeFilled, Edit } from '@element-plus/icons-vue'
import axios from 'axios'

const q = ref('')
const router = useRouter()
const route = useRoute()
const cartCount = ref(0)
const isLoggedIn = ref(false)
const isAdmin = ref(false)
const username = ref('')

const isHomePage = computed(() => route.path === '/')

function search() {
  if (q.value.trim()) {
    router.push({ path: '/', query: { q: q.value.trim() } })
  }
}

const checkLoginStatus = async () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  username.value = localStorage.getItem('username') || '用户'
  isAdmin.value = localStorage.getItem('role') === 'ADMIN'

  if (!isLoggedIn.value) {
    cartCount.value = 0
    return
  }

  try {
    const { data } = await axios.get('/api/cart')
    if (data && data.items) {
      cartCount.value = data.items.reduce((acc: number, item: any) => acc + item.quantity, 0)
    }
  } catch {
    cartCount.value = 0
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  window.dispatchEvent(new Event('auth-change'))
  router.push('/login')
}

onMounted(() => {
  checkLoginStatus()
  window.addEventListener('auth-change', checkLoginStatus)
})
</script>

<style scoped>
.container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nutrishop-top-bar {
  background-color: #131921;
  color: #fff;
  font-size: 16px;
  padding: 10px 0;
}

.top-bar-left,
.top-bar-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #fff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.nav-link:hover {
  text-decoration: underline;
}

.nutrishop-search-section {
  background: #f5f5f5;
  padding: 24px 0 10px;
}

.search-content {
  flex-direction: column;
  gap: 16px;
}

.nutrishop-logo {
  text-decoration: none;
  display: flex;
  align-items: center;
  color: #222;
}

.logo-icon {
  font-size: 30px;
}

.logo-text {
  font-size: 28px;
  font-weight: 600;
  margin-left: 10px;
}

.search-bar {
  width: 100%;
  max-width: 700px;
}

.search-input-wrapper {
  display: flex;
  width: 100%;
  background: #fff;
  border-radius: 40px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
}

.search-input {
  flex: 1;
  border: none;
  padding: 12px 16px;
  outline: none;
}

.search-btn {
  border: none;
  background: #111;
  color: #fff;
  padding: 0 22px;
  cursor: pointer;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 12px;
  }
}
</style>
