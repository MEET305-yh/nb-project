import request from './request'

export const getProductEvaluations = (productId) => {
  return request({
    url: `/evaluations/product/${productId}`,
    method: 'get'
  })
}

export const createEvaluation = (data) => {
  return request({
    url: '/evaluations',
    method: 'post',
    data
  })
}

















