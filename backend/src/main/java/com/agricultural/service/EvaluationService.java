package com.agricultural.service;

import com.agricultural.entity.Evaluation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface EvaluationService extends IService<Evaluation> {
    List<Evaluation> getProductEvaluations(Long productId);
    Evaluation createEvaluation(Long userId, Long orderId, Long productId, Integer rating, String content, String images);
}


