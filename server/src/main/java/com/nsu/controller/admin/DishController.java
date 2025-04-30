package com.nsu.controller.admin;
/*
  Date:2025/3/13
  Time:13:45
  @author llh 
 */

import com.nsu.dto.DishDTO;
import com.nsu.dto.DishPageQueryDTO;
import com.nsu.entity.Dish;
import com.nsu.result.PageResult;
import com.nsu.result.Result;
import com.nsu.service.DishService;
import com.nsu.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

// requestController注解表示这是一个控制器类,返回的是json数据
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    DishService dishService;

    @Autowired
    RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("添加菜品")
    // RequestBody注解用于接收前端传递的json数据
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.addDish(dishDTO);

        // 为保证数据一致性，对菜品进行操作时，清理缓存，更新重置发生变化的菜品。
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        // 如果前端是通过Query和？的形式传递参数，以及PathVariable直接包含url的形式传递参数，那么这里就不需要加@RequestBody注解
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        // 可以单独和批量删除，所以可用String接收前端的参数，也可以用List接收，加上@RequestParam注解，由MVC框架自动解析
        log.info("批量或者单个删除菜品id:{}",ids);
        dishService.delete(ids);
        // 清理所有缓存
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询菜品id:{}",id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品");
        dishService.update(dishDTO);
        // 清理所有缓存
        cleanCache("dish_*");
        return Result.success();
    }

    // 根据分类ID查询菜品，可能有多个菜品
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        log.info("根据分类ID查询菜品，{}",categoryId);
        List<Dish> dishes = dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }

    // 清理缓存，保证redis和数据库数据一致性
    public void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStopDish(@PathVariable Integer status, Long id){
        log.info("起售或停售菜品,id:{},status:{}",id,status);
        dishService.startOrStop(id,status);
        cleanCache("dish_*");
        return Result.success();

    }
}

