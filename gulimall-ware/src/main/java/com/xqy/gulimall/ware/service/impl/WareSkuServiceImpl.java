package com.xqy.gulimall.ware.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.exception.NoStockException;
import com.xqy.common.to.mq.OrderTo;
import com.xqy.common.to.mq.StockDetailTo;
import com.xqy.common.to.mq.StockLockedTo;
import com.xqy.common.utils.R;
import com.xqy.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.xqy.gulimall.ware.entity.WareOrderTaskEntity;
import com.xqy.gulimall.ware.feign.OrderFeignService;
import com.xqy.gulimall.ware.feign.ProductFeignService;
import com.xqy.gulimall.ware.service.WareOrderTaskDetailService;
import com.xqy.gulimall.ware.service.WareOrderTaskService;
import com.xqy.gulimall.ware.vo.*;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.ware.dao.WareSkuDao;
import com.xqy.gulimall.ware.entity.WareSkuEntity;
import com.xqy.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 器皿sku服务impl
 *
 * @author xqy
 * @date 2023/08/11
 */
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    OrderFeignService orderFeignService;


    /**
     * 库存自动解锁
     * 库存解锁的场景
     * * 1) 下订单成功了，订单过期没有支付被系统自动取消，或被用户手动取消，都要解锁库存
     * * 2) 下订单成功了，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存也要自动解锁
     * * 3) 锁定库存失败，下订单失败
     *
     * @param stockLockedTo 股票锁定
     */
    @Transactional
    public void handleStockLockedRelease(StockLockedTo stockLockedTo) {
        /*解锁库存
        1)、查询数据库关于这个订单的锁定库存信息
         有：证明库存锁定成功了
              解锁：订单情况
                1、没有这个订单，解锁库存
                2、有这个订单，但是订单状态是已取消，解锁库存
                3、有这个订单，但是订单状态是没有取消，不解锁库存
         没有：证明库存锁定失败了
              无需解锁
         */
        StockDetailTo detail = stockLockedTo.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if (taskDetailEntity != null) {
            //有  解锁
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity orderTaskServiceById = wareOrderTaskService.getById(id);
            String orderSn = orderTaskServiceById.getOrderSn(); //根据订单号查询订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if ((data == null || data.getStatus() == 4) && taskDetailEntity.getLockStatus() == 1) {
                    //订单已经取消 解锁库存
                    //库存工作单状态是1  已锁定但是未解锁才可以解锁
                    unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                }
            } else {
                //消息拒绝以后重新放回队列,让别人继续消费
                throw new RuntimeException("远程服务失败");
            }
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态一直未改变，库存消息优先到期，
     * 查询订单状态为新建状态，无法解锁库存导致卡顿订单永远不能解锁库存
     *
     * @param orderTo 以
     */
    @Override
    @Transactional
    public void handleStockLockedRelease(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查询最新库存状态，防止重复解锁库存
        WareOrderTaskEntity orderTask = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = orderTask.getId();
        //查询库存工作单的最新状态
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", id)
                        .eq("lock_status", 1));
        //解锁库存
        for (WareOrderTaskDetailEntity item : entities) {
            unLockStock(item.getSkuId(), item.getWareId(), item.getSkuNum(), item.getId());
        }
    }

    /**
     * 联合国锁定股票
     *
     * @param skuId        sku id
     * @param wareId       器皿id
     * @param skuNum       sku num
     * @param taskDetailId 任务详细id
     */
    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long taskDetailId) {
        //解锁库存
        baseMapper.unLockStock(skuId, wareId, skuNum);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId)
                .eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            //远程查询sku的name
            //TODO 使用什么办法可以出现异常之后不会回滚
            R info = productFeignService.info(skuId);
            Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
            if (info.getCode() == 0) {
                wareSkuEntity.setSkuName((String) data.get("skuName"));
                System.err.println(wareSkuEntity);
            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        return skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //查询当先sku的总库存量
//            SELECT SUM(stock-stock_locked) FROM wms_ware_sku WHERE sku_id = 1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 订单锁定股票
     *
     * @param vo 签证官
     */
    @Override
    @Transactional
//    @GlobalTransactional
    public void orderLockStock(WareSkuLockVo vo) {
        /**
         * 1.保存库存工作单
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);


        LockStockResult result = new LockStockResult();
        //1.按照下单的收货地址，找到一个就近仓库，锁定库存
        //1.1 找到每一个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {

            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());
            skuWareHasStock.setNum(item.getCount());
            //查询当前商品在哪些仓库有库存
            List<Long> wareId = baseMapper.listWareIdHasStock(item.getSkuId(), item.getCount());
            skuWareHasStock.setWareId(wareId);

            return skuWareHasStock;
        }).collect(Collectors.toList());

        //2.锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Long skuId = hasStock.getSkuId();
            List<Long> wareId = hasStock.getWareId();
            if (wareId == null || wareId.size() == 0) {
                //库存小于购买量
                throw new NoStockException(skuId);
            }

            boolean skuLock = false;
            //如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            //锁定失败，前面保存的工作单信息就回滚了。发送出去的消息及时要解锁记录，由于查不到工作单ID，所以就不用去解锁
            for (Long l : wareId) {
                //成功为1，失败为0
                Long count = baseMapper.lockSkuStock(skuId, l, hasStock.getNum());
                if (count == 1) {
                    //锁定成功
                    skuLock = true;
                    //TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(
                            null, skuId, "", hasStock.getNum(), taskEntity.getId(), l, 1);
                    wareOrderTaskDetailService.save(detailEntity);
                    //发送消息给MQ
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);

                    break;
                }
            }
            if (!skuLock) {
                //当前商品锁定失败
                throw new NoStockException(skuId);
            }
        }
        //3.能跑到这全部都锁定成功
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}