<template>
  <div class="cart-page">
    <h1>购物车</h1>
    <El-table :data="cartItems" border>
      <El-table-column prop="image" label="商品图片" width="150">
        <template #default="scope">
          <img
            :src="scope.row.cartProduct.imageUrl"
            alt="商品图片"
            class="cart-image"
          />
        </template>
      </El-table-column>
      <El-table-column prop="name" label="商品名称">
        <template #default="scope">
          <span class="product-name">{{ scope.row.cartProduct.name }}</span>
        </template>
      </El-table-column>
      <El-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          <span class="product-price"
            >¥{{ parseFloat(scope.row.cartProduct.price).toFixed(2) }}</span
          >
        </template>
      </El-table-column>
      <El-table-column prop="quantity" label="数量" width="160">
        <template #default="scope">
          <El-input-number
            v-model="scope.row.quantity"
            :min="1"
            :max="scope.row.cartProduct.stock"
            @change="updateQuantity(scope.row)"
          />
        </template>
      </El-table-column>
      <El-table-column label="操作" width="100">
        <template #default="scope">
          <El-button type="danger" @click="removeItem(scope.row.product_id)"
            >删除</El-button
          >
        </template>
      </El-table-column>
    </El-table>

    <div class="cart-total">
      <h2>
        总计: <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
      </h2>
      <div class="button-group">
        <El-button class="checkout-button" type="primary" @click="checkout"
          >去结算</El-button
        >
        <El-button type="default" @click="goHome">返回首页</El-button>
      </div>
    </div>

    <p v-if="!cartItems.length" class="empty-cart">购物车是空的</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios"; // 导入 axios
import { useRouter } from "vue-router"; // 导入 vue-router
import { ElMessage, ElMessageBox } from "element-plus"; // 导入提示消息组件和消息框组件

const cartItems = ref([]); // 初始化购物车项

// 获取路由实例
const router = useRouter();

// 总计计算
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => {
    const price = parseFloat(item.cartProduct.price) || 0; // 确保 price 为数字
    const quantity = parseInt(item.quantity) || 0; // 确保 quantity 为数字
    return total + price * quantity;
  }, 0);
});

// 获取购物车数据的函数
const fetchCartItems = async () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) {
      console.error("未登录");
      cartItems.value = [];
      return;
    }

    const response = await axios.get("http://localhost:5000/api/cart", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.data.code === 200 && Array.isArray(response.data.data)) {
      cartItems.value = response.data.data.map((item) => ({
        id: item.id,
        product_id: item.productId,
        quantity: item.quantity,
        cartProduct: {
          id: item.productId,
          name: item.productName,
          price: item.price,
          imageUrl: item.imageUrl,
        },
      }));
    } else {
      cartItems.value = [];
    }
  } catch (error) {
    console.error("获取购物车数据失败:", error);
    cartItems.value = [];
  }
};

// 组件加载时获取数据
onMounted(() => {
  fetchCartItems();
});

// 更新商品数量
const updateQuantity = async (item) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) {
      ElMessage.error("请先登录");
      return;
    }

    const response = await axios.put(
      `http://localhost:5000/api/cart?productId=${item.product_id}&quantity=${item.quantity}`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success("数量更新成功");
      fetchCartItems(); // 重新获取购物车数据
    } else {
      ElMessage.error("数量更新失败: " + response.data.message);
    }
  } catch (error) {
    console.error("更新数量失败:", error);
    ElMessage.error(
      "更新数量失败: " + (error.response?.data?.message || error.message),
    );
  }
};

