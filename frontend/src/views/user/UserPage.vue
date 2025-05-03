<template>
  <div class="user-page">
    <div class="back-home">
      <el-button type="primary" class="back-home-btn" @click="goToHome"
        >返回首页</el-button
      >
    </div>
    <h1 class="page-title">用户中心</h1>
    <p class="welcome-message">欢迎回来，{{ userNickname }}</p>

    <div class="user-info card">
      <h2>个人信息</h2>
      <img
        v-if="userAvatar"
        :src="userAvatar"
        alt="用户头像"
        class="user-avatar"
      />
      <ul>
        <li>昵称：{{ userNickname }}</li>
        <li>手机：{{ userPhone }}</li>
        <li>地址：{{ userAddress }}</li>
        <li>
          账户余额：¥{{ userBalance.toFixed(2)
          }}<span class="balance-warning"
            >（账户余额变更后需要退出账号重新登录才能刷新）</span
          >
        </li>
      </ul>
    </div>

    <div class="user-actions">
      <El-button type="primary" class="action-button" @click="showEditDialog"
        >编辑资料</El-button
      >
      <El-button type="success" class="action-button" @click="showPostDialog"
        >发表帖子</El-button
      >
      <El-button type="info" class="action-button" @click="showSecurityDialog"
        >设置密保</El-button
      >
      <El-button type="danger" class="action-button" @click="logout"
        >注销登录</El-button
      >
    </div>

    <!-- 编辑资料对话框 -->
    <El-dialog
      v-model="editDialogVisible"
      title="编辑资料"
      width="500px"
      :close-on-click-modal="false"
    >
      <El-form :model="editForm" label-width="80px">
        <El-form-item label="头像">
          <div class="avatar-upload">
            <img
              v-if="editForm.avatar"
              :src="editForm.avatar"
              class="avatar-preview"
              alt="用户头像"
            />
            <El-upload
              class="avatar-uploader"
              action="http://localhost:5000/api/user/upload-avatar"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
              :with-credentials="true"
            >
              <El-button type="primary">上传头像</El-button>
            </El-upload>
          </div>
        </El-form-item>

        <El-form-item label="昵称">
          <El-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </El-form-item>

        <El-form-item label="手机">
          <El-input v-model="editForm.phone" placeholder="请输入手机号码" />
        </El-form-item>

        <El-form-item label="地址">
          <El-input v-model="editForm.address" placeholder="请输入地址" />
        </El-form-item>
      </El-form>
      <template #footer>
        <span class="dialog-footer">
          <El-button @click="editDialogVisible = false">取消</El-button>
          <El-button type="primary" @click="saveProfile">保存</El-button>
        </span>
      </template>
    </El-dialog>

    <!-- 发表帖子对话框 -->
    <El-dialog
      v-model="postDialogVisible"
      title="发表帖子"
      width="500px"
      :close-on-click-modal="false"
    >
      <El-form :model="postForm" label-width="80px">
        <El-form-item label="内容">
          <El-input
            v-model="postForm.content"
            type="textarea"
            :rows="4"
            placeholder="写下你想分享的内容..."
          />
        </El-form-item>
      </El-form>
      <template #footer>
        <span class="dialog-footer">
          <El-button @click="postDialogVisible = false">取消</El-button>
          <El-button type="primary" @click="submitPost">发表</El-button>
        </span>
      </template>
    </El-dialog>

    <!-- 设置密保对话框 -->
    <El-dialog
      v-model="securityDialogVisible"
      title="设置密保"
      width="500px"
      :close-on-click-modal="false"
    >
      <El-form
        ref="securityFormRef"
        :model="securityForm"
        :rules="securityRules"
        label-width="100px"
      >
        <El-form-item label="密保问题" prop="question">
          <El-select
            v-model="securityForm.question"
            placeholder="请选择密保问题"
          >
            <El-option label="您的出生地是？" value="您的出生地是？" />
            <El-option label="您的母亲姓名是？" value="您的母亲姓名是？" />
            <El-option label="您的小学校名是？" value="您的小学校名是？" />
            <El-option label="您的宠物名字是？" value="您的宠物名字是？" />
            <El-option label="您最喜欢的颜色是？" value="您最喜欢的颜色是？" />
          </El-select>
        </El-form-item>
        <El-form-item label="密保答案" prop="answer">
          <El-input
            v-model="securityForm.answer"
            type="text"
            placeholder="请输入密保答案"
          />
        </El-form-item>
      </El-form>
      <template #footer>
        <El-button @click="securityDialogVisible = false">取消</El-button>
        <El-button type="primary" @click="submitSecurityForm">确认</El-button>
      </template>
    </El-dialog>

    <div class="user-community card">
      <h2>社区互动</h2>
      <div v-if="posts.length === 0">暂无帖子。</div>
      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item">
          <div class="post-header">
            <img
              v-if="post.userAvatar"
              :src="`${API_BASE_URL}${post.userAvatar}`"
              alt="用户头像"
              class="post-user-avatar"
            />
            <span class="post-username">{{ post.username }}</span>
            <span class="post-time">{{ formatDate(post.createdAt) }}</span>
          </div>
          <div class="post-content">{{ post.content }}</div>
          <div class="post-actions">
            <el-button
              class="like-button"
              :type="post.liked ? 'primary' : 'default'"
              @click="toggleLike(post.id)"
            >
              <i class="el-icon-star-off"></i>
              {{ post.likes }} 赞
            </el-button>
            <el-button
              class="comment-button"
              type="text"
              @click="showCommentInput(post.id)"
            >
              <i class="el-icon-chat-dot-round"></i>
              评论
            </el-button>
            <el-button
              v-if="post.userId === userId"
              class="delete-button"
              type="danger"
              text
              @click="deletePost(post.id)"
            >
              <i class="el-icon-delete"></i>
              删除
            </el-button>
          </div>
          <!-- 评论输入框 -->
          <div v-if="post.showCommentInput" class="comment-section">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="2"
              placeholder="写下你的评论..."
            />
            <el-button
              type="primary"
              class="submit-comment"
              @click="submitComment(post.id)"
            >
              提交评论
            </el-button>
          </div>
          <!-- 评论列表 -->
          <div
            v-if="post.comments && post.comments.length"
            class="comments-list"
          >
            <div
              v-for="comment in post.comments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <img
                  v-if="comment.userAvatar"
                  :src="`${API_BASE_URL}${comment.userAvatar}`"
                  alt="用户头像"
                  class="comment-user-avatar"
                />
                <span class="comment-username">{{ comment.username }}</span>
                <span class="comment-time">{{
                  formatDate(comment.createdAt)
                }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button
                  type="text"
                  @click="showReplyInput(post.id, comment.id)"
                >
                  <i class="el-icon-chat-dot-round"></i> 回复
                </el-button>
                <el-button
                  v-if="comment.replyCount > 0"
                  type="text"
                  @click="toggleReplies(comment)"
                >
                  <i
                    :class="
                      comment.showReplies
                        ? 'el-icon-arrow-up'
                        : 'el-icon-arrow-down'
                    "
                  ></i>
                  {{
                    comment.showReplies
                      ? "收起回复"
                      : `查看回复(${comment.replyCount})`
                  }}
                </el-button>
              </div>
              <!-- 回复输入框 -->
              <div v-if="comment.showReplyInput" class="reply-input">
                <el-input
                  v-model="comment.replyContent"
                  type="textarea"
                  :rows="2"
                  placeholder="写下你的回复..."
                />
                <div class="reply-actions">
                  <el-button @click="cancelReply(comment)">取消</el-button>
                  <el-button
                    type="primary"
                    @click="
                      submitReply(post.id, comment.id, comment.replyContent)
                    "
                  >
                    提交回复
                  </el-button>
                </div>
              </div>
              <!-- 子回复列表 -->
              <div
                v-if="
                  comment.showReplies &&
                  comment.replies &&
                  comment.replies.length > 0
                "
                class="replies-list"
              >
                <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
                >
                  <div class="reply-header">
                    <img
                      v-if="reply.userAvatar"
                      :src="`${API_BASE_URL}${reply.userAvatar}`"
                      alt="用户头像"
                      class="reply-user-avatar"
                    />
                    <span class="reply-username">{{ reply.username }}</span>
                    <span class="reply-time">{{
                      formatDate(reply.createdAt)
                    }}</span>
                  </div>
                  <div class="reply-content">{{ reply.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import axios from "axios";

const store = useStore();
const router = useRouter();

// API基础URL
const API_BASE_URL = "http://localhost:5000";

// 获取当前用户ID
const userId = computed(() => store.getters.userId);

// 编辑对话框相关
const editDialogVisible = ref(false);
const editForm = ref({
  avatar: "",
  nickname: "",
  phone: "",
  address: "",
});

// 上传头像相关
const uploadHeaders = computed(() => {
  const token = localStorage.getItem("token");
  return {
    Authorization: `Bearer ${token}`,
  };
});

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("上传头像图片只能是图片格式!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("上传头像图片大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 头像上传成功处理
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    editForm.value.avatar = response.data;
    store.commit("setUserAvatar", response.data);
    ElMessage.success("头像上传成功");
  } else {
    ElMessage.error("头像上传失败：" + response.message);
  }
};

// 计算用户信息
const userNickname = computed(() => store.getters.userNickname);
const userPhone = computed(() => store.getters.userPhoneNumber || "未提供");
const userAddress = computed(() => store.getters.userAddress || "未提供");
const userBalance = computed(() => store.getters.userBalance || 0);
const userAvatar = computed(() => {
  const avatar = store.getters.userAvatar;
  if (!avatar) {
    return "/assets/avatar.png";
  }
  if (avatar.startsWith("/assets/")) {
    return avatar;
  }
  return `${API_BASE_URL}${avatar}`;
});

// 在组件挂载时获取账户余额
onMounted(async () => {
  await store.getters.userBalance;
  await fetchUserInfo();
});

// 显示编辑对话框
const showEditDialog = () => {
  editForm.value = {
    avatar: userAvatar.value,
    nickname: userNickname.value,
    phone: userPhone.value,
    address: userAddress.value,
  };
  editDialogVisible.value = true;
};

// 保存资料
const saveProfile = async () => {
  try {
    await store.dispatch("updateUserProfile", {
      avatar: editForm.value.avatar,
      nickname: editForm.value.nickname,
      phone: editForm.value.phone,
      address: editForm.value.address,
    });
    ElMessage.success("资料更新成功");
    editDialogVisible.value = false;
  } catch (error) {
    ElMessage.error("资料更新失败：" + error.message);
  }
};

const logout = () => {
  store.commit("clearUser");
  router.push({ name: "Login" });
};

const goToHome = () => {
  router.push("/");
};

const userInfo = ref({});
const posts = ref([]);
const newComment = ref("");
const loading = ref(false);

// 获取用户信息
const fetchUserInfo = async () => {
  if (!userId.value) {
    ElMessage.error("用户未登录");
    return;
  }

  try {
    const response = await axios.get(`${API_BASE_URL}/api/user/profile`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
    userInfo.value = response.data;
  } catch (error) {
    console.error("获取用户信息失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else {
      ElMessage.error("获取用户信息失败");
    }
  }
};

// 获取所有帖子
const fetchAllPosts = async () => {
  try {
    loading.value = true;
    const response = await axios.get(`${API_BASE_URL}/api/posts/all`, {
      params: {
        page: Number(0),
        size: Number(10),
        sortBy: "createdAt",
        direction: "DESC",
      },
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      paramsSerializer: (params) => {
        return Object.entries(params)
          .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
          .join("&");
      },
    });
    if (response.data.code === 200 && response.data.data) {
      posts.value = response.data.data.content.map((post) => ({
        ...post,
        showCommentInput: false,
        liked: post.liked || false,
      }));
    } else {
      posts.value = [];
    }
  } catch (error) {
    console.error("获取帖子列表失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error("获取帖子列表失败");
    }
  } finally {
    loading.value = false;
  }
};

// 显示评论输入框
const showCommentInput = (postId) => {
  posts.value = posts.value.map((post) => ({
    ...post,
    showCommentInput: post.id === postId,
    showReplyInput: false, // 关闭所有回复输入框
  }));
};

// 提交评论
const submitComment = async (postId) => {
  if (!newComment.value.trim()) {
    ElMessage.warning("评论内容不能为空");
    return;
  }

  try {
    console.log("发送评论请求:", {
      postId,
      content: newComment.value.trim(),
    });

    const response = await axios.post(
      `${API_BASE_URL}/api/posts/${postId}/comments`,
      {
        content: newComment.value.trim(),
      },
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("评论响应:", response.data);

    if (response.data.code === 200) {
      ElMessage.success("评论成功");
      newComment.value = "";
      // 关闭评论输入框
      posts.value = posts.value.map((post) => ({
        ...post,
        showCommentInput: false,
      }));
      // 重新获取帖子列表以更新评论
      await fetchAllPosts();
    } else {
      ElMessage.error(response.data.message || "评论失败");
    }
  } catch (error) {
    console.error("评论失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else {
      ElMessage.error(error.response?.data?.message || "评论失败");
    }
  }
};

// 点赞/取消点赞
const toggleLike = async (postId) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/posts/${postId}/like`,
      {},
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      },
    );
    if (response.data.code === 200) {
      // 更新帖子的点赞状态
      posts.value = posts.value.map((post) => {
        if (post.id === postId) {
          return {
            ...post,
            likes: post.liked ? post.likes - 1 : post.likes + 1,
            liked: !post.liked,
          };
        }
        return post;
      });
      ElMessage.success(response.data.message || "操作成功");
    }
  } catch (error) {
    console.error("点赞操作失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else {
      ElMessage.error("操作失败");
    }
  }
};

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleString();
};

// 发表帖子相关
const postDialogVisible = ref(false);
const postForm = ref({
  content: "",
});

// 显示发表帖子对话框
const showPostDialog = () => {
  postForm.value.content = "";
  postDialogVisible.value = true;
};

// 提交帖子
const submitPost = async () => {
  if (!postForm.value.content.trim()) {
    ElMessage.warning("帖子内容不能为空");
    return;
  }

  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/posts`,
      {
        content: postForm.value.content.trim(),
        userId: userId.value,
      },
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "application/json",
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success("帖子发表成功");
      postDialogVisible.value = false;
      await fetchAllPosts();
    } else {
      ElMessage.error(response.data.message || "发表帖子失败");
    }
  } catch (error) {
    console.error("发表帖子失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error("发表帖子失败");
    }
  }
};

// 显示回复输入框
const showReplyInput = (postId, commentId, replyToId = null) => {
  posts.value = posts.value.map((post) => {
    if (post.id === postId) {
      post.comments = post.comments.map((comment) => {
        if (comment.id === commentId) {
          return {
            ...comment,
            showReplyInput: true,
            replyContent: "",
            replyToId,
          };
        }
        return {
          ...comment,
          showReplyInput: false,
        };
      });
    }
    return post;
  });
};

// 取消回复
const cancelReply = (comment) => {
  comment.showReplyInput = false;
  comment.replyContent = "";
};

// 切换显示回复
const toggleReplies = async (comment) => {
  try {
    // 如果已经显示回复，则隐藏
    if (comment.showReplies) {
      comment.showReplies = false;
      return;
    }

    // 如果还没有加载回复，则从服务器获取
    if (!comment.replies || comment.replies.length === 0) {
      const response = await axios.get(
        `${API_BASE_URL}/api/posts/comments/${comment.id}/replies`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        },
      );

      if (response.data.code === 200) {
        comment.replies = response.data.data;
        console.log("获取到的回复:", comment.replies); // 添加调试日志
      } else {
        ElMessage.error(response.data.message || "获取回复失败");
        return;
      }
    }

    comment.showReplies = true;
  } catch (error) {
    console.error("获取回复失败:", error);
    ElMessage.error("获取回复失败");
  }
};

