package com.xqy.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.xqy.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import net.minidev.json.JSONArray;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallSearchApplicationTests {

    @Autowired
    public RestHighLevelClient restHighLevelClient;

    @Test
    public void contextLoads() {
        System.out.println(restHighLevelClient);
    }


    /**
     * 测试存储数据到ES
     * 更新也可以
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest IndexRequest = new IndexRequest("users");
        IndexRequest.id("1");
        user user = new user();
        user.setUserName("谢乾玉");
        user.setGender("fdi");
        user.setAge(23);
        String jsonString = JSON.toJSONString(user);
        IndexRequest.source(jsonString , XContentType.JSON);  //要保存的内容

        //执行保存操作
        IndexResponse indexResponse = restHighLevelClient.index(IndexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(indexResponse);


    }

    @Data
    class user{
        private String userName;
        private String gender;
        private Integer age;
    }

}
