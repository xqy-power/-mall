package com.xqy.gulimall.order.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xqy.common.exception.NoStockException;
import com.xqy.common.to.mq.OrderTo;
import com.xqy.common.utils.R;
import com.xqy.common.vo.MemberResponseVo;
import com.xqy.gulimall.order.constant.OrderConstant;
import com.xqy.gulimall.order.dao.OrderItemDao;
import com.xqy.gulimall.order.entity.OrderItemEntity;
import com.xqy.gulimall.order.entity.PaymentInfoEntity;
import com.xqy.gulimall.order.enume.OrderStatusEnum;
import com.xqy.gulimall.order.feign.CartFeignService;
import com.xqy.gulimall.order.feign.MemberFeignService;
import com.xqy.gulimall.order.feign.ProductFeignService;
import com.xqy.gulimall.order.feign.WmsFeignService;
import com.xqy.gulimall.order.interceptor.LoginUserInterceptor;
import com.xqy.gulimall.order.service.OrderItemService;
import com.xqy.gulimall.order.service.PaymentInfoService;
import com.xqy.gulimall.order.to.OrderCreateTo;
import com.xqy.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.order.dao.OrderDao;
import com.xqy.gulimall.order.entity.OrderEntity;
import com.xqy.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * 订单服务impl
 *
 * @author xqy
 * @date 2023/08/06
 */
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    WmsFeignService wmsFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 确认订单需要的数据
     *
     * @return {@link OrderConfirmVo}
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo loginUser = LoginUserInterceptor.loginUser.get();

        //获取当前线程的请求头信息，放入新的线程中，解决异步调用丢失请求头的问题
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //引入线程池异步编排
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //在新线程中放入请求头信息
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询所有的收货地址列表
            confirmVo.setAddress(memberFeignService.getAddress(String.valueOf(loginUser.getId())));
        }, executor);

        CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(() -> {
            //在新线程中放入请求头信息
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询购物车所有选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);

        }, executor).thenRunAsync(() -> {
            //TODO 查询库存
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skusHasStock = wmsFeignService.getSkusHasStock(collect);
            if (skusHasStock.getCode() == 0) {
                List<OrderItemStockVo> data = skusHasStock.getData(new TypeReference<List<OrderItemStockVo>>() {
                });
                if (data != null) {
                    Map<Long, Boolean> hasStockData = data.stream().collect(Collectors.toMap(OrderItemStockVo::getSkuId, OrderItemStockVo::getHasStock));
                    confirmVo.setStocks(hasStockData);
                }
            }
        }, executor);

        CompletableFuture<Void> integrationFuture = CompletableFuture.runAsync(() -> {
            //在新线程中放入请求头信息
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //优惠券 积分 信息
            Integer integration = loginUser.getIntegration();
            confirmVo.setIntegration(integration);

        }, executor);

        //其他数据自动计算
        //TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(
                OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId(),
                token, 30, TimeUnit.MINUTES);


        CompletableFuture.allOf(addressFuture, itemFuture, integrationFuture).get();
        return confirmVo;
    }

    /**
     * 提交订单
     *
     * @param submitVo 提交签证官
     * @return {@link SubmitOrderResponseVo}
     */

    @Override
    @Transactional
