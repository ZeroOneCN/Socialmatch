import { createRouter, createWebHistory } from 'vue-router'

// 路由懒加载
const Login = () => import('../views/Login.vue')
const Layout = () => import('../views/Layout.vue')
const Dashboard = () => import('../views/Dashboard.vue')
const UserManagement = () => import('../views/user/UserList.vue')
const UserDetail = () => import('../views/user/UserDetail.vue')
const ContentManagement = () => import('../views/content/PostList.vue')
const PostDetail = () => import('../views/content/PostDetail.vue')
const ReportManagement = () => import('../views/content/ReportList.vue')
const VerificationManagement = () => import('../views/verification/VerificationManagement.vue')
const SystemSettings = () => import('../views/system/Settings.vue')
const NotFound = () => import('../views/NotFound.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { title: '首页', icon: 'dashboard' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表盘', requiresAuth: true, icon: 'dashboard' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    name: 'UserManagement',
    meta: { title: '用户管理', requiresAuth: true, icon: 'user' },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: UserManagement,
        meta: { title: '用户列表', requiresAuth: true, icon: 'user-filled' }
      },
      {
        path: 'detail/:id',
        name: 'UserDetail',
        component: UserDetail,
        meta: { title: '用户详情', requiresAuth: true, icon: 'user' },
        hidden: true
      }
    ]
  },
  {
    path: '/content',
    component: Layout,
    redirect: '/content/post',
    name: 'ContentManagement',
    meta: { title: '内容管理', requiresAuth: true, icon: 'tickets' },
    children: [
      {
        path: 'post',
        name: 'PostList',
        component: ContentManagement,
        meta: { title: '动态列表', requiresAuth: true, icon: 'document' }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: PostDetail,
        meta: { title: '动态详情', requiresAuth: true, icon: 'document' },
        hidden: true
      },
      {
        path: 'report',
        name: 'ReportList',
        component: ReportManagement,
        meta: { title: '举报管理', requiresAuth: true, icon: 'warning' }
      }
    ]
  },
  {
    path: '/verification',
    component: Layout,
    redirect: '/verification/identity',
    name: 'Verification',
    meta: { title: '认证管理', icon: 'el-icon-s-check' },
    children: [
      {
        path: 'identity',
        name: 'IdentityVerification',
        component: () => import('@/views/verification/IdentityVerification.vue'),
        meta: { title: '身份认证', icon: 'el-icon-user' }
      },
      {
        path: 'education',
        name: 'EducationVerification',
        component: () => import('@/views/verification/EducationVerification.vue'),
        meta: { title: '教育认证', icon: 'el-icon-reading' }
      }
    ]
  },
  {
    path: '/systemsettings',
    component: Layout,
    name: 'SystemSettings',
    meta: { title: '系统设置', requiresAuth: true, icon: 'setting' },
    children: [
      {
        path: '',
        name: 'SystemSettingsIndex',
        component: SystemSettings,
        meta: { title: '系统设置', requiresAuth: true, icon: 'set-up' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Social Admin` : 'Social Admin'
  
  // 检查是否需要身份验证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const token = localStorage.getItem('admin_token')
  
  if (requiresAuth && !token) {
    // 需要身份验证但未登录
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录但访问登录页
    next('/')
  } else {
    next()
  }
})

export default router 