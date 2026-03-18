<template>
  <div class="nutrishop-home-container">
    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <!-- 左侧分类导航 -->
          <div class="left-sidebar">
            <div class="sidebar-section">
              <h3 class="sidebar-title">商品分类</h3>
              <ul class="category-list">
                <li><router-link to="/category/cardio">心血管健康</router-link></li>
                <li><router-link to="/category/eye">视力护眼</router-link></li>
                <li><router-link to="/category/protein">运动营养</router-link></li>
                <li><router-link to="/category/vitamin">综合维矿</router-link></li>
                <li><router-link to="/category/sleep">改善睡眠</router-link></li>
              </ul>
            </div>
            <div class="sidebar-section">
              <h3 class="sidebar-title">价格区间</h3>
              <div class="price-range">
                <el-form :model="priceForm" :rules="priceRules" ref="priceFormRef">
                  <el-form-item label="最低价格" class="price-filter-item" prop="prodBegin">
                    <el-input
                      v-model="priceForm.prodBegin"
                      class="custom-price-input"
                      placeholder="最低"
                    />
                  </el-form-item>
                  <el-form-item label="最高价格" class="price-filter-item" prop="prodEnd">
                    <el-input
                      v-model="priceForm.prodEnd"
                      class="custom-price-input"
                      placeholder="最高"
                    />
                  </el-form-item>
                  <el-button type="primary" size="small" @click="applyPriceFilter" class="apply-btn">应用</el-button>
                </el-form>
              </div>
            </div>
          </div>
          
          <!-- 右侧商品区域 -->
          <div class="right-content">
            <!-- 顶部横幅 -->
            <div class="top-banner">
              <img src="https://placehold.co/800x200/131921/ffffff?text=NutriShop" style="width:100%;height:100%;object-fit:cover;border-radius:4px;" />
            </div>
            
            <!-- 排序和筛选 -->
            <div class="sort-filter">
              <div class="sort-options">
                <span>排序方式：</span>
                <div class="sort-select-wrapper">
                  <select v-model="sortBy" class="sort-select">
                    <option value="default">综合排序</option>
                    <option value="price_asc">价格从低到高</option>
                    <option value="price_desc">价格从高到低</option>
                    <option value="sales">销量优先</option>
                  </select>
                </div>
              </div>
              <div class="filter-options">
                <el-checkbox-group v-model="brandFilters">
                  <el-checkbox label="品牌A">品牌A</el-checkbox>
                  <el-checkbox label="品牌B">品牌B</el-checkbox>
                  <el-checkbox label="品牌C">品牌C</el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
            
            <!-- 商品网格 -->
            <div class="products-grid">
              <div class="product-item" v-for="p in products" :key="p.id">
                <product-card :item="p" @view="go" @add="add" />
              </div>
            </div>
            
            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                layout="prev, pager, next"
                :total="100"
                :page-size="12"
                :current-page="currentPage"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/ProductCard.vue'

const products = ref<any[]>([])
const router = useRouter()

const sortBy = ref('default')
const brandFilters = ref<string[]>([])
const currentPage = ref(1)

// 价格区间相关
const priceForm = ref({
  prodBegin: '0',
  prodEnd: '1000'
})
const priceFormRef = ref<any>(null)

// 价格验证函数
const isComplexMoney = (value: string): boolean => {
  const reg = /(^0$)|(^[1-9]\d{0,6}$)|(^[1-9]\d{0,6}\.\d{1,2}$)/
  return reg.test(value)
}

