package com.xqy.gulimall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.utils.R;
import com.xqy.gulimall.ware.feign.MemberFeignService;
import com.xqy.gulimall.ware.vo.FareVo;
import com.xqy.gulimall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.ware.dao.WareInfoDao;
import com.xqy.gulimall.ware.entity.WareInfoEntity;
import com.xqy.gulimall.ware.service.WareInfoService;


/**
 * impl制品信息服务
 *
 * @author xqy
 * @date 2023/08/08
 */

@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String)params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.eq("id" , key)
                    .or().like("name",key)
                    .or().like("address",key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 得到运费
     * 根据收获地址得到运费
     *
     * @param addrId addr id
     * @return {@link BigDecimal}
     */

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.info(addrId);
        if (info.getCode() == 0){
            MemberAddressVo data = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
            });
            String phone = data.getPhone();
            String s = phone.substring(phone.length() - 1);
            BigDecimal bigDecimal = new BigDecimal(s);
            fareVo.setFare(bigDecimal);
            fareVo.setAddress(data);
            return fareVo;
        }
        return null;
    }

}