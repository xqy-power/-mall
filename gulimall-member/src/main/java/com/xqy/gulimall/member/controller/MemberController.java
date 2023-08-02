package com.xqy.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;


import com.xqy.common.exception.BizCodeEnume;
import com.xqy.gulimall.member.exception.PhoneExistException;
import com.xqy.gulimall.member.exception.UserNameExistException;
import com.xqy.gulimall.member.feign.CouponFeignService;
import com.xqy.gulimall.member.vo.GiteeUserVo;
import com.xqy.gulimall.member.vo.MemberLoginVo;
import com.xqy.gulimall.member.vo.MemberRegisterVo;
import com.xqy.gulimall.member.vo.SociaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.xqy.gulimall.member.entity.MemberEntity;
import com.xqy.gulimall.member.service.MemberService;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.R;



/**
 * 会员
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-06 10:41:24
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;
    @RequestMapping("coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername("谢乾玉");
        R memberCoupon = couponFeignService.memberCoupon();
        return R.ok().put("member",memberEntity).put("coupons",memberCoupon.get("coupons"));
    }

    /**
     * 注册
     *
     * @param memberRegisterVo 会员注册签证官
     * @return {@link R}
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo){
        try {
            memberService.register(memberRegisterVo);
        }catch (UserNameExistException e){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }catch (PhoneExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }


    /**
     * oauth登录
     *
     * @param sociaUser 副部用户
     * @return {@link R}
     */
    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SociaUser sociaUser){
        MemberEntity memberEntity = memberService.oauthLogin(sociaUser);
        if (!StringUtils.isEmpty(memberEntity)){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }

    /**
     * 登录
     *
     * @param memberLoginVo 会员登录签证官
     * @return {@link R}
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo memberLoginVo){
        MemberEntity memberEntity = memberService.login(memberLoginVo);
        if (!StringUtils.isEmpty(memberEntity)){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
   // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
   // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
