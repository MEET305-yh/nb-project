import request from './request'

export const getProductEvaluations = (productId, options = {}) => {
  return request({
    url: `/evaluations/product/${productId}`,
    method: 'get',
    ...options
  })
}

export const createEvaluation = (data) => {
  return request({
    url: '/evaluations',
    method: 'post',
    data
  })
}

















