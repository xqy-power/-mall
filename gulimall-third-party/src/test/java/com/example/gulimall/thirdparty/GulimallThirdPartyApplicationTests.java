package com.example.gulimall.thirdparty;


import com.aliyun.oss.OSSClient;
import com.example.gulimall.thirdparty.component.SmsComponent;
import com.example.gulimall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void setSmsComponent() {
        smsComponent.sendSmsCode("18697771219", "6666");
    }
    @Test
    public void sendSms() {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "061fb5a334e647d282b9c3fccb01b03e";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "18697771219");
        querys.put("param", "**code**:1219,**minute**:15");

//smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "a09602b817fd47e59e7c6e603d3f088d");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //阿里云对象存储OSS
    @Test
    public void testUpload() throws FileNotFoundException {
/*        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5t5nX7uP56Syi77UhmTP";
        String accessKeySecret = "GXyCW87dC6nDS5Po4UpdCASzjRUUlU";
*/

        // 填写Bucket名称，例如examplebucket。
        String bucketName = "gulimall-student-test";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "test3.jpg";
        // 填写本地文件的完整路径，例如D:\\local-path\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        String filePath = "D:\\171336-1665306816c0d2.jpg";

        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        InputStream inputStream = new FileInputStream(filePath);
        // 创建PutObject请求。
        ossClient.putObject(bucketName, objectName, inputStream);

        ossClient.shutdown();
    }

    @Test
    public void contextLoads() {
    }

}
