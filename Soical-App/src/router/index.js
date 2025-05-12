import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import TabBarLayout from '../components/TabBarLayout.vue'

const routes = [
  {
    path: '/',
    redirect: '/recommend'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/auth/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/auth/RegisterPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: TabBarLayout,
    children: [
      {
        path: 'recommend',
        name: 'Recommend',
        component: () => import('../pages/RecommendPage.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('../pages/MessagePage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('../pages/CommunityPage.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../pages/ProfilePage.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  // 发布动态页面
  {
    path: '/post/create',
    name: 'CreatePost',
    component: () => import('../pages/post/CreatePostPage.vue'),
    meta: { requiresAuth: true }
  },
  // 帖子详情页面
  {
    path: '/post/:postId',
    name: 'PostDetail',
    component: () => import('../pages/post/PostDetailPage.vue'),
    meta: { requiresAuth: false }
  },
  // 聊天详情页面
  {
    path: '/chat/:id',
    name: 'Chat',
    component: () => import('../pages/ChatPage.vue'),
    meta: { requiresAuth: true }
  },
  // AI聊天页面
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: () => import('../pages/AiChatPage.vue'),
    meta: { requiresAuth: true }
  },
  // 设置页面
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../pages/settings/SettingsPage.vue'),
    meta: { requiresAuth: true }
  },
  // 同频偏好设置页面
  {
    path: '/settings/frequency',
    name: 'FrequencyPreferences',
    component: () => import('../pages/settings/FrequencyPreferencesPage.vue'),
    meta: { requiresAuth: true }
  },
  // 个人资料设置页面
  {
    path: '/settings/profile',
    name: 'ProfileSettings',
    component: () => import('../pages/settings/ProfileSettingsPage.vue'),
    meta: { requiresAuth: true }
  },
  // 我的同频匹配页面
  {
    path: '/match/list',
    name: 'MatchList',
    component: () => import('../pages/match/MatchListPage.vue'),
    meta: { requiresAuth: true }
  },
  // 我的喜欢记录页面
  {
    path: '/match/likes',
    name: 'LikesList',
    component: () => import('../pages/match/LikesListPage.vue'),
    meta: { requiresAuth: true }
  },
  // 用户资料页面
  {
    path: '/user',
    children: [
      {
        path: 'profile/:id',
        component: () => import('../pages/user/UserProfilePage.vue'),
        meta: { title: '用户资料' }
      },
      {
        path: 'profile/:id/following',
        component: () => import('../pages/user/FollowingList.vue'),
        meta: { title: '关注列表' }
      },
      {
        path: 'profile/:id/followers',
        component: () => import('../pages/user/FollowersList.vue'),
        meta: { title: '粉丝列表' }
      }
    ]
  },
  // 用户帖子页面
  {
    path: '/user/:id/posts',
    name: 'UserPostsDetail',
    component: () => import('../pages/user/UserPostsPage.vue'),
    meta: { requiresAuth: true }
  },
  // 我的动态列表
  {
    path: '/user/posts',
    name: 'UserPosts',
    component: () => import('../pages/user/UserPostsPage.vue'),
    meta: {
      requiresAuth: true
    }
  },
  // 我的点赞列表
  {
    path: '/user/likes',
    name: 'UserLikes',
    component: () => import('../pages/user/UserLikesPage.vue'),
    meta: {
      requiresAuth: true
    }
  },
  // 旧的首页路径重定向
  {
    path: '/home',
    redirect: '/recommend'
  },
  // 认证中心页面
  {
    path: '/verification',
    name: 'VerificationCenter',
    component: () => import('../pages/verification/VerificationCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '认证中心'
    }
  },
  // 关于我们页面
  {
    path: '/about',
    name: 'About',
    component: () => import('../pages/AboutPage.vue'),
    meta: { title: '关于我们' }
  },
  // 捕获所有未匹配的路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/recommend'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 保存登录前的URL
const saveLoginRedirectUrl = (to) => {
  if (to.meta.requiresAuth) {
    localStorage.setItem('login_redirect', to.fullPath)
  }
}

// 路由守卫，检查登录状态
router.beforeEach(async (to, from, next) => {
  console.log('路由守卫: ', to.path);
  
  // 使用pinia store而不是直接访问localStorage
  // 注意：这里需要动态引入，因为pinia store在创建路由前可能还没有完全初始化
  const { useUserStore } = await import('../stores/user');
  const userStore = useUserStore();
  
  // 如果有token但没有用户信息，尝试加载用户信息
  if (userStore.token && !userStore.userId) {
    console.log('检测到TOKEN但无用户信息，尝试恢复登录状态');
    await userStore.restoreLoginState();
  }
  
  // 使用store的isLoggedIn而不是直接检查token
  const isLoggedIn = userStore.isLoggedIn;
  
  // 特殊处理关注和粉丝列表页面
  if ((to.path.includes('/following') || to.path.includes('/followers')) && !to.params.id) {
    console.log('检测到关注/粉丝页面但无用户ID，添加当前用户ID');
    const currentUserId = userStore.userId;
    if (currentUserId) {
      // 使用当前用户ID重定向到正确的路径
      const newPath = to.path.replace('/user/profile/', `/user/profile/${currentUserId}/`);
      next(newPath);
      return;
    }
  }
  
  // 如果页面需要登录但用户未登录，保存URL并重定向到登录页
  if (to.meta.requiresAuth && !isLoggedIn) {
    console.log('需要登录, 保存URL并重定向到登录页');
    saveLoginRedirectUrl(to);
    next({ name: 'Login' });
  } else {
    // 如果用户已登录且尝试访问登录/注册页，重定向到首页
    if (isLoggedIn && (to.name === 'Login' || to.name === 'Register')) {
      console.log('已登录, 重定向到首页');
      next({ name: 'Recommend' });
    } else {
      next();
    }
  }
});

export default router 