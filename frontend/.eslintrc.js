// frontend/.eslintrc.js
module.exports = {
  env: {
    browser: true, // 浏览器环境
    node: true, // 确保启用 Node.js 环境
    es2021: true, // 启用 ECMAScript 2021
  },
  extends: [
    "eslint:recommended", // 使用 ESLint 推荐的规则
    "plugin:vue/vue3-recommended", // Vue 3 推荐的规则
    "plugin:prettier/recommended", // 如果使用 Prettier，需要这行
    "prettier", // 确保 ESLint 与 Prettier 兼容
  ],
  parserOptions: {
    ecmaVersion: 12, // 使用 ECMAScript 12
    sourceType: "module", // 启用 ES6 模块
  },
  rules: {
    quotes: ["error", "double"], // 使用双引号
    "vue/no-unused-vars": "warn", // Vue 中未使用的变量警告
  },
};
