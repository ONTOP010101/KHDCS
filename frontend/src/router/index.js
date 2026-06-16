import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuth } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue')
      },
      {
        path: 'sample',
        name: 'Sample',
        component: () => import('@/views/sample/SampleView.vue'),
        meta: { title: '样品资料', subtitle: '管理样品信息、拍摄记录与资料归档', icon: 'database', keepAlive: true }
      },
      {
        path: 'sample/manufacturer/:manufacturerCode',
        name: 'SampleManufacturer',
        component: () => import('@/views/sample/SampleView.vue'),
        meta: { title: '样品资料', subtitle: '按厂商查看样品', icon: 'database', keepAlive: true, dynamicTitle: true }
      },
      {
        path: 'stall-info',
        name: 'StallInfo',
        component: () => import('@/views/stall/StallInfoView.vue'),
        meta: { title: '摊位资料', subtitle: '管理摊位信息与展位数据', icon: 'store', keepAlive: true }
      },
      {
        path: 'stall-overview',
        name: 'StallOverview',
        component: () => import('@/views/stall/StallOverviewView.vue'),
        meta: { title: '摊位概况', subtitle: '查看摊位统计概况与分布', icon: 'chart-bar', keepAlive: true }
      },
      {
        path: 'customer-info',
        name: 'CustomerInfo',
        component: () => import('@/views/customer/CustomerInfoView.vue'),
        meta: { title: '客户资料', subtitle: '管理客户信息与业务记录', icon: 'contact', keepAlive: true }
      },
      {
        path: 'removed-manufacturer',
        name: 'RemovedManufacturer',
        component: () => import('@/views/removed/RemovedManufacturerView.vue'),
        meta: { title: '下架厂商资料', subtitle: '查看已下架的厂商资料信息', icon: 'archive-x', keepAlive: true }
      },
      {
        path: 'removed-sample',
        name: 'RemovedSample',
        component: () => import('@/views/removed/RemovedSampleView.vue'),
        meta: { title: '下架样品资料', subtitle: '查看已下架的样品资料信息', icon: 'archive-x', keepAlive: true }
      },
      {
        path: 'manufacturer-export',
        name: 'ManufacturerExport',
        component: () => import('@/views/export/ManufacturerExportView.vue'),
        meta: { title: '厂商带出记录', subtitle: '查看厂商带出记录与历史', icon: 'file-text', keepAlive: true }
      },
      {
        path: 'gallery',
        name: 'Gallery',
        component: () => import('@/views/gallery/GalleryView.vue'),
        meta: { title: '择样图库', subtitle: '管理择样图片资料、代号、客户与拍摄记录', icon: 'image', keepAlive: true }
      },
      {
        path: 'client-sample',
        name: 'ClientSample',
        component: () => import('@/views/clientSample/ClientSampleView.vue'),
        meta: { title: '客户择样', subtitle: '管理客户择样信息与业务记录', icon: 'clipboard-check', keepAlive: true }
      },
      {
        path: 'manufacturer',
        name: 'Manufacturer',
        component: () => import('@/views/manufacturer/ManufacturerView.vue'),
        meta: { title: '厂商资料', subtitle: '维护合作厂商资料与业务信息', icon: 'store', keepAlive: true }
      },
      {
        path: 'friends',
        name: 'Friends',
        component: () => import('@/views/friends/FriendsView.vue'),
        meta: { title: '好友列表', subtitle: '好友会话、群聊、消息与文件沟通', icon: 'users', keepAlive: true }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/UsersView.vue'),
        meta: { title: '用户管理', subtitle: '管理系统用户、账号状态与角色分配', icon: 'users-round', keepAlive: true }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/roles/RolesView.vue'),
        meta: { title: '角色管理', subtitle: '管理系统角色、数据范围与功能权限', icon: 'shield-check', keepAlive: true }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/logs/LogsView.vue'),
        meta: { title: '系统日志', subtitle: '查看系统登录日志、操作日志与异常记录', icon: 'file-clock', keepAlive: true }
      },
      {
        path: 'image-search',
        name: 'ImageSearch',
        component: () => import('@/views/imageSearch/ImageSearchView.vue'),
        meta: { title: '图像搜索', keepAlive: true }
      }
    ]
  },
  {
    path: '/report/designer',
    name: 'ReportDesigner',
    component: () => import('@/views/report/ReportDesigner.vue'),
    meta: { title: '报表设计器', keepAlive: false, requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const { isLoggedIn } = useAuth()
  if (to.meta.requiresAuth !== false && !isLoggedIn()) {
    next('/login')
  } else if (to.path === '/login' && isLoggedIn()) {
    next('/')
  } else {
    next()
  }
})

export default router
