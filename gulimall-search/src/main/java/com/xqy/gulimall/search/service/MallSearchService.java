package com.xqy.gulimall.search.service;

import com.xqy.gulimall.search.vo.SearchParam;
import com.xqy.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

/**
 * @author xqy
 */
@Service
public interface MallSearchService {
    SearchResult search(SearchParam param);
}
