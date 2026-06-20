<template>
  <nav class="pc-nav">
    <div class="pc-nav-inner">
      <router-link to="/" class="pc-nav-brand">🌸 MiyaShop</router-link>
      <div class="pc-nav-links">
        <router-link to="/" class="pc-nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
        <router-link to="/seckill" class="pc-nav-link" :class="{ active: $route.path === '/seckill' }">秒杀</router-link>
        <router-link to="/groupbuy" class="pc-nav-link" :class="{ active: $route.path.startsWith('/groupbuy') }">拼团</router-link>
        <router-link to="/cart" class="pc-nav-link" :class="{ active: $route.path === '/cart' }">购物车</router-link>
        <router-link to="/user" class="pc-nav-link" :class="{ active: $route.path.startsWith('/user') }">我的</router-link>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.pc-nav {
  display: none;
}

@media screen and (min-width: 768px) {
  .pc-nav {
    display: flex;
    background: #fff;
    border-bottom: 1px solid #eee;
    position: sticky;
    top: 0;
    z-index: 999;
  }

  .pc-nav-inner {
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    height: 56px;
  }

  .pc-nav-brand {
    font-size: 20px;
    font-weight: 700;
    color: var(--primary-color);
    text-decoration: none;
  }

  .pc-nav-links {
    display: flex;
    gap: 32px;
  }

  .pc-nav-link {
    font-size: 15px;
    color: #666;
    text-decoration: none;
    transition: color 0.2s;
    padding: 4px 0;
    border-bottom: 2px solid transparent;
  }

  .pc-nav-link:hover,
  .pc-nav-link.active {
    color: var(--primary-color);
    font-weight: 600;
    border-bottom-color: var(--primary-color);
  }
}
</style>
