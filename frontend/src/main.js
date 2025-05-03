// frontend/src/main.js
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";
// 图标
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import { createStore } from "vuex";
import userStore from "@/store/userStore";
import productStore from "@/store/productStore";

const app = createApp(App);

// 创建 Vuex store
const store = createStore({
  modules: {
    user: userStore, // 注册 userStore
    product: productStore, // 注册 productStore
  },
});

// 在应用创建时初始化用户状态（确保在 userStore 中存在）
store.commit("initializeUser"); // 使用 Vuex 的 commit 方法初始化用户

// 注册全局应用
app.use(store); // 使用 Vuex store
app.use(router); // 路由
app.use(ElementPlus, {
  locale: zhCn, // 使用中文语言
});

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 挂载应用
app.mount("#app");
