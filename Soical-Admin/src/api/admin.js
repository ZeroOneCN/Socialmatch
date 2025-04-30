import request from './request'

// 管理员登录
export function login(data) {
  return request({
    url: '/api/admin/login',
    method: 'post',
    data
  })
}

// 获取管理员信息
export function getInfo() {
  return request({
    url: '/api/admin/current',
    method: 'get'
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/api/admin/logout',
    method: 'post'
  })
}

// 修改管理员密码
export function changePassword(data) {
  return request({
    url: '/api/admin/password',
    method: 'put',
    data
  })
}

// 获取管理员列表
export function getAdminList(params) {
  return request({
    url: '/api/admin/list',
    method: 'get',
    params
  })
}

// 添加管理员
export function addAdmin(data) {
  return request({
    url: '/api/admin',
    method: 'post',
    data
  })
}

// 修改管理员状态
export function updateAdminStatus(id, status) {
  return request({
    url: `/api/admin/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 删除管理员
export function deleteAdmin(id) {
  return request({
    url: `/api/admin/${id}`,
    method: 'delete'
  })
}

// 更新管理员个人信息
export function updateAdminProfile(data) {
  return request({
    url: '/api/admin/info',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(formData) {
  return request({
    url: '/api/admin/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 