<template>
  <el-config-provider namespace="ep">
    <el-container>
      <app-header />
      <el-main>
        <router-view />
      </el-main>
      <app-footer />
    </el-container>
  </el-config-provider>
</template>

<script setup lang="ts">
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
</script>

<style>
:root {
  --brand: #2ecc71;
  --brand-700: #27b864;
  --brand-100: #eafaf1;
  --brand-light: #bdebd0;
  --bg-page: #f6f8f7;
  --bg-card: #ffffff;
  --text-main: #0f172a;
  --text-sub: #64748b;
  --line-soft: #e8edf1;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --shadow-soft: 0 8px 30px rgba(15, 23, 42, 0.06);
  --shadow-hover: 0 14px 36px rgba(15, 23, 42, 0.12);

  --el-color-primary: #2ecc71;
  --el-color-primary-light-3: #59d98b;
  --el-color-primary-light-7: #b9efcf;
  --el-color-primary-dark-2: #27b864;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
}

body {
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 14px;
  line-height: 1.55;
  color: var(--text-main);
  background: var(--bg-page);
}

a {
  text-decoration: none;
  color: inherit;
}

.el-main {
  --el-main-padding: 0;
}

.el-card {
  border-radius: var(--radius-xl);
  border-color: var(--line-soft);
}

.el-button--primary {
  background-color: var(--brand) !important;
  border-color: var(--brand) !important;
  color: #ffffff !important;
}

.el-button--primary:hover {
  background-color: var(--brand-700) !important;
  border-color: var(--brand-700) !important;
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3) !important;
  transform: translateY(-1px);
}

.el-button {
  border-radius: var(--radius-lg) !important;
  font-weight: 500 !important;
  transition: all 0.25s ease, transform 0.2s ease, box-shadow 0.2s ease !important;
}

.el-button:not(.is-disabled):active {
  transform: translateY(1px) !important;
}

.el-button:not(.el-button--primary):not(.el-button--success):not(.el-button--warning):not(.el-button--danger):not(.el-button--info):not(.el-button--text):not(.is-text):not(.is-link):not(.is-disabled):hover {
  color: var(--brand) !important;
  border-color: var(--brand-light) !important;
  background-color: var(--brand-100) !important;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05) !important;
  transform: translateY(-1px);
}

.el-button--danger {
  transition: all 0.25s ease !important;
}

.el-button--danger:not(.is-disabled):hover {
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3) !important;
  transform: translateY(-1px);
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  border-radius: 12px !important;
}

.el-icon,
.ep-icon {
  width: 16px !important;
  height: 16px !important;
  line-height: 16px !important;
  font-size: 16px !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  vertical-align: middle;
  fill: currentColor;
  overflow: hidden;
}

.el-icon svg,
.ep-icon svg {
  width: 1em !important;
  height: 1em !important;
}

.el-empty,
.ep-empty {
  padding: 20px 0 !important;
}

.el-empty__image,
.ep-empty__image {
  display: none !important;
}

.el-message__icon,
.ep-message__icon {
  width: 16px !important;
  height: 16px !important;
  font-size: 16px !important;
  line-height: 16px !important;
}
</style>
