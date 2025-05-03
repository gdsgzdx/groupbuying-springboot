<template>
  <div class="homepage">
    <!-- 页头 -->
    <header class="header">
      <div class="header-container">
        <!-- Logo -->
        <img src="/assets/logo.png" alt="Logo" class="logo" />
        <div class="title">团团赚</div>
        <!-- 首页按钮 -->
        <El-button class="home-button" @click="goHome">首页</El-button>
        <!-- 用户功能 -->
        <div class="user-actions">
          <span class="user-nickname">欢迎，{{ userNickname }}</span>
          <img
            v-if="userAvatar"
            :src="userAvatar"
            alt="用户头像"
            class="user-avatar"
          />
          <span class="user-action" @click="goToProfile">
            <el-icon><User /></el-icon>
            用户中心
          </span>
          <span class="user-action" @click="goToCart">
            <el-icon><ShoppingCart /></el-icon>
            购物车
          </span>
          <span class="user-action" @click="goToOrders">
            <el-icon><List /></el-icon>
            我的订单
          </span>
          <span class="user-action" @click="showMyGroupBuys">
            <el-icon><People /></el-icon>
            我的拼团
          </span>
          <span class="user-action" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </span>
          <div class="search-container">
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="搜索商品"
            />
            <El-button type="primary" @click="applySearch">搜索</El-button>
          </div>
        </div>
      </div>
    </header>

    <div class="main-content">
      <!-- 商品分类 -->
      <aside class="sidebar">
        <El-menu default-active="" class="category-menu">
          <El-menu-item index="" @click="selectCategory('')"
            >所有类目</El-menu-item
          >
          <El-menu-item index="1" @click="selectCategory('家用电器')"
            >家用电器</El-menu-item
          >
          <El-menu-item index="2" @click="selectCategory('手机通讯数码')"
            >手机/通讯/数码</El-menu-item
          >
          <El-menu-item index="3" @click="selectCategory('电脑办公文具用品')"
            >电脑/办公/文具用品</El-menu-item
          >
          <El-menu-item index="4" @click="selectCategory('家具家居厨房')"
            >家具/家居/厨房</El-menu-item
          >
          <El-menu-item index="5" @click="selectCategory('男装女装童装内衣')"
            >男装/女装/童装/内衣</El-menu-item
          >
          <El-menu-item index="6" @click="selectCategory('美妆个护清洁')"
            >美妆/个护清洁</El-menu-item
          >
          <El-menu-item index="7" @click="selectCategory('女鞋箱包珠宝')"
            >女鞋/箱包/珠宝</El-menu-item
          >
          <El-menu-item index="8" @click="selectCategory('男鞋运动户外')"
            >男鞋/运动/户外</El-menu-item
          >
          <El-menu-item index="9" @click="selectCategory('食品酒类生鲜')"
            >食品/酒类/生鲜</El-menu-item
          >
          <El-menu-item index="10" @click="selectCategory('艺术礼品花园艺')"
            >艺术/礼品花/园艺</El-menu-item
          >
          <El-menu-item index="11" @click="selectCategory('图书文娱教育')"
            >图书/文娱/教育</El-menu-item
          >
          <El-menu-item index="12" @click="selectCategory('机票酒店旅游')"
            >机票/酒店/旅游</El-menu-item
          >
          <El-menu-item index="13" @click="selectCategory('支付保险金融')"
            >支付/保险/金融</El-menu-item
          >
          <El-menu-item index="14" @click="selectCategory('玩具乐器')"
            >玩具/乐器</El-menu-item
          >
        </El-menu>
      </aside>

      <section class="product-section">
        <section class="ad-banner">
          <div class="static-ads">
            <img src="/assets/车厘子.png" alt="广告1" class="ad-image" />
            <img src="/assets/广告1.png" alt="广告2" class="ad-image" />
          </div>
          <El-carousel
            class="carousel"
            :autoplay="true"
            interval="5000"
            arrow="always"
          >
            <El-carousel-item v-for="ad in ads" :key="ad.image">
              <img :src="ad.image" alt="广告" class="ad-image" />
            </El-carousel-item>
          </El-carousel>
        </section>

        <!-- 热卖商品标题 -->
        <h2 class="section-title">热卖商品</h2>

        <!-- 优惠券按钮区域 -->
        <div class="coupon-actions">
          <El-button type="danger" @click="showCouponDrawDialog">
            <el-icon><Ticket /></el-icon>
            抽取优惠券
          </El-button>
          <El-button type="warning" @click="showMyCoupons">
            <el-icon><Tickets /></el-icon>
            我的优惠券
          </El-button>
        </div>

        <!-- 排序选择框 -->
        <div class="filter-section">
          <El-select v-model="selectedSort" placeholder="排序方式">
            <El-option label="默认排序" value="default-sort"></El-option>
            <El-option label="价格从低到高" value="low-to-high"></El-option>
            <El-option label="价格从高到低" value="high-to-low"></El-option>
            <El-option label="销量从高到低" value="hot-to-high"></El-option>
          </El-select>
        </div>

        <!-- 商品展示区域 -->
        <El-row :gutter="20">
          <El-col
            v-for="product in paginatedProducts"
            :key="product.id"
            :span="6"
          >
            <El-card
              :style="{
                padding: '45px',
                backgroundColor: getRandomPastelColor(),
                transition: 'transform 0.3s ease',
              }"
              class="product-card"
              @mouseenter="
                $event.currentTarget.style.transform = 'translateY(-5px)'
              "
              @mouseleave="
                $event.currentTarget.style.transform = 'translateY(0)'
              "
            >
              <img
                :src="product.image || '/assets/default-product.png'"
                class="product-image"
                alt=""
              />
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="price">
                  ¥{{ product.price ? product.price : "暂无价格" }}
                </p>
                <El-button type="primary" @click="viewProductDetails(product)"
                  >查看商品详情</El-button
                >
              </div>
            </El-card>
          </El-col>
        </El-row>
        <p v-if="!paginatedProducts.length">没有找到相关商品</p>

        <!-- 分页 -->
        <El-pagination
          :current-page="currentPage"
          :page-size="itemsPerPage"
          :total="sortedProducts.length"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </section>
    </div>

    <!-- 我的拼团对话框 -->
    <El-dialog
      v-model="groupBuyDialogVisible"
      title="我的拼团"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="my-group-buys">
        <div v-if="myGroupBuys.length === 0" class="empty-message">
          暂无拼团记录
        </div>
        <div v-else class="group-buy-list">
          <div
            v-for="group in myGroupBuys"
            :key="group.id"
            class="group-buy-item"
          >
            <div class="group-info">
              <img
                :src="group.productImage"
                alt="商品图片"
                class="product-image"
              />
              <div class="group-details">
                <h3>{{ group.productName }}</h3>
                <p class="group-price">拼团价：¥{{ group.groupPrice }}</p>
                <p class="group-status">
                  状态：
                  <span
                    :class="[
                      'status-label',
                      group.status === 'COMPLETED'
                        ? 'success'
                        : group.status === 'FAILED'
                          ? 'failed'
                          : 'ongoing',
                    ]"
                  >
                    {{ formatGroupStatus(group.status) }}
                  </span>
                </p>
                <p class="group-progress">
                  进度：{{ group.currentSize }}/{{ group.requiredSize || 5 }}人
                </p>
                <p v-if="group.status === 'IN_PROGRESS'" class="remaining-time">
                  剩余时间：{{ formatTime(group.remainingSeconds) }}
                </p>
                <!-- 添加参与者头像展示 -->
                <div class="participants-avatars">
                  <div
                    v-for="i in group.requiredSize || 5"
                    :key="i"
                    class="avatar-slot"
                    :class="{
                      empty:
                        !group.participants || i > group.participants.length,
                    }"
                  >
                    <template
                      v-if="group.participants && group.participants[i - 1]"
                    >
                      <el-tooltip
                        :content="
                          group.participants[i - 1].nickname || '参与者'
                        "
                        placement="top"
                      >
                        <img
                          :src="getParticipantAvatar(group.participants[i - 1])"
                          :alt="group.participants[i - 1].nickname || '参与者'"
                          class="participant-avatar"
                        />
                      </el-tooltip>
                    </template>
                    <el-icon v-else class="empty-avatar"><User /></el-icon>
                  </div>
                </div>
              </div>
            </div>
            <div class="group-actions">
              <El-button
                v-if="group.status === 'IN_PROGRESS'"
                type="primary"
                size="small"
                @click="copyInviteCode(group.inviteCode)"
              >
                复制邀请码
              </El-button>
            </div>
          </div>
        </div>
      </div>
    </El-dialog>

    <!-- 优惠券抽奖对话框 -->
    <El-dialog
      v-model="couponDrawDialogVisible"
      title="抽取优惠券"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="coupon-draw-container">
        <div class="wheel-container" :class="{ spinning: isSpinning }">
          <div class="wheel">
            <div
              v-for="(coupon, index) in couponOptions"
              :key="index"
              class="wheel-item"
              :style="{ transform: `rotate(${(360 / 5) * index}deg)` }"
            >
              {{ coupon.text }}
            </div>
          </div>
          <div class="wheel-pointer"></div>
          <div class="wheel-center"></div>
        </div>
        <El-button
          type="danger"
          :disabled="isSpinning || hasDrewToday"
          @click="drawCoupon"
        >
          {{ hasDrewToday ? "今日已抽奖" : "开始抽奖" }}
        </El-button>
      </div>
    </El-dialog>

    <!-- 我的优惠券对话框 -->
    <El-dialog
      v-model="myCouponsDialogVisible"
      title="我的优惠券"
      width="500px"
    >
      <div class="my-coupons-container">
        <div v-if="myCoupons.length === 0" class="empty-coupons">
          暂无优惠券
        </div>
        <div v-else class="coupon-list">
          <div
            v-for="coupon in myCoupons"
            :key="coupon.id"
            class="coupon-item"
            :class="{ expired: isExpired(coupon), used: coupon.isUsed }"
          >
            <div class="coupon-info">
              <div class="discount">{{ coupon.discountText }}</div>
              <div class="validity">
                有效期至: {{ formatDate(coupon.expiryTime) }}
              </div>
              <div class="status">
                {{ getCouponStatus(coupon) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </El-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import {
  ElCarousel,
  ElCarouselItem,
  ElButton,
  ElMenu,
  ElMenuItem,
  ElSelect,
  ElOption,
  ElRow,
  ElCol,
  ElCard,
  ElPagination,
  ElDialog,
  ElMessage,
} from "element-plus";
import { useRouter } from "vue-router"; // 导入路由
import { useStore } from "vuex"; // 导入 Vuex Store
import axios from "axios";
import {
  User,
  ShoppingCart,
  List,
  People,
  SwitchButton,
  Ticket,
  Tickets,
} from "@element-plus/icons-vue"; // 导入需要的图标

const API_BASE_URL = "http://localhost:5000"; // 添加 API 基础 URL
const router = useRouter(); // 获取路由对象
const store = useStore(); // 获取 Vuex Store
const searchQuery = ref(""); // 搜索查询
const selectedSort = ref("default-sort"); // 默认排序方式
const currentPage = ref(1); // 当前页码
const itemsPerPage = ref(8); // 每页显示的商品数量
const sortedProducts = computed(() => {
  const products = store.getters.allProducts || [];
  console.log("当前产品列表:", products);

  switch (selectedSort.value) {
    case "low-to-high":
      return [...products].sort((a, b) => a.price - b.price);
    case "high-to-low":
      return [...products].sort((a, b) => b.price - a.price);
    case "hot-to-high":
      return [...products].sort((a, b) => b.sales - a.sales);
    default:
      return products;
  }
});
const ads = ref([
  { image: "/assets/苹果13.png" },
  { image: "/assets/苹果15Pro.png" },
  { image: "/assets/微软二合一ipad.png" },
]);

const goToProfile = () => {
  router.push({ name: "UserPage" }); // 跳转到个人中心页面
};

// 获取产品数据
const fetchProductData = async () => {
  try {
    await store.dispatch("FetchProducts");
    console.log("产品数据获取成功");
  } catch (error) {
    console.error("获取产品数据失败:", error);
  }
};
// 添加跳转到购物车的功能
const goToCart = () => {
  router.push({ name: "CartPage" }); // 跳转到购物车页面
};

// 添加跳转到我的订单的功能
const goToOrders = () => {
  // 使用路径导航
  router.push({ name: "OrderPage" });
};

// 计算属性获取用户昵称
const userNickname = computed(() => store.getters.userNickname);
// 计算属性获取用户头像
const userAvatar = computed(() => {
  const avatar = store.getters.userAvatar;
  if (!avatar) {
    return "/assets/avatar.png";
  }
  if (avatar.startsWith("/assets/")) {
    return avatar;
  }
  return `${API_BASE_URL}${avatar}`;
});

// 获取当前页的产品
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return sortedProducts.value.slice(start, end);
});

