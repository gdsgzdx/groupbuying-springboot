<template>
  <div class="product-page">
    <div class="product-container">
      <div class="product-header">
        <h1>{{ product.name }}</h1>
        <p class="price">价格: ¥{{ product.price }}</p>
        <El-button type="default" class="back-home-button" @click="goHome"
          >返回首页</El-button
        >
      </div>

      <div class="product-content">
        <img :src="product.image" alt="商品图片" class="product-image" />

        <div class="product-details">
          <p class="description">{{ product.description }}</p>
          <p class="stock">库存: {{ product.stock }}件</p>

          <div class="action-buttons">
            <ElButton type="primary" @click="addToCart">
              <i class="el-icon-shopping-cart-full"></i> 加入购物车
            </ElButton>
            <ElButton type="danger" @click="showPurchaseOptions"
              >立即购买</ElButton
            >
          </div>

          <!-- 增加的购买方式选项 -->
          <div v-if="optionsVisible" class="purchase-options">
            <p>请选择购买方式：</p>
            <div class="group-buy-tip">拼团购买享受8折优惠！</div>
            <ElButton type="success" @click="showGroupBuyDialog"
              >拼团购买</ElButton
            >
            <ElButton type="warning" @click="askForPurchaseConfirmation"
              >单独购买</ElButton
            >
          </div>
        </div>
      </div>
    </div>

    <!-- 确认购买对话框 -->
    <El-dialog
      v-model="buyDialogVisible"
      title="确认购买"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="buy-dialog-content">
        <div class="product-summary">
          <img :src="product.image" class="product-image" />
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="price">原价：¥{{ product.price }}</p>
            <div v-if="availableCoupons.length > 0" class="coupon-section">
              <p>可用优惠券：</p>
              <El-select v-model="selectedCouponId" placeholder="选择优惠券">
                <El-option
                  v-for="coupon in availableCoupons"
                  :key="coupon.id"
                  :label="coupon.discountText"
                  :value="coupon.id"
                />
              </El-select>
            </div>
            <p v-if="selectedCoupon" class="final-price">
              优惠后价格：¥{{
                calculateDiscountedPrice(product.price, selectedCoupon.discount)
              }}
            </p>
          </div>
        </div>
        <div class="payment-section">
          <El-input
            v-model="paymentPassword"
            type="password"
            placeholder="请输入支付密码"
            show-password
          />
        </div>
      </div>
      <template #footer>
        <El-button @click="buyDialogVisible = false">取消</El-button>
        <El-button
          type="primary"
          :loading="isProcessing"
          @click="confirmPurchase"
        >
          确认购买
        </El-button>
      </template>
    </El-dialog>

    <!-- 输入支付密码对话框 -->
    <El-dialog
      v-model="isPasswordDialogVisible"
      title="输入支付密码"
      width="400px"
      @close="resetPasswordDialog"
    >
      <El-input
        v-model="paymentPassword"
        type="password"
        placeholder="请输入支付密码"
        show-password
      ></El-input>
      <template #footer>
        <ElButton @click="resetPasswordDialog">取消</ElButton>
        <ElButton type="primary" @click="handlePaymentSubmit"
          >确认支付</ElButton
        >
      </template>
    </El-dialog>

    <!-- 拼团对话框 -->
    <El-dialog
      v-model="isGroupBuyDialogVisible"
      title="拼团详情"
      width="500px"
      @close="resetGroupBuyDialog"
    >
      <div class="group-buy-container">
        <div class="group-price">
          <span class="label">拼团价：</span>
          <span class="price">¥{{ (product.price * 0.8).toFixed(2) }}</span>
          <span class="original-price">原价：¥{{ product.price }}</span>
        </div>

        <div class="member-avatars">
          <template v-if="currentGroup">
            <div
              v-for="i in currentGroup.requiredSize"
              :key="i"
              class="avatar-slot"
            >
              <template v-if="i <= currentGroup.currentSize">
                <el-avatar :src="getParticipantAvatar(i - 1)" />
                <span class="member-name">{{ getParticipantName(i - 1) }}</span>
              </template>
              <template v-else>
                <el-avatar src="/assets/avatar-placeholder.png" />
                <span class="member-name">等待加入</span>
              </template>
            </div>
          </template>
          <template v-else>
            <div v-for="i in 5" :key="i" class="avatar-slot">
              <el-avatar src="/assets/avatar-placeholder.png" />
              <span class="member-name">等待加入</span>
            </div>
          </template>
        </div>

        <div
          v-if="currentGroup && currentGroup.remainingSeconds !== undefined"
          class="countdown"
        >
          <span class="label">剩余时间：</span>
          <span class="time">{{
            formatTime(currentGroup?.remainingSeconds)
          }}</span>
        </div>

        <template v-if="!currentGroup">
          <div v-if="availableGroups.length > 0" class="available-groups">
            <h3>可加入的拼团：</h3>
            <div
              v-for="group in availableGroups"
              :key="group.id"
              class="group-item"
            >
              <div class="group-info">
                <el-avatar :src="group.leaderAvatar || '/assets/avatar.png'" />
                <span class="leader-name">{{ group.leaderName }}的团</span>
                <span class="member-count"
                  >{{ group.currentMembers }}/{{
                    group.requiredMembers
                  }}人</span
                >
              </div>
              <ElButton type="primary" size="small" @click="joinGroup(group)"
                >加入拼团</ElButton
              >
            </div>
          </div>

          <div class="create-group">
            <ElButton type="success" @click="createGroup">创建拼团</ElButton>
          </div>
        </template>

        <template v-else>
          <div class="invite-code">
            <span class="label">邀请码：</span>
            <span class="code">{{ currentGroup.inviteCode }}</span>
            <ElButton type="primary" size="small" @click="copyInviteCode"
              >复制</ElButton
            >
          </div>
        </template>

        <div v-if="!currentGroup" class="join-group">
          <div class="invite-input">
            <El-input
              v-model="inviteCode"
              type="text"
              placeholder="请输入邀请码"
            ></El-input>
          </div>
          <ElButton type="primary" @click="joinGroupByCode">加入拼团</ElButton>
        </div>
      </div>
    </El-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ElButton,
  ElDialog,
  ElInput,
  ElMessage,
  ElAvatar,
  ElSelect,
  ElOption,
} from "element-plus";
import axios from "axios";
import { useStore } from "vuex";

