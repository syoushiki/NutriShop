<template>
  <div class="cart-page container">
    <el-row :gutter="18">
      <el-col :xs="24" :md="16">
        <el-card class="cart-list-card">
          <template #header>
            <div class="card-header">
              <h3>购物车商品</h3>
              <span>共 {{ items.length }} 件</span>
            </div>
          </template>

          <el-empty v-if="items.length === 0" description="购物车还是空的，去首页挑选商品吧" />

          <el-table v-else :data="items" class="cart-table">
            <el-table-column prop="name" label="商品" min-width="220" />
            <el-table-column label="单价" width="120">
              <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="170">
              <template #default="{ row }">
                <div class="qty-box">
                  <el-button size="small" @click="decreaseQty(row)">-</el-button>
                  <span class="qty-text">{{ row.quantity }}</span>
                  <el-button size="small" @click="increaseQty(row)">+</el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="小计" width="130">
              <template #default="{ row }">¥{{ (Number(row.price) * Number(row.quantity)).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" plain size="small" @click="removeItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card class="summary-card">
          <div class="summary-title">订单汇总</div>
          <el-form>
            <el-form-item label="优惠码">
              <el-input v-model="coupon" placeholder="输入优惠码" />
            </el-form-item>
          </el-form>

          <div class="summary-row"><span>商品总计</span><span>¥{{ total.toFixed(2) }}</span></div>
          <div class="summary-row"><span>预计运费</span><span>¥{{ shipping.toFixed(2) }}</span></div>
          <div class="summary-row final"><span>应付金额</span><span>¥{{ payable.toFixed(2) }}</span></div>

          <el-alert v-if="risk?.message" :title="risk.message" type="warning" show-icon />
          <el-button type="primary" class="checkout-btn" @click="checkout" :disabled="items.length === 0">去结算</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const items = ref<any[]>([])
const risk = ref<any>(null)
const coupon = ref('')
const router = useRouter()

const loadCart = async () => {
  const { data } = await axios.get('/api/cart')
  items.value = data.items || []
  const r = await axios.post('/api/cart/evaluate', { items: items.value })
  risk.value = r.data
}

onMounted(loadCart)

const total = computed(() => items.value.reduce((s, i) => s + Number(i.price) * Number(i.quantity), 0))
const shipping = computed(() => (items.value.length === 0 ? 0 : (total.value >= 199 ? 0 : 12)))
const payable = computed(() => total.value + shipping.value)

const increaseQty = async (row: any) => {
  const next = Number(row.quantity) + 1
  await axios.put(`/api/cart/${row.id}/quantity`, { quantity: next })
  row.quantity = next
}

const decreaseQty = async (row: any) => {
  const next = Number(row.quantity) - 1
  if (next <= 0) {
    await removeItem(row)
    return
  }
  await axios.put(`/api/cart/${row.id}/quantity`, { quantity: next })
  row.quantity = next
}

const removeItem = async (row: any) => {
  await axios.delete(`/api/cart/${row.id}`)
  items.value = items.value.filter((it) => it.id !== row.id)
  ElMessage.success('已删除该商品')
}

async function checkout() {
  try {
    const { data } = await axios.post('/api/orders/submit-from-cart')
    if (data?.id) {
      ElMessage.success(`订单提交成功，订单号 #${data.id}`)
      items.value = []
      router.push('/orders')
      return
    }
    ElMessage.success('订单提交成功')
    router.push('/orders')
  } catch {
    ElMessage.error('结算失败，请稍后重试')
  }
}
</script>

<style scoped>
.container {
  width: 1200px;
  margin: 24px auto;
}

.cart-page {
  padding: 4px 0 20px;
}

:deep(.cart-list-card),
:deep(.summary-card) {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.9);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #173e30;
}

.qty-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-text {
  min-width: 22px;
  text-align: center;
}

.summary-title {
  font-weight: 700;
  margin-bottom: 12px;
  color: #1f4f3d;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  color: #4b6359;
}

.summary-row.final {
  font-size: 16px;
  font-weight: 700;
  color: #173e30;
  border-top: 1px solid #dcefe4;
  padding-top: 10px;
}

.checkout-btn {
  width: 100%;
  margin-top: 12px;
}

:deep(.cart-table .el-table__inner-wrapper)::before {
  display: none;
}

:deep(.cart-table th.el-table__cell) {
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
