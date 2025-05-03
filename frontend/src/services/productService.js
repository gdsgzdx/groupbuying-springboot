//frontend/src/services/productService.js

import request from "@/utils/request";

// 获取所有产品（支持分页）
export const getProducts = (page = 0, size = 20, sort = "id,desc") => {
  return request({
    url: "/api/products",
    method: "get",
    params: { page, size, sort },
  });
};

// 创建新产品
export const createProduct = (productData) => {
  return request({
    url: "/api/products",
    method: "post",
    data: productData,
  });
};

// 获取特定产品
export const getProductById = (id) => {
  return request({
    url: `/api/products/${id}`,
    method: "get",
  });
};

// 更新产品信息
export const updateProduct = (id, updatedData) => {
  return request({
    url: `/api/products/${id}`,
    method: "put",
    data: updatedData,
  });
};

// 删除特定产品
export const deleteProduct = (id) => {
  return request({
    url: `/api/products/${id}`,
    method: "delete",
  });
};

// 按类别获取产品
export const getProductsByCategory = (
  category,
  page = 0,
  size = 20,
  sort = "id,desc",
) => {
  const encodedCategory = encodeURIComponent(category);
  return request({
    url: `/api/products/category/${encodedCategory}`,
    method: "get",
    params: { page, size, sort },
  });
};

// 搜索产品
export const searchProducts = (
  keyword,
  page = 0,
  size = 20,
  sort = "id,desc",
) => {
  return request({
    url: "/api/products/search",
    method: "get",
    params: { keyword, page, size, sort },
  });
};
