package com.xqy.gulimall.product.exception;

import com.xqy.common.exception.BizCodeEnume;
import com.xqy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * gulimall异常控制器建议
 *集中处理所有异常
 * @author xqy
 * @date 2022/12/17
 */
@Slf4j
//@ResponseBody  //需要返回JSON格式的信息，故此加该注解
//@ControllerAdvice(basePackages = "com.xqy.gulimall.product.controller")
@RestControllerAdvice(basePackages = "com.xqy.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出错{},类型{}",e.getMessage() , e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data" , errorMap);
    }

//    @ExceptionHandler(value = Throwable.class)
//    public R handleException(Throwable e){
//        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
//    }

}
