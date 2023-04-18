package com.xqy.gulimall.product.web;

import com.xqy.gulimall.product.entity.CategoryEntity;
import com.xqy.gulimall.product.service.CategoryService;
import com.xqy.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xqy
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redissonClient;

    @RequestMapping({"/" , "/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> catalogJson(){
        return categoryService.getCatelogJson();
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        //获取一把锁 ， 只要锁名一样就是 一把锁
        RLock myLock = redissonClient.getLock("myLock");
        //加锁
//        myLock.lock();
        //1)、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉
        //2)、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动刷除。

        myLock.lock(31 , TimeUnit.SECONDS);  //此处的解锁时间必须比业务执行的时间长
        try {
            System.out.println("加锁------- ， 执行业务"+Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
        } finally {
            System.out.println("释放锁-------"+Thread.currentThread().getId());
            myLock.unlock();
        }
        return "hello";
    }


}
