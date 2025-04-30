package com.nsu.controller.user;
/*
  Date:2025/3/17
  Time:20:04
  @author llh
 */


import com.nsu.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "营业相关接口")
public class ShopController {

    public static final String KEY = "shop_status";

    // 注入redisTemplate，用于操作redis
    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取店铺营业状态为:{}",shopStatus == 1 ? "营业中" : "打烊中");
        return Result.success(shopStatus);
    }
}

