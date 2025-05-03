<template>
  <div class="order-page">
    <!-- 装饰元素 -->
    <div class="decoration-circle circle-1"></div>
    <div class="decoration-circle circle-2"></div>
    <div class="decoration-dots"></div>

    <div class="order-header">
      <div class="header-content">
        <h1>我的订单</h1>
        <El-button type="primary" class="back-home-btn" @click="goToHome">
          <El-icon><HomeFilled /></El-icon>
          返回首页
        </El-button>
      </div>

      <div class="order-filters">
        <div class="search-container">
          <El-input
            v-model="searchQuery"
            placeholder="搜索订单号/商品名称"
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <El-icon><Search /></El-icon>
            </template>
          </El-input>
          <El-button type="primary" class="search-btn" @click="handleSearch">
            搜索
          </El-button>
        </div>

        <El-select
          v-model="statusFilter"
          placeholder="订单状态"
          @change="handleFilter"
        >
          <El-option label="全部订单" value="" />
          <El-option label="待付款" value="PENDING" />
          <El-option label="已付款" value="PAID" />
          <El-option label="已发货" value="SHIPPED" />
          <El-option label="已完成" value="COMPLETED" />
          <El-option label="已取消" value="CANCELLED" />
          <El-option label="拼团进行中" value="GROUP_BUY_IN_PROGRESS" />
          <El-option label="拼团成功" value="GROUP_BUY_COMPLETED" />
          <El-option label="拼团失败" value="GROUP_BUY_FAILED" />
        </El-select>

        <El-select
          v-model="timeFilter"
          placeholder="时间筛选"
          @change="handleFilter"
        >
          <El-option label="全部时间" value="" />
          <El-option label="最近一周" value="week" />
          <El-option label="最近一个月" value="month" />
          <El-option label="最近三个月" value="quarter" />
        </El-select>
      </div>
    </div>

    <!-- 订单统计卡片 -->
    <div class="order-stats">
      <div class="stat-card" :class="{ 'animate-pulse': loading }">
        <div class="stat-icon pending-icon">
          <El-icon><Timer /></El-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ getOrderCountByStatus("待付款") }}</div>
          <div class="stat-label">待付款</div>
        </div>
      </div>

      <div class="stat-card" :class="{ 'animate-pulse': loading }">
        <div class="stat-icon paid-icon">
          <El-icon><Money /></El-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ getOrderCountByStatus("已付款") }}</div>
          <div class="stat-label">已付款</div>
        </div>
      </div>

      <div class="stat-card" :class="{ 'animate-pulse': loading }">
        <div class="stat-icon completed-icon">
          <El-icon><Check /></El-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ getOrderCountByStatus("已完成") }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <div class="order-list">
      <El-empty v-if="filteredOrders.length === 0" description="暂无订单" />

      <div v-else class="order-items">
        <div v-for="order in filteredOrders" :key="order.id" class="order-item">
          <div class="order-header">
            <div class="order-info">
              <span class="order-number">订单号：{{ order.id }}</span>
              <span class="order-time">{{ order.created_at }}</span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ order.status }}
            </div>
          </div>

          <div class="order-content">
            <div class="product-info">
              <img
                v-if="order.Product"
                :src="order.Product.imageUrl"
                :alt="order.Product.name"
                class="product-image"
              />
              <div class="product-details">
                <h3>{{ order.Product.name }}</h3>
                <p class="quantity">数量：{{ order.quantity }}</p>
                <p class="price">总金额：¥{{ order.total_amount }}</p>
              </div>
            </div>

            <div class="order-actions">
              <El-button
                v-if="order.status === '待付款'"
                type="primary"
                @click="handlePay(order)"
              >
                立即付款
              </El-button>
              <El-button
                v-if="order.status === '已发货'"
                type="success"
                @click="handleConfirmReceive(order)"
              >
                确认收货
              </El-button>
              <El-button
                v-if="order.status === '待付款'"
                type="danger"
                @click="handleCancel(order)"
              >
                取消订单
              </El-button>
              <El-button
                v-if="order.status === '拼团进行中'"
                type="warning"
                @click="viewGroupBuyDetails(order)"
              >
                查看拼团
              </El-button>
              <El-button @click="viewOrderDetails(order.id)">
                查看详情
              </El-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多按钮 -->
    <div v-if="hasMore" class="load-more">
      <El-button :loading="loading" @click="loadMore">
        {{ loading ? "加载中..." : "加载更多" }}
      </El-button>
    </div>

    <!-- 订单详情对话框 -->
    <El-dialog
      v-model="detailsDialogVisible"
      title="订单详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentOrder" class="order-details">
        <div class="detail-item">
          <span class="label">订单号：</span>
          <span class="value">{{ currentOrder.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">下单时间：</span>
          <span class="value">{{ currentOrder.created_at }}</span>
        </div>
        <div class="detail-item">
          <span class="label">订单状态：</span>
          <span class="value" :class="getStatusClass(currentOrder.status)">
            {{ currentOrder.status }}
          </span>
        </div>
        <div class="detail-item">
          <span class="label">商品名称：</span>
          <span class="value">{{ currentOrder.Product.name }}</span>
        </div>
        <div class="detail-item">
          <span class="label">购买数量：</span>
          <span class="value">{{ currentOrder.quantity }}</span>
        </div>
        <div class="detail-item">
          <span class="label">订单金额：</span>
          <span class="value price">¥{{ currentOrder.total_amount }}</span>
        </div>
      </div>
    </El-dialog>

    <!-- 支付对话框 -->
    <El-dialog
      v-model="payDialogVisible"
      title="订单支付"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="pay-dialog">
        <div class="pay-amount">
          支付金额：<span class="price">¥{{ currentOrder?.total_amount }}</span>
        </div>
        <El-input
          v-model="paymentPassword"
          type="password"
          placeholder="请输入支付密码"
          show-password
        />
      </div>
      <template #footer>
        <El-button @click="payDialogVisible = false">取消</El-button>
        <El-button type="primary" :loading="paying" @click="confirmPay">
          确认支付
        </El-button>
      </template>
    </El-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  HomeFilled,
  Timer,
  Money,
  Check,
} from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import axios from "axios";

const API_BASE_URL = "http://localhost:5000";
const router = useRouter();

// 状态变量
const orders = ref([]);
const loading = ref(false);
const searchQuery = ref("");
const statusFilter = ref("");
const timeFilter = ref("");
const detailsDialogVisible = ref(false);
const payDialogVisible = ref(false);
const paying = ref(false);
const currentOrder = ref(null);
const paymentPassword = ref("");
const hasMore = ref(true);
const page = ref(1);
const pageSize = ref(10);

// 返回首页
const goToHome = () => {
  router.push("/");
};

// 获取订单列表
const fetchOrders = async (isLoadMore = false) => {
  if (!isLoadMore) {
    page.value = 1;
    orders.value = [];
  }

  loading.value = true;
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");

  if (!token || !userId) {
    ElMessage.error("请先登录");
    return;
  }

  try {
    const response = await axios.get(`${API_BASE_URL}/api/orders`, {
      params: {
        userId,
        page: page.value,
        size: pageSize.value,
        status: statusFilter.value,
        search: searchQuery.value,
        timeRange: timeFilter.value,
      },
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.data.code === 200) {
      const newOrders = response.data.data.content.map((order) => ({
        id: order.id,
        Product: {
          id: order.productId,
          name: order.productName || "未知商品",
          imageUrl: order.productImageUrl || "",
        },
        quantity: order.quantity || 1,
        total_amount: order.totalAmount,
        status: translateOrderStatus(order.status),
        created_at: formatDate(order.createdAt),
      }));

      if (isLoadMore) {
        orders.value = [...orders.value, ...newOrders];
      } else {
        orders.value = newOrders;
      }

      hasMore.value = newOrders.length === pageSize.value;
    } else {
      ElMessage.error(response.data.message || "获取订单失败");
    }
  } catch (error) {
    console.error("获取订单失败:", error);
    ElMessage.error("获取订单失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 获取特定状态的订单数量
const getOrderCountByStatus = (status) => {
  return orders.value.filter((order) => order.status === status).length;
};

// 过滤后的订单列表
const filteredOrders = computed(() => {
  return orders.value;
});

// 处理搜索
const handleSearch = () => {
  fetchOrders();
};

// 处理筛选
const handleFilter = () => {
  fetchOrders();
};

// 加载更多
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    page.value++;
    fetchOrders(true);
  }
};

// 查看订单详情
const viewOrderDetails = (orderId) => {
  currentOrder.value = orders.value.find((order) => order.id === orderId);
  detailsDialogVisible.value = true;
};

// 处理支付
const handlePay = (order) => {
  currentOrder.value = order;
  payDialogVisible.value = true;
};

// 确认支付
const confirmPay = async () => {
  if (!paymentPassword.value) {
    ElMessage.warning("请输入支付密码");
    return;
  }

  paying.value = true;
  try {
    const token = localStorage.getItem("token");
    const response = await axios.post(
      `${API_BASE_URL}/api/orders/${currentOrder.value.id}/pay`,
      {
        paymentPassword: paymentPassword.value,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success("支付成功");
      payDialogVisible.value = false;
      paymentPassword.value = "";
      fetchOrders();
    } else {
      ElMessage.error(response.data.message || "支付失败");
    }
  } catch (error) {
    console.error("支付失败:", error);
    ElMessage.error(error.response?.data?.message || "支付失败，请稍后重试");
  } finally {
    paying.value = false;
  }
};

// 确认收货
const handleConfirmReceive = async (order) => {
  try {
    const token = localStorage.getItem("token");
    const response = await axios.post(
      `${API_BASE_URL}/api/orders/${order.id}/confirm`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success("确认收货成功");
      fetchOrders();
    } else {
      ElMessage.error(response.data.message || "确认收货失败");
    }
  } catch (error) {
    console.error("确认收货失败:", error);
    ElMessage.error("确认收货失败，请稍后重试");
  }
};

// 取消订单
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm("确定要取消该订单吗？", "取消订单", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const token = localStorage.getItem("token");
    const response = await axios.post(
      `${API_BASE_URL}/api/orders/${order.id}/cancel`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success("订单已取消");
      fetchOrders();
    } else {
      ElMessage.error(response.data.message || "取消订单失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("取消订单失败:", error);
      ElMessage.error("取消订单失败，请稍后重试");
    }
  }
};

// 查看拼团详情
const viewGroupBuyDetails = (order) => {
  router.push(`/group-buy/${order.id}`);
};

// 获取状态样式类
const getStatusClass = (status) => {
  const statusMap = {
    待付款: "status-pending",
    已付款: "status-paid",
    已发货: "status-shipped",
    已完成: "status-completed",
    已取消: "status-cancelled",
    拼团进行中: "status-group-buying",
    拼团成功: "status-group-success",
    拼团失败: "status-group-failed",
  };
  return statusMap[status] || "";
};

// 转换订单状态为中文
const translateOrderStatus = (status) => {
  const statusMap = {
    PENDING: "待付款",
    PAID: "已付款",
    SHIPPED: "已发货",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
    GROUP_BUY_IN_PROGRESS: "拼团进行中",
    GROUP_BUY_COMPLETED: "拼团成功",
    GROUP_BUY_FAILED: "拼团失败",
  };
  return statusMap[status] || status;
};

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleString();
};

// 组件挂载时获取订单数据
onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.order-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}

/* 装饰元素 */
.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  z-index: 0;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  top: -100px;
  right: -100px;
  animation: float 15s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
  bottom: -50px;
  left: -50px;
  animation: float 20s ease-in-out infinite reverse;
}

.decoration-dots {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(#ddd 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.3;
  z-index: 0;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(20px, 20px) rotate(5deg);
  }
  100% {
    transform: translate(0, 0) rotate(0deg);
  }
}

.order-header {
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
  text-align: left;
  position: relative;
  display: inline-block;
}

.order-header h1::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 3px;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-container {
  display: flex;
  gap: 10px;
  width: 300px;
}

.search-input {
  flex: 1;
}

.search-btn {
  white-space: nowrap;
}

/* 订单统计卡片 */
.order-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.stat-card {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.pending-icon {
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
}

.paid-icon {
  background: linear-gradient(135deg, #409eff, #67c23a);
}

.completed-icon {
  background: linear-gradient(135deg, #67c23a, #409eff);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.animate-pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.order-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
  background: white;
  position: relative;
  overflow: hidden;
}

.order-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #409eff, #67c23a);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.order-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.order-item:hover::before {
  opacity: 1;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.order-info {
  display: flex;
  gap: 20px;
  color: #606266;
}

.order-status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.status-pending {
  background-color: #e6a23c;
  color: #fff;
}

.status-paid {
  background-color: #409eff;
  color: #fff;
}

.status-shipped {
  background-color: #67c23a;
  color: #fff;
}

.status-completed {
  background-color: #909399;
  color: #fff;
}

.status-cancelled {
  background-color: #f56c6c;
  color: #fff;
}

.status-group-buying {
  background-color: #e6a23c;
  color: #fff;
}

.status-group-success {
  background-color: #67c23a;
  color: #fff;
}

.status-group-failed {
  background-color: #f56c6c;
  color: #fff;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  transition: transform 0.3s ease;
}

.product-image:hover {
  transform: scale(1.05);
}

.product-details h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.quantity,
.price {
  color: #606266;
  margin: 5px 0;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.order-actions .el-button {
  transition: all 0.3s ease;
}

.order-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.load-more {
  text-align: center;
  margin-top: 20px;
  position: relative;
  z-index: 1;
}

.order-details {
  padding: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
}

.detail-item .label {
  width: 100px;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

.pay-dialog {
  padding: 20px;
}

.pay-amount {
  margin-bottom: 20px;
  font-size: 18px;
  text-align: center;
}

.pay-amount .price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 24px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .order-filters {
    flex-direction: column;
  }

  .search-container {
    width: 100%;
  }

  .order-stats {
    flex-direction: column;
  }

  .order-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-actions {
    margin-top: 15px;
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
