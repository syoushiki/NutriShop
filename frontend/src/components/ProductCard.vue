<template>
  <article class="product-card" @click="$emit('view', item.id)">
    <div class="thumb-wrap">
      <img :src="item.imageUrl" :alt="item.name" class="thumb" />

      <div class="match-chip" v-if="item.relevanceScore !== undefined">
        <div class="chip-top">
          <span>匹配度</span>
          <strong>{{ scoreText }}</strong>
        </div>
        <div class="chip-bar">
          <i :style="{ width: `${scoreValue}%` }"></i>
        </div>
      </div>
    </div>

    <div class="body">
      <h3 class="title">{{ item.name }}</h3>
      <p class="desc">{{ item.description }}</p>

      <div class="tag-list" v-if="normalizedTags.length">
        <span v-for="t in normalizedTags" :key="t" class="tag">{{ t }}</span>
      </div>

      <div class="price-row">
        <div class="price">¥{{ Number(item.price || 0).toFixed(2) }}</div>
        <span class="ship">免费配送</span>
      </div>

      <div class="actions">
        <el-button type="primary" @click.stop="$emit('add', item.id)">加入购物车</el-button>
        <el-button @click.stop="$emit('view', item.id)">查看详情</el-button>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ item: any }>()
defineEmits(['view', 'add'])

const normalizedTags = computed(() => {
  if (!props.item?.tags) return []
  return String(props.item.tags).split(',').map((t) => t.trim()).filter(Boolean)
})

const scoreValue = computed(() => {
  const raw = Number(props.item?.relevanceScore || 0)
  return Math.max(0, Math.min(100, Math.round(raw * 100)))
})

const scoreText = computed(() => `${scoreValue.value}%`)
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid transparent;
  box-shadow: var(--shadow-soft);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: var(--shadow-hover);
}

.thumb-wrap {
  position: relative;
  background: radial-gradient(circle at 10% 20%, #f4fff8 0%, #eef7f2 60%, #ecf3f0 100%);
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.28s ease;
}

.product-card:hover .thumb {
  transform: scale(1.05);
}

.match-chip {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 138px;
  padding: 8px 10px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.chip-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #0f172a;
}

.chip-top strong {
  color: #16a34a;
}

.chip-bar {
  margin-top: 6px;
  height: 6px;
  border-radius: 999px;
  background: #eaf2ee;
  overflow: hidden;
}

.chip-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #2ecc71, #8ae8b8);
  transition: width 0.35s ease;
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
  color: #0f172a;
  font-size: 15px;
  line-height: 1.35;
}

.desc {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
  min-height: 36px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 11px;
  color: #0f766e;
  background: #eafaf1;
  border: 1px solid #bfeecf;
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
  color: #15803d;
  background: #ecfdf3;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
</style>
