<template>
  <div class="editor-page">
    <div class="container">
      <el-page-header @back="goBack" :content="isEdit ? '编辑商品' : '新增商品'" />

      <el-card class="editor-card" shadow="never">
        <div class="hero">
          <h2>{{ isEdit ? '编辑商品信息' : '创建新商品' }}</h2>
          <p>支持上传多图、配置推荐标签与适用人群，提升首页个性化推荐效果。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="editor-form">
          <el-row :gutter="18">
            <el-col :xs="24" :md="14">
              <el-form-item label="商品名称" prop="name">
                <el-input v-model="form.name" maxlength="80" show-word-limit placeholder="例如：鱼油进阶款" />
              </el-form-item>

              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="价格" prop="price">
                    <el-input-number v-model="form.price" :min="0" :step="1" :precision="2" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主图 URL" prop="imageUrl">
                    <el-input v-model="form.imageUrl" placeholder="可手动填链接，或使用下方上传" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="商品描述" prop="description">
                <el-input v-model="form.description" type="textarea" :rows="4" placeholder="简明描述商品卖点与适用场景" />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="10">
              <div class="upload-panel">
                <div class="panel-title">商品图片（支持多图）</div>
                <el-upload
                  multiple
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="onUploadChange"
                  accept="image/*"
                >
                  <el-button type="primary" plain>上传图片</el-button>
                </el-upload>
                <p class="panel-tip">上传后自动预览，可设为主图，也可删除。</p>

                <div class="gallery" v-if="galleryImages.length">
                  <div class="img-item" v-for="(img, index) in galleryImages" :key="index">
                    <img :src="img" />
                    <div class="img-actions">
                      <el-button size="small" text @click="setAsMain(img)">设为主图</el-button>
                      <el-button size="small" text type="danger" @click="removeImage(index)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-divider />

          <el-row :gutter="18">
            <el-col :xs="24" :md="12">
              <el-form-item label="标签 tags" prop="tags">
                <el-input v-model="form.tags" placeholder="例如：sleep,eyes,fitness" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="适用人群 targetAudience" prop="targetAudience">
                <el-input v-model="form.targetAudience" placeholder="例如：female,31-50 或 all,18-30" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submit">{{ isEdit ? '保存修改' : '创建商品' }}</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const isEdit = computed(() => route.path.includes('/edit'))
const productId = computed(() => Number(route.params.id))

const form = ref({
  name: '',
  description: '',
  imageUrl: '',
  price: 0,
  tags: '',
  targetAudience: ''
})

const galleryImages = ref<string[]>([])

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }]
}

const parseGallery = (raw?: string) => {
  if (!raw) return [] as string[]
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return parsed.map((v) => String(v)).filter(Boolean)
    }
  } catch {
    return raw.split(',').map((s) => s.trim()).filter(Boolean)
  }
  return []
}

const onUploadChange = (file: UploadFile) => {
  const raw = file.raw
  if (!raw) return
  const reader = new FileReader()
  reader.onload = () => {
    const url = String(reader.result || '')
    if (!url) return
    galleryImages.value.push(url)
    if (!form.value.imageUrl) {
      form.value.imageUrl = url
    }
  }
  reader.readAsDataURL(raw)
}

const setAsMain = (img: string) => {
  form.value.imageUrl = img
}

const removeImage = (index: number) => {
  const removed = galleryImages.value[index]
  galleryImages.value.splice(index, 1)
  if (form.value.imageUrl === removed) {
    form.value.imageUrl = galleryImages.value[0] || ''
  }
}

const toNullableText = (value?: string) => {
  if (!value) return null
  const trimmed = value.trim()
  return trimmed === '' ? null : trimmed
}

const loadProduct = async () => {
  if (!isEdit.value || !productId.value) return
  try {
    const { data } = await axios.get(`/api/products/${productId.value}`)
    form.value.name = data.name || ''
    form.value.description = data.description || ''
    form.value.imageUrl = data.imageUrl || ''
    form.value.price = Number(data.price || 0)
    form.value.tags = data.tags || ''
    form.value.targetAudience = data.targetAudience || ''
    galleryImages.value = parseGallery(data.imageGallery)
    if (form.value.imageUrl && !galleryImages.value.includes(form.value.imageUrl)) {
      galleryImages.value.unshift(form.value.imageUrl)
    }
  } catch {
    ElMessage.error('加载商品失败')
  }
}

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (!form.value.imageUrl && galleryImages.value.length > 0) {
      form.value.imageUrl = galleryImages.value[0]
    }

    const payload = {
      name: form.value.name.trim(),
      description: toNullableText(form.value.description),
      imageUrl: toNullableText(form.value.imageUrl),
      imageGallery: galleryImages.value.length ? JSON.stringify(galleryImages.value) : null,
      price: form.value.price,
      tags: toNullableText(form.value.tags),
      targetAudience: toNullableText(form.value.targetAudience)
    }

    if (isEdit.value && productId.value) {
      await axios.put(`/api/products/${productId.value}`, payload)
      ElMessage.success('商品已更新')
    } else {
      await axios.post('/api/products', payload)
      ElMessage.success('商品已创建')
    }
    router.push('/admin')
  } catch (error: any) {
    const msg = error.response?.data?.message || error.response?.data || error.message || '保存失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}

const goBack = () => router.push('/admin')

onMounted(loadProduct)
</script>

<style scoped>
.editor-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 2% 0%, rgba(46, 204, 113, 0.14), transparent 28%),
    radial-gradient(circle at 92% 8%, rgba(28, 176, 97, 0.1), transparent 24%),
    var(--bg-page);
  padding: 24px 0;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.editor-card {
  margin-top: 14px;
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
}

:deep(.editor-card .el-card__body) {
  padding: 22px;
}

.hero {
  margin-bottom: 14px;
}

.hero h2 {
  margin: 0;
  font-size: 24px;
  color: #173e30;
}

.hero p {
  margin: 6px 0 0;
  color: var(--text-muted);
}

.upload-panel {
  border: 1px dashed #c9e9d7;
  border-radius: var(--radius);
  padding: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fcf9 100%);
}

.panel-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #204939;
}

.panel-tip {
  color: var(--text-muted);
  font-size: 12px;
  margin: 8px 0 10px;
}

.gallery {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.img-item {
  border: 1px solid #dbeee3;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 8px 14px rgba(26, 86, 58, 0.08);
}

.img-item img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.img-actions {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-page-header__content) {
  color: #1f4f3d;
  font-weight: 700;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 12px;
  }
}
</style>
