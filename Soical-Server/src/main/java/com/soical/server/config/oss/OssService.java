package com.soical.server.config.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云OSS文件上传服务
 */
@Service
@Slf4j
public class OssService {

    @Autowired
    private OssConfig ossConfig;
    
    @Autowired(required = false)
    private OSS ossClient;
    
    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * 上传图片到OSS
     *
     * @param file 图片文件
     * @return 图片的URL
     */
    public String uploadImage(MultipartFile file) {
        // 验证OSS客户端是否已配置
        validateOssClient();
        
        // 验证图片类型
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的图片类型");
        }
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String fileName = "images/" + datePath + "/" + UUID.randomUUID().toString().replaceAll("-", "") + "." + extension;
        
        try {
            return uploadFile(file.getInputStream(), fileName, file.getContentType(), file.getSize());
        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new BusinessException(ResultCode.FAILED.getCode(), "图片上传失败");
        }
    }
    
    /**
     * 上传文件到OSS
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param contentType 文件类型
     * @param fileSize    文件大小
     * @return 文件的URL
     */
    private String uploadFile(InputStream inputStream, String fileName, String contentType, long fileSize) {
        try {
            // 创建上传Object的元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), fileName, inputStream, metadata);
            ossClient.putObject(putObjectRequest);
            
            // 返回文件URL
            String url = ossConfig.getOssUrl();
            if (!url.endsWith("/")) {
                url += "/";
            }
            return url + fileName;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ResultCode.FAILED.getCode(), "文件上传失败");
        }
    }
    
    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "jpg";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "jpg";
        }
        return filename.substring(lastIndexOf + 1);
    }
    
    /**
     * 验证OSS客户端是否已配置
     */
    private void validateOssClient() {
        if (ossClient == null) {
            // OSS未配置，可能是开发环境，使用默认的文件上传
            log.warn("OSS客户端未配置，请检查OSS配置");
            throw new BusinessException(ResultCode.FAILED.getCode(), "OSS服务未配置");
        }
        
        if (!StringUtils.hasText(ossConfig.getBucketName()) || !StringUtils.hasText(ossConfig.getOssUrl())) {
            log.warn("OSS BucketName或URL未配置");
            throw new BusinessException(ResultCode.FAILED.getCode(), "OSS服务配置不完整");
        }
    }
} 