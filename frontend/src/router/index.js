// frontend/src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import Login from "@/views/login/LoginPage.vue";
import Home from "@/views/home/HomePage.vue";
import Register from "@/views/login/RegisterPage.vue";
import ForgetPassword from "@/views/login/ForgetpasswordPage.vue";
import ProductPage from "@/views/product/ProductPage.vue"; // 导入商品详情页
import UserPage from "@/views/user/UserPage.vue"; // 导入个人页面
import CartPage from "@/views/user/CartPage.vue";
import OrderPage from "@/views/user/OrderPage.vue"; // 导入购物车页面

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/home",
    name: "Home",
    component: Home,
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/forget-password",
    name: "ForgetPassword",
    component: ForgetPassword,
  },
  {
    path: "/product/:id", // 添加商品详情页面路由
    name: "ProductPage",
    component: ProductPage,
  },
  {
    path: "/userStore", // 添加个人中心页面路由
    name: "UserPage",
    component: UserPage,
  },
  {
    path: "/orders", // 添加我的订单页面路由
    name: "OrderPage",
    component: OrderPage,
  },
  {
    path: "/cart", // 添加购物车页面路由
    name: "CartPage",
    component: CartPage,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  const publicPages = ["/login", "/register", "/forget-password"];
  const authRequired = !publicPages.includes(to.path);

  // 如果已登录且访问登录页，重定向到首页
  if (token && to.path === "/login") {
    next("/home");
    return;
  }

  // 如果需要认证但未登录，重定向到登录页
  if (authRequired && !token) {
    next("/login");
    return;
  }

  next();
});

export default router;
