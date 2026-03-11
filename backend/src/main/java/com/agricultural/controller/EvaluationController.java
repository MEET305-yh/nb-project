package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.entity.Evaluation;
import com.agricultural.service.EvaluationService;
import com.agricultural.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/product/{productId}")
    public Result<List<Evaluation>> getProductEvaluations(@PathVariable Long productId) {
        List<Evaluation> evaluations = evaluationService.getProductEvaluations(productId);
        return Result.success(evaluations);
    }

    @PostMapping
    public Result<Evaluation> createEvaluation(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Long orderId = Long.valueOf(request.get("orderId").toString());
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer rating = Integer.valueOf(request.get("rating").toString());
        String content = request.get("content").toString();
        String images = extractImages(request.get("images"));

        Evaluation evaluation = evaluationService.createEvaluation(userId, orderId, productId, rating, content, images);
        return Result.success("评价成功", evaluation);
    }

    private String extractImages(Object imagesObj) {
        if (imagesObj == null) {
            return null;
        }
        if (imagesObj instanceof List) {
            List<?> list = (List<?>) imagesObj;
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        return imagesObj.toString();
    }
}









