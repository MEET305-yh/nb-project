package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.filter.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN','MERCHANT','USER')")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            return Result.error("只支持 jpg、jpeg、png、gif、webp 格式的图片");
        }

        // 验证文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("图片大小不能超过 10MB");
        }

        try {
            // 生成唯一文件名：日期/随机UUID.扩展名
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = datePath + "/" + UUID.randomUUID().toString() + extension;

            // 上传到阿里云 OSS
            byte[] fileBytes = file.getBytes();
            String imageUrl = aliOssUtil.upload(fileBytes, fileName);

            return Result.success("上传成功", imageUrl);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/video")
    @PreAuthorize("hasAnyRole('ADMIN','MERCHANT','USER')")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        // 允许常见的视频格式
        if (!extension.matches("\\.(mp4|mov|avi|mkv|wmv|flv)")) {
            return Result.error("只支持 mp4、mov、avi、mkv、wmv、flv 等常见视频格式");
        }

        // 最大 200MB
        if (file.getSize() > 200L * 1024 * 1024) {
            return Result.error("视频大小不能超过 200MB");
        }

        try {
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = "video/" + datePath + "/" + UUID.randomUUID().toString() + extension;

            byte[] fileBytes = file.getBytes();
            String videoUrl = aliOssUtil.upload(fileBytes, fileName);

            return Result.success("上传成功", videoUrl);
        } catch (IOException e) {
            return Result.error("视频上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("视频上传失败：" + e.getMessage());
        }
    }
}

