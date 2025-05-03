/* eslint-disable prettier/prettier */
/* eslint-disable quotes */
//frontend/utils/request.js
import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";


console.log("Current environment:", process.env.NODE_ENV);

// 创建axios实例
const service = axios.create({
  baseURL: "http://localhost:5000",  // 修改为本地开发环境地址
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // 允许携带凭证
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    console.log("发送请求配置:", {
      url: config.url,
      method: config.method,
      baseURL: config.baseURL,
      fullPath: config.baseURL + config.url,
      data: config.data,
      headers: config.headers,
    });

    // 获取token
    const token = localStorage.getItem("token");

    // 如果有token，则添加到请求头
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
      console.log("添加token到请求头:", token);
    } else {
      console.warn("未找到token");
    }

    // 如果是文件上传请求，设置正确的Content-Type
    if (config.data instanceof FormData) {
      config.headers["Content-Type"] = "multipart/form-data";
    }

    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  },
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    console.log("收到响应:", response);
    // 如果响应成功，直接返回响应的data部分
    return response.data;
  },
  (error) => {
    console.error("响应错误:", error);
    console.error("错误详情:", {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      config: error.config,
    });

    if (!error.response) {
      ElMessage.error("网络错误，请检查您的网络连接");
      return Promise.reject(new Error("网络错误，请检查您的网络连接"));
    }

    // 处理HTTP错误
    const status = error.response.status;
    let errorMessage = error.response.data?.message || error.message;

    switch (status) {
      case 401:
        errorMessage = "登录已过期，请重新登录";
        localStorage.removeItem("token");
        router.push("/login");
        break;
      case 403:
        errorMessage = "没有权限访问";
        break;
      case 404:
        errorMessage = "请求的资源不存在";
        break;
      case 500:
        errorMessage = "服务器内部错误";
        break;
    }

    ElMessage.error(errorMessage);
    return Promise.reject(new Error(errorMessage));
  },
);

export default service;
