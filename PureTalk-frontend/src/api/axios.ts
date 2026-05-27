import axios, { AxiosInstance, AxiosResponse } from 'axios'

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.satoken = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    console.error('请求错误:', error)
    // 处理后端返回的错误
    if (error.response) {
      console.error('响应错误:', error.response.data)
      const errorData = error.response.data
      
      // 处理 401 未登录错误
      if (error.response.status === 401) {
        // 清除本地存储的 token
        localStorage.removeItem('token')
        // 可以在这里添加跳转到登录页面的逻辑
        console.error('用户未登录或登录已过期')
      }
      
      // 处理 409 数据已存在错误
      if (error.response.status === 409) {
        console.error('数据已存在，如手机号/邮箱被占用')
      }
      
      // 处理 500 服务器异常错误
      if (error.response.status === 500) {
        console.error('服务器异常，请稍后重试')
      }
      
      // 直接返回错误数据，让前端能够处理
      return Promise.reject(errorData)
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      console.error('请求已发出，但没有收到响应:', error.request)
      return Promise.reject({ code: 0, msg: '网络错误，请检查网络连接' })
    } else {
      // 请求配置出错
      console.error('请求配置出错:', error.message)
      return Promise.reject({ code: 0, msg: '请求配置出错，请稍后重试' })
    }
  }
)

export default instance