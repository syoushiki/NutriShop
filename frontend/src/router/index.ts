import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Login from '../views/Login.vue'
import Admin from '../views/Admin.vue'
import ProfileSurvey from '../views/ProfileSurvey.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/product/:id', component: ProductDetail },
  { path: '/cart', component: Cart },
  { path: '/login', component: Login },
  { path: '/admin', component: Admin },
  { path: '/profile-survey', component: ProfileSurvey }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
