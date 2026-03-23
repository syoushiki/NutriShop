<template>
  <div class="product-card" @click="$emit('view', item.id)">
    <div class="thumb-wrap">
      <img :src="item.imageUrl" :alt="item.name" class="thumb" />
      <span class="score-badge" v-if="item.relevanceScore !== undefined">匹配度 {{ Math.round(item.relevanceScore * 100) }}%</span>
    </div>

    <div class="body">
      <h3 class="title">{{ item.name }}</h3>
      <p class="desc">{{ item.description }}</p>

      <div class="tag-list" v-if="item.tags">
        <span v-for="t in normalizedTags" :key="t" class="tag">{{ t }}</span>
      </div>

      <div class="price-row">
        <div class="price">¥{{ Number(item.price || 0).toFixed(2) }}</div>
        <span class="ship">包邮</span>
      </div>

      <div class="actions">
        <el-button type="primary" @click.stop="$emit('add', item.id)">加入购物车</el-button>
        <el-button @click.stop="$emit('view', item.id)">查看详情</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ item: any }>()
defineEmits(['view', 'add'])

const normalizedTags = computed(() => {
  if (!props.item?.tags) return []
  return String(props.item.tags)
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
})
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e8edf3;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 30px rgba(12, 38, 66, 0.12);
}

.thumb-wrap {
  position: relative;
  background: linear-gradient(160deg, #f8fbff 0%, #eef7ff 100%);
  height: 210px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.score-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #0f766e;
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
}

.body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.title {
  margin: 0;
  font-size: 15px;
  line-height: 1.35;
  color: #0f172a;
}

.desc {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
  min-height: 34px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 11px;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  padding: 2px 8px;
  border-radius: 999px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price {
  color: #b42318;
  font-size: 22px;
  font-weight: 700;
}

.ship {
  color: #166534;
  font-size: 12px;
  background: #ecfdf3;
  padding: 2px 8px;
  border-radius: 999px;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
</style>