// 分页功能变化时的处理函数
const handlePageChange = (newPage) => {
  currentPage.value = newPage; // 更新当前页
};

// 搜索功能
const applySearch = async () => {
  console.log("搜索产品:", searchQuery.value);
  try {
    if (searchQuery.value.trim()) {
      await store.dispatch("SearchProducts", searchQuery.value);
    } else {
      await store.dispatch("FetchProducts");
    }
    currentPage.value = 1; // 重置页码
  } catch (error) {
    console.error("搜索产品失败:", error);
  }
};

// 跳转到首页并刷新
const goHome = () => {
  window.location.reload(); // 强制刷新页面
};

// 跳转到商品详情
const viewProductDetails = (product) => {
  console.log(`Navigating to product with ID: ${product.id}`); // 调试输出
  router.push(`/product/${product.id}`);
};

// 选择分类
const selectCategory = async (category) => {
  console.log(`选择了分类: ${category}`);
  try {
    if (category) {
      await store.dispatch("FetchProductsByCategory", category);
    } else {
      await store.dispatch("FetchProducts");
    }
    currentPage.value = 1; // 重置页码
  } catch (error) {
    console.error("获取分类产品失败:", error);
  }
};

// 退出登录功能
const logout = () => {
  console.log("用户已注销");
  store.commit("clearUser"); // 清空用户信息
  router.push({ name: "Login" }); // 跳转到登录页面
};

