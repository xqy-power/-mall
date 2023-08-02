package com.xqy.gulimall.member;


import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {
//        String s = Md5Crypt.md5Crypt("123456".getBytes(),"$1$12345678");
//        System.out.println(s);
//        $1$12345678$a4ge4d5iJ5vwvbFS88TEN0
//        $1$12345678$a4ge4d5iJ5vwvbFS88TEN0

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
//        $2a$10$0pxssaxbvcB9KwVsWmjjK.WHHCENR3Y779j1Gxs8C6/EOmDBdLTqK
        boolean matches = passwordEncoder.matches("123456", "$2a$10$0pxssaxbvcB9KwVsWmjjK.WHHCENR3Y779j1Gxs8C6/EOmDBdLTqK");
        System.out.println(matches);
        System.out.println(encode);
    }

}
