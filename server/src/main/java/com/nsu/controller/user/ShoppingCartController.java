package com.nsu.controller.user;
/*
  Date:2025/3/20
  Time:10:13
  @author llh 
 */


import com.nsu.dto.ShoppingCartDTO;
import com.nsu.entity.ShoppingCart;
import com.nsu.result.Result;
import com.nsu.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api("C端-购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @ApiOperation("添加进购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加进购物车:{}",shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车");
        return Result.success(shoppingCartService.showShoppingCarts());
    }

    @DeleteMapping("clean")
    public Result clean(){
        shoppingCartService.clean();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除购物车商品数量")
    public Result sub(@RequestBody  ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车内商品数量:{}",shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }
}



