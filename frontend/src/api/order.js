import request from './request'

export const createOrder = (data) => {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

export const getOrders = () => {
  return request({
    url: '/orders',
    method: 'get'
  })
}

export const getOrder = (id) => {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

export const payOrder = (id) => {
  return request({
    url: `/orders/${id}/pay`,
    method: 'post'
  })
}

export const completeOrder = (id) => {
  return request({
    url: `/orders/${id}/complete`,
    method: 'post'
  })
}

















