<template>
  <div class="product-detail-container" v-if="product">
    <el-card class="detail-card">
      <el-row :gutter="40">
        <!-- 左侧图片区 -->
        <el-col :span="10">
          <div class="main-image-box">
            <img :src="currentImage || product.imageUrl" class="main-image" />
          </div>
          <el-row :gutter="10" class="thumbnail-list">
            <el-col :span="5" v-for="(img, index) in thumbnails" :key="index">
              <div 
                class="thumbnail-item" 
                :class="{ active: currentImage === img }"
                @mouseenter="currentImage = img"
              >
                <img :src="img" />
              </div>
            </el-col>
          </el-row>
        </el-col>

        <!-- 右侧信息区 -->
        <el-col :span="14">
          <h1 class="product-title">{{ product.name }}</h1>
          <p class="product-subtitle">{{ product.description }}</p>
          
          <div class="price-panel">
            <div class="price-label">价格</div>
            <div class="price-value">
              <span class="currency">¥</span>
              <span class="amount">{{ product.price }}</span>
            </div>
            <div class="promo-tag">新品上市</div>
          </div>

          <div class="sku-panel">
            <div class="sku-row">
              <span class="sku-label">规格</span>
              <el-radio-group v-model="selectedSku" size="large">
                <el-radio-button label="默认规格" />
                <el-radio-button label="礼盒装" disabled />
              </el-radio-group>
            </div>
            
            <div class="sku-row">
              <span class="sku-label">数量</span>
              <el-input-number v-model="qty" :min="1" :max="99" />
              <span class="stock-info">库存充足</span>
            </div>
          </div>

          <div class="action-panel">
            <el-button type="warning" size="large" plain @click="addToCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
          </div>
          
          <div class="service-guarantee">
            <span><el-icon><CircleCheck /></el-icon> 正品保障</span>
            <span><el-icon><Van /></el-icon> 极速发货</span>
            <span><el-icon><Service /></el-icon> 售后无忧</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 下方详情Tabs -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="5">
        <el-card class="recommend-sidebar">
          <template #header>
            <div class="sidebar-header">看了又看</div>
          </template>
          <!-- 侧边栏推荐占位 -->
           <div v-for="i in 3" :key="i" class="sidebar-item">
             <div class="sidebar-img-box">
               <img :src="`https://placehold.co/150x150/f5f5f5/ccc?text=Item+${i}`" />
             </div>
             <div class="sidebar-name">推荐商品 {{i}}</div>
             <div class="sidebar-price">¥{{ 99 * i }}</div>
           </div>
        </el-card>
      </el-col>
      <el-col :span="19">
        <el-card class="detail-tabs-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="商品详情" name="detail">
              <div class="product-intro-images">
                 <el-alert title="温馨提示：本品不能代替药物" type="warning" show-icon style="margin-bottom:20px" />
                 <img src="https://placehold.co/800x400/eef4ff/333?text=Product+Feature+1" style="width:100%;margin-bottom:10px" />
                 <img src="https://placehold.co/800x400/fff3e8/333?text=Product+Feature+2" style="width:100%;margin-bottom:10px" />
                 <img src="https://placehold.co/800x600/f5fff0/333?text=Usage+Instruction" style="width:100%" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="营养成分" name="nutrition">
              <el-table :data="product.nutrition || []" border style="width: 100%">
                <el-table-column prop="name" label="营养成分" width="180" />
                <el-table-column prop="amount" label="每份含量" />
                <el-table-column prop="nrv" label="NRV%" width="120">
                  <template #default>--</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="用户评价 (99+)" name="comments">
              <div class="comment-item" v-for="i in 3" :key="i">
                <div class="comment-user">
                  <el-avatar :size="32" icon="UserFilled" />
                  <span class="username">用户***{{i}}</span>
                  <el-rate v-model="rateValue" disabled text-color="#ff9900" />
                </div>
                <div class="comment-content">效果不错，发货很快！</div>
                <div class="comment-date">2023-10-{{10+i}}</div>
                <el-divider />
              </div>
            </el-tab-pane>
            <el-tab-pane label="问大家" name="qa">
              <el-empty description="暂无提问" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, Van, Service, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const product = ref<any>(null)
const qty = ref(1)
const activeTab = ref('detail')
const selectedSku = ref('默认规格')
const currentImage = ref('')
const rateValue = ref(5)

// 模拟缩略图
const thumbnails = computed(() => {
  if (!product.value) return []
  return [
    product.value.imageUrl,
    'https://placehold.co/400x400/eef/333?text=Side+View',
    'https://placehold.co/400x400/ffe/333?text=Detail',
    'https://placehold.co/400x400/efe/333?text=Package'
  ]
})

onMounted(async () => {
  try {
    const { data } = await axios.get(`/api/products/${route.params.id}`)
    product.value = data
    currentImage.value = data.imageUrl
  } catch (e) {
    ElMessage.error('加载商品失败')
  }
})

async function addToCart() {
  try {
    await axios.post('/api/cart/add', { productId: product.value.id, quantity: qty.value })
    ElMessage.success('成功加入购物车')
  } catch(e) {
    ElMessage.error('请先登录')
  }
}

async function buyNow() {
  await addToCart()
  const amount = Number(product.value.price) * qty.value
  const { data } = await axios.post('/api/pay/create', { amount })
  window.location.href = data.payUrl
}
</script>

<style scoped>
.product-detail-container {
  padding-top: 20px;
}
.detail-card {
  border: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}
.main-image-box {
  width: 100%;
  height: 400px;
  border: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  overflow: hidden;
}
.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumbnail-item {
  width: 100%;
  height: 64px;
  border: 2px solid transparent;
  cursor: pointer;
}
.thumbnail-item.active {
  border-color: #409EFF;
}
.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.product-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}
.product-subtitle {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}
.price-panel {
  background: #fff5f5;
  padding: 15px;
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
}
.price-label {
  font-size: 14px;
  color: #999;
  margin-right: 15px;
}
.price-value {
  color: #e4393c;
  font-weight: bold;
  margin-right: 10px;
}
.currency {
  font-size: 18px;
}
.amount {
  font-size: 28px;
}
.promo-tag {
  background: #e4393c;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
}
.sku-panel {
  border-top: 1px dotted #ddd;
  border-bottom: 1px dotted #ddd;
  padding: 20px 0;
  margin-bottom: 30px;
}
.sku-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.sku-row:last-child {
  margin-bottom: 0;
}
.sku-label {
  width: 60px;
  color: #666;
}
.stock-info {
  margin-left: 15px;
  color: #999;
  font-size: 12px;
}
.action-panel {
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
}
.service-guarantee {
  color: #999;
  font-size: 12px;
  display: flex;
  gap: 20px;
}
.service-guarantee span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.recommend-sidebar {
  border: none;
}
.sidebar-header {
  font-weight: bold;
}
.sidebar-item {
  margin-bottom: 20px;
  text-align: center;
}
.sidebar-img-box {
  width: 100%;
  height: 150px;
  margin-bottom: 8px;
}
.sidebar-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.sidebar-name {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sidebar-price {
  color: #e4393c;
  font-weight: bold;
}
.comment-item {
  padding: 15px 0;
}
.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.username {
  font-size: 12px;
  color: #666;
}
.comment-date {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
