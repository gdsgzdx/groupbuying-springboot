<!-- frontend/src/views/login/LoginPage.vue -->
<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="logo-title-container">
        <img :src="logo" alt="团团赚拼购" class="logo" />
        <h1 class="system-title">团团赚拼购</h1>
      </div>

      <El-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <El-form-item prop="username">
          <El-input
            v-model="loginForm.username"
            prefix-icon="User"
            placeholder="用户名"
            clearable
          />
        </El-form-item>

        <El-form-item prop="password">
          <El-input
            v-model="loginForm.password"
            type="password"
            prefix-icon="Lock"
            placeholder="密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </El-form-item>

        <El-form-item>
          <El-button
            type="primary"
            class="login-btn"
            :loading="isLoading"
            :disabled="isLoading"
            @click="handleLogin"
          >
            登录
          </El-button>
        </El-form-item>

        <div class="login-extra">
          <El-link type="primary" @click="handleRegister">立即注册</El-link>
          <El-link type="info" @click="handleForgotPassword">忘记密码</El-link>
        </div>
      </El-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { ElMessage } from "element-plus";

const logo = "/assets/logo.png";
const loginFormRef = ref(null);
const router = useRouter();
const store = useStore();

// 登录表单数据
const loginForm = reactive({
  username: "",
  password: "",
});

// 表单验证规则
const loginRules = {
  username: [
    {
      required: true,
      message: "请输入用户名",
      trigger: "blur",
    },
  ],
  password: [
    {
      required: true,
      message: "请输入密码",
      trigger: "blur",
    },
    {
      min: 6,
      message: "密码不能少于6位",
      trigger: "blur",
    },
  ],
};

// 加载状态
const isLoading = ref(false);

// 登录处理
const handleLogin = () => {
  if (!loginFormRef.value) return;

  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        isLoading.value = true;
        const loginData = {
          username: loginForm.username,
          password: loginForm.password,
        };
        const result = await store.dispatch("login", loginData);
        if (result && result.user) {
          ElMessage.success(
            `欢迎回来，${result.user.nickname || result.user.username}`,
          );
          router.push("/home");
        }
      } catch (error) {
        console.error("登录失败:", error);
        ElMessage.error(error.message || "登录失败，请检查用户名和密码");
      } finally {
        isLoading.value = false;
      }
    } else {
      ElMessage.warning("请填写完整的登录信息");
      return false;
    }
  });
};

// 注册处理
const handleRegister = () => {
  router.push({ name: "Register" });
};

// 找回密码处理
const handleForgotPassword = () => {
  router.push({ name: "ForgetPassword" });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to right, #3494e6, #ec6ead);
}

.login-wrapper {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 400px;
}

.logo-title-container {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 10px;
}

.system-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-extra {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}
</style>
