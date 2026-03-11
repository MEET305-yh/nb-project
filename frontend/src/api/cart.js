import request from './request'

export const getCart = () => {
  return request({
    url: '/cart',
    method: 'get'
  })
}

export const addToCart = (data) => {
  return request({
    url: '/cart',
    method: 'post',
    data
  })
}

export const updateCartQuantity = (id, data) => {
  return request({
    url: `/cart/${id}`,
    method: 'put',
    data
  })
}

export const removeFromCart = (id) => {
  return request({
    url: `/cart/${id}`,
    method: 'delete'
  })
}

















