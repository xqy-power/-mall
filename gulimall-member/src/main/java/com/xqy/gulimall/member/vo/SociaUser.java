package com.xqy.gulimall.member.vo;

import lombok.Data;

/**
 * 副部用户
 *
 * @author xqy
 * @date 2023/07/30
 */
@Data
public class SociaUser {
        private String access_token;

        private String token_type;

        private Long expires_in;

        private String refresh_token;

        private String scope;

        private int created_at;

        private String uid;

        private GiteeUserVo giteeUserVo;

}
