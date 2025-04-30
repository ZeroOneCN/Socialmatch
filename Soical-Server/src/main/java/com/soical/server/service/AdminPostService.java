package com.soical.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soical.server.dto.AdminPostDTO;

/**
 * 管理员动态管理服务接口
 */
public interface AdminPostService {
    
    /**
     * 获取动态列表（分页）
     *
     * @param status   状态：0-删除，1-正常，2-违规，null-全部
     * @param keyword  关键词搜索（用户名、昵称、内容）
     * @param page     页码
     * @param pageSize 每页条数
     * @return 动态列表
     */
    Page<AdminPostDTO> getPostList(Integer status, String keyword, Integer page, Integer pageSize);
    
    /**
     * 获取动态详情
     *
     * @param postId 动态ID
     * @return 动态详情
     */
    AdminPostDTO getPostDetail(Long postId);
    
    /**
     * 审核动态
     *
     * @param postId      动态ID
     * @param status      状态：1-正常，2-违规
     * @param adminId     管理员ID
     * @param rejectReason 拒绝原因（当status=2时提供）
     * @return 是否成功
     */
    boolean reviewPost(Long postId, Integer status, Long adminId, String rejectReason);
    
    /**
     * 删除动态
     *
     * @param postId  动态ID
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean deletePost(Long postId, Long adminId);
    
    /**
     * 获取动态统计数据
     *
     * @return 统计数据（总数、正常数、违规数、删除数）
     */
    AdminPostStatistics getPostStatistics();
    
    /**
     * 动态统计数据
     */
    class AdminPostStatistics {
        private Long total;
        private Long normal;
        private Long violation;
        private Long deleted;
        
        public Long getTotal() {
            return total;
        }
        
        public void setTotal(Long total) {
            this.total = total;
        }
        
        public Long getNormal() {
            return normal;
        }
        
        public void setNormal(Long normal) {
            this.normal = normal;
        }
        
        public Long getViolation() {
            return violation;
        }
        
        public void setViolation(Long violation) {
            this.violation = violation;
        }
        
        public Long getDeleted() {
            return deleted;
        }
        
        public void setDeleted(Long deleted) {
            this.deleted = deleted;
        }
    }
} 