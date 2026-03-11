import request from './request'

// 用户管理
export const getAdminUsers = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export const getAdminUser = (id) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

export const createAdminUser = (data) => {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

export const updateAdminUser = (id, data) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  })
}

export const deleteAdminUser = (id) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

export const getUserStats = () => {
  return request({
    url: '/admin/users/stats',
    method: 'get'
  })
}

// 商品管理
export const getAdminProducts = (params) => {
  return request({
    url: '/admin/products',
    method: 'get',
    params
  })
}

export const getAdminProduct = (id) => {
  return request({
    url: `/admin/products/${id}`,
    method: 'get'
  })
}

export const createAdminProduct = (data) => {
  return request({
    url: '/admin/products',
    method: 'post',
    data
  })
}

export const updateAdminProduct = (id, data) => {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data
  })
}

export const deleteAdminProduct = (id) => {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

export const updateProductStatus = (id, status) => {
  return request({
    url: `/admin/products/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 订单管理
export const getAdminOrders = (params) => {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

export const getAdminOrder = (id) => {
  return request({
    url: `/admin/orders/${id}`,
    method: 'get'
  })
}

export const shipOrder = (id) => {
  return request({
    url: `/admin/orders/${id}/ship`,
    method: 'post'
  })
}

export const cancelOrder = (id) => {
  return request({
    url: `/admin/orders/${id}/cancel`,
    method: 'post'
  })
}

export const getOrderStats = () => {
  return request({
    url: '/admin/orders/stats',
    method: 'get'
  })
}

export const getStatistics = (days) => {
  return request({
    url: '/admin/orders/statistics',
    method: 'get',
    params: { days }
  })
}

export const getProductSales = (days) => {
  return request({
    url: '/admin/orders/product-sales',
    method: 'get',
    params: { days }
  })
}

// 分类管理
export const getAdminCategories = (params) => {
  return request({
    url: '/admin/categories',
    method: 'get',
    params
  })
}

export const getAllCategories = () => {
  return request({
    url: '/admin/categories/all',
    method: 'get'
  })
}

export const getAdminCategory = (id) => {
  return request({
    url: `/admin/categories/${id}`,
    method: 'get'
  })
}

export const createAdminCategory = (data) => {
  return request({
    url: '/admin/categories',
    method: 'post',
    data
  })
}

export const updateAdminCategory = (id, data) => {
  return request({
    url: `/admin/categories/${id}`,
    method: 'put',
    data
  })
}

export const deleteAdminCategory = (id) => {
  return request({
    url: `/admin/categories/${id}`,
    method: 'delete'
  })
}

// 文件上传
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 轮播图管理
export const getAdminBanners = (params) => {
  return request({
    url: '/admin/banners',
    method: 'get',
    params
  })
}

export const getAdminBanner = (id) => {
  return request({
    url: `/admin/banners/${id}`,
    method: 'get'
  })
}

export const createAdminBanner = (data) => {
  return request({
    url: '/admin/banners',
    method: 'post',
    data
  })
}

export const updateAdminBanner = (id, data) => {
  return request({
    url: `/admin/banners/${id}`,
    method: 'put',
    data
  })
}

export const deleteAdminBanner = (id) => {
  return request({
    url: `/admin/banners/${id}`,
    method: 'delete'
  })
}

export const updateBannerStatus = (id, status) => {
  return request({
    url: `/admin/banners/${id}/status`,
    method: 'put',
    params: { status }
  })
}

