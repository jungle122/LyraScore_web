import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const service = axios.create({
// 在这里配 baseURL 和 timeout
baseURL : '/api',
timeout : 10000,
})
//请求拦截器
service.interceptors.request.use(
(config) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
        config.headers.Authorization = 'Bearer ' + token
    }
    // 这里能改 config，必须 return config
    return config
},
(error) => {
    return Promise.reject(error)
}
)
// 401 防重复：并行请求同时拿到 401 时只弹一个 toast、只跳一次
let lastUnauthorizedAt = 0

//响应拦截器（剥 R 壳 + 错误处理）
service.interceptors.response.use(
(response) => {
    // response.data 就是后端返的 {code, message, data}
    const res = response.data

    if (res.code === 0) {
    return res.data        // ← 剥壳：把 data 字段直接返回
    }

    if (res.code === 401) {
    const now = Date.now()
    // 2 秒内的多次 401 合并：只弹一次提示、只清一次态、只跳一次路由
    if (now - lastUnauthorizedAt > 2000) {
        lastUnauthorizedAt = now
        ElMessage.error('未登录或登录已过期')
        const userStore = useUserStore()
        userStore.logout()
        if (router.currentRoute.value.path !== '/login') {
            router.push('/login')
        }
    }
    return Promise.reject(new Error(res.message))
    }

    // 其他错误码
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
},
(error) => {
    // 网络层错误（断网、超时、CORS 等）
    ElMessage.error('网络异常，请检查后端是否启动')
    return Promise.reject(error)
}
)
export default service