// 价格验证规则
const priceRules = {
  prodBegin: [{
    validator: (rule: any, value: string, callback: any) => {
      let prodEndNum = priceForm.value.prodEnd
      if (!isComplexMoney(value) || !isComplexMoney(prodEndNum)) {
        callback(new Error('请输入正确格式的金额'))
      } else if (Number(value) >= Number(prodEndNum)) {
        callback(new Error(`最大值输入值必须大于${value}`))
      } else if (value.length > 7 || prodEndNum.length > 7) {
        callback(new Error('输入值不能超过6位'))
      } else if ((value + '').indexOf('.') == -1 && value.length > 6) {
        callback(new Error('输入最小值不能超过6位'))
      } else if ((prodEndNum + '').indexOf('.') == -1 && prodEndNum.length > 6) {
        callback(new Error('输入最大值不能超过6位'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }],
  prodEnd: [{
    validator: (rule: any, value: string, callback: any) => {
      let prodBeginNum = priceForm.value.prodBegin
      if (!isComplexMoney(value) || !isComplexMoney(prodBeginNum)) {
        callback(new Error('请输入正确格式的金额'))
      } else if (Number(prodBeginNum) >= Number(value)) {
        callback(new Error(`最大值输入值必须大于${prodBeginNum}`))
      } else if (value.length > 7 || prodBeginNum.length > 7) {
        callback(new Error('输入值不能超过6位'))
      } else if ((value + '').indexOf('.') == -1 && value.length > 6) {
        callback(new Error('输入最大值不能超过6位'))
      } else if ((prodBeginNum + '').indexOf('.') == -1 && prodBeginNum.length > 6) {
        callback(new Error('输入最小值不能超过6位'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }]
}

// 应用价格过滤
function applyPriceFilter() {
  priceFormRef.value.validate((valid: boolean) => {
    if (valid) {
      // 这里可以添加价格过滤逻辑
      ElMessage.success('价格过滤已应用')
    } else {
      ElMessage.error('请输入正确的价格区间')
    }
  })
}

function go(id: number) { router.push(`/product/${id}`) }

async function add(id: number) {
  try {
    await axios.post('/api/cart/add', { productId: id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch(e) {
    ElMessage.error('加入购物车失败，请先登录')
  }
}

function handleCurrentChange(page: number) {
  currentPage.value = page
  // 这里可以添加分页加载逻辑
}

onMounted(async () => {
  const { data } = await axios.get('/api/products')
  products.value = data
})
</script>

<style scoped>
.nutrishop-home-container {
  background-color: #f5f5f5;
  min-height: 800px;
}

.main-content {
  padding: 20px 0;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.content-wrapper {
  display: flex;
  gap: 20px;
}

/* 左侧边栏 */
.left-sidebar {
  width: 200px;
  background-color: #fff;
  border-radius: 4px;
  padding: 15px;
}

.sidebar-section {
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #333;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 8px;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  margin-bottom: 8px;
}

.category-list li a {
  color: #333;
  text-decoration: none;
  font-size: 14px;
  display: block;
  padding: 5px 0;
}

.category-list li a:hover {
  color: #f56c6c;
  text-decoration: underline;
}

/* 价格区间容器 */
.price-range {
  padding: 5px 0;
}

/* 调整 Form Item 标签和内容的布局 */
.price-filter-item {
  margin-bottom: 10px !important;
}

.price-filter-item :deep(.el-form-item__label) {
  width: 60px;
  font-size: 14px;
}

.price-filter-item :deep(.el-form-item__content) {
  margin-left: 65px !important;
}

/* 输入框样式 */
.custom-price-input {
  width: 100%;
}

:deep(.custom-price-input .el-input__wrapper) {
  border-radius: 8px !important;
  height: 32px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.custom-price-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #f56c6c inset !important;
}

.apply-btn {
  border-radius: 6px;
  height: 32px;
  font-weight: bold;
  width: 100%;
  margin-top: 8px;
}

/* 针对 Element UI (Vue2) 的写法 */
/* ::v-deep .custom-price-input .el-input__inner { border-radius: 8px; height: 36px; } */



/* 右侧内容 */
.right-content {
  flex: 1;
}

.top-banner {
  margin-bottom: 20px;
  border-radius: 4px;
  overflow: hidden;
}

/* 排序和筛选 */
.sort-filter {
  background-color: #fff;
  padding: 12px 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.sort-options span {
  color: #606266;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.sort-select-wrapper {
  position: relative;
}

/* 优化选择框样式 */
:deep(.custom-select) {
  min-width: 120px;
}

:deep(.custom-select .ep-select__wrapper) {
  border-radius: 6px;
  height: 32px;
  display: flex;
  align-items: center;
}

/* 调整SVG图标大小和位置 */
:deep(.custom-select .ep-select__suffix) {
  display: flex;
  align-items: center;
}

:deep(.custom-select .ep-select__caret) {
  font-size: 14px;
  margin-left: 4px;
}

:deep(.custom-select .ep-select__caret svg) {
  width: 14px;
  height: 14px;
}

/* 隐藏输入框 */
:deep(.custom-select .ep-select__input) {
  display: none !important;
}

/* 确保选择框内容居中 */
:deep(.custom-select .ep-select__selection) {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 移除下拉列表项前的点 */
:deep(.custom-select .ep-select-dropdown__list) {
  list-style: none;
  padding: 0;
  margin: 0;
}

:deep(.custom-select .ep-select-dropdown__item) {
  list-style: none;
  padding: 8px 16px;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.custom-select .ep-select-dropdown__item:last-child) {
  border-bottom: none;
}

/* 确保下拉列表对齐 */
:deep(.custom-select .ep-select-dropdown) {
  margin-top: 4px !important;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 排序选择框样式 */
.sort-select {
  width: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 6px 12px;
  outline: none;
  background: white;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
  appearance: none;
  background-image: url('data:image/svg+xml;charset=utf-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 1024 1024%22%3E%3Cpath fill=%22%23909399%22 d=%22M831.872 340.864 512 652.672 192.128 340.864a30.59 30.59 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.59 30.59 0 0 0-42.752 0z%22/%3E%3C/svg%3E');
  background-repeat: no-repeat;
  background-position: right 8px center;
  background-size: 14px 14px;
  cursor: pointer;
}

.sort-select:hover {
  border-color: #c0c4cc;
}

.sort-select:focus {
  border-color: #f56c6c;
  box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.2);
}

.sort-select option {
  padding: 8px 12px;
  font-size: 14px;
  color: #606266;
}

/* 移除下拉列表的默认样式 */
.sort-select::-ms-expand {
  display: none;
}

.filter-options {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 优化复选框样式 */
:deep(.filter-options .ep-checkbox) {
  margin-right: 10px;
}

:deep(.filter-options .ep-checkbox__label) {
  font-size: 14px;
  color: #606266;
}

:deep(.filter-options .ep-checkbox__inner) {
  border-radius: 3px;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.product-item {
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.product-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

/* 分页 */
.pagination {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  display: flex;
  justify-content: center;
}
</style>
