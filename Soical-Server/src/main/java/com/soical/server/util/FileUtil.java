package com.soical.server.util;

import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.config.oss.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Slf4j
@Component  // 改为Spring组件以便注入服务
public class FileUtil {

    // 上传目录
    private static final String UPLOAD_DIR = "uploads";
    
    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    // 允许的文件类型
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip", "text/plain"
    );
    
    // 静态实例，用于在非Spring环境中使用
    private static FileUtil instance;
    
    // 环境变量，用于判断是否使用OSS
    @Autowired
    private Environment environment;
    
    // OSS服务
    @Autowired(required = false)
    private OssService ossService;
    
    // 标记是否使用OSS
    private boolean useOss = false;
    
    /**
     * 初始化方法
     */
    @PostConstruct
    public void init() {
        // 初始化静态实例
        instance = this;
        
        // 如果有OSS服务，并且环境配置允许使用OSS，则启用OSS
        try {
            if (ossService != null && environment.getProperty("aliyun.oss.enabled", Boolean.class, false)) {
                useOss = true;
                log.info("启用阿里云OSS存储服务");
            } else {
                log.info("使用本地文件存储服务");
            }
        } catch (Exception e) {
            log.warn("初始化存储服务失败，将使用本地存储", e);
        }
    }

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    public static String uploadImage(MultipartFile file) {
        // 验证图片类型
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的图片类型");
        }
        
        // 如果启用了OSS且实例已初始化，则使用OSS上传
        if (instance != null && instance.useOss && instance.ossService != null) {
            try {
                return instance.ossService.uploadImage(file);
            } catch (Exception e) {
                log.error("OSS上传图片失败，回退到本地上传", e);
            }
        }
        
        // 回退到本地存储
        return uploadFile(file, "images");
    }

    /**
     * 上传文件
     *
     * @param file     文件
     * @param subDir   子目录
     * @return 文件URL
     */
    public static String uploadFile(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }

        // 创建上传目录
        String uploadPath = UPLOAD_DIR;
        if (StringUtils.hasText(subDir)) {
            uploadPath = UPLOAD_DIR + File.separator + subDir;
        }
        
        try {
            // 创建目录
            Path path = Paths.get(uploadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID() + "." + extension;
            
            // 保存文件
            Path filePath = Paths.get(uploadPath, filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL
            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FAILED.getCode(), "文件上传失败");
        }
    }
    
    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }
} 