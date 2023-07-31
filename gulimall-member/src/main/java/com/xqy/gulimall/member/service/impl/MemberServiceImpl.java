package com.xqy.gulimall.member.service.impl;

import com.mysql.cj.Session;
import com.xqy.gulimall.member.entity.MemberLevelEntity;
import com.xqy.gulimall.member.exception.PhoneExistException;
import com.xqy.gulimall.member.exception.UserNameExistException;
import com.xqy.gulimall.member.service.MemberLevelService;
import com.xqy.gulimall.member.vo.MemberLoginVo;
import com.xqy.gulimall.member.vo.MemberRegisterVo;
import com.xqy.gulimall.member.vo.SociaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.member.dao.MemberDao;
import com.xqy.gulimall.member.entity.MemberEntity;
import com.xqy.gulimall.member.service.MemberService;
import org.springframework.util.StringUtils;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册
     *
     * @param memberRegisterVo 会员注册签证官
     */
    @Override
    public void register(MemberRegisterVo memberRegisterVo) {
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一，为了让Controller感知到异常，我们需要抛出异常
        checkPhoneUnique(memberRegisterVo.getPhone());
        checkUserNameUnique(memberRegisterVo.getUserName());

        memberEntity.setMobile(memberRegisterVo.getPhone());
        memberEntity.setUsername(memberRegisterVo.getUserName());

        //密码加密 BcryptPasswordEncoder加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(memberRegisterVo.getPassword());
        memberEntity.setPassword(encode);

        //创建时间
        memberEntity.setCreateTime(new java.util.Date());

        this.baseMapper.insert(memberEntity);
    }


    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        Integer selectCountUserName = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (selectCountUserName > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer selectCountPhone = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (selectCountPhone > 0) {
            throw new PhoneExistException();
        }
    }

    /**
     * 登录
     *
     * @param memberLoginVo 会员登录签证官
     * @return {@link MemberEntity}
     */
    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        String loginAccount = memberLoginVo.getLoginacct();
        String loginPassword = memberLoginVo.getPassword();

        //1.去数据库查询 SELECT * FROM `ums_member` WHERE `username` = 'admin' OR `mobile` = 'admin' OR `email` = 'admin'
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount).or().eq("email", loginAccount));
        if (!StringUtils.isEmpty(memberEntity)) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(loginPassword, memberEntity.getPassword());
            if (matches) {
                return memberEntity;
            }
        }

        return null;
    }

    /**
     * oauth登录
     *
     * @param sociaUser 副部用户
     * @return {@link MemberEntity}
     */
    @Override
    public MemberEntity oauthLogin(SociaUser sociaUser) {

        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", sociaUser.getUid()));
        if (!StringUtils.isEmpty(memberEntity)){
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(sociaUser.getAccess_token());
            update.setExpiresIn(sociaUser.getExpires_in());
            baseMapper.updateById(update);

            memberEntity.setAccessToken(sociaUser.getAccess_token());
            memberEntity.setExpiresIn(sociaUser.getExpires_in());
            return memberEntity;
        }else {
            //没有查到当前社交用户对应的记录，需要注册一个
            MemberEntity register = new MemberEntity();
            try {
                register.setUsername(sociaUser.getGiteeUserVo().getName());
                register.setEmail(sociaUser.getGiteeUserVo().getEmail());
                register.setNickname(sociaUser.getGiteeUserVo().getName());
            }catch (Exception e){
                e.printStackTrace();
            }
            register.setSocialUid(sociaUser.getUid());
            register.setAccessToken(sociaUser.getAccess_token());
            register.setExpiresIn(sociaUser.getExpires_in());
            //调用远程服务查询默认会员等级
            MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
            if (!StringUtils.isEmpty(levelEntity)){
                register.setLevelId(levelEntity.getId());
            }
            register.setCreateTime(new java.util.Date());
            baseMapper.insert(register);
            return register;
        }
    }


}