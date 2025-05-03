// frontend/src/store/userStore.js
import { reactive } from "vue";
import * as userService from "@/services/userService";
import request from "@/utils/request";

const state = reactive({
  user: JSON.parse(localStorage.getItem("userStore") || "{}"),
  token: localStorage.getItem("token"),
});

// 定义 mutations
const mutations = {
  setUser(state, userData) {
    state.user = userData;
    localStorage.setItem("userStore", JSON.stringify(userData));
  },
  setToken(state, token) {
    state.token = token;
    localStorage.setItem("token", token);
  },
  clearUser(state) {
    state.user = {};
    state.token = null;
    localStorage.removeItem("userStore");
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
  },
  setUserAvatar(state, avatarUrl) {
    if (state.user) {
      state.user.avatar = avatarUrl;
      localStorage.setItem("userStore", JSON.stringify(state.user));
    }
  },
  UPDATE_USER_PROFILE(state, { nickname, phone, address }) {
    if (state.user) {
      state.user.nickname = nickname;
      state.user.phone = phone;
      state.user.address = address;
    }
  },
};

// 定义 actions
const actions = {
  async register(_, registrationData) {
    try {
      const response = await request({
        url: "/api/auth/register",
        method: "post",
        data: registrationData,
      });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "注册失败");
    }
  },

  async login({ commit }, loginData) {
    try {
      const response = await request({
        url: "/api/auth/login",
        method: "post",
        data: loginData,
      });

      if (response.code === 200 && response.data) {
        // 从响应中获取token和用户信息
        const { token, user } = response.data;

        // 保存token
        commit("setToken", token);

        // 保存用户信息
        if (user) {
          commit("setUser", user);
          if (user.id) {
            localStorage.setItem("userId", user.id);
          }
        }

        // 设置请求头的Authorization
        request.defaults.headers.common["Authorization"] = `Bearer ${token}`;

        return { token, user };
      } else {
        throw new Error(response.message || "登录失败");
      }
    } catch (error) {
      console.error("登录失败:", error);
      throw error;
    }
  },

  async getUserInfo({ commit }) {
    try {
      const response = await userService.getUserInfo();
      commit("setUser", response.data);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "获取用户信息失败");
    }
  },

  async updateUserInfo({ commit }, userData) {
    try {
      const response = await userService.updateUserInfo(userData);
      commit("setUser", response.data);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "更新用户信息失败");
    }
  },

  async changePassword(_, passwordData) {
    try {
      const response = await userService.changePassword(passwordData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "修改密码失败");
    }
  },

  async resetPassword(_, resetData) {
    try {
      const response = await userService.resetPassword(resetData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "重置密码失败");
    }
  },

  async sendVerificationCode(_, phone) {
    try {
      const response = await userService.sendVerificationCode(phone);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "发送验证码失败");
    }
  },

  async verifyPaymentPassword(_, password) {
    try {
      const response = await userService.verifyPaymentPassword(password);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || "支付密码验证失败");
    }
  },

  logout({ commit }) {
    commit("clearUser");
  },

  async updateUserProfile({ commit }, { nickname, phone, address }) {
    const response = await userService.updateUserProfile({
      nickname,
      phone,
      address,
    });
    if (response.code === 200) {
      commit("UPDATE_USER_PROFILE", { nickname, phone, address });
      return response;
    }
    throw new Error(response.message || "更新资料失败");
  },
};

// 定义 getters
const getters = {
  isAuthenticated: (state) => !!state.token,
  user: (state) => state.user,
  userId: (state) => state.user.id,
  userPhoneNumber: (state) => state.user.phone,
  userBalance: (state) => state.user.accountBalance,
  userNickname: (state) => state.user.nickname || "用户",
  userAvatar: (state) => {
    if (!state.user || !state.user.avatar) {
      return "/assets/avatar.png";
    }
    return state.user.avatar;
  },
  userAddress: (state) => state.user.address || "",
};

// 导出对象
export default {
  state,
  mutations,
  actions,
  getters,
};