// 删除商品
const removeItem = async (productId) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) {
      ElMessage.error("请先登录");
      return;
    }

    const response = await axios.delete(
      `http://localhost:5000/api/cart?productId=${productId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data.code === 200) {
      cartItems.value = cartItems.value.filter(
        (item) => item.product_id !== productId,
      );
      ElMessage.success("商品删除成功");
    } else {
      ElMessage.error("商品删除失败");
    }
  } catch (error) {
    console.error("删除商品失败:", error);
    ElMessage.error("删除商品失败");
  }
};

// 返回首页功能
const goHome = () => {
  router.push("/");
};

// 结算功能
const checkout = async () => {
  if (!cartItems.value.length) {
    ElMessage.warning("购物车是空的");
    return;
  }

  try {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    if (!token || !userId) {
      ElMessage.error("请先登录");
      return;
    }

    // 显示支付密码输入框
    const paymentPassword = await ElMessageBox.prompt(
      "请输入支付密码",
      "支付确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputType: "password",
        inputPattern: /.+/,
        inputErrorMessage: "支付密码不能为空",
      },
    ).catch(() => {
      return { action: "cancel" };
    });

    if (paymentPassword.action === "cancel") {
      return;
    }

    // 为每个商品创建订单
    for (const item of cartItems.value) {
      const orderData = {
        productId: item.product_id,
        paymentPassword: paymentPassword.value,
      };

      console.log("创建订单数据:", { ...orderData, paymentPassword: "***" });

      try {
        const response = await axios.post(
          "http://localhost:5000/api/orders",
          orderData,
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          },
        );

        // 检查响应是否存在
        if (!response || !response.data) {
          throw new Error("服务器返回数据为空");
        }

        // 订单创建成功，继续处理下一个商品
        console.log("订单创建成功:", response.data);
      } catch (error) {
        console.error("创建订单失败:", error);

        // 获取错误信息
        const errorResponse = error.response?.data;
        const errorMessage =
          errorResponse?.message || error.message || "创建订单失败";

        // 根据后端的错误码显示不同的错误信息
        if (errorResponse?.code === "USER_NOT_FOUND") {
          ElMessage.error("用户信息无效，请重新登录");
          router.push("/login");
          return;
        } else if (errorResponse?.code === "INVALID_PAYMENT_PASSWORD") {
          ElMessage.error("支付密码错误，请重试");
          return;
        } else if (errorResponse?.code === "INSUFFICIENT_BALANCE") {
          ElMessage.error("账户余额不足，请充值后重试");
          return;
        } else if (errorResponse?.code === "PRODUCT_NOT_FOUND") {
          ElMessage.error(`商品 ${item.cartProduct.name} 已下架或不存在`);
          return;
        } else {
          ElMessage.error(errorMessage);
          return;
        }
      }
    }

    ElMessage.success("所有订单创建成功！");
    // 清空购物车
    await clearCart();
    // 跳转到订单页面
    router.push("/orders");
  } catch (error) {
    console.error("结算时发生错误:", error);
    ElMessage.error(
      error.response?.data?.message || error.message || "结算失败，请稍后重试",
    );
  }
};

// 清空购物车
const clearCart = async () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) return;

    await axios.delete("http://localhost:5000/api/cart/clear", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  } catch (error) {
    console.error("清空购物车失败:", error);
  }
};
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 30px;
  background: linear-gradient(135deg, #f5f7fa, #c3cfe2);
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.cart-page::before {
  content: "🛒";
  position: absolute;
  top: -30px;
  right: -30px;
  font-size: 150px;
  opacity: 0.1;
  transform: rotate(15deg);
}

.cart-page::after {
  content: "💰";
  position: absolute;
  bottom: -20px;
  left: -20px;
  font-size: 120px;
  opacity: 0.1;
  transform: rotate(-15deg);
}

h1 {
  text-align: center;
  color: #2c3e50;
  font-size: 2.5em;
  margin-bottom: 30px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
}

h1::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, #4caf50, #2196f3);
  border-radius: 3px;
}

.cart-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  transition: transform 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.cart-image:hover {
  transform: scale(1.1);
}

.product-name {
  font-weight: bold;
  color: #2c3e50;
  transition: color 0.3s ease;
}

.product-name:hover {
  color: #4caf50;
}

.product-price {
  color: #e94e77;
  font-weight: bold;
  font-size: 1.1em;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.cart-total::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #4caf50, #2196f3);
}

.total-price {
  font-size: 28px;
  color: #f65a8b;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.button-group {
  display: flex;
  gap: 15px;
}

.checkout-button {
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.checkout-button::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition:
    width 0.6s ease,
    height 0.6s ease;
}

.checkout-button:hover::before {
  width: 300px;
  height: 300px;
}

.empty-cart {
  text-align: center;
  color: #666;
  font-size: 1.2em;
  padding: 40px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  margin-top: 20px;
  position: relative;
}

.empty-cart::before {
  content: "🛍️";
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
  opacity: 0.5;
}

:deep(.el-table) {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #4caf50, #2196f3);
  color: white;
  font-weight: bold;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(76, 175, 80, 0.05);
}

:deep(.el-input-number) {
  width: 120px;
}

:deep(.el-button--danger) {
  transition: all 0.3s ease;
}

:deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}
</style>
