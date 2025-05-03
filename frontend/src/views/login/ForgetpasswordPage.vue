<!--frontend/src/views/login/ForgetpasswordPage-->
<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="logo-title-container">
        <img :src="logo" alt="团团赚拼购" class="logo" />
        <h1 class="system-title">找回密码</h1>
      </div>

      <El-form
        ref="forgetFormRef"
        :model="forgetForm"
        :rules="forgetRules"
        class="login-form"
      >
        <El-form-item prop="username">
          <El-input
            v-model="forgetForm.username"
            prefix-icon="User"
            placeholder="请输入用户名"
            clearable
            @blur="checkUsername"
          />
        </El-form-item>

        <!-- 显示密保问题 -->
        <div v-if="securityQuestion" class="security-question">
          <p>密保问题：{{ securityQuestion }}</p>
          <El-form-item prop="securityAnswer">
            <El-input
              v-model="forgetForm.securityAnswer"
              prefix-icon="Lock"
              placeholder="请输入密保答案"
            />
          </El-form-item>
        </div>

        <El-form-item v-if="isAnswerCorrect" prop="newPassword">
          <El-input
            v-model="forgetForm.newPassword"
            type="password"
            prefix-icon="Lock"
            placeholder="请设置新密码"
            show-password
          />
        </El-form-item>

        <El-form-item v-if="isAnswerCorrect" prop="confirmPassword">
          <El-input
            v-model="forgetForm.confirmPassword"
            type="password"
            prefix-icon="Lock"
            placeholder="确认新密码"
            show-password
          />
        </El-form-item>

        <El-form-item>
          <El-button
            type="primary"
            class="submit-btn"
            :loading="isLoading"
            @click="handleSubmit"
          >
            {{ submitButtonText }}
          </El-button>
        </El-form-item>

        <div class="login-extra">
          <El-link type="primary" @click="goToLogin">返回登录</El-link>
        </div>
      </El-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import axios from "axios";

const API_BASE_URL = "http://localhost:5000";
const logo = "/assets/logo.png";
const router = useRouter();
const forgetFormRef = ref(null);
const isLoading = ref(false);
const securityQuestion = ref("");
const isAnswerCorrect = ref(false);

const forgetForm = ref({
  username: "",
  securityAnswer: "",
  newPassword: "",
  confirmPassword: "",
});

const submitButtonText = computed(() => {
  if (!securityQuestion.value) return "获取密保问题";
  if (!isAnswerCorrect.value) return "验证密保答案";
  return "重置密码";
});

const forgetRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  securityAnswer: [
    { required: true, message: "请输入密保答案", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码不能少于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== forgetForm.value.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 检查用户名并获取密保问题
const checkUsername = async () => {
  if (!forgetForm.value.username) return;

  try {
    const response = await axios.get(
      `${API_BASE_URL}/api/user/security-question/${forgetForm.value.username}`,
    );

    if (response.data.code === 200) {
      securityQuestion.value = response.data.data;
    } else {
      ElMessage.error(response.data.message || "获取密保问题失败");
    }
  } catch (error) {
    console.error("获取密保问题失败:", error);
    ElMessage.error("获取密保问题失败，请重试");
  }
};

// 处理表单提交
const handleSubmit = async () => {
  if (!forgetFormRef.value) return;

  await forgetFormRef.value.validate(async (valid) => {
    if (!valid) return;

    isLoading.value = true;
    try {
      if (!securityQuestion.value) {
        await checkUsername();
      } else if (!isAnswerCorrect.value) {
        // 验证密保答案
        const response = await axios.post(
          `${API_BASE_URL}/api/user/verify-security-answer`,
          {
            username: forgetForm.value.username,
            answer: forgetForm.value.securityAnswer,
          },
        );

        if (response.data.code === 200) {
          isAnswerCorrect.value = true;
          ElMessage.success("密保答案正确，请设置新密码");
        } else {
          ElMessage.error("密保答案错误");
        }
      } else {
        // 重置密码
        const response = await axios.post(
          `${API_BASE_URL}/api/user/reset-password`,
          {
            username: forgetForm.value.username,
            newPassword: forgetForm.value.newPassword,
            securityAnswer: forgetForm.value.securityAnswer,
          },
        );

        if (response.data.code === 200) {
          ElMessage.success("密码重置成功，请重新登录");
          router.push("/login");
        } else {
          ElMessage.error(response.data.message || "密码重置失败");
        }
      }
    } catch (error) {
      console.error("操作失败:", error);
      ElMessage.error("操作失败，请重试");
    } finally {
      isLoading.value = false;
    }
  });
};

const goToLogin = () => {
  router.push("/login");
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
  color: #333;
  margin: 0;
}

.login-form {
  margin-top: 20px;
}

.submit-btn {
  width: 100%;
}

.login-extra {
  margin-top: 15px;
  text-align: center;
}

.security-question {
  margin: 15px 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.security-question p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}
</style>
