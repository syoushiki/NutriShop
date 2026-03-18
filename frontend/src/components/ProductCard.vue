<template>
  <div class="amazon-product-card" @click="$emit('view', item.id)">
    <div class="image-container">
      <img :src="item.imageUrl" :alt="item.name" class="product-image" />
    </div>
    <div class="product-info">
      <h3 class="product-title">{{ item.name }}</h3>
      <div class="product-rating" v-if="item.rating">
        <div class="stars">
          <el-rate v-model="item.rating" disabled :max="5" size="small" />
        </div>
        <span class="rating-count">({{ item.ratingCount || 0 }})</span>
      </div>
      <p class="product-description">{{ item.description }}</p>
      <div class="price-section">
        <div class="price">
          <span class="currency">¥</span>
          <span class="price-amount">{{ item.price }}</span>
        </div>
        <div class="delivery-info" v-if="item.delivery">
          <span class="delivery-text">免费配送</span>
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" size="small" plain @click.stop="$emit('add', item.id)" class="add-to-cart-btn">
          <el-icon><ShoppingCart /></el-icon> 加入购物车
        </el-button>
        <el-button type="default" size="small" plain class="buy-now-btn">
          立即购买
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ShoppingCart } from '@element-plus/icons-vue'
defineProps<{ item: any }>()
defineEmits(['view', 'add'])
</script>

<style scoped>
.amazon-product-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.amazon-product-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border-color: #ddd;
}

.image-container {
  width: 100%;
  height: 200px;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 10px;
}

.product-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.amazon-product-card:hover .product-image {
  transform: scale(1.05);
}

.product-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 14px;
  font-weight: 500;
  color: #111;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-rating {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.stars {
  margin-right: 5px;
}

.rating-count {
  font-size: 12px;
  color: #666;
}

.product-description {
  font-size: 12px;
  color: #666;
  margin: 0 0 12px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.price-section {
  margin-bottom: 12px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #B12704;
  margin-bottom: 4px;
}

.currency {
  font-size: 14px;
}

.price-amount {
  font-size: 18px;
}

.delivery-info {
  font-size: 12px;
  color: #007600;
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.add-to-cart-btn {
  flex: 1;
  border-color: #D9A404;
  color: #D9A404;
  background-color: #FFF2CC;
}

.add-to-cart-btn:hover {
  border-color: #C29203;
  color: #C29203;
  background-color: #FFE699;
}

.buy-now-btn {
  flex: 1;
  border-color: #B12704;
  color: #B12704;
  background-color: #FFE7CC;
}

.buy-now-btn:hover {
  border-color: #9F2303;
  color: #9F2303;
  background-color: #FFD699;
}
</style>
