/* eslint-disable */
<!--frontend/ src/views/login/RegisterPage.vue -->
<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-form-container">
        <div class="register-header">
          <img :src="logo" alt="团团赚拼购" class="logo" />
          <h2>用户注册</h2>
        </div>

        <El-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
        >
          <El-form-item prop="username">
            <El-input
              v-model="registerForm.username"
              placeholder="用户名"
              clearable
            >
              <template #prefix>
                <El-icon><User /></El-icon>
              </template>
            </El-input>
          </El-form-item>

          <El-form-item prop="nickname">
            <El-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              clearable
            >
              <template #prefix>
                <El-icon><User /></El-icon>
              </template>
            </El-input>
          </El-form-item>

          <El-form-item prop="password">
            <El-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              show-password
            >
              <template #prefix>
                <El-icon><Lock /></El-icon>
              </template>
            </El-input>
          </El-form-item>

          <El-form-item prop="confirmPassword">
            <El-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              show-password
            >
              <template #prefix>
                <El-icon><Lock /></El-icon>
              </template>
            </El-input>
          </El-form-item>

          <El-form-item prop="paymentPassword">
            <El-input
              v-model="registerForm.paymentPassword"
              type="password"
              placeholder="支付密码"
              show-password
            >
              <template #prefix>
                <El-icon><Lock /></El-icon>
              </template>
            </El-input>
          </El-form-item>

          <El-form-item>
            <El-button
              type="primary"
              class="register-btn"
              round
              block
              @click="handleRegister"
            >
              注 册
            </El-button>
          </El-form-item>

          <div class="register-footer">
            <El-link type="primary" @click="$router.push('/login')">
              已有账号？立即登录
            </El-link>
          </div>
        </El-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import request from "@/utils/request";

const logo = "/assets/logo.png";
const router = useRouter();

const registerFormRef = ref(null);
const registerForm = ref({
  username: "",
  nickname: "",
  password: "",
  confirmPassword: "",
  paymentPassword: "",
});

const registerRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  nickname: [{ required: true, message: "请输入昵称", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少需要6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error("两次密码输入不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  paymentPassword: [
    { required: true, message: "请输入支付密码", trigger: "blur" },
    { min: 6, message: "支付密码至少需要6个字符", trigger: "blur" },
  ],
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  try {
    const valid = await registerFormRef.value.validate();
    if (!valid) {
      ElMessage.warning("请填写完整的注册信息");
      return;
    }

    const response = await request({
      url: "/api/auth/register",
      method: "post",
      data: {
        username: registerForm.value.username,
        password: registerForm.value.password,
        nickname: registerForm.value.nickname,
        paymentPassword: registerForm.value.paymentPassword,
      },
    });

    console.log("注册响应:", response);

    if (response.code === 200) {
      // 显示成功提示
      ElMessage({
        message: "注册成功！正在跳转到登录页面...",
        type: "success",
        duration: 2000,
        showClose: true,
        center: true,
      });

      // 延迟跳转
      setTimeout(() => {
        router.push("/login");
      }, 2000);
    } else {
      throw new Error(response.message || "注册失败");
    }
  } catch (error) {
    console.error("注册错误:", error);
    ElMessage.error(
      error.response?.data?.message || error.message || "注册失败，请稍后重试",
    );
  }
};
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f3f4f6, #e4e7ec);
}

.register-wrapper {
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 400px;
}

.register-header {
  margin-bottom: 20px;
  text-align: center;
}

.logo {
  width: 80px;
}

.register-form {
  max-width: 350px;
  margin: 0 auto;
}

.register-btn {
  margin-top: 20px;
  padding: 10px;
  font-size: 16px;
}

.register-footer {
  margin-top: 20px;
  text-align: center;
}
</style>
