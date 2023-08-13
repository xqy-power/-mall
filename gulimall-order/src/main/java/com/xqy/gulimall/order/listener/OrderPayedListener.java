package com.xqy.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.xqy.gulimall.order.config.AlipayTemplate;
import com.xqy.gulimall.order.service.OrderService;
import com.xqy.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单支付侦听器
 *
 * @author xqy
 * @date 2023/08/13
 */
@RestController
public class OrderPayedListener {

    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String HandleOrderPayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
//        Map<String, String[]> map = request.getParameterMap();
//        System.out.println("支付宝通知我们的参数信息："+map);
//        for (String key : map.keySet()) {
//            String value = request.getParameter(key);
//            System.out.println("参数名："+key+"==>参数值："+value);
//        }
        System.out.println("支付宝通知我们的参数信息：" + vo);
        //验签 验证是不是支付宝返回给我们的数据
        //TODO 需要验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        //只要我们收到了支付宝的异步通知，告诉我们订单支付成功，返回success支付宝就再也不通知了
        if (signVerified) {
            System.out.println("签名验证成功...");
            //去修改订单状态
            return orderService.handlePayResult(vo);
        } else {
            System.out.println("签名验证失败...");
            return "error";
        }
    }
}
