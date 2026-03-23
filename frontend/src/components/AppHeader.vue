<template>
  <header class="header-shell">
    <div class="top-nav container glass-card">
      <div class="left-links">
        <router-link to="/" class="nav-link"><el-icon><HomeFilled /></el-icon>首页</router-link>
        <router-link to="/profile-survey" class="nav-link"><el-icon><Edit /></el-icon>定制推荐</router-link>
      </div>

      <div class="right-links">
        <router-link to="/login" class="nav-link" v-if="!isLoggedIn">
          <el-icon><User /></el-icon>登录/注册
        </router-link>

        <el-dropdown v-else>
          <span class="nav-link user-link">
            <el-icon><User /></el-icon>{{ username }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人主页</el-dropdown-item>
              <el-dropdown-item @click="router.push('/orders')">我的订单</el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" @click="router.push('/admin')">后台管理</el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" @click="router.push('/admin/orders')">订单管理</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <router-link to="/cart" class="nav-link cart-link">
          <el-icon><ShoppingCart /></el-icon>购物车
          <el-badge :value="cartCount" class="badge" type="danger" v-if="cartCount > 0" />
        </router-link>
      </div>
    </div>

    <div class="search-wrap" v-if="isHomePage">
      <div class="container search-shell glass-card">
        <router-link to="/" class="logo-block">
          <span class="logo-icon">🌿</span>
          <div>
            <div class="logo-text">NutriShop</div>
            <div class="logo-sub">Health • Trust • Science</div>
          </div>
        </router-link>

        <div class="search-input-wrap">
          <input v-model="q" @keydown.enter="search" placeholder="搜索你关心的健康产品..." class="search-input" />
          <button @click="search" class="search-btn">搜索</button>
        </div>
      </div>
    </div>
  </header>
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
    if (data?.items) {
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
}

.header-shell {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(4px);
  background: linear-gradient(180deg, rgba(246, 248, 247, 0.95) 0%, rgba(246, 248, 247, 0.7) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.glass-card {
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.62);
  box-shadow: 0 10px 30px rgba(2, 32, 20, 0.08);
}

.top-nav {
  margin-top: 10px;
  border-radius: 18px;
  padding: 10px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-links,
.right-links {
  display: flex;
  align-items: center;
  gap: 18px;
}

.nav-link {
  color: #0f172a;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.nav-link:hover {
  color: var(--brand-700);
}

.cart-link {
  position: relative;
}

.badge {
  margin-left: 2px;
}

.search-wrap {
  padding: 12px 0 14px;
}

.search-shell {
  border-radius: 22px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.logo-block {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 30px;
}

.logo-text {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.4px;
  color: #0b3d2b;
}

.logo-sub {
  font-size: 12px;
  color: #64817a;
}

.search-input-wrap {
  width: min(760px, 100%);
  display: flex;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px 16px;
  background: transparent;
}

.search-btn {
  border: none;
  background: linear-gradient(120deg, var(--brand), var(--brand-700));
  color: #fff;
  padding: 0 24px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
}

.search-btn:hover {
  background: linear-gradient(120deg, var(--brand-700), #219d55);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

.search-btn:active {
  transform: translateY(1px);
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 12px;
  }
}

@media (max-width: 900px) {
  .search-shell {
    flex-direction: column;
    align-items: stretch;
  }

  .logo-text {
    font-size: 24px;
  }
}
</style>
