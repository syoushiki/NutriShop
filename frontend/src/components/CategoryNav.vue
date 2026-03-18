<template>
  <el-card>
    <div style="font-weight:600;margin-bottom:8px">分类</div>
    <el-menu :default-active="'hot'">
      <el-menu-item index="hot">热门推荐</el-menu-item>
      <el-menu-item index="cardio">心血管</el-menu-item>
      <el-menu-item index="eye">护眼</el-menu-item>
      <el-menu-item index="bone">骨骼健康</el-menu-item>
      <el-menu-item index="immune">免疫提升</el-menu-item>
      <el-menu-item index="protein">蛋白质</el-menu-item>
      <el-menu-item index="vitamin">维生素</el-menu-item>
    </el-menu>
  </el-card>
</template>
<script setup lang="ts">
</script>
