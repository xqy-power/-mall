package com.xqy.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xqy.common.utils.PageUtils;
import com.xqy.gulimall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-06 10:41:24
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);

    MemberLevelEntity getDefaultLevel();
}

