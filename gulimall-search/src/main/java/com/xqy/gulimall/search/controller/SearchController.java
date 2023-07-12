package com.xqy.gulimall.search.controller;

import com.xqy.gulimall.search.service.MallSearchService;
import com.xqy.gulimall.search.vo.SearchParam;
import com.xqy.gulimall.search.vo.SearchResult;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xqy
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;


    @GetMapping("/list.html")
    public String listPage(SearchParam param , Model model , HttpServletRequest request){
        param.set_queryString(request.getQueryString());

        // 根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }


}
