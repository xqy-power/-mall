package com.xqy.gulimall.product.web;

import com.xqy.gulimall.product.entity.CategoryEntity;
import com.xqy.gulimall.product.service.CategoryService;
import com.xqy.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author xqy
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;
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
}
