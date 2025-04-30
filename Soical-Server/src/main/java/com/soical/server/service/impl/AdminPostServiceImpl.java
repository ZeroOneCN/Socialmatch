package com.soical.server.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soical.server.common.BusinessException;
import com.soical.server.common.ResultCode;
import com.soical.server.dto.AdminPostDTO;
import com.soical.server.entity.Post;
import com.soical.server.mapper.AdminPostMapper;
import com.soical.server.service.AdminPostService;
import com.soical.server.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员动态管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AdminPostServiceImpl implements AdminPostService {

    private final AdminPostMapper adminPostMapper;
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPostDTO> getPostList(Integer status, String keyword, Integer page, Integer pageSize) {
        Page<Post> postPage = new Page<>(page, pageSize);
        Page<Post> postResult = adminPostMapper.selectPostsForAdmin(postPage, status, keyword);
        
        // 转换为DTO
        return convertToAdminPostDTOPage(postResult);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPostDTO getPostDetail(Long postId) {
        Post post = adminPostMapper.selectPostDetailForAdmin(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在");
        }
        
        return convertToAdminPostDTO(post);
    }

    @Override
    public boolean reviewPost(Long postId, Integer status, Long adminId, String rejectReason) {
        // 验证状态参数
        if (status != 1 && status != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "状态参数错误");
        }
        
        // 获取动态
        Post post = adminPostMapper.selectPostDetailForAdmin(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在");
        }
        
        // 更新状态
        post.setStatus(status);
        int rows = adminPostMapper.updateById(post);
        
        if (rows > 0) {
            // 记录操作日志
            String operationType = status == 1 ? "APPROVE_POST" : "REJECT_POST";
            String content = status == 1 
                ? "审核通过动态，ID：" + postId
                : "拒绝动态，ID：" + postId + "，原因：" + rejectReason;
            
            operationLogService.addLog(adminId, operationType, content, null);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean deletePost(Long postId, Long adminId) {
        // 获取动态
        Post post = adminPostMapper.selectPostDetailForAdmin(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "动态不存在");
        }
        
        // 逻辑删除
        post.setStatus(0);
        int rows = adminPostMapper.updateById(post);
        
        if (rows > 0) {
            // 记录操作日志
            operationLogService.addLog(adminId, "DELETE_POST", "删除动态，ID：" + postId, null);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPostStatistics getPostStatistics() {
        AdminPostStatistics statistics = new AdminPostStatistics();
        
        // 获取各状态的动态数量
        Long total = adminPostMapper.countPostsByStatus(null);
        Long normal = adminPostMapper.countPostsByStatus(1);
        Long violation = adminPostMapper.countPostsByStatus(2);
        Long deleted = adminPostMapper.countPostsByStatus(0);
        
        statistics.setTotal(total);
        statistics.setNormal(normal);
        statistics.setViolation(violation);
        statistics.setDeleted(deleted);
        
        return statistics;
    }
    
    /**
     * 将Post对象转换为AdminPostDTO
     */
    private AdminPostDTO convertToAdminPostDTO(Post post) {
        AdminPostDTO adminPostDTO = new AdminPostDTO();
        BeanUtils.copyProperties(post, adminPostDTO);
        
        // 设置状态文本
        String statusText;
        switch (post.getStatus()) {
            case 0:
                statusText = "已删除";
                break;
            case 1:
                statusText = "正常";
                break;
            case 2:
                statusText = "违规";
                break;
            default:
                statusText = "未知";
        }
        adminPostDTO.setStatusText(statusText);
        
        // 处理图片列表
        try {
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                List<String> images = objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {});
                adminPostDTO.setImages(images);
            } else {
                adminPostDTO.setImages(new ArrayList<>());
            }
        } catch (JsonProcessingException e) {
            adminPostDTO.setImages(new ArrayList<>());
        }
        
        return adminPostDTO;
    }
    
    /**
     * 将Post分页对象转换为AdminPostDTO分页对象
     */
    private Page<AdminPostDTO> convertToAdminPostDTOPage(Page<Post> postPage) {
        Page<AdminPostDTO> adminPostDTOPage = new Page<>();
        BeanUtils.copyProperties(postPage, adminPostDTOPage, "records");
        
        List<AdminPostDTO> adminPostDTOList = postPage.getRecords().stream()
                .map(this::convertToAdminPostDTO)
                .collect(Collectors.toList());
        
        adminPostDTOPage.setRecords(adminPostDTOList);
        return adminPostDTOPage;
    }
} 