import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'product',
        name: 'Product',
        redirect: '/product/list',
        meta: { title: '商品管理', icon: 'Goods' },
        children: [
          {
            path: 'list',
            name: 'ProductList',
            component: () => import('@/views/product/ProductList.vue'),
            meta: { title: '商品列表' }
          },
          {
            path: 'category',
            name: 'Category',
            component: () => import('@/views/product/Category.vue'),
            meta: { title: '分类管理' }
          }
        ]
      },
      {
        path: 'order',
        name: 'Order',
        redirect: '/order/list',
        meta: { title: '订单管理', icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'OrderList',
            component: () => import('@/views/order/OrderList.vue'),
            meta: { title: '订单列表' }
          }
        ]
      },
      {
        path: 'seckill',
        name: 'Seckill',
        redirect: '/seckill/activity',
        meta: { title: '秒杀管理', icon: 'Lightning' },
        children: [
          {
            path: 'activity',
            name: 'SeckillActivity',
            component: () => import('@/views/seckill/Activity.vue'),
            meta: { title: '秒杀活动' }
          }
        ]
      },
      {
        path: 'groupbuy',
        name: 'GroupBuy',
        redirect: '/groupbuy/activity',
        meta: { title: '团购管理', icon: 'Users' },
        children: [
          {
            path: 'activity',
            name: 'GroupBuyActivity',
            component: () => import('@/views/groupbuy/Activity.vue'),
            meta: { title: '团购活动' }
          }
        ]
      },
      {
        path: 'coupon',
        name: 'Coupon',
        redirect: '/coupon/list',
        meta: { title: '优惠券管理', icon: 'Ticket' },
        children: [
          {
            path: 'list',
            name: 'CouponList',
            component: () => import('@/views/marketing/CouponList.vue'),
            meta: { title: '优惠券列表' }
          }
        ]
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'user',
        name: 'User',
        redirect: '/user/list',
        meta: { title: '用户管理', icon: 'User' },
        children: [
          {
            path: 'list',
            name: 'UserList',
            component: () => import('@/views/user/UserList.vue'),
            meta: { title: '用户列表' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router