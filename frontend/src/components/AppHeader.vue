<template>
  <!-- 顶部黑色导航条 -->
  <div class="nutrishop-top-bar">
    <div class="container">
      <div class="top-bar-left">
        <router-link to="/" class="nav-link">
          <el-icon><HomeFilled /></el-icon> 首页
        </router-link>
        <router-link to="/" class="nav-link">
          <el-icon><Present /></el-icon> 今日特惠
        </router-link>
        <router-link to="/" class="nav-link">
          <el-icon><Service /></el-icon> 顾客服务
        </router-link>
        <router-link to="/profile-survey" class="nav-link">
          <el-icon><Edit /></el-icon> 定制推荐
        </router-link>
      </div>
      <div class="top-bar-right">
        <router-link to="/login" class="nav-link" v-if="!isLoggedIn">
          <el-icon><User /></el-icon> 登录/注册
        </router-link>
        <el-dropdown v-else>
          <span class="nav-link">
            <el-icon><User /></el-icon> 我的账户<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>我的订单</el-dropdown-item>
              <el-dropdown-item>我的账户</el-dropdown-item>
              <el-dropdown-item>我的收藏</el-dropdown-item>
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
  
  <!-- 搜索区域 - 只在首页显示 -->
  <div class="nutrishop-search-section" v-if="isHomePage">
    <div class="container">
      <div class="search-content">
        <router-link to="/" class="nutrishop-logo">
          <span class="logo-icon">🛍️</span>
          <span class="logo-text">NutriShop</span>
        </router-link>
        <div class="search-bar">
          <div class="search-input-wrapper">
            <select
              v-model="searchCategory"
              class="search-category"
            >
              <option value="all">全部分类</option>
              <option value="cardio">心血管健康</option>
              <option value="eye">视力护眼</option>
              <option value="protein">运动营养</option>
              <option value="vitamin">综合维矿</option>
              <option value="sleep">改善睡眠</option>
            </select>
            <input
              v-model="q"
              @keydown.enter="search"
              placeholder="搜索健康好物..."
              class="search-input"
            />
            <button @click="search" class="search-btn">
              搜索
            </button>
          </div>
          <div class="search-tags">
            <span class="tag-label">热门搜索：</span>
            <router-link to="/?q=鱼油" class="tag-link">鱼油</router-link>
            <router-link to="/?q=叶黄素" class="tag-link">叶黄素</router-link>
            <router-link to="/?q=蛋白粉" class="tag-link">蛋白粉</router-link>
            <router-link to="/?q=辅酶Q10" class="tag-link">辅酶Q10</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, ShoppingCart, Goods, ArrowDown, Grid, User, HomeFilled, Present, Service, Edit } from '@element-plus/icons-vue'
import axios from 'axios'

const q = ref('')
const active = ref('/')
const searchCategory = ref('all')
const router = useRouter()
const route = useRoute()
const cartCount = ref(0)
const isLoggedIn = ref(false)

// 判断是否是首页
const isHomePage = computed(() => {
  return route.path === '/'
})

function search() { 
  if(q.value) router.push({ path: '/', query: { q: q.value, category: searchCategory.value } }) 
}

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
}

onMounted(async () => {
  checkLoginStatus()
  window.addEventListener('auth-change', checkLoginStatus)
  
  // 简单获取购物车数量占位
  try {
    const { data } = await axios.get('/api/cart')
    if(data && data.items) {
      cartCount.value = data.items.reduce((acc: number, item: any) => acc + item.quantity, 0)
    }
  } catch (e) {
    // ignore login error
  }
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

/* 顶部黑色导航条 */
.nutrishop-top-bar {
  background-color: #131921;
  color: #fff;
  font-size: 16px;
  padding: 10px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav-link {
  color: #fff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  padding: 4px 0;
}

.nav-link i {
  font-size: 20px;
}

.top-bar-left .ep-dropdown {
  display: inline-flex;
  align-items: center;
}

.nav-link:hover {
  color: #f0f0f0;
  text-decoration: underline;
}

.cart-link {
  position: relative;
}

/* 搜索区域 */
.nutrishop-search-section {
  background: #f5f5f5;
  padding: 40px 0 10px 0;
}

.search-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 0 20px;
}

.nutrishop-logo {
  text-decoration: none;
  display: flex;
  align-items: center;
  margin-bottom: 25px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-left: 10px;
}

.search-bar {
  width: 100%;
  max-width: 720px;
}

.search-input-wrapper {
  display: flex;
  width: 100%;
  background: #ffffff;
  border-radius: 50px;
  box-shadow: 0 10px 30px rgba(17, 14, 14, 0.08);
  overflow: hidden;
}

.search-category {
  width: 160px;
  border: none;
  padding: 12px;
  outline: none;
  background: transparent;
}

.search-input {
  flex: 1;
  border: none;
  padding: 12px;
  outline: none;
  font-size: 16px;
}

.search-btn {
  background-color: #000;
  color: #fff;
  border: none;
  padding: 0 24px;
  cursor: pointer;
  font-size: 16px;
}

.search-tags {
  margin-top: 18px;
  font-size: 14px;
  color: #888;
  text-align: center;
}

.tag-label {
  margin-right: 10px;
}

.tag-link {
  color: #666;
  text-decoration: none;
  margin-right: 12px;
}

.tag-link:hover {
  color: #409eff;
  text-decoration: underline;
}

/* 清除浮动 */
.nutrishop-top-bar::after {
  content: "";
  display: table;
  clear: both;
}

/* 移除下拉菜单列表项的黑点 */
.ep-dropdown-menu {
  list-style-type: none;
}

.ep-dropdown-menu li {
  list-style: none;
}
</style>