// 提交回复
const submitReply = async (postId, commentId, content, replyToId = null) => {
  if (!content || !content.trim()) {
    ElMessage.warning("回复内容不能为空");
    return;
  }

  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/posts/comments/${commentId}/reply`,
      {
        content: content.trim(),
        postId,
        parentId: commentId,
        replyToId,
      },
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "application/json",
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success("回复成功");
      // 更新评论列表
      await fetchAllPosts();
    } else {
      ElMessage.error(response.data.message || "回复失败");
    }
  } catch (error) {
    console.error("回复失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else {
      ElMessage.error("回复失败");
    }
  }
};

// 删除帖子
const deletePost = async (postId) => {
  try {
    const response = await axios.delete(`${API_BASE_URL}/api/posts/${postId}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });

    if (response.data.code === 200) {
      ElMessage.success("帖子删除成功");
      // 从列表中移除已删除的帖子
      posts.value = posts.value.filter((post) => post.id !== postId);
    }
  } catch (error) {
    console.error("删除帖子失败:", error);
    if (error.response?.status === 401) {
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");
    } else if (error.response?.status === 403) {
      ElMessage.error("您没有权限删除此帖子");
    } else {
      ElMessage.error(error.response?.data?.message || "删除帖子失败");
    }
  }
};

// 密保相关
const securityDialogVisible = ref(false);
const securityFormRef = ref(null);
const securityForm = ref({
  question: "",
  answer: "",
});

