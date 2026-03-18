<template>
  <el-row :gutter="16">
    <el-col :span="16">
      <el-card>
        <el-table :data="items">
          <el-table-column prop="name" label="商品" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="price" label="单价" />
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card>
        <div style="font-weight:600;margin-bottom:8px">订单汇总</div>
        <el-form>
          <el-form-item label="优惠券">
            <el-input v-model="coupon" placeholder="输入优惠码" />
          </el-form-item>
        </el-form>
        <div style="display:flex;justify-content:space-between;margin:8px 0">
          <span>商品总计</span><span>¥{{ total }}</span>
        </div>
        <el-alert v-if="risk" :title="risk.message" type="warning" show-icon />
        <el-button type="primary" style="width:100%;margin-top:8px" @click="checkout">去结算</el-button>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref, computed } from 'vue'
const items = ref<any[]>([])
const risk = ref<any>(null)
const coupon = ref('')
onMounted(async () => {
  const { data } = await axios.get('/api/cart')
  items.value = data.items || []
  const r = await axios.post('/api/cart/evaluate', { items: items.value })
  risk.value = r.data
})
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))
async function checkout() {
  const { data } = await axios.post('/api/pay/create', { amount: total.value })
  window.location.href = data.payUrl
}
</script>
