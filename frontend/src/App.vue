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
}

.el-button--primary:hover {
  background-color: var(--brand-700) !important;
  border-color: var(--brand-700) !important;
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  border-radius: 12px !important;
}

.el-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}

.el-icon svg {
  width: 100%;
  height: 100%;
}
</style>
