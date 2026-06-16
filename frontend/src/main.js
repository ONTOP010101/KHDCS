import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import VXETable from 'vxe-table'
import 'vxe-table/lib/style.css'
import 'vxe-pc-ui/lib/style.css'
import { VxeLoading, VxeTooltip } from 'vxe-pc-ui'
import './styles/base-layout.css'
import './styles/common.css'
import './styles/login.css'

const app = createApp(App)
app.use(router)
app.use(VXETable)
app.use(VxeLoading)
app.use(VxeTooltip)
app.mount('#app')
