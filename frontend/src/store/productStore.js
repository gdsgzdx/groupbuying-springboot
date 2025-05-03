// store/modules/product.js
import {
  getProducts,
  createProduct,
  getProductById,
  updateProduct,
  deleteProduct,
  getProductsByCategory,
  searchProducts,
} from "@/services/productService.js";

const state = {
  products: [],
  product: null,
  error: null,
};

const mutations = {
  SET_PRODUCTS(state, products) {
    state.products = products.content || []; // 从分页响应中提取产品列表
    console.log("设置的产品数据:", state.products);
  },
  SET_PRODUCT(state, product) {
    state.product = product;
  },
  SET_ERROR(state, error) {
    state.error = error;
  },
};

const actions = {
  async FetchProducts({ commit }) {
    try {
      const response = await getProducts(); // 使用正确的 API 方法
      console.log("API响应:", response);
      if (response.code === 200 && response.data) {
        commit("SET_PRODUCTS", response.data); // 提交到mutation更新状态
      } else {
        throw new Error(response.message || "获取产品数据失败");
      }
    } catch (error) {
      commit("SET_ERROR", error.message); // 处理错误
      console.error("获取产品数据失败:", error);
    }
  },

  async FetchProductsByCategory({ commit }, category) {
    try {
      const response = await getProductsByCategory(category);
      console.log("分类产品API响应:", response);
      if (response.code === 200 && response.data) {
        commit("SET_PRODUCTS", response.data); // 提交到mutation更新状态
      } else {
        throw new Error(response.message || "获取分类产品数据失败");
      }
    } catch (error) {
      commit("SET_ERROR", error.message); // 处理错误
      console.error("获取分类产品数据失败:", error);
    }
  },

  async SearchProducts({ commit }, keyword) {
    try {
      const response = await searchProducts(keyword);
      console.log("搜索产品API响应:", response);
      if (response.code === 200 && response.data) {
        commit("SET_PRODUCTS", response.data);
      } else {
        throw new Error(response.message || "搜索产品失败");
      }
    } catch (error) {
      commit("SET_ERROR", error.message);
      console.error("搜索产品失败:", error);
    }
  },

  async createNewProduct({ commit }, productData) {
    try {
      const newProduct = await createProduct(productData); // 调用服务创建新产品
      commit("SET_PRODUCT", newProduct); // 提交到mutation更新状态
    } catch (error) {
      commit("SET_ERROR", error.message); // 处理错误
    }
  },

  async fetchProductById({ commit }, id) {
    try {
      const product = await getProductById(id); // 调用服务获取特定产品
      commit("SET_PRODUCT", product); // 提交到mutation更新状态
    } catch (error) {
      commit("SET_ERROR", error.message); // 处理错误
    }
  },

  async updateProduct({ commit }, { id, updatedData }) {
    try {
      const updatedProduct = await updateProduct(id, updatedData); // 调用服务更新产品信息
      commit("SET_PRODUCT", updatedProduct); // 提交到mutation更新状态
    } catch (error) {
      commit("SET_ERROR", error.message); // 处理错误
    }
  },

  async deleteProduct({ commit }, id) {
    try {
      await deleteProduct(id); // 调用服务删除特定产品
      commit(
        "SET_PRODUCTS",
        state.products.filter((product) => product.id !== id),
      ); // 更新状态，移除已删除的产品
    } catch (error) {
      commit("SET_ERROR", error.message); // 处理错误
    }
  },
};

const getters = {
  allProducts(state) {
    return state.products;
  },
  currentProduct(state) {
    return state.product;
  },
  error(state) {
    return state.error;
  },
};

export default {
  state,
  mutations,
  actions,
  getters,
};
