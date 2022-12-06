package com.xqy.gulimall.member.dao;

import com.xqy.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-06 10:41:24
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
