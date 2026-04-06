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
            <el-button
              class="status-btn confirm-btn"
              :type="order.confirmed ? 'success' : 'primary'"
              :plain="order.confirmed"
              :loading="isUpdating(order.id, 'confirmed')"
              :disabled="order.confirmed"
              @click="confirmOrder(order)"
            >
              {{ order.confirmed ? '已确认' : '确认订单' }}
            </el-button>

            <el-button
              class="status-btn ship-btn"
              :type="order.shipped ? 'success' : 'warning'"
              :plain="order.shipped"
              :loading="isUpdating(order.id, 'shipped')"
              :disabled="order.shipped || !order.confirmed"
              @click="shipOrder(order)"
            >
              {{ order.shipped ? '已发货' : '立即发货' }}
            </el-button>
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
const statusLoading = ref<Record<string, boolean>>({})

const loadOrders = async () => {
  try {
    const { data } = await axios.get('/api/orders/admin')
    orders.value = data || []
  } catch {
    ElMessage.error('加载订单失败')
  }
}

const statusKey = (id: number, field: 'confirmed' | 'shipped') => `${id}-${field}`

const isUpdating = (id: number, field: 'confirmed' | 'shipped') => !!statusLoading.value[statusKey(id, field)]

const updateStatus = async (order: any, field: 'confirmed' | 'shipped', nextValue: boolean) => {
  const key = statusKey(order.id, field)
  statusLoading.value[key] = true
  try {
    const payload: any = {}
    payload[field] = nextValue
    await axios.put(`/api/orders/${order.id}/status`, payload)
    order[field] = nextValue
    ElMessage.success(field === 'confirmed' ? '订单已确认' : '订单已发货')
  } catch {
    ElMessage.error('更新失败')
    await loadOrders()
  } finally {
    statusLoading.value[key] = false
  }
}

const confirmOrder = async (order: any) => {
  await updateStatus(order, 'confirmed', true)
}

const shipOrder = async (order: any) => {
  await updateStatus(order, 'shipped', true)
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
  gap: 10px;
  align-items: center;
}

.status-btn {
  min-width: 98px;
  border-radius: 999px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.confirm-btn:not(.is-disabled):not(.is-plain) {
  background: linear-gradient(120deg, #28c76f, #1f9b60);
  border-color: #1f9b60;
  box-shadow: 0 8px 16px rgba(31, 155, 96, 0.22);
}

.confirm-btn:not(.is-disabled):not(.is-plain):hover {
  background: linear-gradient(120deg, #23b465, #1a8a53);
  border-color: #1a8a53;
}

.ship-btn:not(.is-disabled):not(.is-plain) {
  background: linear-gradient(120deg, #ffb347, #ff9800);
  border-color: #f39a1f;
  color: #ffffff;
  box-shadow: 0 8px 16px rgba(243, 154, 31, 0.24);
}

.ship-btn:not(.is-disabled):not(.is-plain):hover {
  background: linear-gradient(120deg, #f3a637, #ed8f00);
  border-color: #dd8300;
  color: #ffffff;
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