const securityRules = {
  question: [{ required: true, message: "请选择密保问题", trigger: "change" }],
  answer: [
    { required: true, message: "请输入密保答案", trigger: "blur" },
    { min: 2, message: "答案长度至少2个字符", trigger: "blur" },
  ],
};

const showSecurityDialog = () => {
  securityForm.value = {
    question: "",
    answer: "",
  };
  securityDialogVisible.value = true;
};

const submitSecurityForm = async () => {
  if (!securityFormRef.value) return;

  await securityFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await axios.post(
          `${API_BASE_URL}/api/user/security-question`,
          securityForm.value,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          },
        );

        if (response.data.code === 200) {
          ElMessage.success("密保设置成功");
          securityDialogVisible.value = false;
        } else {
          ElMessage.error(response.data.message || "设置失败");
        }
      } catch (error) {
        console.error("设置密保失败:", error);
        ElMessage.error("设置密保失败，请重试");
      }
    }
  });
};

onMounted(() => {
  fetchUserInfo();
  fetchAllPosts();
});
</script>

<style scoped>
.user-page {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  min-height: 100vh;
}

/* 装饰元素 */
.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  z-index: 0;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  top: -100px;
  right: -100px;
  animation: float 15s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
  bottom: -50px;
  left: -50px;
  animation: float 20s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #9c27b0 0%, #2196f3 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 8s ease-in-out infinite;
}

