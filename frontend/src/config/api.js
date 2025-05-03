// API配置
export const API_BASE_URL = 'http://localhost:5000';

// 导出完整的API URL
export const API_URLS = {
  // 用户相关
  USER_UPLOAD_AVATAR: `${API_BASE_URL}/api/user/upload-avatar`,
  USER_INFO: `${API_BASE_URL}/api/user/info`,
  
  // 订单相关
  ORDERS: `${API_BASE_URL}/api/orders`,
  
  // 购物车相关
  CART: `${API_BASE_URL}/api/cart`,
  CART_CLEAR: `${API_BASE_URL}/api/cart/clear`,
  
  // 商品相关
  PRODUCTS: `${API_BASE_URL}/api/products`,
  
  // 其他API...
}; 