const API_BASE_URL = "http://localhost:5000";
const paymentPassword = ref("");
const route = useRoute();
const router = useRouter();
const store = useStore();

const product = ref({
  id: null,
  name: "",
  price: 0,
  image: "",
  description: "",
  stock: 0,
});

const optionsVisible = ref(false);
const isPasswordDialogVisible = ref(false);
const isGroupBuyDialogVisible = ref(false);
const currentGroup = ref(null);
const availableGroups = ref([]);
const inviteCode = ref("");
const groupBuyAction = ref(""); // 'create' or 'join'
const timers = ref({});
const buyDialogVisible = ref(false);
const availableCoupons = ref([]);
const selectedCouponId = ref(null);
const isProcessing = ref(false);

// 获取商品详细信息
onMounted(async () => {
  await fetchProductDetails();
  await fetchAvailableGroups();
});

const fetchProductDetails = async () => {
  const productId = route.params.id;
  try {
    const response = await axios.get(
      `${API_BASE_URL}/api/products/${productId}`,
    );
    if (response.data.code === 200) {
      product.value = response.data.data;
      product.value.price = parseFloat(product.value.price) || 0;
    }
  } catch (error) {
    ElMessage.error("获取商品详情失败");
  }
};

const fetchAvailableGroups = async () => {
  try {
    const response = await axios.get(
      `${API_BASE_URL}/api/group-buys/available?productId=${product.value.id}`,
    );
    if (response.data.code === 200) {
      availableGroups.value = response.data.data;
    }
  } catch (error) {
    console.error("获取可用拼团失败:", error);
  }
};

