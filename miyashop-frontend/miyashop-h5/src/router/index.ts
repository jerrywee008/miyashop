import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Index.vue'),
    meta: { title: '首页', keepAlive: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/Detail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/cart/Index.vue'),
    meta: { title: '购物车' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/order/Checkout.vue'),
    meta: { title: '结算' }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/List.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/Detail.vue'),
    meta: { title: '订单详情' }
  },
  {
    path: '/seckill',
    name: 'Seckill',
    component: () => import('@/views/seckill/Index.vue'),
    meta: { title: '秒杀专区' }
  },
  {
    path: '/groupbuy',
    name: 'GroupBuy',
    component: () => import('@/views/groupbuy/Index.vue'),
    meta: { title: '团购专区' }
  },
  {
    path: '/groupbuy/team/:id',
    name: 'GroupBuyTeam',
    component: () => import('@/views/groupbuy/Team.vue'),
    meta: { title: '团购详情' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/Index.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/user/address',
    name: 'Address',
    component: () => import('@/views/user/Address.vue'),
    meta: { title: '收货地址' }
  },
  {
    path: '/user/profile',
    name: 'ProfileEdit',
    component: () => import('@/views/user/ProfileEdit.vue'),
    meta: { title: '编辑资料' }
  },
  {
    path: '/user/coupons',
    name: 'Coupons',
    component: () => import('@/views/user/Coupons.vue'),
    meta: { title: '我的优惠券' }
  },
  {
    path: '/user/favorites',
    name: 'Favorites',
    component: () => import('@/views/user/Favorites.vue'),
    meta: { title: '我的收藏' }
  },
  {
    path: '/user/settings',
    name: 'Settings',
    component: () => import('@/views/user/Settings.vue'),
    meta: { title: '设置' }
  },
  {
    path: '/user/balance',
    name: 'Balance',
    component: () => import('@/views/user/Balance.vue'),
    meta: { title: '我的余额' }
  },
  {
    path: '/user/history',
    name: 'History',
    component: () => import('@/views/user/History.vue'),
    meta: { title: '浏览记录' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router