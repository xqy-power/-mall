package com.xqy.gulimall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.xqy.common.constant.WareConstant;
import com.xqy.gulimall.ware.entity.PurchaseDetailEntity;
import com.xqy.gulimall.ware.service.PurchaseDetailService;
import com.xqy.gulimall.ware.service.WareSkuService;
import com.xqy.gulimall.ware.vo.MergeVo;
import com.xqy.gulimall.ware.vo.PurchaseDoneVo;
import com.xqy.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.ware.dao.PurchaseDao;
import com.xqy.gulimall.ware.entity.PurchaseEntity;
import com.xqy.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id" , key)
                    .or().eq("assignee_id" , key);
        }
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status" , status);
        }

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnRecevie(Map<String, Object> params) {



        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status" , 0)
                        .or().eq("status" , 1)
        );

        return new PageUtils(page);
    }

    /**
     * 合并购买
     *
     * @param mergeVo 合并签证官
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null){
            //新建一个订单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //TODO 确认采购单状态是  0或者 1 才可以合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity Entity = new PurchaseEntity();
        Entity.setId(purchaseId);
        Entity.setUpdateTime(new Date());
        this.updateById(Entity);
    }

    /**
     * 领取采购单
     *
     * @param ids 采购单id
     */
    @Override
    public void received(List<Long> ids) {
        //1.确认当前采购单是新建或已分配状态
        List<PurchaseEntity> collect = ids.stream().map(item -> {
            PurchaseEntity byId = this.getById(item);
            return byId;
        }).filter(i -> {
            if (i.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    i.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(i->{
            i.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            i.setUpdateTime(new Date());
            return i;
        }).collect(Collectors.toList());

        if (!collect.isEmpty()){
            //改变采购单状态
            this.updateBatchById(collect);

            //改变采购项状态
            collect.forEach(i -> {
                List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchase(i.getId());
                List<PurchaseDetailEntity> detailEntities = entities.stream().map(item -> {
                    PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                    purchaseDetailEntity.setId(item.getId());
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return purchaseDetailEntity;
                }).collect(Collectors.toList());

                if (!detailEntities.isEmpty()){
                    purchaseDetailService.updateBatchById(detailEntities);
                }
            });
        }
    }

    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();

        //2.改变采购项状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> updatas = new ArrayList<>();
        for(PurchaseItemDoneVo item:items){
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus()==WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3.将采购成功的入库
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId() , byId.getWareId() , byId.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updatas.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updatas);

        //1.改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}