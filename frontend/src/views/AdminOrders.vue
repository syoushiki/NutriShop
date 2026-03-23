<template>
  <div class="orders-page container">
    <el-card>
      <template #header>
        <div class="head">
          <h3>订单后台管理</h3>
          <span>{{ orders.length }} 笔</span>
        </div>
      </template>

      <el-empty v-if="orders.length === 0" description="暂无订单" />

      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-top">
          <div>
            <div class="order-no">订单号 #{{ order.id }} / 用户：{{ order.username }}</div>
            <div class="order-time">{{ formatTime(order.createdAt) }}</div>
          </div>
          <div class="status-group">
            <el-switch v-model="order.confirmed" active-text="已确认" inactive-text="未确认" @change="updateStatus(order, 'confirmed')" />
            <el-switch v-model="order.shipped" active-text="已发货" inactive-text="未发货" @change="updateStatus(order, 'shipped')" />
          </div>
        </div>

        <div class="addr">收货地址：{{ order.userAddress || '未填写地址' }}</div>

        <el-table :data="order.items" size="small" border>
          <el-table-column prop="productName" label="商品" min-width="220" />
          <el-table-column prop="unitPrice" label="单价" width="120">
            <template #default="{ row }">¥{{ Number(row.unitPrice).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column prop="subtotal" label="小计" width="130">
            <template #default="{ row }">¥{{ Number(row.subtotal).toFixed(2) }}</template>
          </el-table-column>
        </el-table>

        <div class="order-total">订单总额：<strong>¥{{ Number(order.totalAmount).toFixed(2) }}</strong></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const orders = ref<any[]>([])

const loadOrders = async () => {
  try {
    const { data } = await axios.get('/api/orders/admin')
    orders.value = data || []
  } catch {
    ElMessage.error('加载订单失败')
  }
}

const updateStatus = async (order: any, field: 'confirmed' | 'shipped') => {
  try {
    const payload: any = {}
    payload[field] = order[field]
    await axios.put(`/api/orders/${order.id}/status`, payload)
    ElMessage.success('状态已更新')
  } catch {
    ElMessage.error('更新失败')
    await loadOrders()
  }
}

const formatTime = (value: string) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(loadOrders)
</script>

<style scoped>
.container {
  width: 1200px;
  margin: 24px auto;
}

.orders-page {
  padding: 4px 0 20px;
}

:deep(.orders-page > .el-card) {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.9);
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.head h3 {
  margin: 0;
  color: #173e30;
}

.order-card {
  border: 1px solid #d8ece1;
  border-radius: var(--radius);
  padding: 12px;
  margin-bottom: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fcf9 100%);
  box-shadow: 0 8px 16px rgba(31, 79, 61, 0.08);
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-weight: 700;
}

.order-time,
.addr {
  color: var(--text-muted);
  font-size: 13px;
}

.addr {
  margin: 10px 0;
}

.status-group {
  display: flex;
  gap: 16px;
}

.order-total {
  text-align: right;
  margin-top: 10px;
  color: #1f4f3d;
}

:deep(.el-table__inner-wrapper)::before {
  display: none;
}

:deep(.el-table th.el-table__cell) {
  background: #f3faf6;
  color: #2f5546;
  font-weight: 700;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 12px;
  }
}
</style>