//    @GlobalTransactional  AT模式无法满足高并发场景
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        confirmVoThreadLocal.set(submitVo);
        //1.验证令牌 令牌和redis中的令牌比较必须保证原子性
        //lua 脚本
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        //使用lua脚本验证令牌和删除令牌必须保证原子性
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
                orderToken);
        if (result != null && result == 0L) {
            //令牌验证失败
            responseVo.setCode(1);
        } else {
            //令牌验证成功，删除令牌
            //1.创建订单，订单项，订单项信息
            OrderCreateTo order = createOrder();
            //2.验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //3.金额对比成功，保存订单
                saveOrder(order);
                //4.库存锁定 只要有异常都回滚 远程锁定库存
                //订单号，所有订单项的skuId，所有订单项的购买数量
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(locks);
                //锁定库存


                //为了保证高并发，库存服务自己回滚，  可靠消息MQ-最终一致性方案
                //采用延迟队列的方式进行库存回滚
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    //锁定成功
                    responseVo.setOrder(order.getOrder());
//                    int i = 10/0;
                    //5.订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

                    return responseVo;
                } else {
                    //锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                //金额对比失败
                responseVo.setCode(2);
                return responseVo;
            }
        }

        return responseVo;
    }

    /**
     * 得到订单,订单sn
     *
     * @param orderSn 订单sn
     * @return {@link OrderEntity}
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单
     *
     * @param entity 实体
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前的订单状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            //只有新建订单才可以关闭订单
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(orderEntity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);
            //发送消息给MQ
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                //发送消息给MQ
                //TODO 保证消息一定能够发送成功，每一个消息都做好日志记录（给数据库保存每个消息的详细信息）
                //TODO 定期扫描数据库将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO 将没发送成功的消息进行重新发送
                log.error("发送消息释放库存失败");
            }
        }
    }

    /**
     * 得到订单支付信息
     *
     * @param orderSn 订单sn
     * @return {@link PayVo}
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        BigDecimal bigDecimal = orderEntity.getPayAmount().setScale(2, RoundingMode.UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(orderEntity.getOrderSn());

        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = order_sn.get(0);

        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    /**
     * 查询页面,项目
     *
     * @param params 参数个数
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberResponseVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> orderEntities = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));

            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntities);

        return new PageUtils(page);
    }

    /**
     * 异步处理支付结果
     *
     * @param vo 签证官
     * @return {@link String}
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1.保存交易流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoEntity.setCallbackContent(vo.toString());
        paymentInfoService.save(paymentInfoEntity);
        //2.修改订单状态
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            //支付成功
            String orderSn = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
        //只要我们收到了支付宝的异步通知，告诉我们订单支付成功，返回success支付宝就再也不通知了
        return "success";
    }


    /**
     * 保存订单
     *
     * @param order 订单
     */

    private void saveOrder(OrderCreateTo order) {
        //保存订单
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        baseMapper.insert(orderEntity);
        //保存订单项
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     *
     * @return {@link OrderCreateTo}
     */

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //1.生成订单号
        String orderSn = IdWorker.getTimeId();

        //2.创建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        //3.获取所有的订单项,从购物车中获取
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        //4.验价 计算价格相关
        if (itemEntities != null) {
            computePrice(orderEntity, itemEntities);
        }
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);

        return orderCreateTo;
    }

    /**
     * 计算价格
     *
     * @param orderEntity  订单实体
     * @param itemEntities 项目实体
     */

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        //1.计算订单的价格
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        Integer gift = new Integer("0");
        Integer growth = new Integer("0");
        //叠加每个订单项的总价
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            BigDecimal couponAmount = itemEntity.getCouponAmount();
            BigDecimal integrationAmount = itemEntity.getIntegrationAmount();
            BigDecimal promotionAmount = itemEntity.getPromotionAmount();
            Integer giftIntegration = itemEntity.getGiftIntegration();
            Integer giftGrowth = itemEntity.getGiftGrowth();
            //计算总价
            total = total.add(realAmount);
            //计算优惠券的价格
            coupon = coupon.add(couponAmount);
            //计算积分的价格
            integration = integration.add(integrationAmount);
            //计算促销的价格
            promotion = promotion.add(promotionAmount);
            //计算赠送的成长值
            growth += giftGrowth;
            //计算赠送的积分
            gift += giftIntegration;
        }
        //设置订单总额
        orderEntity.setTotalAmount(total);
        //设置应付总额  总额+运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        //设置促销的价格
        orderEntity.setPromotionAmount(promotion);
        //设置积分的价格
        orderEntity.setIntegrationAmount(integration);
        //设置优惠券的价格
        orderEntity.setCouponAmount(coupon);
        //设置赠送的积分
        orderEntity.setIntegration(gift);
        //设置赠送的成长值
        orderEntity.setGrowth(growth);
        //设置删除状态
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 构建顺序
     *
     * @param orderSn 订单sn
     * @return {@link OrderEntity}
     */

    private OrderEntity buildOrder(String orderSn) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberResponseVo.getId());
        //2.获取到订单收货地址
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        //远程查询收货地址
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        if (fare.getCode() == 0) {
            FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
            });
            //设置运费
            orderEntity.setFreightAmount(fareVo.getFare());
            //设置 收货人信息
            orderEntity.setReceiverCity(fareVo.getAddress().getCity());
            orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
            orderEntity.setReceiverName(fareVo.getAddress().getName());
            orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
            orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
            orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
            orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());

            //设置订单的相关状态
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            orderEntity.setAutoConfirmDay(7);
        }
        return orderEntity;
    }

    /**
     * 建立订单项
     * 建立所有的订单项
     *
     * @return {@link List}<{@link OrderItemEntity}>
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        if (cartItems != null && cartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);

                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 建立订单项
     * 建立具体单个订单项
     *
     * @param item 项
     * @return {@link OrderItemEntity}
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1.设置订单项的基本信息:spu信息 sku信息
        Long skuId = item.getSkuId();
        //远程查询spu信息
        R info = productFeignService.getSpuInfoBySkuId(skuId);
        if (info.getCode() == 0) {
            SpuInfoVo spuInfoVo = info.getData(new TypeReference<SpuInfoVo>() {
            });
            orderItemEntity.setSpuId(spuInfoVo.getId());
            orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
            orderItemEntity.setSpuName(spuInfoVo.getSpuName());
            orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        }
        //设置sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";"));
        orderItemEntity.setSkuQuantity(item.getCount());
        //2.设置订单项的优惠信息
        //3.设置积分信息
        orderItemEntity.setGiftGrowth((item.getPrice().multiply(new BigDecimal(item.getCount()))).intValue());
        orderItemEntity.setGiftIntegration((item.getPrice().multiply(new BigDecimal(item.getCount()))).intValue());
        //4.设置满减信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        //5.设置订单项的实际价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal realAmount = origin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realAmount);
        return orderItemEntity;
    }
}