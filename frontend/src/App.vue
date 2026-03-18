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
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, Helvetica, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  background-color: #f5f5f5;
}

a {
  text-decoration: none;
  color: #0066c0;
}

a:hover {
  color: #c45500;
  text-decoration: underline;
}

/* 覆盖Element Plus默认样式 */
.el-button--primary {
  background-color: #ff9900 !important;
  border-color: #ff9900 !important;
}

.el-button--primary:hover {
  background-color: #f08800 !important;
  border-color: #f08800 !important;
}

.el-input__wrapper {
  border-radius: 4px !important;
}

.el-menu {
  border-bottom: none !important;
}

.el-menu-item {
  font-size: 14px !important;
}

.el-menu-item.is-active {
  color: #ff9900 !important;
  font-weight: 500 !important;
}

.el-rate__icon {
  color: #ff9900 !important;
}
</style>