const showGroupBuyDialog = () => {
  isGroupBuyDialogVisible.value = true;
  optionsVisible.value = false;
  fetchAvailableGroups();
};

const createGroup = () => {
  groupBuyAction.value = "create";
  isPasswordDialogVisible.value = true;
};

const joinGroup = (group) => {
  groupBuyAction.value = "join";
  inviteCode.value = group.inviteCode;
  isPasswordDialogVisible.value = true;
};

const joinGroupByCode = () => {
  if (!inviteCode.value) {
    ElMessage.error("请输入邀请码");
    return;
  }
  groupBuyAction.value = "join";
  isPasswordDialogVisible.value = true;
};

const handlePaymentSubmit = async () => {
  console.log("提交支付时的购买方式:", groupBuyAction.value);

  if (!paymentPassword.value) {
    ElMessage.error("请输入支付密码");
    return;
  }

  try {
    const token = localStorage.getItem("token");
    if (!token) {
      ElMessage.error("请先登录");
      return;
    }

    // 获取用户余额
    const userResponse = await axios.get(`${API_BASE_URL}/api/user/balance`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (userResponse.data.code !== 200) {
      throw new Error("获取用户余额失败");
    }

    const userBalance = userResponse.data.data;
    const discountedPrice = product.value.price * 0.8; // 拼团价格（8折）

    // 检查余额是否足够
    if (userBalance < discountedPrice) {
      ElMessage.error(
        `账户余额不足，需要 ¥${discountedPrice.toFixed(2)}，当前余额 ¥${userBalance.toFixed(2)}`,
      );
      return;
    }

    let response;
    if (groupBuyAction.value === "create") {
      response = await axios.post(
        `${API_BASE_URL}/api/group-buys/create`,
        {
          productId: product.value.id,
          paymentPassword: paymentPassword.value,
          paymentAmount: discountedPrice,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (response.data.code === 200) {
        currentGroup.value = response.data.data;
        isPasswordDialogVisible.value = false;
        if (currentGroup.value?.remainingSeconds !== undefined) {
          startCountdown();
        }
        ElMessage.success("创建拼团成功！已支付拼团价格");
      }
    } else if (groupBuyAction.value === "join") {
      response = await axios.post(
        `${API_BASE_URL}/api/group-buys/join`,
        {
          inviteCode: inviteCode.value,
          paymentPassword: paymentPassword.value,
          paymentAmount: discountedPrice,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (response.data.code === 200) {
        currentGroup.value = response.data.data;
        isPasswordDialogVisible.value = false;
        if (currentGroup.value?.remainingSeconds !== undefined) {
          startCountdown();
        }
        ElMessage.success("加入拼团成功！已支付拼团价格");
      }
    } else {
      // 单独购买的处理逻辑
      console.log("开始处理单独购买...");
      response = await axios.post(
        `${API_BASE_URL}/api/orders`,
        {
          userId: store.getters.userId,
          productId: product.value.id,
          quantity: 1,
          paymentAmount: product.value.price,
          paymentPassword: paymentPassword.value,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (response.data.code === 200) {
        isPasswordDialogVisible.value = false;
        paymentPassword.value = "";
        groupBuyAction.value = "";
        ElMessage.success("购买成功！");
        setTimeout(() => {
          router.push("/orders");
        }, 500);
      }
    }
  } catch (error) {
    console.error("支付失败:", error);
    ElMessage.error(error.response?.data?.message || "支付失败，请稍后重试");
  } finally {
    paymentPassword.value = "";
  }
};

const copyInviteCode = () => {
  if (currentGroup.value?.inviteCode) {
    navigator.clipboard
      .writeText(currentGroup.value.inviteCode)
      .then(() => ElMessage.success("邀请码已复制到剪贴板"))
      .catch(() => ElMessage.error("复制失败，请手动复制"));
  }
};

const formatTime = (seconds) => {
  if (seconds == null || isNaN(seconds)) return "00:00:00";
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;
  return `${hours.toString().padStart(2, "0")}:${minutes
    .toString()
    .padStart(2, "0")}:${remainingSeconds.toString().padStart(2, "0")}`;
};

const startCountdown = () => {
  console.log(`开始拼团${currentGroup.value.id}的倒计时`);
  const timer = setInterval(async () => {
    if (!currentGroup.value) {
      clearInterval(timer);
      return;
    }

    // 每30秒更新一次拼团状态
    if (currentGroup.value.remainingSeconds % 30 === 0) {
      await fetchGroupBuyStatus(currentGroup.value.id);
    }

    // 确保 requiredSize 至少为 5
    currentGroup.value.requiredSize = currentGroup.value.requiredSize || 5;

    const currentSize = Number(currentGroup.value.currentSize);
    const requiredSize = Number(currentGroup.value.requiredSize);

    console.log(
      `拼团${currentGroup.value.id}倒计时检查 - 当前人数:${currentSize}, 要求人数:${requiredSize}, 剩余时间:${currentGroup.value.remainingSeconds}`,
    );

    if (currentGroup.value.remainingSeconds <= 0) {
      clearInterval(timer);
      if (currentSize < requiredSize) {
        console.log(
          `拼团${currentGroup.value.id}时间到期且人数未达标，标记为失败`,
        );
        currentGroup.value.status = "FAILED";

        // 处理退款
        try {
          const token = localStorage.getItem("token");
          await axios.post(
            `${API_BASE_URL}/api/group-buys/${currentGroup.value.id}/refund`,
            {},
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            },
          );
          ElMessage.warning("拼团失败，已自动退款到您的账户");
        } catch (error) {
          console.error("退款处理失败:", error);
          ElMessage.error("退款处理失败，请联系客服");
        }
      }
      return;
    }

    if (currentSize === requiredSize) {
      clearInterval(timer);
      console.log(`拼团${currentGroup.value.id}达到人数要求，标记为成功`);
      currentGroup.value.status = "SUCCESS";
      ElMessage.success("拼团成功！");
      return;
    }

    currentGroup.value.remainingSeconds--;
  }, 1000);

  // 保存定时器ID以便在组件销毁时清除
  timers.value[currentGroup.value.id] = timer;
};

const fetchGroupBuyStatus = async (groupId) => {
  try {
    const response = await axios.get(
      `${API_BASE_URL}/api/group-buys/${groupId}`,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      },
    );
    if (response.data.code === 200) {
      currentGroup.value = response.data.data;
    }
  } catch (error) {
    // 静默失败，不显示任何错误信息
  }
};

const resetGroupBuyDialog = () => {
  isGroupBuyDialogVisible.value = false;
  // 不再重置currentGroup，只在实际拼团结束时重置
  inviteCode.value = "";
  groupBuyAction.value = "";
};

// 加入购物车
const addToCart = async () => {
  const quantity = 1;
  const token = localStorage.getItem("token");

  if (!token) {
    ElMessage.error("请先登录");
    return;
  }

  try {
    const response = await axios.post(
      `http://localhost:5000/api/cart?productId=${product.value.id}&quantity=${quantity}`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success(`${product.value.name} 已加入购物车！`);
    } else {
      ElMessage.error("加入购物车失败: " + response.data.message);
    }
  } catch (error) {
    console.error("加入购物车时发生错误:", error);
    ElMessage.error("加入购物车失败，请稍后重试。");
  }
};

// 显示购买选项
const showPurchaseOptions = () => {
  optionsVisible.value = true;
};

// 打开确认购买对话框
const askForPurchaseConfirmation = async () => {
  groupBuyAction.value = "purchase";
  optionsVisible.value = false;
  await showBuyDialog();
};

// 重置密码输入对话框状态
const resetPasswordDialog = () => {
  isPasswordDialogVisible.value = false;
  paymentPassword.value = "";
};

// 在 script setup 部分添加 goHome 方法
const goHome = () => {
  router.push("/");
};

const getParticipantAvatar = (index) => {
  if (!currentGroup.value?.participants) return "/assets/avatar.png";
  const participant = Array.from(currentGroup.value.participants)[index];
  return participant?.avatar || "/assets/avatar.png";
};

const getParticipantName = (index) => {
  if (!currentGroup.value?.participants) return "用户";
  const participant = Array.from(currentGroup.value.participants)[index];
  return participant?.nickname || "用户";
};

const calculateDiscountedPrice = (price, discount) => {
  return (price * discount).toFixed(2);
};

const showBuyDialog = async () => {
  buyDialogVisible.value = true;
  paymentPassword.value = "";
  selectedCouponId.value = null;

  try {
    const response = await axios.get(`${API_BASE_URL}/api/coupons/my`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
    availableCoupons.value = response.data;
  } catch (error) {
    console.error("获取优惠券失败:", error);
  }
};

const confirmPurchase = async () => {
  if (!paymentPassword.value) {
    ElMessage.warning("请输入支付密码");
    return;
  }

  isProcessing.value = true;

  try {
    const token = localStorage.getItem("token");
    if (!token) {
      ElMessage.error("请先登录");
      return;
    }

    // 获取用户余额
    const userResponse = await axios.get(`${API_BASE_URL}/api/user/balance`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (userResponse.data.code !== 200) {
      throw new Error("获取用户余额失败");
    }

    const userBalance = userResponse.data.data;
    const finalPrice = selectedCoupon.value
      ? parseFloat(
          calculateDiscountedPrice(
            product.value.price,
            selectedCoupon.value.discount,
          ),
        )
      : parseFloat(product.value.price);

    // 检查余额是否足够
    if (userBalance < finalPrice) {
      ElMessage.error(
        `账户余额不足，需要 ¥${finalPrice.toFixed(2)}，当前余额 ¥${userBalance.toFixed(2)}`,
      );
      return;
    }

    const orderData = {
      productId: parseInt(product.value.id),
      paymentPassword: paymentPassword.value,
      paymentAmount: finalPrice,
      quantity: 1,
      userId: store.getters.userId,
    };

    // 只有在选择了优惠券时才添加 couponId
    if (selectedCouponId.value) {
      orderData.couponId = parseInt(selectedCouponId.value);
    }

    console.log("发送订单数据:", JSON.stringify(orderData));

    const response = await axios.post(`${API_BASE_URL}/api/orders`, orderData, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    console.log("订单响应:", response.data);

    // 修改判断逻辑：如果返回的数据中包含 id 字段，说明订单创建成功
    if (response.data && response.data.id) {
      ElMessage.success("购买成功！");
      buyDialogVisible.value = false;
      setTimeout(() => {
        router.push("/orders");
      }, 500);
    } else {
      ElMessage.error(response.data.message || "购买失败");
    }
  } catch (error) {
    console.error("购买失败:", error);
    ElMessage.error(error.response?.data?.message || "购买失败，请稍后重试");
  } finally {
    isProcessing.value = false;
  }
};

// 添加计算属性
const selectedCoupon = computed(() => {
  return availableCoupons.value.find((c) => c.id === selectedCouponId.value);
});
</script>

<style scoped>
.product-page {
  max-width: 1200px;
  margin: 40px auto;
  padding: 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

/* 装饰元素 */
.product-page::before {
  content: "";
  position: absolute;
  top: -50px;
  right: -50px;
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border-radius: 50%;
  opacity: 0.1;
  animation: float 15s ease-in-out infinite;
}

.product-page::after {
  content: "";
  position: absolute;
  bottom: -30px;
  left: -30px;
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
  border-radius: 50%;
  opacity: 0.1;
  animation: float 20s ease-in-out infinite reverse;
}

.product-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 30px;
  position: relative;
  z-index: 1;
  animation: slideUp 0.8s ease-out;
  backdrop-filter: blur(10px);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.product-header {
  margin-bottom: 30px;
  text-align: center;
  position: relative;
}

.product-header h1 {
  font-size: 2.5em;
  color: #2c3e50;
  margin-bottom: 15px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  animation: titleGlow 2s ease-in-out infinite;
}

@keyframes titleGlow {
  0% {
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  }
  50% {
    text-shadow: 2px 2px 8px rgba(64, 158, 255, 0.3);
  }
  100% {
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  }
}

.price {
  font-size: 2em;
  color: #f65a8b;
  font-weight: bold;
  margin: 15px 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  animation: pricePulse 2s infinite;
  position: relative;
}

.price::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #f65a8b, transparent);
  animation: priceLine 2s linear infinite;
}

@keyframes priceLine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes pricePulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
    color: #ff4d8f;
  }
  100% {
    transform: scale(1);
  }
}

.back-home-button {
  position: absolute;
  top: 0;
  left: 0;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.back-home-button:hover {
  transform: translateX(-5px) scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.product-content {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  position: relative;
}

.product-image {
  width: 400px;
  height: 400px;
  object-fit: cover;
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.product-image:hover {
  transform: scale(1.02) rotate(1deg);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.product-details {
  flex: 1;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.product-details:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.description {
  font-size: 1.2em;
  line-height: 1.6;
  color: #34495e;
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #409eff;
  position: relative;
  overflow: hidden;
}

.stock {
  font-size: 1.1em;
  color: #606266;
  margin-bottom: 25px;
  padding: 10px 20px;
  background: #f0f9eb;
  border-radius: 8px;
  display: inline-block;
  position: relative;
  overflow: hidden;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.action-buttons .el-button {
  padding: 12px 25px;
  font-size: 1.1em;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.action-buttons .el-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.purchase-options {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  animation: fadeIn 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

.group-buy-tip {
  color: #67c23a;
  font-size: 1.2em;
  margin: 15px 0;
  padding: 10px 20px;
  background: #f0f9eb;
  border-radius: 8px;
  text-align: center;
  animation: glow 2s infinite;
  position: relative;
  overflow: hidden;
}

.group-buy-container {
  padding: 20px;
}

.group-price {
  margin-bottom: 20px;
  text-align: center;
}

.group-price .price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
  margin: 0 10px;
}

.group-price .original-price {
  color: #909399;
  text-decoration: line-through;
}

.member-avatars {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
}

.avatar-slot {
  text-align: center;
}

.member-name {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: #606266;
}

.countdown {
  text-align: center;
  margin: 20px 0;
  font-size: 18px;
}

.countdown .time {
  color: #f56c6c;
  font-weight: bold;
}

.available-groups {
  margin: 20px 0;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.group-item:hover {
  background: #f5f7fa;
  transform: translateX(5px);
}

.group-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.invite-code {
  text-align: center;
  margin: 20px 0;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.invite-code .code {
  font-size: 20px;
  font-weight: bold;
  margin: 0 10px;
  color: #409eff;
  letter-spacing: 2px;
}

.join-group {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.invite-input {
  flex: 1;
}

.buy-dialog-content {
  padding: 20px;
}

.product-summary {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 20px;
}

.product-summary .product-image {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.product-summary .product-image:hover {
  transform: scale(1.1) rotate(5deg);
}

.product-info {
  flex: 1;
}

.product-info h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.3em;
}

.final-price {
  color: #f56c6c;
  font-size: 1.4em;
  font-weight: bold;
  margin: 15px 0;
}

.coupon-section {
  margin: 20px 0;
  padding: 15px;
  background: #f0f9eb;
  border-radius: 8px;
}

.payment-section {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-content {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: auto;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes glow {
  0% {
    box-shadow: 0 0 5px rgba(103, 194, 58, 0.2);
  }
  50% {
    box-shadow: 0 0 20px rgba(103, 194, 58, 0.4);
  }
  100% {
    box-shadow: 0 0 5px rgba(103, 194, 58, 0.2);
  }
}
</style>