.decoration-dots {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(#ddd 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.3;
  z-index: 0;
  animation: dotsMove 20s linear infinite;
}

.decoration-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.1) 50%,
      transparent 100%
    ),
    linear-gradient(
      0deg,
      transparent 0%,
      rgba(255, 255, 255, 0.1) 50%,
      transparent 100%
    );
  background-size: 30px 30px;
  opacity: 0.2;
  z-index: 0;
  animation: linesMove 15s linear infinite;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(20px, 20px) rotate(5deg);
  }
  100% {
    transform: translate(0, 0) rotate(0deg);
  }
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.1;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.2;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.1;
  }
}

@keyframes dotsMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 20px 20px;
  }
}

@keyframes linesMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 30px 30px;
  }
}

.back-home {
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.back-home-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.back-home-btn:hover::before {
  left: 100%;
}

.back-home-btn:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  animation: slideDown 0.8s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.page-title {
  font-size: 2.5em;
  color: #2c3e50;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  display: inline-block;
  animation: titleGlow 2s ease-in-out infinite;
}

@keyframes titleGlow {
  0% {
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  }
  50% {
    text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
  }
  100% {
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  }
}

.page-title::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #4caf50, #2196f3, #9c27b0);
  border-radius: 3px;
  animation: borderGlow 2s ease-in-out infinite;
}

