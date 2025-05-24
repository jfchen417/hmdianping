package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Objects;

import static com.hmdp.utils.RedisConstants.CACHE_TYPE_LIST;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        String key = CACHE_TYPE_LIST;
        // 1、从Redis中查询店铺数据
        String typeJson = stringRedisTemplate.opsForValue().get(key);

        List<ShopType> shopTypeList = null;
        // 2、判断缓存是否命中
        if (StrUtil.isNotBlank(typeJson)) {
            // 2.1 缓存命中，直接返回店铺数据
            shopTypeList = JSONUtil.toList(typeJson, ShopType.class);
            return Result.ok(shopTypeList);
        }
        // 2.2 缓存未命中，从数据库中查询店铺数据
        shopTypeList = query().orderByAsc("sort").list();

        // 4、判断数据库是否存在店铺数据
        if (Objects.isNull(shopTypeList)) {
            // 4.1 数据库中不存在，返回失败信息
            return Result.fail("类型不存在");
        }
        // 4.2 数据库中存在，写入Redis，并返回店铺数据
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypeList));
        return Result.ok(shopTypeList);
    }
}
