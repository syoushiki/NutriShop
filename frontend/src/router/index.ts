import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Login from '../views/Login.vue'
import Admin from '../views/Admin.vue'
import ProfileSurvey from '../views/ProfileSurvey.vue'
import UserProfile from '../views/UserProfile.vue'
import ProductEditor from '../views/ProductEditor.vue'
import MyOrders from '../views/MyOrders.vue'
import AdminOrders from '../views/AdminOrders.vue'
import Checkout from '../views/Checkout.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/product/:id', component: ProductDetail },
  { path: '/cart', component: Cart },
  { path: '/login', component: Login },
  { path: '/admin', component: Admin },
  { path: '/admin/orders', component: AdminOrders },
  { path: '/admin/products/new', component: ProductEditor },
  { path: '/admin/products/:id/edit', component: ProductEditor },
  { path: '/profile-survey', component: ProfileSurvey },
  { path: '/profile', component: UserProfile },
  { path: '/checkout', component: Checkout },
  { path: '/orders', component: MyOrders }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path.startsWith('/admin')) {
    const role = localStorage.getItem('role')
    if (role === 'ADMIN') {
      next()
    } else {
      next('/')
    }
  } else if (to.path === '/orders' || to.path === '/profile' || to.path === '/profile-survey' || to.path === '/cart' || to.path === '/checkout') {
    const token = localStorage.getItem('token')
    if (token) next()
    else next('/login')
  } else {
    next()
  }
})

export default router
