package com.agricultural.service.impl;

import com.agricultural.entity.Evaluation;
import com.agricultural.mapper.EvaluationMapper;
import com.agricultural.service.EvaluationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Override
    public List<Evaluation> getProductEvaluations(Long productId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getProductId, productId);
        wrapper.eq(Evaluation::getDeleted, 0);
        wrapper.orderByDesc(Evaluation::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Evaluation createEvaluation(Long userId, Long orderId, Long productId, Integer rating, String content, String images) {
        Evaluation evaluation = new Evaluation();
        evaluation.setUserId(userId);
        evaluation.setOrderId(orderId);
        evaluation.setProductId(productId);
        evaluation.setRating(rating);
        evaluation.setContent(content);
        evaluation.setImages(images);
        evaluation.setCreateTime(LocalDateTime.now());
        evaluation.setDeleted(0);

        save(evaluation);
        return evaluation;
    }
}