// 拼团相关
const groupBuyDialogVisible = ref(false);
const myGroupBuys = ref([]);
const timers = ref({}); // 存储所有计时器

// 显示我的拼团对话框
const showMyGroupBuys = async () => {
  groupBuyDialogVisible.value = true;
  await fetchMyGroupBuys();
};

// 获取我的拼团列表
const fetchMyGroupBuys = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/group-buys/user`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
    if (response.data.code === 200) {
      console.log("完整的拼团数据:", response.data.data);
      myGroupBuys.value = response.data.data.map((group) => {
        // 确保 requiredSize 至少为 5
        group.requiredSize = group.requiredSize || 5;

        console.log("处理拼团:", group.id, {
          status: group.status,
          currentSize: group.currentSize,
          requiredSize: group.requiredSize,
          remainingSeconds: group.remainingSeconds,
        });

        // 检查拼团状态
        if (group.status === "IN_PROGRESS") {
          const currentSize = Number(group.currentSize);
          const requiredSize = Number(group.requiredSize);
          console.log(
            `拼团${group.id} - 当前人数:${currentSize}, 要求人数:${requiredSize}, 类型:`,
            {
              currentSizeType: typeof group.currentSize,
              requiredSizeType: typeof group.requiredSize,
            },
          );

          // 只有在人数严格等于要求时才标记为成功
          if (currentSize === requiredSize) {
            console.log(`拼团${group.id}达到人数要求，标记为成功`);
            group.status = "SUCCESS";
          }
          // 只有在时间到期且人数未达到要求时才标记为失败
          else if (group.remainingSeconds <= 0 && currentSize < requiredSize) {
            console.log(`拼团${group.id}时间到期且人数未达标，标记为失败`);
            group.status = "FAILED";
          } else {
            console.log(`拼团${group.id}保持进行中状态`);
          }
        }
        return group;
      });

      // 为每个进行中的拼团启动倒计时
      myGroupBuys.value.forEach((group) => {
        if (group.status === "IN_PROGRESS" && group.remainingSeconds > 0) {
          startGroupCountdown(group);
        }
      });
    }
  } catch (error) {
    // 静默失败，不显示任何错误信息
  }
};

// 修改倒计时逻辑
const startGroupCountdown = (group) => {
  console.log(`开始拼团${group.id}的倒计时`);
  const timer = setInterval(() => {
    // 确保 requiredSize 至少为 5
    group.requiredSize = group.requiredSize || 5;

    const currentSize = Number(group.currentSize);
    const requiredSize = Number(group.requiredSize);

    console.log(
      `拼团${group.id}倒计时检查 - 当前人数:${currentSize}, 要求人数:${requiredSize}, 剩余时间:${group.remainingSeconds}`,
    );

    if (group.remainingSeconds <= 0) {
      clearInterval(timer);
      if (currentSize < requiredSize) {
        console.log(`拼团${group.id}时间到期且人数未达标，标记为失败`);
        group.status = "FAILED";
      }
      return;
    }

    if (currentSize === requiredSize) {
      clearInterval(timer);
      console.log(`拼团${group.id}达到人数要求，标记为成功`);
      group.status = "SUCCESS";
      return;
    }

    group.remainingSeconds--;
  }, 1000);

  // 保存定时器ID以便在组件销毁时清除
  timers.value[group.id] = timer;
};

// 格式化拼团状态
const formatGroupStatus = (status) => {
  const statusMap = {
    IN_PROGRESS: "进行中",
    COMPLETED: "拼团成功",
    FAILED: "拼团失败",
  };
  return statusMap[status] || status;
};

// 格式化剩余时间
const formatTime = (seconds) => {
  if (seconds == null || isNaN(seconds)) return "00:00:00";
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;
  return `${hours.toString().padStart(2, "0")}:${minutes
    .toString()
    .padStart(2, "0")}:${remainingSeconds.toString().padStart(2, "0")}`;
};

// 复制邀请码
const copyInviteCode = async (inviteCode) => {
  try {
    await navigator.clipboard.writeText(inviteCode);
    ElMessage.success("邀请码已复制到剪贴板");
  } catch (error) {
    console.error("复制失败:", error);
    ElMessage.error("复制失败，请手动复制");
  }
};

// 获取参与者头像
const getParticipantAvatar = (participant) => {
  console.log("处理头像的参与者数据:", participant); // 添加调试日志
  if (!participant || !participant.avatar) {
    return "/assets/avatar.png";
  }

  // 如果头像路径是相对路径，添加API基础URL
  if (participant.avatar.startsWith("/")) {
    return `${API_BASE_URL}${participant.avatar}`;
  }

  // 如果是完整的URL或默认头像，直接返回
  return participant.avatar;
};

// 在组件挂载时获取产品数据
onMounted(() => {
  fetchProductData();
});

// 在对话框关闭时清理所有计时器
const cleanupTimers = () => {
  Object.values(timers.value).forEach((timer) => clearInterval(timer));
  timers.value = {};
};

// 监听对话框关闭事件
watch(groupBuyDialogVisible, (newValue) => {
  if (!newValue) {
    cleanupTimers();
  }
});

// 组件卸载时清理计时器
onUnmounted(() => {
  cleanupTimers();
});

const getRandomPastelColor = () => {
  // 生成柔和的随机颜色
  const hue = Math.floor(Math.random() * 360); // 随机色相
  const saturation = 25 + Math.random() * 20; // 较低的饱和度，使颜色更柔和
  const lightness = 85 + Math.random() * 10; // 较高的亮度，确保背景色淡雅
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`;
};

