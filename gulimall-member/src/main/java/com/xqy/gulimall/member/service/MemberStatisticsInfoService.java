package com.xqy.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xqy.common.utils.PageUtils;
import com.xqy.gulimall.member.entity.MemberStatisticsInfoEntity;

import java.util.Map;

/**
 * 会员统计信息
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-06 10:41:24
 */
public interface MemberStatisticsInfoService extends IService<MemberStatisticsInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

