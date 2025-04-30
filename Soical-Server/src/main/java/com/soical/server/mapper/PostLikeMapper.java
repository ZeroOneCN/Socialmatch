package com.soical.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soical.server.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 动态点赞数据访问接口
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
} 