// 优惠券相关
const couponDrawDialogVisible = ref(false);
const myCouponsDialogVisible = ref(false);
const isSpinning = ref(false);
const hasDrewToday = ref(false);
const myCoupons = ref([]);

const couponOptions = [
  { text: "95折", discount: 0.95 },
  { text: "9折", discount: 0.9 },
  { text: "85折", discount: 0.85 },
  { text: "8折", discount: 0.8 },
  { text: "75折", discount: 0.75 },
];

// 显示抽奖对话框
const showCouponDrawDialog = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/coupons/can-draw`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
    hasDrewToday.value = !response.data;
    couponDrawDialogVisible.value = true;
  } catch (error) {
    ElMessage.error("检查抽奖资格失败");
  }
};

// 显示我的优惠券
const showMyCoupons = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/coupons/my`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
    myCoupons.value = response.data;
    myCouponsDialogVisible.value = true;
  } catch (error) {
    ElMessage.error("获取优惠券失败");
  }
};

// 抽取优惠券
const drawCoupon = async () => {
  if (isSpinning.value || hasDrewToday.value) return;

  isSpinning.value = true;
  const randomDuration = 3000 + Math.random() * 1000; // 3-4秒随机

  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/coupons/draw`,
      {},
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      },
    );

    // 等待动画完成后处理结果
    setTimeout(() => {
      isSpinning.value = false;
      if (response.data) {
        const coupon = response.data;
        ElMessage.success(`恭喜获得${coupon.discountText}优惠券！`);
        hasDrewToday.value = true;
        // 刷新优惠券列表
        showMyCoupons();
      } else {
        ElMessage.error("抽奖失败，请稍后重试");
      }
    }, randomDuration);
  } catch (error) {
    console.error("抽奖请求失败:", error);
    setTimeout(() => {
      isSpinning.value = false;
      if (error.response?.data?.message) {
        ElMessage.error(error.response.data.message);
      } else {
        ElMessage.error("抽奖失败，请检查网络连接");
      }
      hasDrewToday.value = false;
    }, 100);
  }
};

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
};

// 检查优惠券是否过期
const isExpired = (coupon) => {
  return new Date(coupon.expiryTime) < new Date();
};

// 获取优惠券状态
const getCouponStatus = (coupon) => {
  if (coupon.isUsed) return "已使用";
  if (isExpired(coupon)) return "已过期";
  return "可使用";
};
</script>

<style lang="scss" scoped>
.homepage {
  font-family: "Helvetica Neue", Arial, sans-serif;

  .header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    padding: 15px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;

    .header-container {
      display: flex;
      align-items: center;
      width: 100%;

      .logo {
        width: 50px;
        height: auto;
        margin-right: 10px;
      }

      .title {
        font-size: 24px;
        font-weight: bold;
        margin-right: 20px;
      }

      .home-button {
        background: linear-gradient(135deg, #42b983 0%, #33a06f 100%);
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 20px;
        font-weight: bold;
        transition: all 0.3s ease;
        box-shadow: 0 2px 6px rgba(66, 185, 131, 0.2);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(66, 185, 131, 0.3);
        }
      }

      .user-actions {
        display: flex;
        align-items: center;
        margin-right: 20px;

        .user-nickname {
          margin-right: 15px;
          font-size: 16px;
          color: #333;
        }

        .user-action {
          cursor: pointer;
          color: #606266;
          margin: 0 15px;
          padding: 8px 12px;
          border-radius: 15px;
          transition: all 0.3s ease;
          font-weight: 500;
          font-size: 15px;
          white-space: nowrap;
          display: inline-flex;
          align-items: center;
          gap: 4px;

          .el-icon {
            margin-right: 4px;
            font-size: 16px;
          }

          &:hover {
            color: #42b983;
            background-color: rgba(66, 185, 131, 0.1);
          }
        }

        .search-container {
          display: flex;
          align-items: center;
        }

        .search-input {
          padding: 12px 15px;
          border: 2px solid #e4e7ed;
          border-radius: 20px;
          width: 300px;
          margin-left: 15px;
          transition: all 0.3s ease;

          &:focus {
            border-color: #42b983;
            box-shadow: 0 0 8px rgba(66, 185, 131, 0.2);
            outline: none;
          }
        }
      }
    }
  }

  .main-content {
    display: flex;
    flex-direction: row;
  }

  .user-avatar {
    width: 40px; /* 头像宽度 */
    height: 40px; /* 头像高度 */
    border-radius: 50%; /* 圆形头像 */
    margin-left: 10px; /* 与昵称之间的间距 */
  }

  .sidebar {
    width: 220px;
    background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
    border-right: 1px solid #e4e7ed;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  }

  .category-menu {
    padding: 15px 0;

    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      font-size: 15px;
      color: #606266;
      margin: 4px 0;
      border-radius: 8px;
      margin: 4px 10px;
      transition: all 0.3s ease;

      &:hover {
        background: linear-gradient(
          135deg,
          rgba(66, 185, 131, 0.1) 0%,
          rgba(66, 185, 131, 0.05) 100%
        );
        color: #42b983;
        padding-left: 25px;
      }

      &.is-active {
        background: linear-gradient(135deg, #42b983 0%, #33a06f 100%);
        color: white;
        font-weight: bold;
      }
    }
  }

  .product-section {
    flex-grow: 1;
    padding: 20px;

    .ad-banner {
      display: flex;
      align-items: center; /* 此时保持广告栏对齐 */
      margin-bottom: 20px;

      .static-ads {
        display: flex; /* 使用 flexbox 使广告在同一行 */
        flex: 1; /* 调整两侧静态广告的宽度 */
        justify-content: space-between; /* 在广告之间留出间距 */
        margin-right: 20px; /* 右侧留出间距 */
      }

      .ad-image {
        height: 500px; /* 设置每个静态广告的高度 */
        flex: 1; /* 每个广告等分宽度 */
        object-fit: cover; /* 保持比例 */
        margin: 0 10px; /* 设置广告之间的间隙 */
      }

      .carousel {
        height: 500px; /* 高度设置为 500px 以对齐 */
        flex: 2; /* 确保轮播占用更多空间 */
        position: relative; /* 为了确保图片可以覆盖整个区域 */

        .el-carousel__item {
          height: 500px; /* 确保每个轮播项的高度为 500px */
        }

        .ad-image {
          width: 100%; /* 确保图片宽度100% */
          height: 100%; /* 确保图片高度为100% */
          object-fit: cover; /* 确保图片填充，同时保持比例 */
        }
      }
    }

    .filter-section {
      margin-bottom: 20px;
      display: flex;
      align-items: center;

      El-select {
        margin-right: 10px; /* 排序框右边距 */
      }
    }

    .section-title {
      font-size: 22px;
      margin-bottom: 20px;
      text-align: center;
    }

    .product-card {
      text-align: center;
      height: 400px; /* 设置产品卡片的高度，根据需要调整 */
      overflow: hidden; /* 隐藏溢出的部分 */
      display: flex; /* 使用 flexbox 布局 */
      flex-direction: column; /* 列方向排列子元素 */

      .product-image {
        flex: 1; /* 让产品图片占据剩余空间 */
        width: 100%;
        height: auto; /* 自适应高度 */
        object-fit: cover; /* 确保图片填充，同时保持比例 */
        border-radius: 4px;
      }

      .product-info {
        margin-top: 10px; /* 调整与图片的间距 */
        margin-bottom: 10px; /* 添加底部间距 */

        .price {
          font-size: 18px;
          color: #f65a8b;
        }
      }
    }
  }
}

.my-group-buys {
  max-height: 500px;
  overflow-y: auto;
}

.empty-message {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.group-buy-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.group-info {
  display: flex;
  gap: 15px;
  flex: 1;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.group-details {
  flex: 1;
}

.group-details h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.group-price {
  color: #f56c6c;
  font-weight: bold;
  margin: 5px 0;
}

.group-status {
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-status .status-label {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.group-status .status-label.success {
  background-color: #67c23a;
  color: white;
}

.group-status .status-label.failed {
  background-color: #f56c6c;
  color: white;
}

.group-status .status-label.ongoing {
  background-color: #409eff;
  color: white;
}

.group-progress {
  margin: 5px 0;
  color: #606266;
}

.remaining-time {
  color: #e6a23c;
  font-weight: bold;
  margin: 5px 0;
}

.group-actions {
  margin-left: 15px;
}

.participants-avatars {
  display: flex;
  gap: 10px;
  margin: 15px 0;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;

  .avatar-slot {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: 2px solid #409eff;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background-color: #fff;
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    &.empty {
      border-style: dashed;
      border-color: #dcdfe6;
      background-color: #f5f7fa;
    }

    .participant-avatar {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .empty-avatar {
      font-size: 24px;
      color: #dcdfe6;
    }
  }
}

.coupon-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.coupon-draw-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.wheel-container {
  position: relative;
  width: 300px;
  height: 300px;
  margin-bottom: 20px;

  &.spinning .wheel {
    animation: spin 3s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.wheel {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #fff;
  border: 8px solid #f56c6c;
  transform-origin: center;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

.wheel-item {
  position: absolute;
  width: 50%;
  height: 50%;
  transform-origin: 100% 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  transform: rotate(var(--rotate)) skewY(var(--skew));

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      45deg,
      rgba(255, 255, 255, 0.1),
      rgba(255, 255, 255, 0.4)
    );
    z-index: -1;
  }

  @for $i from 1 through 5 {
    &:nth-child(#{$i}) {
      --rotate: #{72deg * ($i - 1)};
      --skew: -18deg;
    }
  }
}

.wheel-pointer {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0;
  height: 0;
  border-left: 20px solid transparent;
  border-right: 20px solid transparent;
  border-top: 40px solid #f56c6c;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  z-index: 2;
}

.wheel-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: radial-gradient(circle at 30% 30%, #ff8a8a, #f56c6c);
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(1800deg);
  }
}

.my-coupons-container {
  max-height: 400px;
  overflow-y: auto;
}

.empty-coupons {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.coupon-item {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%);
  border-radius: 8px;
  padding: 15px;
  color: white;
  position: relative;
  overflow: hidden;

  &.expired {
    background: linear-gradient(135deg, #a8a8a8 0%, #d3d3d3 100%);
    opacity: 0.7;
  }

  &.used {
    background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  }
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.discount {
  font-size: 24px;
  font-weight: bold;
}

.validity {
  font-size: 14px;
}

.status {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
}
</style>
