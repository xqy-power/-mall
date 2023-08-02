package com.xqy.gulimall.auth.feign;

import com.xqy.common.utils.R;
import com.xqy.gulimall.auth.vo.GiteeUserVo;
import com.xqy.gulimall.auth.vo.SociaUser;
import com.xqy.gulimall.auth.vo.UserLoginVo;
import com.xqy.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 假装成员服务
 *
 * @author xqy
 * @date 2023/07/29
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 注册
     *
     * @param userRegisterVo 用户注册签证官
     * @return {@link R}
     */
    @PostMapping("/member/member/register")
    public R register(@RequestBody UserRegisterVo userRegisterVo);

    /**
     * 登录
     *
     * @param userLoginVo 用户登录签证官
     * @return {@link R}
     */
    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo userLoginVo);


    /**
     * oauth登录
     *
     * @param sociaUser 副部用户
     * @return {@link R}
     */
    @PostMapping("/member/member/oauth/login")
    public R oauthLogin(@RequestBody SociaUser sociaUser);
}
