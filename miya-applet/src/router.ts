/**
 * Capacitor 专用路由 — Hash History + 静态导入
 */
import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import HomePage from '@/views/home/Index.vue'
import LoginPage from '@/views/login/Index.vue'
import ProductDetail from '@/views/product/Detail.vue'
import CartPage from '@/views/cart/Index.vue'
import CheckoutPage from '@/views/order/Checkout.vue'
import OrderList from '@/views/order/List.vue'
import OrderDetail from '@/views/order/Detail.vue'
import SeckillPage from '@/views/seckill/Index.vue'
import GroupBuyPage from '@/views/groupbuy/Index.vue'
import GroupBuyTeam from '@/views/groupbuy/Team.vue'
import UserPage from '@/views/user/Index.vue'
import AddressPage from '@/views/user/Address.vue'
import ProfileEdit from '@/views/user/ProfileEdit.vue'
import CouponsPage from '@/views/user/Coupons.vue'
import FavoritesPage from '@/views/user/Favorites.vue'
import SettingsPage from '@/views/user/Settings.vue'
import BalancePage from '@/views/user/Balance.vue'
import HistoryPage from '@/views/user/History.vue'
import CategoryList from '@/views/category/List.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'Home', component: HomePage, meta: { title: '首页', keepAlive: true } },
  { path: '/category/:name', name: 'CategoryList', component: CategoryList, meta: { title: '分类商品' } },
  { path: '/login', name: 'Login', component: LoginPage, meta: { title: '登录' } },
  { path: '/product/:id', name: 'ProductDetail', component: ProductDetail, meta: { title: '商品详情' } },
  { path: '/cart', name: 'Cart', component: CartPage, meta: { title: '购物车' } },
  { path: '/checkout', name: 'Checkout', component: CheckoutPage, meta: { title: '结算' } },
  { path: '/order/list', name: 'OrderList', component: OrderList, meta: { title: '我的订单' } },
  { path: '/order/:id', name: 'OrderDetail', component: OrderDetail, meta: { title: '订单详情' } },
  { path: '/seckill', name: 'Seckill', component: SeckillPage, meta: { title: '秒杀专区' } },
  { path: '/groupbuy', name: 'GroupBuy', component: GroupBuyPage, meta: { title: '团购专区' } },
  { path: '/groupbuy/team/:id', name: 'GroupBuyTeam', component: GroupBuyTeam, meta: { title: '团购详情' } },
  { path: '/user', name: 'User', component: UserPage, meta: { title: '个人中心' } },
  { path: '/user/address', name: 'Address', component: AddressPage, meta: { title: '收货地址' } },
  { path: '/user/profile', name: 'ProfileEdit', component: ProfileEdit, meta: { title: '编辑资料' } },
  { path: '/user/coupons', name: 'Coupons', component: CouponsPage, meta: { title: '我的优惠券' } },
  { path: '/user/favorites', name: 'Favorites', component: FavoritesPage, meta: { title: '我的收藏' } },
  { path: '/user/settings', name: 'Settings', component: SettingsPage, meta: { title: '设置' } },
  { path: '/user/balance', name: 'Balance', component: BalancePage, meta: { title: '我的余额' } },
  { path: '/user/history', name: 'History', component: HistoryPage, meta: { title: '浏览记录' } }
]

// Capacitor WKWebView 中，初始 URL 可能没有 hash
// 手动设置确保 router 能找到初始路由
if (!window.location.hash) {
  window.location.hash = '#/'
}

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 诊断：导出 router 状态
;(window as any)._routerDiag = {
  ready: false,
  current: null,
  routes: routes.length,
  hash: window.location.hash
}

router.isReady().then(() => {
  ;(window as any)._routerDiag = {
    ready: true,
    current: router.currentRoute.value.path,
    name: router.currentRoute.value.name,
    matched: router.currentRoute.value.matched.length,
    routes: router.getRoutes().length,
    hash: window.location.hash
  }
})

export default router
