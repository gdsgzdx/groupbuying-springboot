import request from "@/utils/request";

// 用户登录
export const login = (data) => {
  return request({
    url: "/api/auth/login",
    method: "post",
    data,
  });
};

// 用户注册
export const register = (data) => {
  return request({
    url: "/api/auth/register",
    method: "post",
    data,
  });
};

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: "/api/auth/info",
    method: "get",
  });
};

// 修改用户信息
export const updateUserInfo = (data) => {
  return request({
    url: "/api/auth/update",
    method: "put",
    data,
  });
};

// 修改密码
export const changePassword = (data) => {
  return request({
    url: "/api/auth/change-password",
    method: "post",
    data,
  });
};

// 重置密码
export const resetPassword = (data) => {
  return request({
    url: "/api/auth/reset-password",
    method: "post",
    data,
  });
};

// 发送验证码
export const sendVerificationCode = (phone) => {
  return request({
    url: "/api/auth/send-verification-code",
    method: "post",
    data: { phone },
  });
};

// 验证支付密码
export const verifyPaymentPassword = (password) => {
  return request({
    url: "/api/auth/check-payment-password",
    method: "post",
    data: { password },
  });
};

// 更新用户资料
export const updateUserProfile = (data) => {
  return request({
    url: "/api/user/profile",
    method: "put",
    data,
  });
};
