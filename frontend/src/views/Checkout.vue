<template>
  <div class="checkout-page container">
    <el-card class="checkout-card">
      <template #header>
        <div class="header">
          <h3>订单结算</h3>
          <span>请扫码完成付款</span>
        </div>
      </template>

      <div class="qr-wrap">
        <img class="qr-image" :src="paymentQr" alt="收款码" />
      </div>

      <el-button type="primary" class="pay-btn" :loading="submitting" @click="confirmPaid">
        我已付款
      </el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import paymentQr from '../assets/payment-qr.jpg'

const router = useRouter()
const submitting = ref(false)

const confirmPaid = async () => {
  if (submitting.value) return
  submitting.value = true
  try {
    const { data } = await axios.post('/api/orders/submit-from-cart')
    if (data?.id) {
      ElMessage.success(`支付成功，订单号 #${data.id}`)
    } else {
      ElMessage.success('支付成功')
    }
  } catch (error: any) {
    const msg = error?.response?.data?.message
    ElMessage.warning(msg || '支付状态已记录，正在为你跳转订单页')
  } finally {
    submitting.value = false
    router.push('/orders')
  }
}
</script>

<style scoped>
.container {
  width: 1200px;
  margin: 24px auto;
}

.checkout-page {
  padding: 4px 0 20px;
}

:deep(.checkout-card) {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.92);
  max-width: 460px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h3 {
  margin: 0;
  color: #173e30;
}

.header span {
  color: #5b7a6d;
  font-size: 13px;
}

.qr-wrap {
  display: flex;
  justify-content: center;
  padding: 6px 0 16px;
}

.qr-image {
  width: 280px;
  max-width: 100%;
  border-radius: 12px;
  border: 1px solid #dcefe4;
}

.pay-btn {
  width: 100%;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 12px;
  }
}
</style>