@keyframes borderGlow {
  0% {
    box-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(33, 150, 243, 0.5);
  }
  100% {
    box-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
  }
}

.welcome-message {
  font-size: 1.2em;
  color: #34495e;
  margin-top: 10px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.user-info {
  background: rgba(255, 255, 255, 0.95);
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
  animation: slideUp 0.8s ease-out;
}

/* 添加装饰性背景图案 */
.user-info::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
      circle at 20% 20%,
      rgba(64, 158, 255, 0.05) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 80% 80%,
      rgba(103, 194, 58, 0.05) 0%,
      transparent 50%
    ),
    linear-gradient(
      45deg,
      transparent 48%,
      rgba(64, 158, 255, 0.1) 50%,
      transparent 52%
    );
  background-size:
    100% 100%,
    100% 100%,
    20px 20px;
  animation: backgroundMove 20s linear infinite;
  pointer-events: none;
}

@keyframes backgroundMove {
  0% {
    background-position:
      0 0,
      0 0,
      0 0;
  }
  100% {
    background-position:
      0 0,
      0 0,
      20px 20px;
  }
}

/* 添加装饰性边角 */
.user-info::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 2px solid transparent;
  border-radius: 15px;
  background: linear-gradient(45deg, #409eff, #67c23a) border-box;
  -webkit-mask:
    linear-gradient(#fff 0 0) padding-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: destination-out;
  mask-composite: exclude;
  animation: borderRotate 10s linear infinite;
}

@keyframes borderRotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 添加装饰性图标 */
.info-section li {
  margin-bottom: 10px;
  padding: 15px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.info-section li::before {
  content: "";
  width: 24px;
  height: 24px;
  margin-right: 10px;
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.info-section li:nth-child(1)::before {
  content: "👤";
  font-size: 20px;
}

.info-section li:nth-child(2)::before {
  content: "📱";
  font-size: 20px;
}

.info-section li:nth-child(3)::before {
  content: "📍";
  font-size: 20px;
}

.info-section li:nth-child(4)::before {
  content: "💰";
  font-size: 20px;
}

.info-section li:hover::before {
  transform: scale(1.2) rotate(10deg);
  opacity: 1;
}

/* 添加悬浮光效 */
.info-section li::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.info-section li:hover::after {
  left: 100%;
}

/* 添加信息标签动画 */
.info-label {
  font-weight: bold;
  color: #409eff;
  margin-right: 5px;
  position: relative;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.1);
  transition: all 0.3s ease;
}

.info-label:hover {
  background: rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

/* 添加余额显示动画 */
.balance {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.1em;
  position: relative;
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  background: rgba(245, 108, 108, 0.1);
  transition: all 0.3s ease;
}

.balance:hover {
  background: rgba(245, 108, 108, 0.2);
  transform: translateY(-2px) scale(1.05);
}

/* 添加警告提示动画 */
.balance-warning {
  font-size: 0.8em;
  color: #909399;
  margin-left: 5px;
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(144, 147, 153, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.balance-warning::before {
  content: "⚠️";
  margin-right: 5px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.balance-warning:hover::before {
  opacity: 1;
  transform: translateX(0);
}

.balance-warning:hover {
  background: rgba(144, 147, 153, 0.2);
  transform: translateY(-2px);
}

/* 添加头像装饰 */
.avatar-section {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
  padding: 15px;
}

.avatar-section::before {
  content: "";
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border: 2px solid transparent;
  border-radius: 50%;
  background: linear-gradient(45deg, #409eff, #67c23a) border-box;
  -webkit-mask:
    linear-gradient(#fff 0 0) padding-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: destination-out;
  mask-composite: exclude;
  animation: rotate 4s linear infinite;
}

.avatar-section::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: conic-gradient(
    from 0deg,
    #409eff,
    #67c23a,
    #e6a23c,
    #f56c6c,
    #409eff
  );
  animation: spin 4s linear infinite;
  z-index: -1;
}

@keyframes spin {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

/* 添加信息卡片标题动画 */
.info-section h2 {
  color: #2c3e50;
  margin-bottom: 15px;
  text-align: center;
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
  padding: 0 20px;
}

.info-section h2::before,
.info-section h2::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  animation: lineGlow 2s ease-in-out infinite;
}

.info-section h2::before {
  left: -40px;
}

.info-section h2::after {
  right: -40px;
}

@keyframes lineGlow {
  0% {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(103, 194, 58, 0.5);
  }
  100% {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
  }
}

.user-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.action-button {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  border-radius: 8px;
  padding: 12px 24px;
  position: relative;
  overflow: hidden;
}

.action-button::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition:
    width 0.6s ease,
    height 0.6s ease;
}

.action-button:hover::before {
  width: 300px;
  height: 300px;
}

.action-button:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.user-community {
  background: rgba(255, 255, 255, 0.95);
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  z-index: 1;
  animation: slideIn 0.8s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.user-community::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
      circle at top right,
      rgba(64, 158, 255, 0.1),
      transparent 70%
    ),
    radial-gradient(
      circle at bottom left,
      rgba(103, 194, 58, 0.1),
      transparent 70%
    );
  pointer-events: none;
}

.user-community h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  text-align: center;
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
}

.user-community h2::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
  animation: borderFlow 2s linear infinite;
}

.empty-posts {
  text-align: center;
  color: #909399;
  padding: 30px;
  font-style: italic;
  position: relative;
}

.empty-posts::before {
  content: "💭";
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 40px;
  opacity: 0.2;
  animation: float 3s ease-in-out infinite;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.post-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #409eff, #67c23a);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.post-item:hover::before {
  opacity: 1;
}

.post-item:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.post-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
  transition: all 0.3s ease;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.post-user-avatar:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.post-username {
  font-weight: bold;
  color: #409eff;
  margin-right: 10px;
  position: relative;
}

.post-username::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 1px;
  background: #409eff;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.post-item:hover .post-username::after {
  transform: scaleX(1);
}

.post-time {
  color: #909399;
  font-size: 0.9em;
  position: relative;
}

.post-time::before {
  content: "🕒";
  margin-right: 5px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.post-item:hover .post-time::before {
  opacity: 1;
  transform: translateX(0);
}

.post-content {
  margin-bottom: 15px;
  line-height: 1.6;
  color: #303133;
  position: relative;
}

.post-content::before {
  content: "";
  position: absolute;
  top: -10px;
  left: -10px;
  font-size: 40px;
  color: rgba(64, 158, 255, 0.1);
  font-family: serif;
}

.post-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.comment-section,
.reply-section {
  margin-top: 15px;
  margin-bottom: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.comment-section::before,
.reply-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.comment-section:hover::before,
.reply-section:hover::before {
  transform: translateX(100%);
}

.submit-comment,
.submit-reply {
  margin-top: 10px;
  position: relative;
  overflow: hidden;
}

.comments-list {
  margin-top: 15px;
  position: relative;
}

.comments-list::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 2px;
  height: 100%;
  background: linear-gradient(to bottom, #409eff, #67c23a);
  opacity: 0.3;
}

.comment-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  transition: all 0.3s ease;
  position: relative;
  margin-left: 20px;
}

.comment-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: -20px;
  width: 20px;
  height: 2px;
  background: #409eff;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.comment-item:hover::before {
  opacity: 1;
}

.comment-item:hover {
  background: #f5f7fa;
  transform: translateX(5px);
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user-avatar,
.reply-user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
  transition: all 0.3s ease;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.comment-user-avatar:hover,
.reply-user-avatar:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.comment-username,
.reply-username {
  font-weight: bold;
  color: #409eff;
  margin-right: 10px;
  position: relative;
}

.comment-username::after,
.reply-username::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 1px;
  background: #409eff;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.comment-item:hover .comment-username::after,
.reply-item:hover .reply-username::after {
  transform: scaleX(1);
}

.comment-time,
.reply-time {
  color: #909399;
  font-size: 0.8em;
  position: relative;
}

.comment-time::before,
.reply-time::before {
  content: "🕒";
  margin-right: 5px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.comment-item:hover .comment-time::before,
.reply-item:hover .reply-time::before {
  opacity: 1;
  transform: translateX(0);
}

.comment-content,
.reply-content {
  margin-bottom: 10px;
  line-height: 1.5;
  color: #303133;
  position: relative;
}

.comment-content::before,
.reply-content::before {
  content: "";
  position: absolute;
  top: -10px;
  left: -10px;
  font-size: 40px;
  color: rgba(64, 158, 255, 0.1);
  font-family: serif;
}

.comment-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.replies-list {
  margin-left: 20px;
  padding-left: 15px;
  border-left: 2px solid #ebeef5;
  position: relative;
}

.replies-list::before {
  content: "";
  position: absolute;
  top: 0;
  left: -2px;
  width: 2px;
  height: 100%;
  background: linear-gradient(to bottom, #409eff, #67c23a);
  opacity: 0.3;
}

.reply-item {
  padding: 10px 0;
  position: relative;
}

.reply-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: -15px;
  width: 15px;
  height: 2px;
  background: #67c23a;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.reply-item:hover::before {
  opacity: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-actions {
    flex-direction: column;
    align-items: center;
  }

  .action-button {
    width: 100%;
    justify-content: center;
  }

  .user-info,
  .user-community {
    padding: 20px;
  }

  .decoration-circle {
    display: none;
  }
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto 10px;
  display: block;
  border: 3px solid #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  object-fit: cover;
  position: relative;
  z-index: 1;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  margin: 10px 0;
  border: 3px solid #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
</style>
