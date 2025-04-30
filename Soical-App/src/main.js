import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Vant UI组件
import { 
  Button, 
  Field, 
  CellGroup, 
  Form, 
  Tabbar, 
  TabbarItem, 
  Icon, 
  Loading, 
  Empty, 
  Popup, 
  Dialog, 
  Picker,
  Cell,
  NavBar,
  Badge,
  Image as VanImage,
  PullRefresh,
  ActionSheet,
  showDialog,
  showToast
} from 'vant'
import 'vant/lib/index.css'

// 全局样式
import './assets/styles/global.css'

// 创建pinia实例
const pinia = createPinia()

// 创建Vue应用
const app = createApp(App)

// 注册pinia
app.use(pinia)

// 注册路由
app.use(router)

// 注册Vant组件
app.use(Button)
app.use(Field)
app.use(CellGroup)
app.use(Form)
app.use(Tabbar)
app.use(TabbarItem)
app.use(Icon)
app.use(Loading)
app.use(Empty)
app.use(Popup)
app.use(Dialog)
app.use(Picker)
app.use(Cell)
app.use(NavBar)
app.use(Badge)
app.use(VanImage)
app.use(PullRefresh)
app.use(ActionSheet)

// 挂载应用
app.mount('#app')
