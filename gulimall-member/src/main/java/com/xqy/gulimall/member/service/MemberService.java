package com.xqy.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xqy.common.utils.PageUtils;
import com.xqy.gulimall.member.entity.MemberEntity;
import com.xqy.gulimall.member.exception.PhoneExistException;
import com.xqy.gulimall.member.exception.UserNameExistException;
import com.xqy.gulimall.member.vo.MemberLoginVo;
import com.xqy.gulimall.member.vo.MemberRegisterVo;
import com.xqy.gulimall.member.vo.SociaUser;

import java.util.Map;

/**
 * 会员
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-06 10:41:24
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo memberRegisterVo);

    void checkUserNameUnique(String userName) throws UserNameExistException;
    void checkPhoneUnique(String phone) throws PhoneExistException;

    MemberEntity login(MemberLoginVo memberLoginVo);

    MemberEntity oauthLogin(SociaUser sociaUser);
}

