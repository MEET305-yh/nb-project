import request from './request'

export const getProducts = (params) => {
  return request({
    url: '/products/public/list',
    method: 'get',
    params
  })
}

export const getProduct = (id) => {
  return request({
    url: `/products/public/${id}`,
    method: 'get'
  })
}

export const createProduct = (data) => {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

export const updateProduct = (id, data) => {
  return request({
    url: `/products/${id}`,
    method: 'put',
    data
  })
}

// 获取所有分类
export const getAllCategories = () => {
  return request({
    url: '/categories/public/all',
    method: 'get'
  })
}

// 获取轮播图列表
export const getBanners = () => {
  return request({
    url: '/banners/public/list',
    method: 'get'
  })
}


